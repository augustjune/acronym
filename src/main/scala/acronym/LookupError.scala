package acronym

sealed trait LookupError

case object NoWordProvided extends LookupError
case class Misspelling(misspelledTerm: String) extends LookupError
case class JsonParsingError(message: String) extends LookupError
case class ServerError(message: String) extends LookupError
