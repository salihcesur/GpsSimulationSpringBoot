package com.gps.simulation.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    private Long vehicleId;
    private int speed;
    private double currentLatitude;
    private double currentLongitude;

    // Yeni enum alanı
    @Enumerated(EnumType.STRING)
    private Status status;

    private double startLatitude;
    private double startLongitude;
    private double destinationLatitude;
    private double destinationLongitude;

    public Vehicle(int speed) {
        this.speed = speed;
        this.status = Status.READY;
    }
}
