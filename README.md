# acronym
[![Build Status](https://travis-ci.com/augustjune/acronym.svg?branch=master)](https://travis-ci.com/augustjune/acronym)

**acronym** is a scala API for thesaurus.com, 
which provides a list of synonyms, antonyms and usage examples for a given word.

## Getting started
Package `acronym` contains all declarations you need, 
so you can directly import and use it.

```scala
import com.softwaremill.sttp.HttpURLConnectionBackend
import acronym._

implicit val backend = HttpURLConnectionBackend()

val thesaurusApi = new Thesaurus()
val word = thesaurusApi.lookup("convenient")
```
