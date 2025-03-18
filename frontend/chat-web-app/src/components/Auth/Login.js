import React, { useState, useContext, useEffect } from 'react';
import axios from 'axios';
import { useNavigate, Link } from 'react-router-dom';
import { UserContext } from '../../context/UserContext';
import '../../style/Login.css';

const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const { setUser } = useContext(UserContext);
  const navigate = useNavigate();

  const BASE_URL = process.env.REACT_APP_API_BASE_URL;

  useEffect(() => {
    setUser(null);
     axios.get(`${BASE_URL}/users/profile`, {
      withCredentials: true,
     })
     .then((meResponse) => {
       setUser(meResponse.data);
       navigate('/chat');
     })
     .catch(() => {
       console.log('Not logged in');
     });
  }, [setUser, navigate, BASE_URL]);

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post(
        `${BASE_URL}/users/login`,
        { username, password },
        {
          headers: { "Content-Type": "application/json" },
          withCredentials: true,
        }
      );
      const meResponse = await axios.get(`${BASE_URL}/users/profile`, {
        withCredentials: true
      });
      setUser(meResponse.data);

      navigate('/chat');
    } catch (error) {
      alert('Login failed!');
    }
  };

  const handleGoogleLogin = () => {
    window.location.href = `${BASE_URL}/users/oauth2/authorization/google`;
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <h2>Sign in</h2>
        <form onSubmit={handleLogin} className="login-form">
          <input
            type="text"
            placeholder="Username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
            className="login-input"
          />
          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            className="login-input"
          />
          <button type="submit" className="login-button">Continue</button>
        </form>
        <hr />
        <button onClick={handleGoogleLogin} className="login-button google">
          Login with Google
        </button>
        <div className="login-footer">
          <p>
            Don't have an account? <Link to="/register">Sign up</Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;
