package thesaurus.api

sealed trait LookupError

case class Misspelling(misspelledTerm: String) extends LookupError

case object NoWordProvided extends LookupError

case class JsonParsingError(message: String) extends LookupError
