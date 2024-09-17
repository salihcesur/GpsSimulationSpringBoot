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

@Service
public class RouteSimulator {

    private final DistanceCalculatorService distanceCalculatorService;
    private final VehicleManager vehicleManager;
    private final VehicleProducerService vehicleProducerService;
    private final RandomRouteService randomRouteService;

    @Autowired
    @Lazy
    private RouteSimulator self;

    public RouteSimulator(DistanceCalculatorService distanceCalculatorService, VehicleManager vehicleManager,
                          VehicleProducerService vehicleProducerService, RandomRouteService randomRouteService) {
        this.distanceCalculatorService = distanceCalculatorService;
        this.vehicleManager = vehicleManager;
        this.vehicleProducerService = vehicleProducerService;
        this.randomRouteService = randomRouteService;
    }

    @Async("taskExecutor")
    public void simulateJourney(int vehicleCount, int distanceInterval) {
        List<Vehicle> vehicles = vehicleManager.createVehicles(vehicleCount);  // VehicleManager'dan çağırıyoruz

        for (Vehicle vehicle : vehicles) {
            String[] cities = randomRouteService.getRandomCities();
            List<double[]> routeSteps = distanceCalculatorService.getRouteSteps(cities[0], cities[1]);
            self.simulateVehicleJourney(vehicle, routeSteps, distanceInterval);
        }
    }

    @Async("taskExecutor")
    public void simulateVehicleJourney(Vehicle vehicle, List<double[]> routeSteps, int distanceInterval) {
        int stepIndex = 0;
        double totalDistance = 0;
        double remainingDistanceToNotify = distanceInterval;
        double speedKmPerHour = 120;
        double timeToTravelOneKm = 3600 / speedKmPerHour;  // 1 km gitmek için gerçek süre (saniye cinsinden)

        // Gerçek dünyadaki 1 saat = 5 saniye simülasyon olacak şekilde hızlandırma faktörü
        double simulationSpeedFactor = 3600.0 / 5.0; // 720 kat hızlandırma
        long sleepTime = (long) ((timeToTravelOneKm / simulationSpeedFactor) * 1000); // Sleep süresi milisaniye cinsinden

        while (stepIndex < routeSteps.size()) {
            double[] currentStep = routeSteps.get(stepIndex);
            double[] nextStep = stepIndex + 1 < routeSteps.size() ? routeSteps.get(stepIndex + 1) : null;

            vehicle.setCurrentLatitude(currentStep[0]);
            vehicle.setCurrentLongitude(currentStep[1]);

            if (nextStep != null) {
                double distance = distanceCalculatorService.calculateDistance(currentStep, nextStep);
                totalDistance += distance;
                remainingDistanceToNotify -= distance;

                if (remainingDistanceToNotify <= 0) {
                    vehicleProducerService.sendVehicleData(vehicle);
                    System.out.println(getCurrentTime() + " - Vehicle ID: " + vehicle.getVehicleId() + " " + distanceInterval + " km yol aldı.");
                    remainingDistanceToNotify += distanceInterval;
                }

                // Hızlandırılmış simülasyon için bekleme süresi
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            stepIndex++;
        }

        vehicle.setCompleted(true);
        vehicleProducerService.sendVehicleData(vehicle);
        System.out.println(getCurrentTime() + " - Vehicle ID: " + vehicle.getVehicleId() + " hedefe ulaştı.");
    }



    private String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(new Date());
    }
}

