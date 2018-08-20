package object thesaurus {
	case class WordLookup(term: String)

	trait ThesaurusWord {
		def term: String

		def synonyms: Seq[String]

		def antonyms: Seq[String]

		def usageExamples: Seq[String]
	}
}
