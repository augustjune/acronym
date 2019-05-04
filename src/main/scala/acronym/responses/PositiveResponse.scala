package acronym.responses

import acronym.{JsonParsingError, LookupErrorOr, ThesaurusResponse, ThesaurusWord, WordMeaning}
import com.typesafe.config.Config

import scala.collection.JavaConverters._
import scala.util.Try

private[acronym] class PositiveResponse(json: Config) extends ThesaurusResponse {
  def extractWord: LookupErrorOr[ThesaurusWord] = for {
    term <- extractTerm
    meanings <- extractMeanings
    examples <- extractExamples
  } yield ThesaurusWord(term, meanings, examples)

  def extractTerm: LookupErrorOr[String] =
    Try(json.getString("searchData.searchTerm"))
      .toEither.left.map(_ => JsonParsingError("Problem while extracting the term"))

  def extractMeanings: LookupErrorOr[Seq[WordMeaning]] = Try {
    json.getConfigList("searchData.tunaApiData.posTabs").asScala.toList.map {
      tab =>
        val definition = tab.getString("definition")
        val synonyms = tab.getConfigList("synonyms").asScala.toList.map(_.getString("term"))
        val antonyms = tab.getConfigList("antonyms").asScala.toList.map(_.getString("term"))

        WordMeaning(definition, synonyms, antonyms)
    }
  }.toEither.left.map(_ => JsonParsingError("Problem while extracting word meanings"))

  def extractExamples: LookupErrorOr[Seq[String]] = Try {
    json.getConfigList("searchData.tunaApiData.exampleSentences")
      .asScala.toList.map(_.getString("sentence"))
  }.toEither.left.map(_ => JsonParsingError("Problem while extracting example sentences"))
}
