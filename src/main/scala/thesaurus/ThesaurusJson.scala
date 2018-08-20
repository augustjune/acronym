package thesaurus

import spray.json._

import scala.util.matching.Regex

private[thesaurus] object ThesaurusJson {
	private val wordSectorHeader = """<script>window.INITIAL_STATE = """
	private val wordSectorExpression: Regex = (wordSectorHeader + """\{.*\}""").r

	def parseExpression(input: String): JsValue = wordSectorExpression.findFirstIn(input) match {
		case Some(jsonSelection) => jsonSelection.substring(wordSectorHeader.length).parseJson
		case None => throw new RuntimeException("Exception raised during parsing json")
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

