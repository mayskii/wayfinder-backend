package com.wayfinder.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class AttractionService {

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
                );
            out center 50;
            """, bbox, bbox, bbox);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> entity = new HttpEntity<>("data=" + query, headers);


        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(overpassUrl, entity, Map.class);

        if (response == null || !response.containsKey("elements")) {
            return Collections.emptyList();
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> elements = (List<Map<String, Object>>) response.get("elements");

        return elements;
    }
}