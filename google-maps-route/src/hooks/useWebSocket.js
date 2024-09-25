import { useState, useEffect } from "react";
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

const useWebSocket = () => {
  const [vehicles, setVehicles] = useState([]);
  const [notifications, setNotifications] = useState([]);

  useEffect(() => {
    const socket = new SockJS("http://localhost:8081/ws");
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {
      // Vehicle location updates
      stompClient.subscribe("/topic/vehicleLocation", (message) => {
        const vehicle = JSON.parse(message.body);
        setVehicles((prevVehicles) => {
          const existingVehicle = prevVehicles.find(v => v.vehicleId === vehicle.vehicleId);
          return existingVehicle
            ? prevVehicles.map(v => v.vehicleId === vehicle.vehicleId ? vehicle : v)
            : [...prevVehicles, vehicle];
        });
      });

      // Country change or start/end notifications
      stompClient.subscribe("/topic/vehicleNotification", (message) => {
        setNotifications((prevNotifications) => [message.body, ...prevNotifications]);
      });

      stompClient.subscribe("/topic/vehicleStartEnd", (message) => {
        setNotifications((prevNotifications) => [message.body, ...prevNotifications]);
      });

      // Vehicle end location updates
      stompClient.subscribe("/topic/vehicleEndLocation", (message) => {
        const endLocationMessage = message.body;
        setNotifications((prevNotifications) => [endLocationMessage, ...prevNotifications]);

        // Optionally, update vehicles state with the last location
        const [vehicleId, lat, lon] = endLocationMessage.match(/Araç (\d+) .*Son konum: \(([\d.]+), ([\d.]+)\)/).slice(1);
        setVehicles((prevVehicles) =>
          prevVehicles.map(v =>
            v.vehicleId === parseInt(vehicleId)
              ? { ...v, currentLatitude: parseFloat(lat), currentLongitude: parseFloat(lon), status: 'COMPLETED' }
              : v
          )
        );
      });
    });

    return () => stompClient.disconnect();
  }, []);

  return { vehicles, notifications };
};

export default useWebSocket;
