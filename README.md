# Spring Boot Microservices Architecture

A distributed microservices-based application built using **Spring Boot**, **Spring Cloud**, **RabbitMQ**, and *
*multiple databases** (MySQL, PostgreSQL, MongoDB).  
Each service has its own database and configuration is managed **centrally via Spring Cloud Config Server** with *
*dynamic refresh** using **Spring Cloud Bus**.

---

## Architecture Overview

### 🔧 Core Components

| Component                                  | Description                                                                   |
|--------------------------------------------|-------------------------------------------------------------------------------|
| **Spring Boot**                            | Foundation for each microservice                                              |
| **Spring Cloud Config Server**             | Centralized externalized configuration (stored in GitHub)                     |
| **Spring Cloud Bus**                       | Event bus for dynamic configuration refresh across all services               |
| **Spring Cloud Eureka / Discovery Server** | Service registry for locating microservices                                   |
| **Spring Cloud Gateway**                   | API gateway for routing and load balancing                                    |
| **RabbitMQ**                               | Message broker for inter-service communication and config refresh propagation |
| **Databases**                              | Separate DB per service (MySQL, PostgreSQL, MongoDB)                          |

---

## Microservices

| Service             | Description                                                      | Database       |
|---------------------|------------------------------------------------------------------|----------------|
| **config-server**   | Centralized configuration service pulling YAML files from GitHub | —              |
| **eureka-server**   | Service registry for discovery                                   | —              |
| **gateway-service** | Routes external traffic to internal services                     | —              |
| **order-service**   | Handles order creation and management                            | **PostgreSQL** |
| **product-service** | Manages product inventory and catalog                            | **MySQL**      |
| **user-service**    | Manages users and authentication                                 | **MongoDB**    |

---

## Databases

| Service         | DB Engine  | Default URL                                 |
|-----------------|------------|---------------------------------------------|
| order-service   | PostgreSQL | `jdbc:postgresql://localhost:5433/order_db` |
| product-service | MySQL      | `jdbc:mysql://localhost:3307/product_db`    |
| user-service    | MongoDB    | `mongodb://localhost:27017/user_db`         |

Each service defines its datasource in the config repository (GitHub), not in the local `.yml`.

---

Features Summary

1. Centralized Configuration (Spring Cloud Config + GitHub)
2. Dynamic Config Refresh (Spring Cloud Bus + RabbitMQ)
3. Independent Databases per Service
4. Service Discovery via Eureka
5. API Gateway Routing
6. Asynchronous Messaging via RabbitMQ
7. Cloud-ready Architecture

---

## 🌐 Spring Cloud Config Setup

### 1️⃣ Config Server (config-server)

Clone your **GitHub config repo** (example):

```yaml
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/salemshah/s-mc-e-commerce-spring-boot-configs
          clone-on-start: true
