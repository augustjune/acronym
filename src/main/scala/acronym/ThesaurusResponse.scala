package acronym

import acronym.responses._
import com.typesafe.config.ConfigFactory

private[acronym] trait ThesaurusResponse {
  def extractWord: LookupErrorOr[ThesaurusWord]
}

private[acronym] object ThesaurusResponse {
  private val misspellingRegex = """misspelling\?term=([A-Za-z]*)"""".r
  private val wordSectorExpression = """<script>window.INITIAL_STATE = (\{.*\})""".r

  def fromHttpResponse(input: String): ThesaurusResponse =
    Stream(extractMisspelled _, extractPositive _, (_: String) => Some(new UnknownResponse()))
      .flatMap(_.apply(input)).head

  private def extractMisspelled(input: String): Option[ThesaurusResponse] =
    misspellingRegex.findFirstMatchIn(input)
      .map(matching => new MisspelledResponse(matching.group(1)))

  private def extractPositive(input: String): Option[ThesaurusResponse] =
    wordSectorExpression.findFirstMatchIn(input)
      .map(matching => new PositiveResponse(ConfigFactory.parseString(matching.group(1))))
}
