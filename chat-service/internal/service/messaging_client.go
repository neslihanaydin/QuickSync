package service

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"time"

	"chat-service/internal/models"
)

type MessagingClient struct {
	BaseURL string
}

func NewMessagingClient(baseURL string) *MessagingClient {
	return &MessagingClient{BaseURL: baseURL}
}

func (mc *MessagingClient) GetLastMessage(username, partner string) (*models.Message, error) {
	url := fmt.Sprintf("%s/messages/last-message/%s/%s", mc.BaseURL, username, partner)
	client := http.Client{Timeout: 5 * time.Second}

	resp, err := client.Get(url)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("failed to get last message, status: %d", resp.StatusCode)
	}

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, err
	}

	var msg models.Message
	if err := json.Unmarshal(body, &msg); err != nil {
		fmt.Println(err)
		return nil, err
	}

	return &msg, nil
}
