package handler

import (
	"context"
	"encoding/json"
	"net/http"
	"strings"
	"time"

	"chat-service/internal/models"
	"chat-service/internal/service"
)

type ChatHandler struct {
	ChatService *service.ChatService
}

func NewChatHandler(chatService *service.ChatService) *ChatHandler {
	return &ChatHandler{
		ChatService: chatService,
	}
}

// Cache warm up
func (h *ChatHandler) WarmupLastMessages(w http.ResponseWriter, r *http.Request) {
	username := r.URL.Query().Get("username")
	if username == "" {
		http.Error(w, "username parameter is required", http.StatusBadRequest)
		return
	}

	partnersParam := r.URL.Query().Get("partners")
	if partnersParam == "" {
		http.Error(w, "partners parameter is required", http.StatusBadRequest)
		return
	}
	partners := strings.Split(partnersParam, ",")

	messagingClient := service.NewMessagingClient("http://localhost:8080")

	ctx, cancel := context.WithTimeout(r.Context(), 5*time.Minute)
	defer cancel()

	// Calling the warm-up process
	messages, err := h.ChatService.WarmupLastMessages(ctx, username, partners, messagingClient)
	if err != nil {
		http.Error(w, "Cache warm-up error: "+err.Error(), http.StatusInternalServerError)
		return
	}

	// Returning JSON response
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	json.NewEncoder(w).Encode(messages)
}

type messageAlias models.Message

func (h *ChatHandler) SendMessage(w http.ResponseWriter, r *http.Request) {
	var temp messageAlias
	err := json.NewDecoder(r.Body).Decode(&temp)
	if err != nil {
		http.Error(w, "Invalid JSON format", http.StatusBadRequest)
		return
	}

	msg := models.Message(temp)
	msg.Time = time.Now()
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Minute)
	defer cancel()

	response, err := h.ChatService.SendMessage(ctx, msg)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	w.Header().Set("Content-Type", "application/json")

	w.WriteHeader(http.StatusOK)
	w.Write(response)
}

func (h *ChatHandler) GetLastMessages(w http.ResponseWriter, r *http.Request) {
	username := r.URL.Query().Get("username")
	if username == "" {
		http.Error(w, "username parameter is required", http.StatusBadRequest)
		return
	}

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Minute)
	defer cancel()

	lastMessages, err := h.ChatService.GetLastMessages(ctx, username)
	if err != nil {
		http.Error(w, "Failed to retrieve last messages: "+err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(lastMessages)
}
