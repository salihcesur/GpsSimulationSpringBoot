import React from "react";
import { Marker } from "@react-google-maps/api";

// iconWithLabel fonksiyonunu doğrudan MarkerComponent içerisinde tanımlıyoruz
const iconWithLabel = (color) => ({
  url: `http://maps.google.com/mapfiles/ms/icons/${color}-dot.png`,
  labelOrigin: new window.google.maps.Point(15, 35),
  scaledSize: new window.google.maps.Size(32, 32),
});

const MarkerComponent = ({ vehicle }) => {
  return (
    <Marker
      position={{ lat: vehicle.currentLatitude, lng: vehicle.currentLongitude }}
      label={{
        text: vehicle.vehicleId ? String(vehicle.vehicleId) : "Unknown",
        fontSize: "14px",
        fontWeight: "bold",
        color: "black",
      }}
      icon={iconWithLabel(vehicle.status === "COMPLETED" ? 'green' : 'red')}
    />
  );
};

export default MarkerComponent;
