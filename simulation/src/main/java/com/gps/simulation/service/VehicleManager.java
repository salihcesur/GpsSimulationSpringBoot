package com.gps.simulation.service;

import com.gps.simulation.model.Vehicle;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class VehicleManager {
    private final List<Vehicle> vehicles = new ArrayList<>();

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
    }

    public void updateVehiclePosition(Vehicle vehicle, double newLatitude, double newLongitude) {
        vehicle.setCurrentLatitude(newLatitude);
        vehicle.setCurrentLongitude(newLongitude);
    }
}
