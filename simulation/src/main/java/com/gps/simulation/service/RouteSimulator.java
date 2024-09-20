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
        double speedKmPerHour = 120;
        double timeToTravelOneKm = 3600 / speedKmPerHour;

        String lastCountry = distanceCalculatorService.getCountryFromCoordinates(vehicle.getCurrentLatitude(), vehicle.getCurrentLongitude());
        vehicle.setCurrentCountry(lastCountry);

        double simulationSpeedFactor = 3600.0 / 30.0;
        long sleepTime = (long) (100);

        while (stepIndex < routeSteps.size()) while (stepIndex < routeSteps.size()) {
            vehicle.setStatus(Status.ON_ROAD);
            double[] currentStep = routeSteps.get(stepIndex);
            double[] nextStep = stepIndex + 1 < routeSteps.size() ? routeSteps.get(stepIndex + 1) : null;

            vehicle.setCurrentLatitude(currentStep[0]);
            vehicle.setCurrentLongitude(currentStep[1]);

            if (nextStep != null) {
                double distance = distanceCalculatorService.calculateDistance(currentStep, nextStep);
                totalDistance += distance;
                remainingDistanceToNotify -= distance;

                String currentCountry = distanceCalculatorService.getCountryFromCoordinates(vehicle.getCurrentLatitude(), vehicle.getCurrentLongitude());

                if (!currentCountry.equals(lastCountry)) {
                    String message = vehicle.getVehicleId() + " ID'li araç " + currentCountry + " ülkesine giriş yaptı.";
                    vehicle.setCurrentCountry(currentCountry);
                    vehicleProducerService.sendCountryChangeNotification(vehicle.getVehicleId(), currentCountry);
                    lastCountry = currentCountry;
                }

                if (remainingDistanceToNotify <= 0) {
                    vehicleProducerService.sendVehicleData(vehicle);
                    remainingDistanceToNotify += distanceInterval;
                }

                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            vehicleRepository.save(vehicle);
            stepIndex++;
        }

        vehicle.setStatus(Status.COMPLETED);
        vehicleRepository.save(vehicle);
        vehicleProducerService.sendVehicleData(vehicle);
        System.out.println(getCurrentTime() + " - Vehicle ID: " + vehicle.getVehicleId() + " hedefe ulaştı.");
    }

    @Async("taskExecutor")
    public void simulateJourney(int vehicleCount, int distanceInterval) {
        List<Vehicle> vehicles = vehicleManager.createVehicles(vehicleCount);

        for (Vehicle vehicle : vehicles) {
            String[] cities = randomRouteService.getRandomCities();
            List<double[]> routeSteps = distanceCalculatorService.getRouteSteps(cities[0], cities[1]);

            double[] startPoint = routeSteps.get(0);
            double[] endPoint = routeSteps.get(routeSteps.size() - 1);

            vehicle.setStartLatitude(startPoint[0]);
            vehicle.setStartLongitude(startPoint[1]);
            vehicle.setDestinationLatitude(endPoint[0]);
            vehicle.setDestinationLongitude(endPoint[1]);

            try {
                vehicle.setStatus(Status.READY);
                vehicleProducerService.sendVehicleData(vehicle);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            self.simulateVehicleJourney(vehicle, routeSteps, distanceInterval);
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(new Date());
    }
}
