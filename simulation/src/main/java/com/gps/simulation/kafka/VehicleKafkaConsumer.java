package com.gps.simulation.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gps.simulation.model.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class VehicleKafkaConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public VehicleKafkaConsumer(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "vehicle_data", groupId = "vehicle_group")
    public void consumeVehicleData(String vehicleMessage) {
        try {
            Vehicle vehicle = objectMapper.readValue(vehicleMessage, Vehicle.class);
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
