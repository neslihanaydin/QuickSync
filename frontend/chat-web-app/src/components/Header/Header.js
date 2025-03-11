import React, { useState, useContext } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { Badge, TextField } from "@mui/material";
import AppsIcon from "@mui/icons-material/Apps";
import ChatIcon from "@mui/icons-material/Chat";
import NotificationsIcon from "@mui/icons-material/Notifications";
import PersonIcon from '@mui/icons-material/Person';
import { useWebSocket } from "../../context/WebSocketContext";
import NotificationList from "../Notification/NotificationList";
import "../../style/Header.css";
import Profile from "../Profile/Profile";

const Header = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { messages } = useWebSocket();
  const [showNotifications, setShowNotifications] = useState(false);
  const [showProfile, setShowProfile] = useState(false);

  const isChatActive = location.pathname === "/chat";
  const isNotificationsActive = showNotifications;
  const isProfileActive = showProfile;

  const newNotificationsCount = messages.length;

  const handleChatClick = () => {
    setShowNotifications(false);
    navigate("/chat");
  };

  const handleProfileClick = () => {
    setShowProfile((prev) => !prev);
    if (showNotifications) {
        setShowNotifications(false);
    }
  };

  const handleNotificationsClick = () => {
    setShowNotifications((prev) => !prev);
    if (showProfile) {
        setShowProfile(false);
    }
  };

  return (
    <header className="header">
      <div className="header-left">
        <AppsIcon className="app-icon" />
        <TextField
          variant="outlined"
          placeholder="Search..."
          size="small"
          className="header-search"
        />
      </div>
      <div className="header-right">
        <div
          className={`header-tab ${isChatActive ? "active" : ""}`}
          onClick={handleChatClick}
        >
          <ChatIcon className="header-icon" />
          <span className="header-tab-label">Messaging</span>
        </div>
        
            <div
            className={`header-tab ${isProfileActive ? "active" : ""}`}
            onClick={handleProfileClick}
            >
            <PersonIcon className="header-icon" />
            <span className="header-tab-label">Profile</span>
        </div>
        {showProfile && (
        <div className="notification-overlay">
          <Profile />
        </div>
      )}
        <div
          className={`header-tab ${isNotificationsActive ? "active" : ""}`}
          onClick={handleNotificationsClick}
        >
          <Badge
            color="error"
            variant="dot"
            invisible={newNotificationsCount === 0}
          >
            <NotificationsIcon className="header-icon" />
          </Badge>
          <span className="header-tab-label">Notifications</span>
        </div>
      </div>
      {showNotifications && (
        <div className="notification-overlay">
          <NotificationList />
        </div>
      )}
    </header>
  );
};

export default Header;
