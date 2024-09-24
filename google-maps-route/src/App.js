import React, { useState, useEffect } from "react";
import { GoogleMap, LoadScript, Marker, DirectionsRenderer } from "@react-google-maps/api";
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

const mapContainerStyle = {
  width: "100%",
  height: "500px",
};

const initialCenter = {
  lat: 39.9334,
  lng: 32.8597,
};

const defaultZoom = 6;

const iconWithLabel = (color) => ({
  url: `http://maps.google.com/mapfiles/ms/icons/${color}-dot.png`,
  labelOrigin: new window.google.maps.Point(15, 35),
  scaledSize: new window.google.maps.Size(32, 32),
});

const App = () => {
  const [vehicles, setVehicles] = useState([]);
  const [routes, setRoutes] = useState({});
  const [notifications, setNotifications] = useState([]); // Ülke değişim bildirimleri için state
  const [loading, setLoading] = useState(true); // Yükleme durumu kontrolü

  // Araç rotasını Google Maps API ile hesaplama
  const calculateRoute = (vehicle) => {
    const directionsService = new window.google.maps.DirectionsService();
    directionsService.route(
      {
        origin: { lat: vehicle.startLatitude, lng: vehicle.startLongitude },
        destination: { lat: vehicle.destinationLatitude, lng: vehicle.destinationLongitude },
        travelMode: window.google.maps.TravelMode.DRIVING,
      },
      (result, status) => {
        if (status === window.google.maps.DirectionsStatus.OK) {
          setRoutes((prevRoutes) => ({
            ...prevRoutes,
            [vehicle.vehicleId]: result,
          }));
        } else {
          console.error(`Rota hesaplama hatası: ${status}`);
        }
      }
    );
  };

  // WebSocket bağlantısı ve veri alma
  useEffect(() => {
    const socket = new SockJS("http://localhost:8081/ws");
    const stompClient = Stomp.over(socket);
  
    stompClient.connect({}, () => {
      // Araç konumlarını dinleme
      stompClient.subscribe("/topic/vehicleLocation", (message) => {
        const vehicle = JSON.parse(message.body);
        setVehicles((prevVehicles) => {
          const existingVehicle = prevVehicles.find(v => v.vehicleId === vehicle.vehicleId);
          if (existingVehicle) {
            return prevVehicles.map(v => v.vehicleId === vehicle.vehicleId ? vehicle : v);
          } else {
            return [...prevVehicles, vehicle];
          }
        });
        setLoading(false);
      });
  
      // Ülke değişim bildirimlerini dinleme
      stompClient.subscribe("/topic/vehicleNotification", (message) => {
        setNotifications((prevNotifications) => [message.body, ...prevNotifications]);
      });
  
      // Başlangıç ve bitiş şehir bildirimlerini dinleme
      stompClient.subscribe("/topic/vehicleStartEnd", (message) => {
        console.log("Şehir bildirimi alındı:", message.body);
        setNotifications((prevNotifications) => [message.body, ...prevNotifications]);
      });
    });
  
    return () => {
      if (stompClient) {
        stompClient.disconnect();
      }
    };
  }, []);

  // Araç rotalarını hesaplama
  useEffect(() => {
    vehicles.forEach((vehicle) => {
      if (vehicle.status === "READY" && !routes[vehicle.vehicleId]) {
        calculateRoute(vehicle);
      }
    });
  }, [vehicles, routes]);

  // Aracın rotasını temizleme
  const clearRoute = (vehicleId) => {
    setRoutes((prevRoutes) => {
      const newRoutes = { ...prevRoutes };
      delete newRoutes[vehicleId];
      return newRoutes;
    });
  };

  // Araç rotası tamamlandığında rotayı temizleme
  useEffect(() => {
    vehicles.forEach((vehicle) => {
      if (vehicle.status === "COMPLETED") {
        clearRoute(vehicle.vehicleId);
      }
    });
  }, [vehicles]);

  return (
    <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif' }}>
      {/* Yükleme göstergesi */}
      {loading && <p>Veriler yükleniyor...</p>}

      {/* Harita */}
      <LoadScript googleMapsApiKey="AIzaSyAoyH2S0s-LqCrGKcFmF4lmV06_mwKlKK8">
        <GoogleMap
          mapContainerStyle={mapContainerStyle}
          zoom={defaultZoom}
          center={initialCenter}
          options={{
            disableDefaultUI: true, 
            zoomControl: true, 
            fullscreenControl: true,
          }}
        >
          {/* Araçlar */}
          {vehicles.map((vehicle) => (
            <Marker
              key={vehicle.vehicleId}
              position={{ lat: vehicle.currentLatitude, lng: vehicle.currentLongitude }}
              label={{
                text: vehicle.vehicleId ? String(vehicle.vehicleId) : "Unknown",
                fontSize: "14px",
                fontWeight: "bold",
                color: "black",
              }}
              icon={
                vehicle.status === "COMPLETED"
                  ? iconWithLabel('green')
                  : iconWithLabel('red')
              }
            />
          ))}

          {/* Araç rotaları */}
          {Object.keys(routes).map((vehicleId) => (
            <DirectionsRenderer
              key={vehicleId}
              directions={routes[vehicleId]}
              options={{ 
                suppressMarkers: true, 
                preserveViewport: true 
              }}
            />
          ))}
        </GoogleMap>
      </LoadScript>

  
    <div style={{ marginTop: '20px', padding: '10px', border: '1px solid #ccc', borderRadius: '5px', backgroundColor: '#f9f9f9' }}>
      <h3 style={{ textAlign: 'center', color: '#333', fontWeight: 'bold', marginBottom: '10px' }}>Ülke ve Şehir Bildirimleri</h3>
      <ul style={{ listStyleType: 'none', padding: 0, margin: 0, maxHeight: '300px', overflowY: 'scroll' }}>
        {notifications.map((notification, index) => (
        <li key={index} style={{ marginBottom: '10px', padding: '10px', border: '1px solid #ccc', borderRadius: '5px', backgroundColor: '#fff', boxShadow: '0 0   5px rgba(0, 0, 0, 0.1)' }}>
          {notification}
        </li>
    ))}
  </ul>
</div>

    </div>
  );
};

export default App;