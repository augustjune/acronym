package acronym.responses

import acronym.{LookupErrorOr, Misspelling, ThesaurusResponse, ThesaurusWord}

private[acronym] class MisspelledResponse(term: String) extends ThesaurusResponse {
  def extractWord: LookupErrorOr[ThesaurusWord] = Left(Misspelling(term))
}
