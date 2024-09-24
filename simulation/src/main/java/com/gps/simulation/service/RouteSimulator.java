package com.gps.simulation.service;

import com.gps.simulation.kafka.VehicleProducerService;
import com.gps.simulation.model.enums.Status;
import com.gps.simulation.model.Vehicle;
import com.gps.simulation.repositories.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class RouteSimulator {

    private final DistanceCalculatorService distanceCalculatorService;
    private final VehicleProducerService vehicleProducerService;
    private final RandomRouteService randomRouteService;
    private final VehicleManager vehicleManager;
    private final VehicleRepository vehicleRepository;

    @Autowired
    @Lazy
    private RouteSimulator self;

    public RouteSimulator(DistanceCalculatorService distanceCalculatorService, VehicleManager vehicleManager,
                          VehicleProducerService vehicleProducerService, RandomRouteService randomRouteService, VehicleRepository vehicleRepository) {
        this.distanceCalculatorService = distanceCalculatorService;
        this.vehicleManager = vehicleManager;
        this.vehicleProducerService = vehicleProducerService;
        this.randomRouteService = randomRouteService;
        this.vehicleRepository = vehicleRepository;
    }

    @Async("taskExecutor")
    public void simulateVehicleJourney(Vehicle vehicle, List<double[]> routeSteps, int distanceInterval) {
        int stepIndex = 0;
        double totalDistance = 0;
        double remainingDistanceToNotify = distanceInterval;

        // İlk konum bilgilerini ayarlıyoruz
        if (!routeSteps.isEmpty()) {
            double[] startPoint = routeSteps.get(0);
            vehicle.setCurrentLatitude(startPoint[0]);
            vehicle.setCurrentLongitude(startPoint[1]);
            vehicle.setStatus(Status.ON_ROAD);

            // İlk ülkeyi ayarlıyoruz
            String lastCountry = distanceCalculatorService.getCountryFromCoordinates(startPoint[0], startPoint[1]);
            vehicle.setCurrentCountry(lastCountry);
        }

        // Simülasyon boyunca adım adım ilerliyoruz
        while (stepIndex < routeSteps.size() - 1) {  // Her adımda ilerle
            double[] currentStep = routeSteps.get(stepIndex);
            double[] nextStep = routeSteps.get(stepIndex + 1);

            // Google API kullanarak iki konum arasındaki mesafeyi hesapla
            double distance = distanceCalculatorService.calculateDistance(currentStep, nextStep);
            totalDistance += distance;
            remainingDistanceToNotify -= distance;

            // Aracın yeni konumunu güncelle
            vehicle.setCurrentLatitude(nextStep[0]);
            vehicle.setCurrentLongitude(nextStep[1]);

            // Ülke değişimini kontrol et
            String currentCountry = distanceCalculatorService.getCountryFromCoordinates(nextStep[0], nextStep[1]);
            if (!currentCountry.equals(vehicle.getCurrentCountry())) {
                // Eğer ülke değiştiyse, bildirim gönder
                vehicle.setCurrentCountry(currentCountry);
                vehicleProducerService.sendCountryChangeNotification(vehicle.getVehicleId(), currentCountry);
            }

            // Eğer belirlenen mesafe kat edildiyse bildirim gönder
            if (remainingDistanceToNotify <= 0) {
                vehicleProducerService.sendVehicleData(vehicle);  // Bildirim gönder
                remainingDistanceToNotify = distanceInterval;     // Kalan mesafeyi sıfırla
            }

            // Aracın mevcut durumunu veritabanına kaydet
            vehicleRepository.save(vehicle);

            // Simülasyon hızını ayarlamak için bekletme süresi
            try {
                Thread.sleep(1000);  // Simülasyon hızını yavaşlatmak için 1 saniye bekle
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            stepIndex++;  // Bir sonraki adıma geç
        }

        // Simülasyon tamamlandığında aracı tamamlanmış olarak işaretle
        vehicle.setStatus(Status.COMPLETED);
        vehicleRepository.save(vehicle);  // Son durumu kaydet
        vehicleProducerService.sendVehicleData(vehicle);  // Son bir bildirim gönder
    }

    @Async("taskExecutor")
    public void simulateJourney(int vehicleCount, int distanceInterval) {
        List<Vehicle> vehicles = vehicleManager.createVehicles(vehicleCount);

        for (Vehicle vehicle : vehicles) {
            String[] cities = randomRouteService.getRandomCities();
            List<double[]> routeSteps = distanceCalculatorService.getRouteSteps(cities[0], cities[1]);

            double[] startPoint = routeSteps.get(0);
            double[] endPoint = routeSteps.get(routeSteps.size() - 1);

            vehicle.setStartLatitude(startPoint[0]);
            vehicle.setStartLongitude(startPoint[1]);
            vehicle.setDestinationLatitude(endPoint[0]);
            vehicle.setDestinationLongitude(endPoint[1]);

            try {
                vehicle.setStatus(Status.READY);
                vehicleProducerService.sendVehicleData(vehicle);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            self.simulateVehicleJourney(vehicle, routeSteps, distanceInterval);
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(new Date());
    }
}
