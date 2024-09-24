package com.gps.simulation.controller;

import com.gps.simulation.model.Vehicle;
import com.gps.simulation.repositories.VehicleRepository;
import com.gps.simulation.service.VehicleManager;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

@Controller
public class VehicleWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final VehicleManager vehicleManager;
    private final VehicleRepository vehicleRepository;

    public VehicleWebSocketController(SimpMessagingTemplate messagingTemplate, VehicleManager vehicleManager, VehicleRepository vehicleRepository) {
        this.messagingTemplate = messagingTemplate;
        this.vehicleManager = vehicleManager;
        this.vehicleRepository = vehicleRepository;
    }

    public void sendVehicleLocation() {
        for (Vehicle vehicle : vehicleManager.getVehicles()) {
            messagingTemplate.convertAndSend("/topic/vehicleLocation", vehicle);
            vehicleRepository.save(vehicle);
        }
    }

    public void sendCountryChangeNotification(Long vehicleId, String currentCountry) {
        String notificationMessage = "Araç " + vehicleId + " " + currentCountry + " ülkesine giriş yaptı.";
        messagingTemplate.convertAndSend("/topic/vehicleNotification", notificationMessage);
    }

    public void sendStartEndCityNotification(Long vehicleId, String startCity, String endCity) {
        String startEndMessage = "Araç " + vehicleId + " yola çıktı! Başlangıç: " + startCity + ", Bitiş: " + endCity;
        messagingTemplate.convertAndSend("/topic/vehicleStartEnd", startEndMessage);
    }
}
