# How to Run the Application

I am using the latest LTS version of Java (Java 21) and Gradle.

## Prerequisites

- Java 21
- Gradle Understanding

## Steps to Run

1. Clone the repository:
    ```sh
    git clone https://github.com/vputtaparthi/url_shortener.git
    ```

2. Build the project using Gradle:
    ```sh
    ./gradlew build
    ```

3. Run the application:
    ```sh
    ./gradlew bootRun
    ```

4. The application should now be running at `http://localhost:8080`.

## Running Tests

```sh
./gradlew test
```

## Example Requests

### Encode a URL

This request encodes a given URL and allows specifying an optional prefix (up to 4 characters long) for fun:

```sh
curl --location 'http://localhost:8080/encode' \
--header 'Content-Type: application/json' \
--data '{
  "url": "https://example.com",
  "prefix": "cool"
}'
```

Example response:

```json
{
  "url": "https://short.est/cool_5BhOlU7iOi"
}
```

### Decode a Shortened URL

This request decodes the shortened URL back to its original form:

```sh
curl --location 'http://localhost:8080/decode' \
--header 'Content-Type: application/json' \
--data '{
  "url": "https://short.est/cool_5BhOlU7iOi"
}'
```

Example response:

```json
{
  "url": "https://example.com"
}
```

## Prefix Feature Implementation

The URL shortener allows users to specify a custom prefix of up to 4 characters when encoding a URL. If provided, the
prefix is included in the shortened URL before the generated unique identifier. A fun little personalized touch from a previous 
project I worked on.

### How it Works:

1. The application validates that the prefix is at most 4 characters long.
2. If a prefix is provided, it is prepended to the generated unique identifier using an underscore separator (
   `prefix_uniqueId`).
3. If no prefix is provided, it uses a default prefix.
