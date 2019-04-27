package thesaurus.api

import com.softwaremill.sttp._

import scala.concurrent.{ExecutionContext, Future}

import thesaurus.api.Thesaurus._

/**
  * Representation of Thesaurus service
  */
class Thesaurus(implicit backend: SttpBackend[Id, Nothing], executionContext: ExecutionContext) {

  /**
    * Returns synonyms and antonyms in different contexts,
    * as well as usage examples of the provided term
    */
  def lookup(term: String): Future[LookupErrorOr[ThesaurusWord]] =
    if (term.isEmpty) Future.successful(Left(NoWordProvided))
    else
      responseData(request(term)).map { futureResp =>
        for {
          httpResponse <- futureResp
          thesaurusData <- ThesaurusResponse.fromHttpResponse(httpResponse)
          word <- thesaurusData.extractWord
        } yield word
      }

  private def responseData(request: Request[String, Nothing]): Future[LookupErrorOr[String]] = Future {
    request.send.body.left.map(ServerError)
  }
}

object Thesaurus {
  private def request(term: String): Request[String, Nothing] =
    sttp.get(uri"https://www.thesaurus.com/browse/term")
}
