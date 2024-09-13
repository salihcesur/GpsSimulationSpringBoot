package com.gps.simulation.kafka;

import com.gps.simulation.model.Vehicle;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class VehicleProducerService {

    private static final String TOPIC = "vehicle_data";  // Kafka'da veri gönderilecek topic

    @Autowired
    private KafkaTemplate<String, Vehicle> kafkaTemplate;

    public void sendVehicleData(Vehicle vehicle) {
        kafkaTemplate.send(TOPIC, vehicle);  // Aracı Kafka'ya gönder
        System.out.println("Kafka'ya araç verisi gönderildi: " + vehicle);
    }
}