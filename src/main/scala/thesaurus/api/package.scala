package thesaurus

package object api {

  case class ThesaurusWord(term: String, meanings: Seq[WordMeaning], usageExamples: Seq[String])

  case class WordMeaning(definition: String, synonyms: Seq[String], antonyms: Seq[String])

}
