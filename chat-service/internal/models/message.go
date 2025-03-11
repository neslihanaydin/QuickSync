package models

import (
	"encoding/json"
	"fmt"
	"time"
)

type Message struct {
	Sender   string    `json:"sender"`
	Receiver string    `json:"receiver"`
	Text     string    `json:"text"`
	Time     time.Time `json:"timestamp"`
}

// UnmarshalJSON is a custom method to properly parse the timestamp format.
func (m *Message) UnmarshalJSON(data []byte) error {
	type Alias Message
	alias := &struct {
		Time string `json:"timestamp"`
		*Alias
	}{
		Alias: (*Alias)(m),
	}

	if err := json.Unmarshal(data, &alias); err != nil {
		return err
	}

	// Properly parse the timestamp field
	layout := "2006-01-02T15:04:05"
	t, err := time.Parse(layout, alias.Time)
	if err != nil {
		return fmt.Errorf("failed to parse timestamp: %v", err)
	}
	m.Time = t
	return nil
}
