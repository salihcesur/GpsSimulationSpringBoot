package com.gps.simulation.service;

import com.gps.simulation.model.Vehicle;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RouteSimulator {

    private final DistanceCalculatorService distanceCalculatorService;

    // Başlangıç ve bitiş şehirleri
    private String origin = "Istanbul";
    private String destination = "Ankara";

    public RouteSimulator(DistanceCalculatorService distanceCalculatorService) {
        this.distanceCalculatorService = distanceCalculatorService;
    }

    public void simulateJourney(int vehicleCount, int distanceInterval) {
        // Karayolu mesafesini Google API'den alıyoruz
        double totalDistance = distanceCalculatorService.getRoadDistance(origin, destination);
        List<Vehicle> vehicles = createVehicles(vehicleCount, totalDistance);

        for (Vehicle vehicle : vehicles) {
            double remainingDistance = totalDistance;
            double distanceSinceLastMessage = 0; // Mesaj atılmasından sonra gidilen toplam mesafe

            System.out.println(getCurrentTime() + " - Vehicle ID: " + vehicle.getVehicleId() +
                    " hız: " + vehicle.getSpeed() + " km/h ile yolculuğa başladı. Toplam mesafe: " + totalDistance + " km.");

            long lastTime = System.currentTimeMillis(); // Başlangıç zamanını alıyoruz
            while (remainingDistance > 0) {
                long currentTime = System.currentTimeMillis(); // Şu anki zamanı al
                long elapsedTime = currentTime - lastTime; // Geçen süreyi hesapla (milisaniye)

                // Eğer 1 saniye (1000 milisaniye) geçtiyse
                if (elapsedTime >= 1000) {
                    // Araç her saniyede hızının 60'da biri kadar mesafe kat eder
                    double distanceCovered = vehicle.getSpeed() / 60.0;
                    remainingDistance -= distanceCovered;
                    distanceSinceLastMessage += distanceCovered;

                    // Negatif kalan mesafe olmasını önlemek
                    if (remainingDistance < 0) {
                        remainingDistance = 0;
                    }

                    // Eğer distanceInterval'e ulaşıldıysa bildirim gönder
                    if (distanceSinceLastMessage >= distanceInterval) {
                        sendLocationMessage(vehicle, remainingDistance);
                        distanceSinceLastMessage = 0; // Mesaj gönderildikten sonra sıfırla
                    }

                    lastTime = currentTime; // Zamanı güncelle
                }
            }

            // Hedefe ulaştığında mesaj at
            System.out.println(getCurrentTime() + " - Vehicle ID: " + vehicle.getVehicleId() + " hedefe ulaştı.");
        }
    }

    private void sendLocationMessage(Vehicle vehicle, double remainingDistance) {
        System.out.println(getCurrentTime() + " - Vehicle ID: " + vehicle.getVehicleId() +
                " şu an Hız: " + vehicle.getSpeed() + " km/h, Kalan km: " + remainingDistance);
    }

    private List<Vehicle> createVehicles(int vehicleCount, double totalDistance) {
        List<Vehicle> vehicles = new ArrayList<>();
        for (int i = 1; i <= vehicleCount; i++) {
            vehicles.add(new Vehicle("Vehicle-" + i, totalDistance));
        }
        return vehicles;
    }

    // Zaman bilgisini formatlayan yardımcı fonksiyon
    private String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(new Date());
    }
}
