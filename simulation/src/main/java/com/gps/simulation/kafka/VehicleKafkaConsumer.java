package com.gps.simulation.kafka;

import com.gps.simulation.model.Vehicle;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class VehicleKafkaConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public VehicleKafkaConsumer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = new ObjectMapper(); // JSON dönüşümü için ObjectMapper
    }

    @KafkaListener(topics = "vehicle_data", groupId = "vehicle_group")
    public void consumeVehicleData(String vehicleMessage) {
        try {
            // Kafka mesajını Vehicle nesnesine dönüştür
            Vehicle vehicle = objectMapper.readValue(vehicleMessage, Vehicle.class);

            // WebSocket üzerinden araca ait bilgiyi frontend'e ilet
            messagingTemplate.convertAndSend("/topic/vehicleLocation", vehicle);
        } catch (Exception e) {
            throw new RuntimeException("Araç verisi işlenirken hata oluştu", e);
        }
    }
}
