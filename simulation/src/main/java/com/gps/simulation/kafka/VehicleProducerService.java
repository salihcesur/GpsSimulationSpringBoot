package com.gps.simulation.kafka;

import com.gps.simulation.model.Vehicle;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class VehicleProducerService {

    private static final String VEHICLE_TOPIC = "vehicle_data";
    private static final String NOTIFICATION_TOPIC = "vehicle_notification";
    private static final String START_END_TOPIC = "vehicle_start_end";
    private static final String END_LOCATION_TOPIC = "vehicle_last_location";

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

    public void sendStartEndCityNotification(Long vehicleId, String startCity, String endCity) {
        String message = "Araç " + vehicleId + " yola çıktı! Başlangıç: " + startCity + ", Bitiş: " + endCity;
        stringKafkaTemplate.send(START_END_TOPIC, vehicleId.toString(), message);
        System.out.println("Kafka'ya başlangıç ve bitiş şehir bilgisi gönderildi: " + message);
    }

    public void sendEndLocation(Long vehicleId, double latitude, double longitude) {
        String message = "Araç " + vehicleId + " rotayı tamamladı! Son konum: (" + latitude + ", " + longitude + ")";
        stringKafkaTemplate.send(END_LOCATION_TOPIC, vehicleId.toString(), message);
        System.out.println("Kafka'ya araç bitiş konumu bildirimi gönderildi: " + message);
    }
}