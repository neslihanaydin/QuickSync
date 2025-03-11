import React, { useContext } from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Header from "./components/Header/Header.js";
import Login from "./components/Auth/Login";
import Register from "./components/Auth/Register";
import ChatComponent from "./components/Chat/ChatComponent";
import { UserContext, UserProvider } from "./context/UserContext";
import { WebSocketProvider } from "./context/WebSocketContext";
import "./App.css";

const AppContent = () => {
  const { user } = useContext(UserContext);

  return (
    <Router>
      {user && <Header />}
      <div className="app-main">
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/chat" element={<ChatComponent />} />
          <Route path="/" element={<Login />} />
        </Routes>
      </div>
    </Router>
  );
};

function App() {
  return (
    <UserProvider>
      <WebSocketProvider>
        <AppContent />
      </WebSocketProvider>
    </UserProvider>
  );
}

export default App;
