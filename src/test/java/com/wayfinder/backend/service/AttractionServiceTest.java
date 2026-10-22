package com.wayfinder.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AttractionServiceTest {

    private RestTemplate restTemplate;
    private AttractionService attractionService;

    @BeforeEach
    void setup() throws Exception {
        restTemplate = mock(RestTemplate.class);
        attractionService = new AttractionService(restTemplate);

        Field urlField = AttractionService.class.getDeclaredField("overpassUrl");
        urlField.setAccessible(true);
        urlField.set(attractionService, "http://test.overpass.api");
    }

    @Test
    void testGetAttractions_simple() {
        Map<String, Object> fakeResponse = Map.of(
                "elements", List.of(
                        Map.of("id", 1),
                        Map.of("id", 2)
                )
        );

        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(fakeResponse);

        var result = attractionService.getAttractions("1,2,3,4");

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).get("id"));
        assertEquals(2, result.get(1).get("id"));
    }

    @Test
    void testGetAttractions_triesNextMirrorWhenBusy() throws Exception {
        Field urlField = AttractionService.class.getDeclaredField("overpassUrl");
        urlField.setAccessible(true);
        urlField.set(attractionService, "http://busy.test, http://mirror.test");

        when(restTemplate.postForObject(eq("http://busy.test"), any(), eq(Map.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));
        when(restTemplate.postForObject(eq("http://mirror.test"), any(), eq(Map.class)))
                .thenReturn(Map.of("elements", List.of(Map.of("id", 7))));

        var result = attractionService.getAttractions("1,2,3,4");

        assertEquals(1, result.size());
        assertEquals(7, result.get(0).get("id"));
    }

    @Test
    void testGetAttractions_allMirrorsBusy() {
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));

        var e = assertThrows(ResponseStatusException.class, () -> attractionService.getAttractions("1,2,3,4"));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, e.getStatusCode());
    }
}
