# thesaurus-api
[![Build Status](https://travis-ci.com/augustjune/thesaurus-api.svg?branch=master)](https://travis-ci.com/augustjune/thesaurus-api)

Thesaurus API provides asynchronous search of synonyms, 
antonyms and usage examples of provided word.

## Getting started
Package `thesaurus.api` contains all declarations you need, 
so you can directly import and use it.
```
import thesaurus.api._

val thesaurus = new Thesaurus()
val word = thesaurus.lookup("crucial")
```
