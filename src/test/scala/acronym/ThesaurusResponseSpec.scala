package acronym

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.WordSpec
import pureconfig.ConfigReader.Result

import scala.io.Source
import pureconfig.generic.auto.exportReader

class ThesaurusResponseSpec extends WordSpec {

  def resourceContent(path: String): String =
    Source.fromFile(getClass.getResource(path).toURI).mkString

  def config(path: String): Config = ConfigFactory.parseResources(path)

  "A Thesaurus data" when {
    "is being created" should {
      "handle any string" in {
        assert(ThesaurusResponse.fromHttpResponse("").extractWord.isLeft)
        assert(ThesaurusResponse.fromHttpResponse("asdasd").extractWord.isLeft)
      }

      "handle misspelled redirection" in {
        val expected: Result[LookupError] =
          pureconfig.loadConfig[Misspelling](config("words/misspelled.conf"))
        val parsed = ThesaurusResponse.fromHttpResponse(resourceContent("/cached/misspelled")).extractWord
        assert(expected.right.get == parsed.left.get)
      }

      "parse proper response" in {
        assert(ThesaurusResponse.fromHttpResponse(resourceContent("/cached/taken")).extractWord.isRight)
      }
    }

    "is read" should {
      val parsedData = ThesaurusResponse.fromHttpResponse(resourceContent("/cached/taken"))

      "extract word data" in {
        val parsed = parsedData.extractWord
        val expected = pureconfig.loadConfig[ThesaurusWord](config("words/taken.conf"))
        assert(expected == parsed)
      }
    }
  }
}
