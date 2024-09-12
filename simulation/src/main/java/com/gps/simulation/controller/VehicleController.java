package com.gps.simulation.controller;

import com.gps.simulation.service.RouteSimulator;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    private final RouteSimulator routeSimulator;

    public VehicleController(RouteSimulator routeSimulator) {
        this.routeSimulator = routeSimulator;
    }

    @PostMapping("/start")
    public String startSimulation(@RequestParam int vehicleCount, @RequestParam int distanceInterval) {
        routeSimulator.simulateJourney(vehicleCount, distanceInterval); // Asenkron çalışır
        return "Simülasyon başlatıldı, araç sayısı: " + vehicleCount + " Mesaj gönderim aralığı: " + distanceInterval + " km";
    }
}
