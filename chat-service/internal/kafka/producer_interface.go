package kafka

type Producer interface {
	SendMessage(message string) error
}
