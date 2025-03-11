import React, { useContext } from "react";
import { useNavigate } from "react-router-dom";
import { UserContext } from "../../context/UserContext";
import { Button } from "@mui/material";
import ExitToAppIcon from "@mui/icons-material/ExitToApp";

import '../../style/Profile.css';

const Profile = () => {
  const { user, setUser } = useContext(UserContext);
  const navigate = useNavigate();

  const handleSignOut = () => {
    sessionStorage.removeItem("token");
    setUser(null);
    navigate("/login");
  };

  return (
    <div>
      <div className="profile-container">
        <div className="profile-list">
          <div className="profile-item">
            <span className="header-username">{user.username}</span>
          </div>
          <div className="profile-item">
            <Button
              className="header-signout"
              onClick={handleSignOut}
              variant="contained"
              color="error"
              startIcon={<ExitToAppIcon />}
            >
              Sign Out
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;
