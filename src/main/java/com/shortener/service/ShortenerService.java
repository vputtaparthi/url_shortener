package com.shortener.service;

import com.shortener.rest.request.DecodeRequest;
import com.shortener.rest.request.DecodeResponse;
import com.shortener.rest.request.EncodeRequest;
import com.shortener.rest.request.EncodeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import util.SUID;

import java.util.HashMap;

@Service
public class ShortenerService {

    // Base URL pulled from properties that the short URL should be prepended to
    @Value("${shortener.base.url}")
    private String baseUrl;

    // HashMap to store the mapping between short URLs and original URLs
    private static final HashMap<String, String> urlMap = new HashMap<>();

    /**
     * Encodes a given URL into a shortened URL.
     *
     * @param request the request object containing the original URL to be shortened
     * @return the response object containing the shortened URL
     */
    public EncodeResponse encode(EncodeRequest request) {
        EncodeResponse response = new EncodeResponse();
        String prefix = request.getPrefix();

        // Set default prefix - fun little addition
        if (prefix == null || prefix.isEmpty()) {
            prefix = "wow";
        }

        String existingShortURL = urlMap.get(request.getUrl());

        if (existingShortURL != null) {
            response.setUrl(existingShortURL);
            return response;
        }

        // Pass in any prefix you would like. Could expand on this with different types of short URL identifiers
        String shortId = SUID.generate(prefix);
        urlMap.put(shortId, request.getUrl());

        response.setUrl(baseUrl + shortId);
        return response;
    }

    /**
     * Decodes a shortened URL back to its original URL.
     *
     * @param request the request object containing the shortened URL to be decoded
     * @return the response object containing the original URL
     */
    public DecodeResponse decode(DecodeRequest request) {
        String shortId = request.getUrl().substring(baseUrl.length());
        String originalUrl = urlMap.get(shortId);

        DecodeResponse response = new DecodeResponse();
        response.setUrl(originalUrl);
        return response;
    }
}