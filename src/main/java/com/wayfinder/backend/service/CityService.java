package com.wayfinder.backend.service;

import com.wayfinder.backend.model.City;
import com.wayfinder.backend.repository.CityRepository;
import com.wayfinder.backend.exception.CityNotFoundException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CityService {

    @Value("${locationiq.api.key}")
    private String apiKey;

    @Value("${locationiq.api.url}")
    private String apiUrl;

    private final CityRepository cityRepository;
    private final RestTemplate restTemplate;

    public CityService(CityRepository cityRepository, RestTemplate restTemplate) {
        this.cityRepository = cityRepository;
        this.restTemplate = restTemplate;
    }

    // 🔍 GET city coordinates (BD -> API)
    public City getCityCoordinates(String cityName) {
        var cityOptional = cityRepository.findByName(cityName);
        if (cityOptional.isPresent()) {
            return cityOptional.get();
        }

        String url = apiUrl + "?key=" + apiKey + "&q=" + cityName + "&format=json";
        Object[] responseArray;

        try {
            responseArray = restTemplate.getForObject(url, Object[].class);
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {

            throw new CityNotFoundException("City not found via API: " + cityName);
        } catch (Exception e) {

            throw new RuntimeException("API error while searching city: " + cityName, e);
        }

        if (responseArray == null || responseArray.length == 0) {
            throw new CityNotFoundException("City not found: " + cityName);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) responseArray[0];

        if (data.get("lat") == null || data.get("lon") == null) {
            throw new CityNotFoundException("Coordinates not found for city: " + cityName);
        }

        City city = new City();
        city.setName(cityName);
        city.setLat(Double.parseDouble(data.get("lat").toString()));
        city.setLng(Double.parseDouble(data.get("lon").toString()));

        String country = "Unknown";
        if (data.get("display_name") != null) {
            String displayName = data.get("display_name").toString();
            String[] parts = displayName.split(",");
            if (parts.length > 0 && parts[parts.length - 1] != null && !parts[parts.length - 1].isBlank()) {
                country = parts[parts.length - 1].trim();
            }
        }
        city.setCountry(country);

        if (data.get("osm_id") != null) {
            city.setOsmId(Long.parseLong(data.get("osm_id").toString()));
            cityRepository.findByOsmId(city.getOsmId()).ifPresent(existing -> city.setId(existing.getId()));
        }

        if (data.get("boundingbox") != null) {
            @SuppressWarnings("unchecked")
            List<String> bboxList = (List<String>) data.get("boundingbox");
            city.setBbox(String.join(",", bboxList));
        }

        city.setLastUpdated(LocalDateTime.now());
        return cityRepository.save(city);
    }

    // READ ALL
    public List<City> getAllCities() {
        return cityRepository.findAll();
    }

    // READ ONE
    public Optional<City> getCityById(Long id) {
        return cityRepository.findById(id);
    }

    // CREATE
    public City save(City city) {
        city.setLastUpdated(java.time.LocalDateTime.now());
        return cityRepository.save(city);
    }

    // UPDATE
    public Optional<City> updateCity(Long id, City updatedCity) {
        return cityRepository.findById(id).map(city -> {
            city.setName(updatedCity.getName());
            city.setCountry(updatedCity.getCountry());
            city.setLat(updatedCity.getLat());
            city.setLng(updatedCity.getLng());
            city.setBbox(updatedCity.getBbox());

            // Добавляем: обновляем OSM ID
            city.setOsmId(updatedCity.getOsmId());

            // Добавляем: ставим текущее время для lastUpdated
            city.setLastUpdated(java.time.LocalDateTime.now());

            return cityRepository.save(city);
        });
    }


    // DELETE
    public void deleteCity(Long id) {
        cityRepository.deleteById(id);
    }

}

