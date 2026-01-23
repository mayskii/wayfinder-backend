package com.wayfinder.backend.service;

import com.wayfinder.backend.model.City;
import com.wayfinder.backend.repository.CityRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

        return cityRepository.findByName(cityName)
                .orElseGet(() -> {
                    String url = apiUrl + "?key=" + apiKey + "&q=" + cityName + "&format=json";

                    Map[] response = restTemplate.getForObject(url, Map[].class);

                    if (response == null || response.length == 0) {
                        throw new RuntimeException("City not found: " + cityName);
                    }

                    City city = new City();
                    city.setName(cityName);
                    city.setLat(Double.parseDouble(response[0].get("lat").toString()));
                    city.setLng(Double.parseDouble(response[0].get("lon").toString()));

                    return cityRepository.save(city);
                });
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
            city.setLastUpdated(updatedCity.getLastUpdated());
            return cityRepository.save(city);
        });
    }

    // DELETE
    public void deleteCity(Long id) {
        cityRepository.deleteById(id);
    }

}

