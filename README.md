# 🚀 Crypto Alerter: High-Load Notification System

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green)
![WebFlux](https://img.shields.io/badge/Architecture-Reactive-blue)
![Kafka](https://img.shields.io/badge/Apache-Kafka-black)
![Redis](https://img.shields.io/badge/Redis-Cache-red)
![Docker](https://img.shields.io/badge/DevOps-Docker_AWS-2496ED)

## 📖 Overview

**Crypto Alerter** is a real-time, event-driven monitoring system designed to track cryptocurrency prices from the **Binance WebSocket API** and notify users via **Telegram** when specific price targets are hit.

The project demonstrates a **Reactive Non-blocking Architecture** capable of handling high-frequency data streams. It uses **Apache Kafka** for decoupling the ingestion layer from the processing layer and **Redis** for low-latency state management.

---

## 🏗 Architecture

The system is built using the **Producer-Consumer** pattern with an Event-Driven approach:

1.  **Binance Stream Service:** Connects to Binance WebSocket (Push model) and streams real-time trade data.
2.  **Producer Layer:** Pushes raw price events into **Apache Kafka** (`crypto-prices` topic).
3.  **Consumer Layer (Alert Processor):** Reads the stream, checks conditions against active alerts.
4.  **State Management:**
    * **Redis (Reactive):** Acts as a L2 Cache for active alerts to minimize DB hits (Write-Through pattern).
    * **PostgreSQL (R2DBC):** Persistent storage for users and alert history.
5.  **Notification:** Sends asynchronous notifications via a custom **Telegram Bot** client.

---

## 🛠 Tech Stack

* **Core:** Java 21, Spring Boot 4.x
* **Reactive Stack:** Spring WebFlux, Project Reactor (Mono/Flux), Netty
* **Messaging:** Apache Kafka (Reactor Kafka)
* **Database:** PostgreSQL (R2DBC driver), Liquibase (Schema migration)
* **Caching:** Redis (Reactive Redis Template)
* **DevOps:** Docker, Docker Compose, AWS EC2
* **Integrations:** Binance WebSocket API, Telegram Bot API

---

## ✨ Key Features

* **Non-blocking I/O:** Fully reactive pipeline from DB to HTTP clients.
* **Scalability:** Kafka allows multiple consumers to process price streams in parallel.
* **Reliability:** "Fire-and-Forget" notification pattern with idempotent checks using Redis.
* **Zero-Polling:** Uses WebSockets for market data and Webhooks logic for Telegram (simulated via optimized polling).
* **Cloud Ready:** Fully containerized with Docker Compose, ready for AWS deployment.

---

## 🚀 How to Run

### Prerequisites
* Docker & Docker Compose
* Java 21 (optional, for local run without Docker)
