FROM golang:1.21-alpine AS builder
WORKDIR /app
COPY . .
RUN go mod tidy
RUN go build -o go-service ./cmd

FROM alpine:latest
WORKDIR /root/
COPY --from=builder /app/go-service .
CMD ["./go-service"]