import React, { createContext, useState, useEffect } from 'react';
import axios from 'axios';

export const UserContext = createContext();

export const UserProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const BASE_URL = process.env.REACT_APP_API_BASE_URL;

  useEffect(() => {
    const fetchUser = async () => {
      try {

        const token = sessionStorage.getItem("token");
        if (token != null) {
          const meResponse = await axios.get(`${BASE_URL}/users/profile`, {
            headers: { Authorization: `Bearer ${token}` }
          });
          setUser(meResponse.data);
        } else {
          setUser(null);
        }
      } catch (error) {
        setUser(null);
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
