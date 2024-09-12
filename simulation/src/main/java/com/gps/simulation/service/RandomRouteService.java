package com.gps.simulation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Random;

@Service
public class RandomRouteService {

    private final DistanceCalculatorService distanceCalculatorService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public RandomRouteService(DistanceCalculatorService distanceCalculatorService, SimpMessagingTemplate messagingTemplate) {
        this.distanceCalculatorService = distanceCalculatorService;
        this.messagingTemplate = messagingTemplate;
    }

    // Rastgele şehir seçimi
    public String[] getRandomCities() {
        String[] cities = {"Istanbul", "Ankara", "Izmir", "Erzurum", "Antalya", "Trabzon", "Gaziantep", "Diyarbakir"};

        Random random = new Random();
        String origin = cities[random.nextInt(cities.length)];
        String destination = cities[random.nextInt(cities.length)];

        // Eğer aynı şehir seçildiyse farklı bir destinasyon al
        while (origin.equals(destination)) {
            destination = cities[random.nextInt(cities.length)];
        }

        return new String[]{origin, destination};
    }

    // Rastgele rotayı Google Directions API ile al ve frontend'e gönder
    @Async("taskExecutor")
    public void sendRandomRouteToFrontend() {
        String[] cities = getRandomCities();
        List<double[]> randomRoute = distanceCalculatorService.getRouteSteps(cities[0], cities[1]);  // Rastgele şehirler arası rota
        messagingTemplate.convertAndSend("/topic/randomRoute", randomRoute);  // Frontend'e WebSocket ile gönder
    }
}
