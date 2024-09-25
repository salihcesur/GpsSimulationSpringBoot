package com.gps.simulation.service;

import com.gps.simulation.kafka.VehicleProducerService;
import com.gps.simulation.model.enums.Status;
import com.gps.simulation.model.Vehicle;
import com.gps.simulation.repositories.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class RouteSimulator {

    private final DistanceCalculatorService distanceCalculatorService;
    private final VehicleProducerService vehicleProducerService;
    private final RandomRouteService randomRouteService;
    private final VehicleManager vehicleManager;
    private final VehicleRepository vehicleRepository;

    @Autowired
    @Lazy
    private RouteSimulator self;

    @Autowired
    public RouteSimulator(DistanceCalculatorService distanceCalculatorService, VehicleManager vehicleManager,
                          VehicleProducerService vehicleProducerService, RandomRouteService randomRouteService, VehicleRepository vehicleRepository) {
        this.distanceCalculatorService = distanceCalculatorService;
        this.vehicleManager = vehicleManager;
        this.vehicleProducerService = vehicleProducerService;
        this.randomRouteService = randomRouteService;
        this.vehicleRepository = vehicleRepository;
    }

    @Async("taskExecutor")
    public void simulateVehicleJourney(Vehicle vehicle, List<double[]> routeSteps, int distanceInterval) {
        int stepIndex = 0;
        double totalDistance = 0;
        double remainingDistanceToNotify = distanceInterval;

        try {
            if (!routeSteps.isEmpty()) {
                double[] startPoint = routeSteps.get(0);
                vehicle.setCurrentLatitude(startPoint[0]);
                vehicle.setCurrentLongitude(startPoint[1]);
                vehicle.setStatus(Status.ON_ROAD);

                String lastCountry = distanceCalculatorService.getCountryFromCoordinates(startPoint[0], startPoint[1]);
                vehicle.setCurrentCountry(lastCountry);
            }

            while (stepIndex < routeSteps.size() - 1) {
                double[] currentStep = routeSteps.get(stepIndex);
                double[] nextStep = routeSteps.get(stepIndex + 1);

                double distance = distanceCalculatorService.calculateDistance(currentStep, nextStep);
                totalDistance += distance;
                remainingDistanceToNotify -= distance;

                vehicle.setCurrentLatitude(nextStep[0]);
                vehicle.setCurrentLongitude(nextStep[1]);

                String currentCountry = distanceCalculatorService.getCountryFromCoordinates(nextStep[0], nextStep[1]);
                if (!currentCountry.equals(vehicle.getCurrentCountry())) {
                    vehicle.setCurrentCountry(currentCountry);
                    vehicleProducerService.sendCountryChangeNotification(vehicle.getVehicleId(), currentCountry);
                }

                if (remainingDistanceToNotify <= 0) {
                    vehicleProducerService.sendVehicleData(vehicle);
                    remainingDistanceToNotify = distanceInterval;
                }

                //vehicleRepository.save(vehicle);

                Thread.sleep(500);

                stepIndex++;
            }

            vehicle.setStatus(Status.COMPLETED);
            vehicleRepository.save(vehicle);
            vehicleProducerService.sendVehicleData(vehicle);
            vehicleProducerService.sendEndLocation(vehicle.getVehicleId(), vehicle.getCurrentLatitude(), vehicle.getCurrentLongitude());

        } catch (Exception e) {
            // Simülasyon sırasında hata oluşursa
            vehicle.setStatus(Status.FAILED);
            vehicleRepository.save(vehicle);
            vehicleProducerService.sendVehicleData(vehicle);
            System.err.println("Simülasyon sırasında bir hata oluştu: " + e.getMessage());
        }
    }

    @Async("taskExecutor")
    public void simulateJourney(int vehicleCount, int distanceInterval) {
        List<Vehicle> vehicles = vehicleManager.createVehicles(vehicleCount);

        for (Vehicle vehicle : vehicles) {
            try {
                String[] cities = randomRouteService.getRandomCities();
                List<double[]> routeSteps = distanceCalculatorService.getRouteSteps(cities[0], cities[1]);

                double[] startPoint = routeSteps.get(0);
                double[] endPoint = routeSteps.get(routeSteps.size() - 1);

                vehicle.setStartLatitude(startPoint[0]);
                vehicle.setStartLongitude(startPoint[1]);
                vehicle.setDestinationLatitude(endPoint[0]);
                vehicle.setDestinationLongitude(endPoint[1]);

                vehicle.setStatus(Status.READY);

                vehicle.setCurrentLatitude(startPoint[0]);
                vehicle.setCurrentLongitude(startPoint[1]);

                vehicleProducerService.sendVehicleData(vehicle);
                vehicleProducerService.sendStartEndCityNotification(vehicle.getVehicleId(), cities[0], cities[1]);

                Thread.sleep(500);

                self.simulateVehicleJourney(vehicle, routeSteps, distanceInterval);

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
