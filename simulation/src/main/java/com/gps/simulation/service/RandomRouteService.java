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

    public String[] getRandomCities() {
        String[] cities = {"Istanbul", "Ankara", "Izmir", "Erzurum", "Antalya", "Trabzon", "Gaziantep", "Diyarbakir", "Kayseri", "Sivas", "Erzincan", "Balıkesir"};

        Random random = new Random();
        String origin = cities[random.nextInt(cities.length)];
        String destination;

        do {
            destination = cities[random.nextInt(cities.length)];
        } while (origin.equals(destination));

        return new String[]{origin, destination};
    }
}
