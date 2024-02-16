# Epassi Coding Test


## Task description

The goal of this challenge is to write a REST API that takes a plain text file
and finds the top K most frequent words. The program should use Java 11+ and
Spring Boot for the backend API. Here are the specific requirements:

### Backend Requirements

- The API should have an endpoint that takes the link to a plain text file
(stored on AWS S3 or equivalent) and a value for K as a JSON request payload.

- The API should download the file from the original storage site, read it
and find the K most frequent words.

- The API should handle cases where the input text file is very large (e.g.
1&nbsp;GB or even more). Consider both processing and memory efficiency when
counting the words.

- The API should return the K most frequent words and their frequency in
descending order as a JSON response.

- The API should handle punctuation correctly, meaning that if you have a text
like: “This example, and that example and, other example!”, the word “and”
should be counted twice, and “example” three times.

- The API should have tests to ensure its functionality.

- The API should be documented (e.g. using Swagger).  

### Instructions

Provide a GitHub repository with your solution.

Include a README file with instructions on how to run the app and the tests.
 
### Bonus Points

Not required, but considered a differential if included.

- We encourage to add Information about time and space complexity of the
algorithm in the README file, as well as insights into the design choices,
algorithms and data structures used.

- Implement user authentication and authorization so that only authenticated
users can access the app.

- Implement a caching mechanism to avoid re-calculating the top K most frequent
words every time the API is called with the same text file and K value.


## Build and Run

Get project from Github repository (https://github.com/dem4nd/epassi-kfreq):

```bash
git clone https://github.com/dem4nd/epassi-kfreq.git
```

Build and run with the following command. The S3 credentials are specified in the file `/etc/epassi/kfreq/credentials/s3.properties`. The file is attached to the email. 

```bash
gradlew :kfreq-api:bootRun
```

## Tests

### Environment

The application has been built and tested in the following environment:

* MacOS Ventura 13.4.1
* Java openjdk 17.0.4.1
* Gradle 8.6

### Run unit tests

```bash
gradlew :kfreq-api:test
```

### Test with real documents from AWS S3

#### 1. Start server

If the S3 credentials file is exactly at `/etc/epassi/kfreq/credentials/s3.properties`:

```bash
gradlew :kfreq-api:bootRun
```

If the S3 credentials file is located elsewhere:

```bash
S3_CREDENTIALS_FILE=<Credentials file> gradlew :kfreq-api:bootRun
```

If S3 access key and secret are specified in command line:

```bash
S3_ACCESS_KEY=<S3 Key> S3_SECRET=<S3 Secret> gradlew :kfreq-api:bootRun
```

> *The credentials file is attached to the email. Valid access key and secret are provided within the email text as well.*

#### 2. Send request command in another terminal window

```bash
curl -s -X POST http://localhost:8080/api/v1/top \
    -H "Content-Type: application/json" \
    -d '{"resourceUrl":"https://s3.eu-north-1.amazonaws.com/dev.01/epassi/steinbeck.txt","limit":6}'
```

Document size is <b>5.5&nbsp;Kb</b>. The initial request was completed within <b>1.3&nbsp;seconds</b>, whereas
subsequent requests, benefiting from cached results, were completed in <b>3.9&nbsp;milliseconds</b>.

Results looks like that:

```json
[ {
  "word" : "steinbeck",
  "count" : 19
}, {
  "word" : "book",
  "count" : 7
}, {
  "word" : "men",
  "count" : 6
}, {
  "word" : "california",
  "count" : 6
}, {
  "word" : "film",
  "count" : 5
}, {
  "word" : "george",
  "count" : 5
} ]
```

Request with huge document: 

```bash
curl -s -X POST http://localhost:8080/api/v1/top \
    -H "Content-Type: application/json" \
    -d '{"resourceUrl":"https://s3.eu-north-1.amazonaws.com/dev.01/epassi/amazon-reviews.txt","limit":6}'
```

Document size is <b>1.2&nbsp;Gb</b>. The initial request was completed within <b>~&nbsp;5&nbsp;minutes</b>, whereas
subsequent requests, benefiting from cached results, were completed in <b>3.5&nbsp;milliseconds</b>.

Possible documents urls for testing:

* https://s3.eu-north-1.amazonaws.com/dev.01/epassi/steinbeck.txt (5.5Kb)
* https://s3.eu-north-1.amazonaws.com/dev.01/epassi/amazon-reviews.txt (1.2Gb)

### Request specification

Request method is `POST /api/v1/top`.

Full request body looks like that:

```json
{
  "resourceUrl": "https://s3.eu-north-1.amazonaws.com/dev.01/epassi/steinbeck.txt",
  "limit": 10,
  "encoding": "UTF-8",
  "useStopWords": true
}
```

and minimal like that:

```json
{
  "resourceUrl": "https://s3.eu-north-1.amazonaws.com/dev.01/epassi/steinbeck.txt",
  "limit": 10
}
```

## Algorithm details and complexity

### Description

For word frequency calculation, a HashMap is employed, where normalized correct words serve as keys and the
corresponding values represent the frequency count. After populating the HashMap, it is converted into an
ArrayList and sorted in descending order based on the frequency count. Subsequently, the top entries of the
desired size are extracted from the sorted list.

Words consisting of letters and numbers are considered correct. The hyphen `'-'` separator can be used in the
middle of a word. Also, conjunctions, prepositions, modal verbs, pronouns and articles are specified in
the configuration file in `stop-words` property and will be ignored to make the result more sensible. 

Stop words usage can be turned off in request json with property `"useStopWords": false`.

Examples of words:

* Java _(correct)_
* 2nd _(correct)_
* son-in-law _(correct)_
* abc476-x65 _(correct by design)_
* 1235 _(correct)_
* -tail _(incorrect and will be ignored because of hyphen in the biginning)_  
* tail- _(incorrect and will be ignored because of hyphen in the end)_
* John#Doe _(will be splitted to John and Doe)_
* that _(will be ignored in default "stop words" mode)_

### Optimization (cache results)

Results caching is implemented as well. WeakHashMap has been chosen as cache storage to avoid memory overflow.
The key of the cached result is the document url. An exact match of `limit` is not necessary. The cached result
will be retrieved even if the `limit` value is less than the size of the cached list of words with its frequencies.
In this case  required fragment of the cached list will be extracted without redundant document downloading
and processing.

### Computational complexity

Complexity of **N** insertions into HashMap is **O(N)**. Sort complexity is **O(NlogN)**.
The aggregate computational complexity is calculated as a maximum of two parts and will be **O(NlogN)**,
where **N** is number of **unique** words. 

### Space complexity

HashMap space complexity is **O(N)**. ArrayList and sort space complexity are **O(N)** as well.
Thus, the aggregate space complexity has linear growth rate **O(N)**, where **N** is number of **unique** words.

## API Documentation

Swagger injected docs at: http://localhost:8080/swagger-ui/index.html
