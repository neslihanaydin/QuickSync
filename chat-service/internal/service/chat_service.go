package service

import (
	"context"
	"encoding/json"
	"errors"
	"strings"
	"time"

	"chat-service/internal/kafka"
	"chat-service/internal/models"

	"github.com/go-redis/redis/v8"
)

type ChatService struct {
	Producer    kafka.Producer
	RedisClient *redis.Client
}

func NewChatService(prod kafka.Producer, r *redis.Client) *ChatService {
	return &ChatService{
		Producer:    prod,
		RedisClient: r,
	}
}

func (s *ChatService) SendMessage(ctx context.Context, msg models.Message) ([]byte, error) {
	if msg.Sender == "" || msg.Receiver == "" || msg.Text == "" {
		return nil, errors.New("sender, receiver, and message fields cannot be empty")
	}

	msg.Time = time.Now()

	messageJSON, err := json.Marshal(msg)
	if err != nil {
		return nil, err
	}

	// Send message to Kafka
	if err = s.Producer.SendMessage(string(messageJSON)); err != nil {
		return nil, err
	}

	// Store the last message in Redis
	key := "last_message:" + msg.Sender + ":" + msg.Receiver
	s.RedisClient.Set(ctx, key, messageJSON, 1*time.Hour).Err()
	return messageJSON, nil
}

// GetLastMessages retrieves the last messages for a given user,
func (s *ChatService) GetLastMessages(ctx context.Context, username string) (map[string]models.Message, error) {
	// Two patterns to check both sent and received messages
	patternSent := "last_message:" + username + ":*"
	patternReceived := "last_message:*:" + username
	keysSent, err := s.RedisClient.Keys(ctx, patternSent).Result()
	if err != nil {
		return nil, err
	}
	keysReceived, err := s.RedisClient.Keys(ctx, patternReceived).Result()
	if err != nil {
		return nil, err
	}

	allKeys := append(keysSent, keysReceived...)
	conversations := make(map[string]models.Message)

	for _, key := range allKeys {
		parts := strings.Split(key, ":")
		if len(parts) != 3 {
			continue
		}

		// Determine the conversation partner:
		// If the username is the first part of the key, the partner is the second part; otherwise, it is the first part.
		var partner string
		if parts[1] == username {
			partner = parts[2]
		} else {
			partner = parts[1]
		}

		// Retrieve the message from Redis
		msgJSON, err := s.RedisClient.Get(ctx, key).Result()
		if err != nil {
			continue
		}
		var msg models.Message
		if err := json.Unmarshal([]byte(msgJSON), &msg); err != nil {
			continue
		}

		// If there are two keys for the same partner, select the more recent one.
		if existing, ok := conversations[partner]; ok {
			if msg.Time.After(existing.Time) {
				conversations[partner] = msg
			}
		} else {
			conversations[partner] = msg
		}
	}

	return conversations, nil
}
