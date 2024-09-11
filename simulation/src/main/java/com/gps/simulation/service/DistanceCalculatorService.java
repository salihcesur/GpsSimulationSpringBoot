package com.gps.simulation.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.json.JSONObject;

@Service
public class DistanceCalculatorService {

    private static final String API_KEY = "****";

    public double getRoadDistance(String origin, String destination) {
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + origin + "&destination=" + destination + "&key=" + API_KEY;

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        JSONObject jsonResponse = new JSONObject(response.getBody());
        double distanceInMeters = jsonResponse.getJSONArray("routes")
                .getJSONObject(0)
                .getJSONArray("legs")
                .getJSONObject(0)
                .getJSONObject("distance")
                .getDouble("value");

        // Mesafeyi kilometreye çeviriyoruz
        return distanceInMeters / 1000;
    }
}
