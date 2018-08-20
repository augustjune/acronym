package thesaurus

import akka.actor.{Actor, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.Materializer

import scala.concurrent.Future
import scala.concurrent.duration._

object ThesaurusApi {
	def props(implicit materializer: Materializer) = Props(new ThesaurusApi())

	private val strictTimeout: FiniteDuration = 3 seconds

	private def constructRequest(term: String): HttpRequest = HttpRequest(uri = s"https://www.thesaurus.com/browse/$term")

	private case class Word(term: String,
													synonyms: Seq[String],
													antonyms: Seq[String],
													usageExamples: Seq[String]) extends ThesaurusWord

}

class ThesaurusApi(implicit materializer: Materializer) extends Actor {

	import ThesaurusApi._
	import ThesaurusJson._
	import context.{dispatcher, system}

	override def receive: Receive = {
		case WordLookup(term) =>
			val parsedJson = responseData(constructRequest(term)).map(parseExpression)
			val wordData = parsedJson.map(_.extract(List("searchData", "tunaApiData")))

			val fieldExtractor = (path: List[String], field: String) => wordData.map(_.extractField(path, field)(_.toString))

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
