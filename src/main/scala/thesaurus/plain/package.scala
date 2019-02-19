package thesaurus

package object plain {
  trait ThesaurusAPI[F[_]] {
    def lookup(term: String): F[ThesaurusWord]
  }

  case class ThesaurusWord(term: String, meanings: List[WordMeaning], usageExamples: Seq[String])

  case class WordMeaning(definition: String, synonyms: Seq[String], antonyms: Seq[String])
}

