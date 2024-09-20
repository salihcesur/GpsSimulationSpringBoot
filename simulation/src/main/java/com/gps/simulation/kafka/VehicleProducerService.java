package com.gps.simulation.kafka;

import com.gps.simulation.model.Vehicle;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class VehicleProducerService {

    private static final String VEHICLE_TOPIC = "vehicle_data";
    private static final String NOTIFICATION_TOPIC = "vehicle_notification";

    @Autowired
    private KafkaTemplate<String, Vehicle> kafkaTemplate;

    @Autowired
    private KafkaTemplate<String, String> stringKafkaTemplate;

    public void sendVehicleData(Vehicle vehicle) {
        kafkaTemplate.send(VEHICLE_TOPIC, vehicle.getVehicleId().toString(), vehicle);
        System.out.println("Kafka'ya araç verisi gönderildi: " + vehicle);
    }

    public void sendCountryChangeNotification(Long vehicleId, String currentCountry) {
        String message = vehicleId + " ID'li araç " + currentCountry + " ülkesine giriş yaptı.";
        stringKafkaTemplate.send(NOTIFICATION_TOPIC, vehicleId.toString(), message);
        System.out.println("Kafka'ya ülke değişim bildirimi gönderildi: " + message);
    }
}