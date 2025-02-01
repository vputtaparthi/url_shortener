package com.shortener.rest;

import com.shortener.rest.request.DecodeRequest;
import com.shortener.rest.request.DecodeResponse;
import com.shortener.rest.request.EncodeRequest;
import com.shortener.rest.request.EncodeResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the URL shortener application.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestShortenerApplication {

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * Tests the /encode endpoint to ensure it returns a shortened URL.
     */
    @Test
    public void testEncode() {
        String url = "/encode";
        EncodeRequest request = new EncodeRequest();
        request.setUrl("https://example.com");
        request.setPrefix("cool");

        HttpEntity<EncodeRequest> requestEntity = new HttpEntity<>(request);
        ResponseEntity<EncodeResponse> response = restTemplate.postForEntity(url, requestEntity, EncodeResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUrl()).isNotEmpty();
        assertThat(response.getBody().getUrl()).contains("cool");
    }

    /**
     * Tests the /encode endpoint to ensure it returns a BAD_REQUEST status
     * when the prefix is too long.
     */
    @Test
    public void testEncodeFailsWhenPrefixTooLong() {
        String url = "/encode";
        EncodeRequest request = new EncodeRequest();
        request.setUrl("https://example.com");
        request.setPrefix("coool");

        HttpEntity<EncodeRequest> requestEntity = new HttpEntity<>(request);
        ResponseEntity<EncodeResponse> response = restTemplate.postForEntity(url, requestEntity, EncodeResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /**
     * Tests the /decode endpoint to ensure it returns the original URL
     * for a given shortened URL.
     */
    @Test
    public void testDecode() {

        // Encode the url
        String encodeUrl = "/encode";
        EncodeRequest request = new EncodeRequest();
        request.setUrl("https://example.com");

        HttpEntity<EncodeRequest> requestEntity = new HttpEntity<>(request);
        ResponseEntity<EncodeResponse> encodeResponse = restTemplate.postForEntity(encodeUrl, requestEntity, EncodeResponse.class);
        assertThat(encodeResponse.getBody()).isNotNull();

        //Verify that we get the correct url back with the given short url
        String url = "/decode";
        DecodeRequest decodeRequest = new DecodeRequest();
        decodeRequest.setUrl(encodeResponse.getBody().getUrl());

        HttpEntity<DecodeRequest> decodeRequestEntity = new HttpEntity<>(decodeRequest);
        ResponseEntity<DecodeResponse> decodeResponse = restTemplate.postForEntity(url, decodeRequestEntity, DecodeResponse.class);

        assertThat(decodeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(decodeResponse.getBody()).isNotNull();
        assertThat(decodeResponse.getBody().getUrl()).isNotEmpty();
        assertThat(decodeResponse.getBody().getUrl()).contains("https://example.com");
    }

    /**
     * Tests the /encode endpoint to ensure it returns a TOO_MANY_REQUESTS status
     * when the semaphore limit is reached.
     *
     * @throws InterruptedException if any thread has interrupted the current thread
     */
    @Test
    public void testTooManyRequests() throws InterruptedException {
        String url = "/encode";
        EncodeRequest request = new EncodeRequest();
        request.setUrl("https://example.com");

        // Fire a number of requests all at once to overload the server
        HttpEntity<EncodeRequest> requestEntity = new HttpEntity<>(request);
        Thread thread1 = new Thread(() -> restTemplate.postForEntity(url, requestEntity, String.class));
        Thread thread2 = new Thread(() -> restTemplate.postForEntity(url, requestEntity, String.class));
        Thread thread3 = new Thread(() -> restTemplate.postForEntity(url, requestEntity, String.class));
        Thread thread4 = new Thread(() -> restTemplate.postForEntity(url, requestEntity, String.class));

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();

        thread1.join();
        thread2.join();
        thread3.join();
        thread4.join();

        // Final request should be overloaded
        Thread thread5 = new Thread(() -> {
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
            assertThat(response.getBody()).isEqualTo("Too many requests");
        });

        thread5.start();
        thread5.join();
    }
}