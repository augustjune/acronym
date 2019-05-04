package acronym

import com.softwaremill.sttp.HttpURLConnectionBackend
import org.scalatest._

class ThesaurusIT extends FunSuite with Matchers {
  implicit val backend = HttpURLConnectionBackend()
  val thesaurus = new Thesaurus()

  test("should read existing the word from service") {
    assert(thesaurus.lookup("word").isRight)
  }

  test("should inform when word is misspelled") {
    val term = "mispelled"
    thesaurus.lookup(term).left.get shouldBe Misspelling(term)
  }
}
