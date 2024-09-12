package com.gps.simulation.service;

import com.gps.simulation.kafka.VehicleProducerService;
import com.gps.simulation.model.Vehicle;
import com.gps.simulation.repositories.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
    private final VehicleRepository vehicleRepository;
    private final VehicleProducerService vehicleProducerService;  // Kafka producer servisi
    private final RandomRouteService randomRouteService;  // RandomRouteService entegrasyonu

    @Autowired
    @Lazy
    private RouteSimulator self;

    public RouteSimulator(DistanceCalculatorService distanceCalculatorService, VehicleRepository vehicleRepository,
                          VehicleProducerService vehicleProducerService, RandomRouteService randomRouteService) {
        this.distanceCalculatorService = distanceCalculatorService;
        this.vehicleRepository = vehicleRepository;
        this.vehicleProducerService = vehicleProducerService;  // Kafka producer servisi
        this.randomRouteService = randomRouteService;  // RandomRouteService kullanımı
    }

    @Async("taskExecutor")
    public void simulateJourney(int vehicleCount, int distanceInterval) {
        List<Vehicle> vehicles = createVehicles(vehicleCount);  // Araçları oluştur

        for (Vehicle vehicle : vehicles) {
            String[] cities = randomRouteService.getRandomCities(); // Random rotaları al
            List<double[]> routeSteps = distanceCalculatorService.getRouteSteps(cities[0], cities[1]);
            self.simulateVehicleJourney(vehicle, routeSteps, distanceInterval);
        }

    }

    @Async("taskExecutor")
    public void simulateVehicleJourney(Vehicle vehicle, List<double[]> routeSteps, int distanceInterval) {
        int stepIndex = 0;

        while (stepIndex < routeSteps.size()) {
            double[] currentStep = routeSteps.get(stepIndex);
            vehicle.setCurrentLatitude(currentStep[0]);
            vehicle.setCurrentLongitude(currentStep[1]);

            // Kafka'ya araç verilerini gönder
            vehicleProducerService.sendVehicleData(vehicle);

            stepIndex++;

            try {
                Thread.sleep(  distanceInterval + 1000L);  // Gecikmeyi mesafeye göre ayarla (dinamik gecikme)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println(getCurrentTime() + " - Vehicle ID: " + vehicle.getVehicleId() + " hedefe ulaştı.");
    }

    // Araçları oluştur
    private List<Vehicle> createVehicles(int vehicleCount) {
        List<Vehicle> vehicles = new ArrayList<>();
        Random random = new Random();

        for (int i = 1; i <= vehicleCount; i++) {
            int[] possibleSpeeds = {100, 110, 120};
            int speed = possibleSpeeds[random.nextInt(possibleSpeeds.length)];

            Vehicle vehicle = new Vehicle(speed);  // Araç nesnesi oluştur
            vehicleRepository.save(vehicle);  // Veritabanına kaydet

            vehicles.add(vehicle);
        }

        return vehicles;
    }

    private String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(new Date());
    }
}
