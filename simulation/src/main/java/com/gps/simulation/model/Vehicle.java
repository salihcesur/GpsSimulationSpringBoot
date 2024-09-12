package com.gps.simulation.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String vehicleId;
    private int speed;  // Hız
    private double currentLatitude;
    private double currentLongitude;

    public Vehicle(String vehicleId, int speed) {
        this.vehicleId = vehicleId;
        this.speed = speed;
    }
}
