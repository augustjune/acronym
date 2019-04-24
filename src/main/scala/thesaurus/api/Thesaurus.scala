package thesaurus.api

import com.softwaremill.sttp._

import scala.concurrent.{ExecutionContext, Future}

object Thesaurus {
  private def constructRequest(term: String): Request[String, Nothing] = sttp.get(uri"https://www.thesaurus.com/browse/term")

  type FutureErrorOr[T] = Future[Either[LookupError, T]]
}

import thesaurus.api.Thesaurus._

class Thesaurus(implicit backend: SttpBackend[Id, Nothing], executionContext: ExecutionContext) {

  def lookup(term: String): FutureErrorOr[ThesaurusWord] = {
    if (term.isEmpty) Future.successful(Left(NoWordProvided))
    else
      responseData(constructRequest(term))
        .map(response => ThesaurusResponse.fromHttpResponse(response).flatMap(_.extractWord))
  }

  private def responseData(request: Request[String, Nothing]): Future[String] = Future {
    request.send.unsafeBody
  }
}
