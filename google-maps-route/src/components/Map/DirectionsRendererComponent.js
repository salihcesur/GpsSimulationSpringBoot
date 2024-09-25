import React from "react";
import { DirectionsRenderer } from "@react-google-maps/api";

const DirectionsRendererComponent = ({ directions }) => {
  return <DirectionsRenderer directions={directions} options={{ suppressMarkers: true, preserveViewport: true }} />;
};

export default DirectionsRendererComponent;
