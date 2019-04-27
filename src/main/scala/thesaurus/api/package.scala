package thesaurus

package object api {

  case class ThesaurusWord(term: String, meanings: Seq[WordMeaning], usageExamples: Seq[String])

  case class WordMeaning(context: String, synonyms: Seq[String], antonyms: Seq[String])

}
