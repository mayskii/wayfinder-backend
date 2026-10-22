package com.wayfinder.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class AttractionService {

    private static final Logger log = LoggerFactory.getLogger(AttractionService.class);

    private final RestTemplate restTemplate;

    @Value("${overpass.api.url}")
    private String overpassUrl;

    public AttractionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Map<String, Object>> getAttractions(String bbox) {
        String query = String.format("""
            [out:json][timeout:25];
                (
                node["tourism"~"museum|artwork|gallery"]( %s );
                way["tourism"~"museum|artwork|gallery"]( %s );
                relation["tourism"~"museum|artwork|gallery"]( %s );
                node["leisure"="park"](%s);
                way["leisure"="park"](%s);
                relation["leisure"="park"](%s);
                node["amenity"~"restaurant|cafe|fast_food"](%s);
                way["amenity"~"restaurant|cafe|fast_food"](%s);
                relation["amenity"~"restaurant|cafe|fast_food"](%s);
                );
            out center 50;
            """, bbox, bbox, bbox, bbox, bbox, bbox, bbox, bbox, bbox);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // Overpass mirrors rate-limit requests without a meaningful User-Agent
        headers.set(HttpHeaders.USER_AGENT, "Wayfinder/1.0 (+https://github.com/mayskii/wayfinder-backend)");

        HttpEntity<String> entity = new HttpEntity<>("data=" + query, headers);

        // The public Overpass servers are often busy, so try each configured mirror in turn
        RestClientException lastError = null;
        for (String url : overpassUrl.split(",")) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> response = restTemplate.postForObject(url.trim(), entity, Map.class);

                if (response == null || !response.containsKey("elements")) {
                    return Collections.emptyList();
                }

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> elements = (List<Map<String, Object>>) response.get("elements");

                return elements;
            } catch (RestClientException e) {
                log.warn("Overpass request to {} failed: {}", url.trim(), e.getMessage());
                lastError = e;
            }
        }
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "The map data service is busy. Please try again in a minute.", lastError);
    }
}