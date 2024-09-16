package com.gps.simulation.controller;

import com.gps.simulation.model.Vehicle;
import com.gps.simulation.service.VehicleManager;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

@Controller
public class VehicleWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final VehicleManager vehicleManager;

    public VehicleWebSocketController(SimpMessagingTemplate messagingTemplate, VehicleManager vehicleManager) {
        this.messagingTemplate = messagingTemplate;
        this.vehicleManager = vehicleManager;
    }

    @Scheduled(fixedRate = 100)
    public void sendVehicleLocation() {
        for (Vehicle vehicle : vehicleManager.getVehicles()) { // Tüm araçlar için konum bilgilerini gönder
            messagingTemplate.convertAndSend("/topic/vehicleLocation", vehicle);
        }
    }
}
