import React, { useState } from "react";
import { GoogleMap, LoadScript } from "@react-google-maps/api";
import MarkerComponent from "./MarkerComponent";
import DirectionsRendererComponent from "./DirectionsRendererComponent";

const mapContainerStyle = {
  width: "100%",
  height: "500px",
};

const defaultZoom = 6;

const Map = ({ vehicles, routes }) => {
  const initialCenter = { lat: 39.9334, lng: 32.8597 };

  const [center] = useState(initialCenter);

  return (
    <LoadScript googleMapsApiKey="AIzaSyAoyH2S0s-LqCrGKcFmF4lmV06_mwKlKK8">
      <GoogleMap
        mapContainerStyle={mapContainerStyle}
        zoom={defaultZoom}
        center={center}
        options={{
          disableDefaultUI: true,
          zoomControl: true,
          fullscreenControl: true,
        }}
      >
        {vehicles.map((vehicle) => (
          <MarkerComponent key={vehicle.vehicleId} vehicle={vehicle} />
        ))}
        {Object.keys(routes).map((vehicleId) => (
          <DirectionsRendererComponent key={vehicleId} directions={routes[vehicleId]} />
        ))}
      </GoogleMap>
    </LoadScript>
  );
};

export default Map;