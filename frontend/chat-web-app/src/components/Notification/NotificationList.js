import React from "react";
import { useWebSocket } from "../../context/WebSocketContext";
import { useNavigate } from "react-router-dom";
import '../../style/NotificationList.css';

const NotificationList = () => {
  const { messages, removeNotificationsBySender } = useWebSocket();
  const navigate = useNavigate();

  const handleNotificationClick = (sender) => {
    removeNotificationsBySender(sender);
    navigate("/chat", { state: { selectedSender: sender } });
  };

  return (
    <div className="notification-list-container">
      <h2>Notifications</h2>
      {messages.length === 0 ? (
        <p>No notifications yet.</p>
      ) : (
        <div className="notification-list">
          {messages.map((msg, index) => (
            <div
              key={index}
              className="notification-item"
              onClick={() => handleNotificationClick(msg.sender)}
            >
              <strong>{msg.sender}:</strong> {msg.text}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default NotificationList;
