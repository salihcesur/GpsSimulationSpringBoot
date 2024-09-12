package com.gps.simulation.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class DistanceCalculatorService {

    @Value("${google.api.key}")
    private String apiKey;

    // İki şehir arasındaki rotayı almak için kullanılacak metod
    public List<double[]> getRouteSteps(String origin, String destination) {
        // Google Directions API'den rota adımlarını al
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + origin + "&destination=" + destination + "&key=" + apiKey;
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);

        JSONObject jsonResponse = new JSONObject(response);
        JSONArray steps = jsonResponse
                .getJSONArray("routes")
                .getJSONObject(0)
                .getJSONArray("legs")
                .getJSONObject(0)
                .getJSONArray("steps");

        List<double[]> routeSteps = new ArrayList<>();

        // Her bir adımda enlem ve boylam bilgilerini al
        for (int i = 0; i < steps.length(); i++) {
            JSONObject step = steps.getJSONObject(i);
            double startLat = step.getJSONObject("start_location").getDouble("lat");
            double startLng = step.getJSONObject("start_location").getDouble("lng");
            routeSteps.add(new double[]{startLat, startLng});
        }

        return routeSteps;  // Enlem ve boylam çiftlerinden oluşan rota adımları
    }
}
