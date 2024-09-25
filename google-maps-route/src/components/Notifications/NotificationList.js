import React from 'react';

const NotificationList = ({ notifications }) => (
  <div style={{ marginTop: '20px', padding: '10px', border: '1px solid #ccc', borderRadius: '5px', backgroundColor: '#f9f9f9' }}>
    <h3 style={{ textAlign: 'center', color: '#333', fontWeight: 'bold', marginBottom: '10px' }}>Ülke ve Şehir Bildirimleri</h3>
    <ul style={{ listStyleType: 'none', padding: 0, margin: 0, maxHeight: '300px', overflowY: 'scroll' }}>
      {notifications.map((notification, index) => (
        <li key={index} style={{ marginBottom: '10px', padding: '10px', border: '1px solid #ccc', borderRadius: '5px', backgroundColor: '#fff' }}>
          {notification}
        </li>
      ))}
    </ul>
  </div>
);

export default NotificationList;
