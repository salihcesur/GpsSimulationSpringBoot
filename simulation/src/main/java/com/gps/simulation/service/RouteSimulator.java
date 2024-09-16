package com.gps.simulation.service;

import com.gps.simulation.kafka.VehicleProducerService;
import com.gps.simulation.model.Vehicle;
import com.gps.simulation.repositories.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class RouteSimulator {

    private final DistanceCalculatorService distanceCalculatorService;
    private final VehicleRepository vehicleRepository;
    private final VehicleProducerService vehicleProducerService;  // Kafka producer servisi
    private final RandomRouteService randomRouteService;  // RandomRouteService entegrasyonu

    @Autowired
    @Lazy
    private RouteSimulator self;

    public RouteSimulator(DistanceCalculatorService distanceCalculatorService, VehicleRepository vehicleRepository,
                          VehicleProducerService vehicleProducerService, RandomRouteService randomRouteService) {
        this.distanceCalculatorService = distanceCalculatorService;
        this.vehicleRepository = vehicleRepository;
        this.vehicleProducerService = vehicleProducerService;  // Kafka producer servisi
        this.randomRouteService = randomRouteService;  // RandomRouteService kullanımı
    }

    @Async("taskExecutor")
    public void simulateJourney(int vehicleCount, int distanceInterval) {
        List<Vehicle> vehicles = createVehicles(vehicleCount);  // Araçları oluştur

        for (Vehicle vehicle : vehicles) {
            String[] cities = randomRouteService.getRandomCities(); // Random rotaları al
            List<double[]> routeSteps = distanceCalculatorService.getRouteSteps(cities[0], cities[1]);
            self.simulateVehicleJourney(vehicle, routeSteps, distanceInterval);
        }

    }

    @Async("taskExecutor")
    public void simulateVehicleJourney(Vehicle vehicle, List<double[]> routeSteps, int distanceInterval) {
        int stepIndex = 0;
        double totalDistance = 0;  // Toplam mesafeyi takip eden değişken
        double remainingDistanceToNotify = distanceInterval;  // Bildirim için kalan mesafe

        while (stepIndex < routeSteps.size()) {
            double[] currentStep = routeSteps.get(stepIndex);
            double[] nextStep = stepIndex + 1 < routeSteps.size() ? routeSteps.get(stepIndex + 1) : null;

            // Şu anki adımın enlem ve boylamını araca set et
            vehicle.setCurrentLatitude(currentStep[0]);
            vehicle.setCurrentLongitude(currentStep[1]);

            // Sonraki adım varsa, iki adım arasındaki mesafeyi hesapla
            if (nextStep != null) {
                double distance = distanceCalculatorService.calculateDistance(currentStep, nextStep);  // Mesafe hesaplamayı service'den yap
                totalDistance += distance;  // Toplam mesafeyi güncelle
                remainingDistanceToNotify -= distance;  // Kalan bildirime kadar olan mesafeyi düş

                // Eğer kalan mesafe `0` veya altına düştüyse bildirim gönder
                if (remainingDistanceToNotify <= 0) {
                    vehicleProducerService.sendVehicleData(vehicle);  // Kafka'ya araç verilerini gönder

                    System.out.println(getCurrentTime() + " - Vehicle ID: " + vehicle.getVehicleId() + " " + distanceInterval + " km yol aldı.");

                    // Kalan mesafeyi tekrar `distanceInterval` kadar artır (bir sonraki bildirim için)
                    remainingDistanceToNotify += distanceInterval;  // Sadece tamamlanmış mesafeyi resetle
                }
            }

            stepIndex++;

            try {
                Thread.sleep(30000 / distanceInterval);  // Gecikmeyi mesafeye göre ayarla (dinamik gecikme)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Hedefe ulaştığında aracın konumunu ve durumunu güncelle
        vehicle.setCompleted(true);  // Aracın hedefe ulaştığını işaretleyin
        vehicleProducerService.sendVehicleData(vehicle);  // Kafka'ya araç verilerini gönder

        System.out.println(getCurrentTime() + " - Vehicle ID: " + vehicle.getVehicleId() + " hedefe ulaştı.");
    }


    private List<Vehicle> createVehicles(int vehicleCount) {
        List<Vehicle> vehicles = new ArrayList<>();

        for (int i = 1; i <= vehicleCount; i++) {
            int speed = 120;

            Vehicle vehicle = new Vehicle(speed);  // Araç nesnesi oluştur
            vehicleRepository.save(vehicle);  // Veritabanına kaydet

            vehicles.add(vehicle);
        }

        return vehicles;
    }

    private String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(new Date());
    }
}
