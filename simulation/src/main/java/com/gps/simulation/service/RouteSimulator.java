package com.gps.simulation.service;

import com.gps.simulation.model.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@EnableAsync
public class RouteSimulator {

    private final DistanceCalculatorService distanceCalculatorService;

    @Autowired
    @Lazy
    private RouteSimulator self;

    public RouteSimulator(DistanceCalculatorService distanceCalculatorService) {
        this.distanceCalculatorService = distanceCalculatorService;
    }

    @Async("taskExecutor")
    public void simulateJourney(int vehicleCount, int distanceInterval) {
        double totalDistance = distanceCalculatorService.getRoadDistance("Istanbul", "Erzincan");
        List<Vehicle> vehicles = createVehicles(vehicleCount, totalDistance);

        for (Vehicle vehicle : vehicles) {
            self.simulateVehicleJourney(vehicle, totalDistance, distanceInterval); // self ile asenkron simülasyon
        }
    }

    @Async("taskExecutor")
    public void simulateVehicleJourney(Vehicle vehicle, double totalDistance, int distanceInterval) {
        double remainingDistance = totalDistance;
        double distanceSinceLastMessage = 0;

        System.out.println(getCurrentTime() + " - Vehicle ID: " + vehicle.getVehicleId() +
                " hız: " + vehicle.getSpeed() + " km/h ile yolculuğa başladı. Toplam mesafe: " + totalDistance + " km.");

        while (remainingDistance > 0) {
            double distanceCovered = (vehicle.getSpeed() / 60.0) ;
            remainingDistance -= distanceCovered;
            distanceSinceLastMessage += distanceCovered;

            if (remainingDistance < 0) {
                remainingDistance = 0;
            }

            if (Math.abs(distanceSinceLastMessage - distanceInterval) < 0.001) {
                sendLocationMessage(vehicle, remainingDistance);
                distanceSinceLastMessage = 0;
            }


            if (remainingDistance > 0) {
                try {
                    Thread.sleep(500); // 1 saati 1 dakikaya simüle ediyoruz
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println(getCurrentTime() + " - Vehicle ID: " + vehicle.getVehicleId() + " hedefe ulaştı.");
    }

    private void sendLocationMessage(Vehicle vehicle, double remainingDistance) {
        System.out.println(getCurrentTime() + " - Vehicle ID: " + vehicle.getVehicleId() +
                " şu an Hız: " + vehicle.getSpeed() + " km/h, Kalan km: " + remainingDistance);
    }

    private List<Vehicle> createVehicles(int vehicleCount, double totalDistance) {
        List<Vehicle> vehicles = new ArrayList<>();
        for (int i = 1; i <= vehicleCount; i++) {
            vehicles.add(new Vehicle("Vehicle-" + i, totalDistance));
        }
        return vehicles;
    }

    private String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(new Date());
    }
}
