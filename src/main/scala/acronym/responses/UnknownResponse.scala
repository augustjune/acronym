package acronym.responses

import acronym.{JsonParsingError, LookupErrorOr, ThesaurusResponse, ThesaurusWord}

private[acronym] class UnknownResponse extends ThesaurusResponse {
  def extractWord: LookupErrorOr[ThesaurusWord] = Left(JsonParsingError("Error while extracting initial state"))
}

