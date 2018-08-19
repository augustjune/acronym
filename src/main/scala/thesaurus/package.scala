package object thesaurus {
	case class WordLookup(term: String)

	case class ThesaurusWord(term: String,
													 synonyms: List[String],
													 antonyms: List[String],
													 examples: List[String])
}
