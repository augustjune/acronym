package thesaurus.api

import cats.Applicative
import com.softwaremill.sttp._
import thesaurus.api.Thesaurus._

/**
  * Representation of Thesaurus service
  */
class Thesaurus[F[_]](implicit F: Applicative[F], backend: SttpBackend[F, Nothing]) {

  /**
    * Returns synonyms and antonyms in different contexts,
    * as well as usage examples of the provided term
    */
  def lookup(term: String): F[LookupErrorOr[ThesaurusWord]] =
    if (term.isEmpty) F.pure(Left(NoWordProvided))
    else
      F.map(responseData(request(term))) { reply =>
        for {
          httpResponse <- reply
          thesaurusData <- ThesaurusResponse.fromHttpResponse(httpResponse)
          word <- thesaurusData.extractWord
        } yield word
      }

  private def responseData(request: Request[String, Nothing]): F[LookupErrorOr[String]] =
    F.map(request.send)(r => r.body.left.map(ServerError))
}

object Thesaurus {
  private def request(term: String): Request[String, Nothing] =
    sttp.get(uri"https://www.thesaurus.com/browse/$term")
}
