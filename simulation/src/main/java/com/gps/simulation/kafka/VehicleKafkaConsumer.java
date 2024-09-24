package com.gps.simulation.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gps.simulation.model.Vehicle;
import com.gps.simulation.repositories.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class VehicleKafkaConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final VehicleRepository vehicleRepository;

    @Autowired
    public VehicleKafkaConsumer(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper, VehicleRepository vehicleRepository) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
        this.vehicleRepository = vehicleRepository;
    }

    @KafkaListener(topics = "vehicle_data", groupId = "vehicle_group")
    public void consumeVehicleData(String vehicleMessage) {
        try {
            Vehicle vehicle = objectMapper.readValue(vehicleMessage, Vehicle.class);

            vehicleRepository.save(vehicle);

            messagingTemplate.convertAndSend("/topic/vehicleLocation", vehicle);
        } catch (Exception e) {
            throw new RuntimeException("Araç verisi işlenirken hata oluştu", e);
        }
    }

    @KafkaListener(topics = "vehicle_notification", groupId = "vehicle_group")
    public void consumeCountryChangeNotification(String notificationMessage) {
        try {
            messagingTemplate.convertAndSend("/topic/vehicleNotification", notificationMessage);
        } catch (Exception e) {
            throw new RuntimeException("Ülke değişim bildirimi işlenirken hata oluştu", e);
        }
    }

    @KafkaListener(topics = "vehicle_start_end", groupId = "vehicle_group")
    public void consumeStartEndCityNotification(String notificationMessage) {
        try {
            messagingTemplate.convertAndSend("/topic/vehicleStartEnd", notificationMessage);
        } catch (Exception e) {
            throw new RuntimeException("Başlangıç/Bitiş şehir bildirimi işlenirken hata oluştu", e);
        }
    }
}
