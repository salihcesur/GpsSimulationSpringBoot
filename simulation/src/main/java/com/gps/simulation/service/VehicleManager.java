package com.gps.simulation.service;

import com.gps.simulation.model.Vehicle;
import com.gps.simulation.repositories.VehicleRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class VehicleManager {

    @Autowired
    private VehicleRepository vehicleRepository;  // vehicleRepository burada enjekte ediliyor

    @Getter
    private final List<Vehicle> vehicles = new ArrayList<>();


    public List<Vehicle> createVehicles(int vehicleCount) {
        List<Vehicle> newVehicles = new ArrayList<>();

        for (int i = 1; i <= vehicleCount; i++) {
            int speed = 120;
            Vehicle vehicle = new Vehicle(speed);
            vehicleRepository.save(vehicle);  // Veritabanına kaydediyoruz
            newVehicles.add(vehicle);
        }

        return newVehicles;
    }
}
