package thesaurus

import spray.json._

import scala.util.matching.Regex

object ThesaurusJson {
	private val expressionHead = """<script>window.INITIAL_STATE = """
	private val jsonSelectionExpr: Regex = (expressionHead + """\{.*\}""").r

	def parseExpression(input: String): JsValue = jsonSelectionExpr.findFirstIn(input) match {
		case Some(jsonSelection) => jsonSelection.substring(expressionHead.length).parseJson
		case None => throw new RuntimeException("Exception raised during parsing json")
	}

	implicit class RichJsValue(jsValue: JsValue) {
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
}
