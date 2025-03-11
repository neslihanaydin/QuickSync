package config

import (
	"context"
	"time"

	"github.com/go-redis/redis/v8"
)

var RedisClient *redis.Client

func InitializeRedisClient() {
	RedisClient = redis.NewClient(&redis.Options{
		Addr:        getEnv("REDIS_ADDR", "localhost:6379"),
		Password:    getEnv("REDIS_PASSWORD", ""),
		DB:          0, // default DB
		DialTimeout: 5 * time.Second,
	})

	// Check the connection
	ctx, cancel := context.WithTimeout(context.Background(), 2*time.Second)
	defer cancel()
	if err := RedisClient.Ping(ctx).Err(); err != nil {
		panic("Failed to establish Redis connection: " + err.Error())
	}
}
