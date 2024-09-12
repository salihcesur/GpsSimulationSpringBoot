package com.gps.simulation.service;

import com.gps.simulation.model.Vehicle;
import com.gps.simulation.repositories.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
public class RouteSimulator {

    private final DistanceCalculatorService distanceCalculatorService;
    private final SimpMessagingTemplate messagingTemplate;
    private final RandomRouteService randomRouteService;
    private final VehicleRepository vehicleRepository;  // Veritabanına kaydetmek için

    @Autowired
    @Lazy
    private RouteSimulator self;

    public RouteSimulator(DistanceCalculatorService distanceCalculatorService, SimpMessagingTemplate messagingTemplate,
                          RandomRouteService randomRouteService, VehicleRepository vehicleRepository) {
        this.distanceCalculatorService = distanceCalculatorService;
        this.messagingTemplate = messagingTemplate;
        this.randomRouteService = randomRouteService;
        this.vehicleRepository = vehicleRepository;
    }

    @Async("taskExecutor")
    public void simulateJourney(int vehicleCount, int distanceInterval) {
        List<Vehicle> vehicles = createVehicles(vehicleCount);  // Araçları oluştur

        for (Vehicle vehicle : vehicles) {
            String[] cities = randomRouteService.getRandomCities();
            List<double[]> routeSteps = distanceCalculatorService.getRouteSteps(cities[0], cities[1]);
            self.simulateVehicleJourney(vehicle, routeSteps, distanceInterval);
        }
    }

    @Async("taskExecutor")
    public void simulateVehicleJourney(Vehicle vehicle, List<double[]> routeSteps, int distanceInterval) {
        int stepIndex = 0;  // Rota adımları boyunca ilerleyecek

        System.out.println(getCurrentTime() + " - Vehicle ID: " + vehicle.getVehicleId() +
                " hız: " + vehicle.getSpeed() + " km/h ile yolculuğa başladı.");

        sendRouteToFrontend(vehicle, routeSteps);

        while (stepIndex < routeSteps.size()) {
            double[] currentStep = routeSteps.get(stepIndex);
            vehicle.setCurrentLatitude(currentStep[0]);
            vehicle.setCurrentLongitude(currentStep[1]);

            sendLocationMessage(vehicle);

            stepIndex++;

            try {
                Thread.sleep(1000);  // Her adımda 1 saniye gecikme (hızı yavaşlatmak için)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println(getCurrentTime() + " - Vehicle ID: " + vehicle.getVehicleId() + " hedefe ulaştı.");
    }

    // WebSocket ile araç konumunu frontend'e gönderme
    private void sendLocationMessage(Vehicle vehicle) {
        messagingTemplate.convertAndSend("/topic/vehicleLocation", vehicle);  // Frontend'deki WebSocket adresiyle eşleşmeli
    }

    // WebSocket ile araç rotasını frontend'e gönderme
    private void sendRouteToFrontend(Vehicle vehicle, List<double[]> routeSteps) {
        messagingTemplate.convertAndSend("/topic/vehicleRoute/" + vehicle.getVehicleId(), routeSteps);
    }

    // Güncellenmiş createVehicles fonksiyonu
    private List<Vehicle> createVehicles(int vehicleCount) {
        List<Vehicle> vehicles = new ArrayList<>();
        Random random = new Random();

        for (int i = 1; i <= vehicleCount; i++) {
            // Rastgele bir hız belirle (örneğin 100, 110, 120 km/h)
            int[] possibleSpeeds = {100, 110, 120};
            int speed = possibleSpeeds[random.nextInt(possibleSpeeds.length)];

            Vehicle vehicle = new Vehicle(speed);  // Vehicle nesnesi oluştur
            vehicleRepository.save(vehicle);  // Veritabanına kaydet

            vehicles.add(vehicle);  // Listeye ekle
        }

        return vehicles;
    }


    private String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(new Date());
    }
}
