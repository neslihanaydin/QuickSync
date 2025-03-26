import React, { useState, useEffect, useContext, useRef } from 'react';
import axios from 'axios';
import { useLocation } from 'react-router-dom';
import { UserContext } from '../../context/UserContext';
import { useWebSocket } from '../../context/WebSocketContext';
import '../../style/ChatComponent.css';

const ChatComponent = () => {
  const { user } = useContext(UserContext);
  const { messages: socketMessages } = useWebSocket();
  const location = useLocation();
  const BASE_URL = process.env.REACT_APP_API_BASE_URL;

  const [partners, setPartners] = useState([]);
  const [selectedPartner, setSelectedPartner] = useState(null);
  const [conversationMessages, setConversationMessages] = useState([]);
  const [lastMessages, setLastMessages] = useState({});
  const [newMessage, setNewMessage] = useState('');

  const messagesEndRef = useRef(null);


  useEffect(() => {
    if (!user) return;

    // Retrieve chat partners (Messaging service)
    axios
      .get(`${BASE_URL}/messages/chat-partners/${user.username}`)
      .then((response) => {
        setPartners(response.data);
        if (response.data.length > 0) {
          // Retrieve last messages for the partners (chat service, redis)
          const partnerList = response.data.join(',');
          axios
            .get(`${BASE_URL}/chat/warmup?username=${user.username}&partners=${partnerList}`)
            .then((res) => setLastMessages(res.data))
            .catch((error) =>
              console.error('Error occurred during the warmup call:', error)
            );
        }
      })
      .catch((error) => console.error('Failed to retrieve partners:', error));

    axios
      .get(`${BASE_URL}/chat/last-messages?username=${user.username}`)
      .then((response) => setLastMessages(response.data))
      .catch((error) => console.error('Failed to retrieve last messages:', error));
    }, [user, BASE_URL]);

  useEffect(() => {
    if (location.state && location.state.selectedSender && user) {
      const selectedSender = location.state.selectedSender;
      axios
        .get(`${BASE_URL}/messages/chat/${user.username}/${selectedSender}`)
        .then((response) => {
          setSelectedPartner(selectedSender);
          setConversationMessages(response.data);
        })
        .catch((error) =>
          console.error('Failed to retrieve chat history:', error)
        );
    }
  }, [location.state, user, BASE_URL]);

// Update lastMessages with messages received from WebSocket
useEffect(() => {
    if (!user) return;
    socketMessages.forEach((msg) => {
      const partner =
        msg.sender === user.username ? msg.receiver : msg.sender;
      setLastMessages((prev) => ({ ...prev, [partner]: msg }));
    });
  }, [socketMessages, user]);

  useEffect(() => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: "smooth" });
    }
  }, []);


// Retrieve chat history with the selected partner
const fetchConversation = (partner) => {
    axios
      .get(`${BASE_URL}/messages/chat/${user.username}/${partner}`)
      .then((response) => {
        setSelectedPartner(partner);
        setConversationMessages(response.data);
        if (response.data.length > 0) {
          const lastMsg = response.data[response.data.length - 1];
          setLastMessages((prev) => ({ ...prev, [partner]: lastMsg }));
        }
      })
      .catch((error) =>
        console.error('Failed to retrieve chat history:', error)
    );
  };


  // Send a new message
  const sendMessage = () => {
    if (!selectedPartner || newMessage.trim() === '') return;

    axios
      .post(
        `${BASE_URL}/chat/send`,
        {
          sender: user.username,
          receiver: selectedPartner,
          text: newMessage,
        }
      )
      .then((response) => {
        const sentMsg = response.data;
        setConversationMessages((prev) => [...prev, sentMsg]);
        setLastMessages((prev) => ({ ...prev, [selectedPartner]: sentMsg }));
        setNewMessage('');
      })
      .catch((error) =>
        console.error('Message could not be sent', error)
    );
  };

  return (
    <div className="chat-container">
      <div className="chat-sidebar">
        <h3 className="chat-sidebar-header">Messages</h3>
        <div className="chat-partner-list">
          {partners.map((partner, index) => (
            <div
              key={index}
              onClick={() => fetchConversation(partner)}
              className={`chat-partner ${
                selectedPartner === partner ? 'active' : ''
              }`}
            >
              <strong>{partner}</strong>
              <p className="chat-last-message">
                {lastMessages[partner]
                  ? lastMessages[partner].text
                  : 'No messages yet'}
              </p>
            </div>
          ))}
        </div>
      </div>
      <div className="chat-main">
        {selectedPartner ? (
          <>
            <h3 className="chat-header">
              Message Details - {selectedPartner}
            </h3>
            <div className="chat-messages">
              {conversationMessages.map((msg, index) => (
                <div key={index} className="chat-message">
                  <strong>{msg.sender}: </strong>
                  {msg.text}
                </div>
              ))}
            </div>
            <div className="chat-input-container">
              <input
                type="text"
                value={newMessage}
                onChange={(e) => setNewMessage(e.target.value)}
                placeholder="Enter your message..."
                className="chat-input"
              />
              <button onClick={sendMessage} className="chat-send-button">
                Send
              </button>
            </div>
          </>
        ) : (
          <p className="chat-placeholder">Please select a conversation.</p>
        )}
      </div>
      <div ref={messagesEndRef} />

    </div>
  );
};

export default ChatComponent;
