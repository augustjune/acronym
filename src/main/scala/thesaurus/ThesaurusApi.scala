package thesaurus

import akka.actor.Actor

class ThesaurusApi extends Actor {

	override def receive: Receive = {
		case WordLookup(term) =>

			ThesaurusWord(term, Nil, Nil, Nil)
	}


}
