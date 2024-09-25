import React from 'react';
import Map from './components/Map/Map';
import NotificationList from './components/Notifications/NotificationList';
import useWebSocket from './hooks/useWebSocket';
import useRoutes from './hooks/useRoutes';

const App = () => {
  const { vehicles, notifications } = useWebSocket();
  const { routes } = useRoutes(vehicles);

  return (
    <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif' }}>
      <Map vehicles={vehicles} routes={routes} />
      <NotificationList notifications={notifications} />
    </div>
  );
};

export default App;
