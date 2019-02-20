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
  def wordData: Either[String, ThesaurusWord] = Try{
    val term = removeQuotes(jsValue.asJsObject.fields("searchTerm").toString)

    val tunaApiData = jsValue.asJsObject.fields("tunaApiData")

    val meanings = tunaApiData.asJsObject.fields("posTabs") match {
      case JsArray(postabs) => postabs.map {
        case JsObject(fields) =>
          val definition = removeQuotes(fields("definition").toString())

          val synonyms = fields("synonyms") match {
            case JsArray(syns) => syns
              .map(_.asJsObject.fields("term").toString())
              .map(removeQuotes)
          }

          val antonyms = fields("antonyms") match {
            case JsArray(ants) => ants
              .map(_.asJsObject.fields("term").toString())
              .map(removeQuotes)
          }

          WordMeaning(definition, synonyms, antonyms)
      }
    }

    val examples = tunaApiData.asJsObject.fields("exampleSentences") match {
      case JsArray(elements) => elements
        .map(_.asJsObject.fields("sentence").toString())
        .map(removeQuotes)
    }

    ThesaurusWord(term, meanings, examples)
  }.toOption.toRight("Problem while extracting the values")

  private def removeQuotes(s: String): String = s.substring(1, s.length - 1)
}
