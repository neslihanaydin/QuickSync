import React, { createContext, useContext, useEffect, useState } from "react";
import { UserContext } from "./UserContext"; 

const WebSocketContext = createContext(null);

export const WebSocketProvider = ({ children }) => {
  const [ws, setWs] = useState(null);
  const [messages, setMessages] = useState([]);
  const { user } = useContext(UserContext);

  useEffect(() => {
    if (!user) return;
    const socket = new WebSocket("ws://localhost:8080/ws"); 

    socket.onopen = () => {
      console.log("WebSocket connection established!");
    };

    socket.onmessage = (event) => {
      let data = event.data;
      try {
        data = JSON.parse(data);
      } catch (error) {
        console.error("JSON parse error:", error);
      }
      if(data.receiver === user.username) {
        // console.log("New message", data);
        setMessages((prevMessages) => [...prevMessages, data]);
      }
      
    };

    socket.onerror = (error) => {
      console.error("WebSocket error:", error);
    };

    socket.onclose = () => {
      console.log("WebSocket connection closed.");
    };

    setWs(socket);

    return () => {
      if (socket) {
        socket.close();
      }
    };
  }, [user]);

   const removeNotificationsBySender = (sender) => {
    setMessages(prevMessages => prevMessages.filter(msg => msg.sender !== sender));
  };

  return (
    <WebSocketContext.Provider value={{ ws, messages, removeNotificationsBySender }}>
      {children}
    </WebSocketContext.Provider>
  );
};


export const useWebSocket = () => useContext(WebSocketContext);
