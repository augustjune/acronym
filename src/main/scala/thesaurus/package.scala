package object thesaurus {
	case class WordLookup(term: String)

	case class ThesaurusWord(term: String,
													 synonyms: Seq[String],
													 antonyms: Seq[String],
													 examples: Seq[String])
}
