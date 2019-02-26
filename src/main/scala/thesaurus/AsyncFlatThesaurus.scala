package thesaurus

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.Materializer

import scala.concurrent.Future
import scala.concurrent.duration._

object AsyncFlatThesaurus {
  private val strictTimeout: FiniteDuration = 3 seconds

  private def constructRequest(term: String): HttpRequest = HttpRequest(uri = "https://www.thesaurus.com/browse/" + term)

  type FutureErrorOr[T] = Future[Either[LookupError, T]]
}

import thesaurus.AsyncFlatThesaurus._

class AsyncFlatThesaurus(implicit system: ActorSystem, materializer: Materializer) extends ThesaurusAPI[FutureErrorOr] {

  import system.dispatcher

  def lookup(term: String): FutureErrorOr[ThesaurusWord] = {
    if (term.isEmpty) Future.successful(Left(NoWordProvided))
    else responseData(constructRequest(term))
      .map(response => ThesaurusData.fromHttpResponse(response).flatMap(_.extractWord))
  }

  private def responseData(request: HttpRequest): Future[String] = {
    for {
      HttpResponse(_, _, entity, _) <- Http().singleRequest(request)
      strict <- entity.toStrict(strictTimeout)
    } yield strict.data.utf8String
  }
}
