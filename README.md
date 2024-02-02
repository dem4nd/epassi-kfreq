# Epassi Coding Test

## Task description

The goal of this challenge is to write a REST API that takes a plain text file and finds the top K most frequent words. The program should use Java 11+ and Spring Boot for the backend API. Here are the specific requirements:

### Backend Requirements

- The API should have an endpoint that takes the link to a plain text file (stored on AWS S3 or equivalent) and a value for K as a JSON request payload.

- The API should download the file from the original storage site, read it and find the K most frequent words.

- The API should handle cases where the input text file is very large (e.g. 1 GB or even more). Consider both processing and memory efficiency when counting the words.

- The API should return the K most frequent words and their frequency in descending order as a JSON response.

- The API should handle punctuation correctly, meaning that if you have a text like: “This example, and that example and, other example!”, the word “and” should be counted twice, and “example” three times.

- The API should have tests to ensure its functionality.

- The API should be documented (e.g. using Swagger).  

### Instructions

Provide a GitHub repository with your solution.

Include a README file with instructions on how to run the app and the tests.
 
### Bonus Points

Not required, but considered a differential if included.

- We encourage to add Information about time and space complexity of the algorithm in the README file, as well as insights into the design choices, algorithms and data structures used.

- Implement user authentication and authorization so that only authenticated users can access the app.

- Implement a caching mechanism to avoid re-calculating the top K most frequent words every time the API is called with the same text file and K value.

## Build and run

...To be provided...
