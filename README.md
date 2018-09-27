# thesaurus-api
Thesaraus API provides actor-based async scala wrapper for getting synonyms, antonyms and sentence examples for a given word from Thesaurus.com

## Getting started
* Package `thesaurus` contains all declarations you need, so you can directly import it
```
import thesaurus._
```
* Create an actor from `ThesaurusApi.props`
```
val api = system.actorOf(ThesaurusApi.props)
```
* Use ask pattern along with `WordLookup(word)` to get all data about provided word from Thesaurus.com. As a return you will get `Future[Either[String, ThesaurusWord]]`, which corresponds to successful lookup as right or explicit message about any problems with connection or typo.
