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

    public void sendVehicleLocation() {
        for (Vehicle vehicle : vehicleManager.getVehicles()) {
            messagingTemplate.convertAndSend("/topic/vehicleLocation", vehicle);
        }
    }
}
