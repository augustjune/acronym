package object acronym {

  type LookupErrorOr[A] = Either[LookupError, A]

  case class ThesaurusWord(term: String, meanings: Seq[WordMeaning], usageExamples: Seq[String])

  case class WordMeaning(context: String, synonyms: Seq[String], antonyms: Seq[String])
}
