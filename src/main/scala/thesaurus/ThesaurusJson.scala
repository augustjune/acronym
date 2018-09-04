package thesaurus

import spray.json._

import scala.language.implicitConversions
import scala.util.matching.Regex

private[thesaurus] object ThesaurusJson {
	private val misspellingRegex = """.*Redirecting to /misspelling\?term=([A-Za-z]*)""".r
	private val wordSectorExpression: Regex = """<script>window.INITIAL_STATE = \{.*\}""".r

	def parseExpression(input: String): Either[String, JsValue] = input match {
		case misspellingRegex(term) => Left(s"Word '$term' has been misspelled")
		case _ => wordSectorExpression.findFirstMatchIn(input) match {
			case Some(jsonSelection) => Right(jsonSelection.group(1).parseJson)
			case None => Left("The structure of response Json is not applicable")
		}
	}

	implicit def jsValueToThesaurus(jsValue: JsValue): ThesaurusJson = new ThesaurusJson(jsValue)
}

private[thesaurus] class ThesaurusJson(jsValue: JsValue) {

	import ThesaurusJson._

	def extract(path: List[String]): JsValue = path match {
		case Nil => jsValue
		case head :: tl => jsValue match {
			case JsObject(fields) => fields(head).extract(tl)
			case JsArray(elements) => elements.head.extract(path)
		}
	}

	def extractField[A](path: List[String], fieldName: String)(extractor: JsValue => A): Vector[A] =
		jsValue.extract(path) match {
			case JsArray(elements) => elements.map {
				case JsObject(fields) => extractor(fields(fieldName))
			}
		}
}

