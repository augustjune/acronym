package thesaurus

sealed trait LookupError

case class Misspelling(misspelledTerm: String) extends LookupError

case class JsonParsingError(message: String) extends LookupError
