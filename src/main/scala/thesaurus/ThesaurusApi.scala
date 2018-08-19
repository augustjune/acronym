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

	private def constructRequest(term: String): HttpRequest = HttpRequest(uri = s"https://www.thesaurus.com/browse/$term")

	private val strictTimeout: FiniteDuration = 3 seconds
}

class ThesaurusApi(implicit materializer: Materializer) extends Actor {

	import context.system
	import context.dispatcher

	import ThesaurusApi._


	override def receive: Receive = {
		case WordLookup(term) =>

			val expressionHead = """<script>window.INITIAL_STATE = """
			val jsonSelectionExpr: Regex = """<script>window.INITIAL_STATE = \{.*\}""".r

			val futureData = for {
				HttpResponse(_, _, entity, _) <- Http().singleRequest(constructRequest(term))
				strict <- entity.toStrict(strictTimeout)
			} yield strict.data.utf8String

			val parsedJson = futureData.map(jsonSelectionExpr.findFirstIn(_) match {
				case Some(jsonSelection) => jsonSelection.substring(expressionHead.length).parseJson
				case None => throw new RuntimeException("Exception raised during parsing json")
			})

			val wordData = parsedJson.map(extractJsValue(List("searchData", "tunaApiData")))

			val synonyms = wordData.map(extractJsValue(List("posTabs", "synonyms"))(_) match {
				case JsArray(elements) => elements.map {
					case JsObject(fields) => fields("term").toString
				}
			})

			val antonyms = wordData.map(extractJsValue(List("posTabs", "antonyms"))(_) match {
				case JsArray(elements) => elements.map {
					case JsObject(fields) => fields("term").toString
				}
			})

			val examples = wordData.map(extractJsValue(List("exampleSentences"))(_) match {
				case JsArray(elements) => elements.map {
					case JsObject(fields) => fields("sentence").toString
				}
			})


			val futureWord = for {
				s <- synonyms
				a <- antonyms
				e <- examples
			} yield ThesaurusWord(term, s, a, e)

			sender() ! futureWord
	}


	private def extractJsValue(path: List[String])(jsValue: JsValue): JsValue = path match {
		case Nil => jsValue
		case ::(head, tl) => jsValue match {
			case JsObject(fields) => extractJsValue(tl)(fields(head))
			case JsArray(elements) => extractJsValue(path)(elements.head)
		}
	}

}
