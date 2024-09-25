import { useState, useEffect } from "react";

const useRoutes = (vehicles) => {
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
    vehicles.forEach((vehicle) => {
      if (vehicle.status === "READY" && !routes[vehicle.vehicleId]) {
        calculateRoute(vehicle);
      }
    });
  }, [vehicles, routes]);

  return { routes };
};

export default useRoutes;
