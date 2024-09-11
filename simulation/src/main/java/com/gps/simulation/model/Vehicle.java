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
    private Long id;

    private String vehicleId;

    private double totalDistance;

    private double currentLatitude;

    private double currentLongitude;

    private double remainingKm;

    private int speed;

    public Vehicle(String vehicleId, double totalDistance) {
        this.vehicleId = vehicleId;
        this.totalDistance = totalDistance;
        this.speed = new int[]{100,300,200}[new java.util.Random().nextInt(3)];
        this.remainingKm = totalDistance;
    }
}
