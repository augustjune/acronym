package thesaurus

import com.softwaremill.sttp._

import scala.concurrent.{ExecutionContext, Future}

object AsyncFlatThesaurus {
  private def constructRequest(term: String): Request[String, Nothing] = sttp.get(uri"https://www.thesaurus.com/browse/term")

  type FutureErrorOr[T] = Future[Either[LookupError, T]]
}

import thesaurus.AsyncFlatThesaurus._

class AsyncFlatThesaurus(implicit backend: SttpBackend[Id, Nothing], executionContext: ExecutionContext)
  extends ThesaurusAPI[FutureErrorOr] {

  def lookup(term: String): FutureErrorOr[ThesaurusWord] = {
    if (term.isEmpty) Future.successful(Left(NoWordProvided))
    else
      responseData(constructRequest(term))
        .map(response => ThesaurusData.fromHttpResponse(response).flatMap(_.extractWord))
  }

  private def responseData(request: Request[String, Nothing]): Future[String] = Future {
    request.send.unsafeBody
  }
}
