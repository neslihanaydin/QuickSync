import React, { createContext, useState, useEffect } from 'react';
import axios from 'axios';

export const UserContext = createContext();

export const UserProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const BASE_URL = process.env.REACT_APP_API_BASE_URL;

  useEffect(() => {
    const fetchUser = async () => {
      try {
        console.log("Base URL:", BASE_URL);
        const meResponse = await axios.get(`${BASE_URL}/users/profile`, {
          withCredentials: true
        });
        setUser(meResponse.data);
      }
      catch (error) {
        setUser(null);
        console.error("Error fetching user data:", error);
      }
    };
    fetchUser();
  }, []);


  return (
    <UserContext.Provider value={{ user, setUser }}>
      {children}
    </UserContext.Provider>
  );
};
