# Broker Trading Platform

[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0.6-brightgreen)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A modern, production-ready REST API for stock trading operations built with Spring Boot. This platform provides comprehensive portfolio management, order execution, and inventory tracking capabilities.

## Features

- 📈 **Order Management**: Create, retrieve, and cancel buy/sell orders
- 💰 **Portfolio Management**: Track buying power and inventory across portfolios
- 🔄 **Real-time Processing**: Immediate order execution with inventory updates
- 🛡️ **Robust Validation**: Comprehensive business rule validation
- 🗄️ **Persistent Storage**: H2 database integration with JPA
- ⚡ **RESTful API**: Clean, intuitive endpoints

## Technology Stack

- **Java 21** - Modern Java features including pattern matching
- **Spring Boot 3.0.6** - Latest Spring framework
- **Spring Data JPA** - ORM and database operations
- **H2 Database** - In-memory database for development
- **Maven** - Dependency management and build tool
- **JUnit 5** - Comprehensive testing framework

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.6+

### Installation

1. Clone the repository
```bash
git clone https://github.com/liwagu/broker-trading-platform.git
cd broker-trading-platform
```

2. Build the project
```bash
./mvnw clean install
```

3. Run the application
```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`

## API Endpoints

### Create Order
Create a new buy or sell order.

```http
POST /orders
Content-Type: application/json

{
  "portfolioId": "portfolio-1",
  "isin": "US67066G1040",
  "side": "BUY",
  "quantity": 10.00
}
```

**Response (200 OK)**:
```json
{
  "id": 1,
  "portfolioId": "portfolio-1",
  "isin": "US67066G1040",
  "side": "BUY",
  "quantity": 10.0,
  "price": 100.0,
  "status": "CREATED"
}
```

### Get Order
Retrieve order details by ID.

```http
GET /orders/{id}
```

### Cancel Order
Cancel an existing order (only orders with status CREATED can be cancelled).

```http
PUT /orders/{id}
```

## Business Rules

### Buy Orders
- Portfolio must have sufficient buying power
- Initial buying power per portfolio: $5,000.00
- Transaction: `buying_power -= (quantity × price)`
- Inventory is increased by purchased quantity

### Sell Orders
- Portfolio must have sufficient inventory
- Transaction: `buying_power += (quantity × price)`
- Inventory is decreased by sold quantity

### Order Cancellation
- Only orders with status `CREATED` can be cancelled
- Cancellation reverses all transactions

## Project Structure

```
broker-trading-platform/
├── src/main/java/io/github/liwagu/trading/
│   ├── api/                    # REST controllers and services
│   ├── domain/                 # Domain models and business logic
│   └── repository/             # Data access layer
└── pom.xml
```

## Testing

```bash
./mvnw test
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author

**liwagu** - [GitHub](https://github.com/liwagu)
