package com.shortener.rest;

import com.shortener.rest.request.DecodeRequest;
import com.shortener.rest.request.DecodeResponse;
import com.shortener.rest.request.EncodeRequest;
import com.shortener.rest.request.EncodeResponse;
import com.shortener.service.ShortenerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Semaphore;

@RestController
public class ShortenerController {

    @Autowired
    private ShortenerService shortenerService;

    // Sempahore are a simple way to limit the number of requests that can be made to the encode and decode endpoints
    // Additionally functionality could be provided with other packages such as bucket4j, but keeping it simple and native for this exercise.
    private final Semaphore semaphore = new Semaphore(2);

    @PostMapping("/encode")
    public ResponseEntity<EncodeResponse> encode( @Valid @RequestBody EncodeRequest encodeRequest) throws InterruptedException {
        if (semaphore.tryAcquire()) {
            try {
                EncodeResponse result = shortenerService.encode(encodeRequest);
                return ResponseEntity.ok(result);
            } finally {
                semaphore.release();
            }
        } else {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @PostMapping("/decode")
    public ResponseEntity<DecodeResponse> decode(@RequestBody DecodeRequest decodeRequest) {
        if (semaphore.tryAcquire()) {
            try {
                DecodeResponse result = shortenerService.decode(decodeRequest);
                return ResponseEntity.ok(result);
            } finally {
                semaphore.release();
            }
        } else {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }
}