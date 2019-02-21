package thesaurus

import spray.json._

import scala.util.Try

object ThesaurusData {
  private val misspellingRegex = """.*Redirecting to /misspelling\?term=([A-Za-z]*)""".r
  private val wordSectorExpression = """<script>window.INITIAL_STATE = (\{.*\})""".r

  def fromHttpResponse(input: String): Either[String, ThesaurusData] = input match {
    case misspellingRegex(term) => Left(s"Word '$term' has been misspelled")
    case _ => wordSectorExpression.findFirstMatchIn(input) match {
      case Some(jsonSelection) =>
        val parsedResponse = jsonSelection.group(1).parseJson
        Right(new ThesaurusData(parsedResponse.asJsObject.fields("searchData")))

      case None => Left("The structure of response Json is not applicable")
    }
  }
}

class ThesaurusData(jsValue: JsValue) {
  def extractWord: Either[String, ThesaurusWord] = for {
    term <- extractTerm
    meanings <- extractMeanings
    examples <- extractExamples
  } yield ThesaurusWord(term, meanings, examples)

  def extractTerm: Either[String, String] =
    Try(removeQuotes(jsValue.asJsObject.fields("searchTerm").toString))
      .toOption.toRight("Problem while extracting the term")

  def extractMeanings: Either[String, Seq[WordMeaning]] = Try {
    val tunaApiData = jsValue.asJsObject.fields("tunaApiData")
    tunaApiData.asJsObject.fields("posTabs") match {
      case JsArray(postabs) => postabs.map {
        case JsObject(fields) =>
          val definition = removeQuotes(fields("definition").toString())

          def extractTerms(name: String) = fields(name) match {
            case JsArray(syns) => syns
              .map(_.asJsObject.fields("term").toString())
              .map(removeQuotes)
          }

          val synonyms = extractTerms("synonyms")
          val antonyms = extractTerms("antonyms")

          WordMeaning(definition, synonyms, antonyms)
      }
    }
  }.toOption.toRight("Problem while extracting word meanings")

  def extractExamples: Either[String, Seq[String]] = Try {
    val tunaApiData = jsValue.asJsObject.fields("tunaApiData")
    tunaApiData.asJsObject.fields("exampleSentences") match {
      case JsArray(elements) => elements
        .map(_.asJsObject.fields("sentence").toString())
        .map(removeQuotes)
    }
  }.toOption.toRight("Problem while extracting example sentences")

  private def removeQuotes(s: String): String = s.substring(1, s.length - 1)
}
