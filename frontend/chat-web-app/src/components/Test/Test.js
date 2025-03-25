import React, { useEffect, useContext } from 'react';
import axios from 'axios';
import { UserContext } from '../../context/UserContext';

const Test = () => {
  const { user } = useContext(UserContext);

  const BASE_URL = process.env.REACT_APP_API_BASE_URL;


  useEffect(() => {
    console.log("Test:", BASE_URL);
    const fetchUser = async () => {
      try {
        console.log("Base URL:", BASE_URL);
        // Fetch user data
        const meResponse = await axios.get(`${BASE_URL}/users/profile`, {
          withCredentials: true
        });
        console.log("Me response:", meResponse);
        console.log("User data:", meResponse.data);
      } catch (error) {
        console.error("Error fetching user data:", error);
      }
    };
    fetchUser();
  }
    , []);
  return (
    <div className="chat-container">
        <h2>Test Component</h2>
        <p>User: {user ? user.username : 'No user logged in'}</p>
        <p>Base URL: {BASE_URL}</p>
    </div>
  );
};

export default Test;
