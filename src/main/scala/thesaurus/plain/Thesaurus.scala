package thesaurus.plain

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.Materializer
import cats.data.EitherT
import cats.instances.future._
import thesaurus.actors.ThesaurusJson

import scala.concurrent.Future
import scala.concurrent.duration._

object Thesaurus {
  private val strictTimeout: FiniteDuration = 3 seconds

  private def constructRequest(term: String): HttpRequest = HttpRequest(uri = "https://www.thesaurus.com/browse/" + term)

  private def removeQuotes(s: String): String = s.substring(1, s.length - 1)

  type FutureStringOr[T] = Future[Either[String, T]]
}

import ThesaurusJson._
import Thesaurus._

class Thesaurus(implicit system: ActorSystem, materializer: Materializer) extends ThesaurusAPI[FutureStringOr] {

  import system.dispatcher

  def lookup(term: String): FutureStringOr[ThesaurusWord] = {
    val parsedJson = EitherT(responseData(constructRequest(term)).map(parseExpression))
    val wordData = parsedJson.map(_.extract(List("searchData", "tunaApiData")))

    //def fieldExtractor(path: List[String]): EitherT[Future, String, String]

    def listExtractor(path: List[String], field: String): EitherT[Future, String, Vector[String]] =
      wordData.map(_.extractField(path, field)(x => removeQuotes(x.toString())))

    val res = for {
      synonyms <- listExtractor(List("posTabs", "synonyms"), "term")
      antonyms <- listExtractor(List("posTabs", "antonyms"), "term")
      examples <- listExtractor(List("exampleSentences"), "sentence")
    } yield ThesaurusWord(term, List(WordMeaning("", synonyms, antonyms)), examples)

    res.value
  }

  private def responseData(request: HttpRequest): Future[String] = {
    for {
      HttpResponse(_, _, entity, _) <- Http().singleRequest(request)
      strict <- entity.toStrict(strictTimeout)
    } yield strict.data.utf8String
  }
}
