# QuickSync

QuickSync is a real-time chat application built with a microservices architecture. It enables users to register, log in, and chat with each other while receiving live notifications. The system leverages a combination of modern technologies such as Kafka, Redis, PostgreSQL, and WebSockets to ensure efficient message handling and delivery.

## Overview

QuickSync is designed to offer seamless communication between users through a robust, distributed backend. The application comprises several independent services, each responsible for a specific functionality, which together create a scalable and maintainable chat platform.

## Functional Requirements
* User Registration & Authentication: Users can sign up, log in, and manage sessions securely.
* Real-Time Messaging: Both notifications and messages are delivered instantly to ensure an engaging chat experience.
* Microservices Architecture: Each service operates independently while collaborating to deliver a complete solution.

## Architecture & Services

QuickSync is divided into multiple microservices, each implemented using different technologies to best serve its purpose:

### User Service (Kotlin Spring)
<b>Responsibilities:</b>
* User registration and login.
* Session management including JWT validation with refresh token.
* OAuth 2.0 integration for Google sign-in.

### Chat Service (Go)
<b>Responsibilities:</b>
* Handling the sending of messages.
* Publishing events to Kafka.
* Saving recent messages in Redis for quick access.

### Messaging Service (Java Spring)
<b>Responsibilities:</b>
* Consuming events from Kafka.
* Persisting messages in a PostgreSQL database.
* Retrieving a user’s message history from the database upon requested.

### Notification Service (Java Spring)
<b>Responsibilities:</b>
* Managing notifications.
* Consuming Kafka events and relaying real-time notifications through WebSocket connections.

### Gateway Service (Java Spring)
<b>Responsibilities:</b>
* Acting as the single entry point for frontend requests.
* Routing incoming requests to the appropriate backend services.

### Frontend (React)
<b>Responsibilities:</b>
* Providing user interfaces for login, registration, and chat interactions.
* Displaying chat lists and message histories by fetching data through the Gateway Service.
* Sending messages and listening to real-time notifications via WebSockets.

## Technology Stack

* <b>Backend Languages:</b> Kotlin, Go, Java
* <b>Frameworks:</b> Spring (for Kotlin and Java services)
* <b>Message Broker:</b> Kafka (for real-time message/event distribution)
* <b>Caching:</b> Redis (for caching frequently accessed messages)
* <b>Database:</b> PostgreSQL (hosted on RDS for user data and message history)
* <b>Frontend:</b> React (for a responsive and dynamic user interface)
* <b>WebSocket:</b> Used for delivering live notifications

## Note
This project is still under development. The functionality described above reflects the current state of the application. As new features are added and the project evolves, this README will be updated accordingly.
