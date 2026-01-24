package com.wayfinder.backend.service;

import com.wayfinder.backend.model.City;
import com.wayfinder.backend.repository.CityRepository;

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
    private final RestTemplate restTemplate = new RestTemplate();

    public CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    // 🔍 GET city coordinates (BD -> API)
    public City getCityCoordinates(String cityName) {
        var cityOptional = cityRepository.findByName(cityName);
        if (cityOptional.isPresent()) {
            return cityOptional.get();
        }

        String url = apiUrl + "?key=" + apiKey + "&q=" + cityName + "&format=json";
        Object[] responseArray = restTemplate.getForObject(url, Object[].class);

        if (responseArray == null || responseArray.length == 0) {
            throw new RuntimeException("City not found via API: " + cityName);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) responseArray[0];

        City city = new City();
        city.setName(cityName);
        city.setLat(Double.parseDouble(data.get("lat").toString()));
        city.setLng(Double.parseDouble(data.get("lon").toString()));

        if (data.get("osm_id") != null) {
            city.setOsmId(Long.parseLong(data.get("osm_id").toString()));
            cityRepository.findByOsmId(city.getOsmId()).ifPresent(existing -> city.setId(existing.getId()));
        }

        if (data.get("boundingbox") != null) {
            @SuppressWarnings("unchecked")
            List<String> bboxList = (List<String>) data.get("boundingbox");
            city.setBbox(String.join(",", bboxList));
        }

        if (data.get("display_name") != null) {
            String displayName = data.get("display_name").toString();
            String[] parts = displayName.split(",");
            city.setCountry(parts[parts.length - 1].trim());
        } else {
            city.setCountry("Unknown");
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

