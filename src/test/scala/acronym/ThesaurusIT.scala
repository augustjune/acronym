package acronym

import com.softwaremill.sttp.HttpURLConnectionBackend
import org.scalatest.FunSuite

class ThesaurusIT extends FunSuite {
  implicit val backend = HttpURLConnectionBackend()
  val thesaurus = new Thesaurus()

  test("should read existing the word from service") {
    assert(thesaurus.lookup("word").isRight)
  }
}
