package com.gps.simulation.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class DistanceCalculatorService {

    @Value("${google.api.key}")
    private String apiKey;

    public double calculateDistance(double[] start, double[] end) {
        String origin = start[0] + "," + start[1];
        String destination = end[0] + "," + end[1];
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + origin + "&destination=" + destination + "&key=" + apiKey;

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);

        JSONObject jsonResponse = new JSONObject(response);
        JSONArray routes = jsonResponse.getJSONArray("routes");

        // En kısa rotanın toplam mesafesini alıyoruz
        if (routes.length() > 0) {
            JSONObject route = routes.getJSONObject(0);
            JSONArray legs = route.getJSONArray("legs");

            if (legs.length() > 0) {
                JSONObject leg = legs.getJSONObject(0);
                JSONObject distanceObject = leg.getJSONObject("distance");
                double distanceInMeters = distanceObject.getDouble("value");

                // Mesafeyi kilometreye çeviriyoruz
                return distanceInMeters / 1000;
            }
        }

        return 0;  // Eğer mesafe hesaplanamazsa 0 döndürüyoruz
    }


    public List<double[]> getRouteSteps(String origin, String destination) {
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

        for (int i = 0; i < steps.length(); i++) {
            JSONObject step = steps.getJSONObject(i);
            double startLat = step.getJSONObject("start_location").getDouble("lat");
            double startLng = step.getJSONObject("start_location").getDouble("lng");
            routeSteps.add(new double[]{startLat, startLng});
        }

        return routeSteps;
    }

    public String getCountryFromCoordinates(double latitude, double longitude) {
        String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + latitude + "," + longitude + "&key=" + apiKey;
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);

        JSONObject jsonResponse = new JSONObject(response);
        JSONArray results = jsonResponse.getJSONArray("results");

        for (int i = 0; i < results.length(); i++) {
            JSONObject result = results.getJSONObject(i);
            JSONArray addressComponents = result.getJSONArray("address_components");

            for (int j = 0; j < addressComponents.length(); j++) {
                JSONObject component = addressComponents.getJSONObject(j);
                JSONArray types = component.getJSONArray("types");

                for (int k = 0; k < types.length(); k++) {
                    if (types.getString(k).equals("country")) {
                        return component.getString("long_name");
                    }
                }
            }
        }
        return "Unknown";
    }
}
