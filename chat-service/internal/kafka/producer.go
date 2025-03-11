package kafka

import (
	"context"
	"log"
	"time"

	"github.com/segmentio/kafka-go"
)

type KafkaProducer struct {
	Writer *kafka.Writer
}

func NewKafkaProducer(brokers []string, topic string) *KafkaProducer {
	return &KafkaProducer{
		Writer: &kafka.Writer{
			Addr:     kafka.TCP(brokers...),
			Topic:    topic,
			Balancer: &kafka.LeastBytes{},
		},
	}
}

// Send message to the Kafka
func (p *KafkaProducer) SendMessage(message string) error {
	msg := kafka.Message{
		Key:   []byte(time.Now().String()), // Unique key
		Value: []byte(message),
	}

	err := p.Writer.WriteMessages(context.Background(), msg)
	if err != nil {
		log.Printf("Kafka message sending error: %v", err)
		return err
	}

	log.Println("Message successfully sent to Kafka: ", message)
	return nil
}
