import React from 'react';
import './NotificationList.css';

const NotificationList = ({ notifications }) => (
  <div className="notification-container">
    <h3 className="notification-title">Ülke ve Şehir Bildirimleri</h3>
    <ul className="notification-list">
      {notifications.map((notification, index) => (
        <li key={index} className="notification-item">
          {notification}
        </li>
      ))}
    </ul>
  </div>
);

export default NotificationList;
