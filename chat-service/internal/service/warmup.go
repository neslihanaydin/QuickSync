package service

import (
	"chat-service/internal/models"
	"context"
	"encoding/json"
	"time"
)

func (s *ChatService) WarmupLastMessages(ctx context.Context, username string, partners []string, messagingClient *MessagingClient) (map[string]models.Message, error) {
	conversations := make(map[string]models.Message)

	for _, partner := range partners {
		msg, err := messagingClient.GetLastMessage(username, partner)
		if err != nil {
			continue
		}
		messageJSON, err := json.Marshal(&msg)
		if err != nil {
			continue
		}

		key := "last_message:" + msg.Sender + ":" + msg.Receiver
		err = s.RedisClient.Set(ctx, key, messageJSON, 1*time.Hour).Err()
		if err != nil {
			continue
		}
		conversations[partner] = *msg

	}
	return conversations, nil
}
