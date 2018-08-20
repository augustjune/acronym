package thesaurus

import akka.actor.{Actor, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.Materializer
import spray.json._

import concurrent.duration._
import scala.concurrent.Future
import scala.util.matching.Regex

object ThesaurusApi {
	def props(implicit materializer: Materializer) = Props(new ThesaurusApi())

	private val strictTimeout: FiniteDuration = 3 seconds

	private val expressionHead = """<script>window.INITIAL_STATE = """
	private val jsonSelectionExpr: Regex = (expressionHead + """\{.*\}""").r

	private def constructRequest(term: String): HttpRequest = HttpRequest(uri = s"https://www.thesaurus.com/browse/$term")

	private def parseExpression(expression: Regex, headSkip: Int)(input: String): JsValue = {
		expression.findFirstIn(input) match {
			case Some(jsonSelection) => jsonSelection.substring(headSkip).parseJson
			case None => throw new RuntimeException("Exception raised during parsing json")
		}
	}

	private def extractJsValue(path: List[String])(jsValue: JsValue): JsValue = path match {
		case Nil => jsValue
		case ::(head, tl) => jsValue match {
			case JsObject(fields) => extractJsValue(tl)(fields(head))
			case JsArray(elements) => extractJsValue(path)(elements.head)
		}
	}

	private def extractField(path: List[String], fieldName: String)(jsValue: JsValue): Vector[String] = {
		extractJsValue(path)(jsValue) match {
			case JsArray(elements) => elements.map {
				case JsObject(fields) => fields(fieldName).toString
			}
		}
	}

	private case class Word(term: String,
													synonyms: Seq[String], antonyms: Seq[String],
													usageExamples: Seq[String]) extends ThesaurusWord
}

class ThesaurusApi(implicit materializer: Materializer) extends Actor {

	import context.system
	import context.dispatcher

	import ThesaurusApi._

	override def receive: Receive = {
		case WordLookup(term) =>
			val parsedJson = responseData(constructRequest(term)).map(parseExpression(jsonSelectionExpr, expressionHead.length))
			val wordData = parsedJson.map(extractJsValue(List("searchData", "tunaApiData")))

			val fieldExtractor = (path: List[String], field: String) => wordData.map(extractField(path, field))

			val synonyms = fieldExtractor(List("posTabs", "synonyms"), "term")
			val antonyms = fieldExtractor(List("posTabs", "antonyms"), "term")
			val examples = fieldExtractor(List("exampleSentences"), "sentence")

			val futureWord = for {
				s <- synonyms
				a <- antonyms
				e <- examples
			} yield Word(term, s, a, e)

			sender() ! futureWord
	}

	private def responseData(request: HttpRequest): Future[String] = {
		for {
			HttpResponse(_, _, entity, _) <- Http().singleRequest(request)
			strict <- entity.toStrict(strictTimeout)
		} yield strict.data.utf8String
	}
}
