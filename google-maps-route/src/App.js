import React, { useState, useEffect } from "react";
import { GoogleMap, LoadScript, Marker, DirectionsRenderer } from "@react-google-maps/api";
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

const mapContainerStyle = {
  width: "100%",
  height: "500px",
};

// Türkiye'nin merkez koordinatları
const initialCenter = {
  lat: 39.9334,
  lng: 32.8597,
};

// Haritanın başlangıçtaki sabit zoom seviyesi
const defaultZoom = 6;

const iconWithLabel = (color) => ({
  url: `http://maps.google.com/mapfiles/ms/icons/${color}-dot.png`,
  labelOrigin: new window.google.maps.Point(15, 35),
  scaledSize: new window.google.maps.Size(32, 32),
});

const App = () => {
  const [vehicles, setVehicles] = useState([]);
  const [routes, setRoutes] = useState({});

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

  useEffect(() => {
    const socket = new SockJS("http://localhost:8081/ws");
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {
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
      });
    });

    return () => {
      if (stompClient) {
        stompClient.disconnect();
      }
    };
  }, []);

  useEffect(() => {
    vehicles.forEach((vehicle) => {
      if (!vehicle.completed && !routes[vehicle.vehicleId]) {
        calculateRoute(vehicle);
      }
    });
  }, [vehicles, routes]);

  const clearRoute = (vehicleId) => {
    setRoutes((prevRoutes) => {
      const newRoutes = { ...prevRoutes };
      delete newRoutes[vehicleId];
      return newRoutes;
    });
  };

  useEffect(() => {
    vehicles.forEach((vehicle) => {
      if (vehicle.completed) {
        clearRoute(vehicle.vehicleId);
      }
    });
  }, [vehicles]);

  return (
    <LoadScript googleMapsApiKey="AIzaSyAoyH2S0s-LqCrGKcFmF4lmV06_mwKlKK8">
      <GoogleMap
        mapContainerStyle={mapContainerStyle}
        zoom={defaultZoom} // Zoom seviyesini sabitliyoruz
        center={initialCenter} // Haritanın merkezini Türkiye'ye sabitliyoruz
        options={{
          disableDefaultUI: true, // Harita kontrollerini gizle (isteğe bağlı)
          zoomControl: true, // Sadece zoom kontrolü aktif
        }}
      >
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
              vehicle.completed
                ? iconWithLabel('green')
                : iconWithLabel('red')
            }
          />
        ))}

        {Object.keys(routes).map((vehicleId) => (
          <DirectionsRenderer
            key={vehicleId}
            directions={routes[vehicleId]}
            options={{ 
              suppressMarkers: true, 
              preserveViewport: true  // Viewport'u koruyoruz, böylece harita yakınlaşmaz
            }}
          />
        ))}
      </GoogleMap>
    </LoadScript>
  );
};

export default App;
