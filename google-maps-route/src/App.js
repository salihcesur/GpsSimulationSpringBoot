import React, { useState, useEffect } from "react";
import { GoogleMap, LoadScript, Marker } from "@react-google-maps/api";
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

const iconWithLabel = (color) => ({
  url: `http://maps.google.com/mapfiles/ms/icons/${color}-dot.png`, 
  labelOrigin: new window.google.maps.Point(15, 35),
  scaledSize: new window.google.maps.Size(32, 32),
});

const App = () => {
  const [vehicles, setVehicles] = useState([]);

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
    vehicles.forEach(vehicle => {
      if (!vehicle.completed && vehicle.remainingKm === 0) {
        setTimeout(() => {
          setVehicles((prevVehicles) =>
            prevVehicles.map(v =>
              v.vehicleId === vehicle.vehicleId ? { ...v, completed: true } : v
            )
          );
        }, 2000);
      }
    });
  }, [vehicles]);

  return (
    <LoadScript googleMapsApiKey="AIzaSyAoyH2S0s-LqCrGKcFmF4lmV06_mwKlKK8">
      <GoogleMap
        mapContainerStyle={mapContainerStyle}
        zoom={6}
        center={initialCenter}
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
      </GoogleMap>
    </LoadScript>
  );
};

export default App;
