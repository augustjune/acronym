package thesaurus.api

import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._
import scala.util.Try

private[api] object ThesaurusResponse {
  private val misspellingRegex = """.*Redirecting to /misspelling\?term=([A-Za-z]*)""".r
  private val wordSectorExpression = """<script>window.INITIAL_STATE = (\{.*\})""".r

  def fromHttpResponse(input: String): Either[LookupError, ThesaurusResponse] = input match {
    case misspellingRegex(term) => Left(Misspelling(term))
    case _ => wordSectorExpression.findFirstMatchIn(input) match {
      case Some(jsonSelection) =>
        Right(new ThesaurusResponse(ConfigFactory.parseString(jsonSelection.group(1))))

      case None => Left(JsonParsingError("Error while extracting initial state"))
    }
  }
}

private[api] class ThesaurusResponse(json: Config) {
  def extractWord: Either[LookupError, ThesaurusWord] = for {
    term <- extractTerm
    meanings <- extractMeanings
    examples <- extractExamples
  } yield ThesaurusWord(term, meanings, examples)

  def extractTerm: Either[LookupError, String] =
    Try(json.getString("searchData.searchTerm"))
      .toEither.left.map(_ => JsonParsingError("Problem while extracting the term"))

  def extractMeanings: Either[LookupError, Seq[WordMeaning]] = Try {
    json.getConfigList("searchData.tunaApiData.posTabs").asScala.toList.map {
      tab =>
        val definition = tab.getString("definition")
        val synonyms = tab.getConfigList("synonyms").asScala.toList.map(_.getString("term"))
        val antonyms = tab.getConfigList("antonyms").asScala.toList.map(_.getString("term"))

        WordMeaning(definition, synonyms, antonyms)
    }
  }.toEither.left.map(_ => JsonParsingError("Problem while extracting word meanings"))

  def extractExamples: Either[LookupError, Seq[String]] = Try {
    json.getConfigList("searchData.tunaApiData.exampleSentences")
      .asScala.toList.map(_.getString("sentence"))
  }.toEither.left.map(_ => JsonParsingError("Problem while extracting example sentences"))
}
