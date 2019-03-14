package thesaurus

import org.scalatest.WordSpec

import scala.io.Source

class ThesaurusDataSpec extends WordSpec {
  def readFromFile(path: String): String = {
    Source.fromFile(getClass.getResource(path).toURI).mkString
  }


  "A Thesaurus data" when {
    "is created" should {
      "handle any string" in {
        assert(ThesaurusData.fromHttpResponse("").isLeft)
        assert(ThesaurusData.fromHttpResponse("asdasd").isLeft)
      }

      "handle misspelled redirection" in {
        assertResult(ThesaurusData.fromHttpResponse(readFromFile("/cached/misspelled")))(Left(Misspelling("misspelledd")))
      }

      "parse proper response" in {
        assert(ThesaurusData.fromHttpResponse(readFromFile("/cached/convenient")).isRight)
      }
    }

    "is read" should {
      val parsedData = ThesaurusData.fromHttpResponse(readFromFile("/cached/taken"))

      "extract term" in {
        val eitherTerm = for (data <- parsedData; word <- data.extractWord) yield word.term
        assert(eitherTerm == Right("taken"))
      }

      "extract meanings" in {
        val eitherMeanings = for (data <- parsedData; word <- data.extractWord) yield word.meanings
      }
    }

    /*"extract meanings" in {
      assert(parsedData.flatMap(_.extractWord).isRight)
      parsedData.flatMap(_.extractWord) match {
        case Left(value) => fail()
        case Right(value) =>
      }
    }*/
  }
}
