package thesaurus

import akka.actor.{Actor, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.pattern.pipe
import akka.stream.Materializer
import cats.data.EitherT
import cats.instances.future._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

object ThesaurusApi {
	def props(implicit materializer: Materializer) = Props(new ThesaurusApi())

	private val strictTimeout: FiniteDuration = 3 seconds

	private def constructRequest(term: String): HttpRequest = HttpRequest(uri = "https://www.thesaurus.com/browse/" + term)

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
			val parsedJson = EitherT(responseData(constructRequest(term)).map(parseExpression))
			val wordData = parsedJson.map(_.extract(List("searchData", "tunaApiData")))

			val fieldExtractor = (path: List[String], field: String) => wordData.map(_.extractField(path, field)(_.toString))

			val futureWord = for {
				synonyms <- fieldExtractor(List("posTabs", "synonyms"), "term")
				antonyms <- fieldExtractor(List("posTabs", "antonyms"), "term")
				examples <- fieldExtractor(List("exampleSentences"), "sentence")
			} yield Word(term, synonyms, antonyms, examples)

			futureWord.value pipeTo sender()
	}

	private def responseData(request: HttpRequest): Future[String] = {
		for {
			HttpResponse(_, _, entity, _) <- Http().singleRequest(request)
			strict <- entity.toStrict(strictTimeout)
		} yield strict.data.utf8String
	}
}
