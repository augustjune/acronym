package object thesaurus {

  trait ThesaurusAPI[F[_]] {
    def lookup(term: String): F[ThesaurusWord]
  }

  case class ThesaurusWord(term: String, meanings: Seq[WordMeaning], usageExamples: Seq[String])

  case class WordMeaning(definition: String, synonyms: Seq[String], antonyms: Seq[String])

}
