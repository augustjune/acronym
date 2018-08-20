import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.pattern.ask
import akka.util.Timeout
import thesaurus.{ThesaurusApi, ThesaurusWord, WordLookup}

import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import concurrent.duration._


object TestMain extends App {
	implicit val system = ActorSystem("thesaurus-api-test-system")
	implicit val materializer = ActorMaterializer()
	implicit val executionContext: ExecutionContextExecutor = system.dispatcher
	implicit val timeout = Timeout(20 seconds)

	val apiActor: ActorRef = system.actorOf(ThesaurusApi.props, "api-actor")
	val futureWord = apiActor ? WordLookup("expensive") flatMap  {
		case x: Future[ThesaurusWord] => x
	}

	for {
		word <- futureWord
		_ <- system.terminate()
	} println(word)


}


