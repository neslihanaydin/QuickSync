package config

import (
	"os"
)

type Config struct {
	KafkaBrokers string // Addresses of Kafka brokers (e.g., "localhost:9092")
	KafkaTopic   string // Name of the Kafka topic where messages will be sent (e.g., "chat-messages")
	ServerPort   string // Port on which the HTTP server will run (e.g., "8080")
}

func LoadConfig() *Config {
	return &Config{
		KafkaBrokers: getEnv("KAFKA_BROKERS", "localhost:9092"),
		KafkaTopic:   getEnv("KAFKA_TOPIC", "chat-messages"),
		ServerPort:   getEnv("SERVER_PORT", "8084"),
	}
}

func getEnv(key, defaultValue string) string {
	if value, exists := os.LookupEnv(key); exists {
		return value
	}
	return defaultValue
}
