package main

import (
	"log"
	"net/http"

	"chat-service/internal/config"
	"chat-service/internal/handler"
	"chat-service/internal/kafka"
	"chat-service/internal/service"
)

func main() {
	cfg := config.LoadConfig()

	config.InitializeRedisClient()

	kafkaProducer := kafka.NewKafkaProducer([]string{cfg.KafkaBrokers}, cfg.KafkaTopic)

	chatService := service.NewChatService(kafkaProducer, config.RedisClient)

	// Create http endpoints
	chatHandler := handler.NewChatHandler(chatService)

	http.HandleFunc("/chat/send", chatHandler.SendMessage)
	http.HandleFunc("/chat/last-messages", chatHandler.GetLastMessages)
	http.HandleFunc("/chat/warmup", chatHandler.WarmupLastMessages)

	log.Println("Chat server started on port ", cfg.ServerPort)
	log.Fatal(http.ListenAndServe(":"+cfg.ServerPort, nil))
}
