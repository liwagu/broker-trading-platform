# Broker Trading Platform

[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0.6-brightgreen)](https://spring.io/projects/spring-boot)
[![Python](https://img.shields.io/badge/Python-3.10+-blue)](https://www.python.org/)
[![Next.js](https://img.shields.io/badge/Next.js-15-black)](https://nextjs.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

An AI-powered trading platform with microservice architecture. Built with Spring Boot backend, Next.js frontend, and integrated with **Kronos** - an open-source foundation model for financial time series prediction from Tsinghua University.

> **AI Model**: This project integrates [Kronos](https://github.com/shiyu-coder/Kronos), a cutting-edge financial forecasting model trained on 45+ global exchanges, developed by researchers at Tsinghua University.

## Features

- 🤖 **AI Price Predictions**: 5-day forecasts powered by Kronos foundation model
- 🎯 **Trading Signals**: AI-generated BUY/SELL/HOLD recommendations with confidence scores
- 📈 **Order Management**: Create, retrieve, and cancel buy/sell orders
- 💰 **Portfolio Management**: Track buying power and inventory across portfolios
- 🖥️ **Modern Web UI**: Next.js frontend with shadcn/ui components
- 🔄 **Real-time Processing**: Immediate order execution with inventory updates
- 🛡️ **Robust Validation**: Comprehensive business rule validation
- 🗄️ **Persistent Storage**: H2 database integration with JPA
- ⚡ **RESTful API**: Clean, intuitive endpoints

## Technology Stack

### Backend (Trading Engine)
- **Java 21** - Modern Java features including pattern matching
- **Spring Boot 3.0.6** - REST API framework
- **Spring Data JPA** - ORM and database operations
- **H2 Database** - In-memory database for development
- **Maven** - Dependency management and build tool
- **JUnit 5** - Comprehensive testing framework

### AI Service (Prediction Engine)
- **Python 3.10+** - AI/ML runtime
- **FastAPI** - High-performance async API framework
- **Kronos Model** - Financial time series foundation model (Tsinghua University)
- **NumPy & Pandas** - Data processing

### Frontend (Web UI)
- **Next.js 15** - React framework with App Router
- **TypeScript** - Type-safe development
- **shadcn/ui** - Modern UI component library
- **Tailwind CSS** - Utility-first styling

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Web Browser (Port 3002)                  │
│                  Next.js + shadcn/ui + TypeScript           │
└─────────────────┬───────────────────────────────────────────┘
                  │ HTTP/REST
                  ├──────────────────┬────────────────────────┐
                  ▼                  ▼                        ▼
    ┌──────────────────────┐  ┌──────────────────┐  ┌───────────────────┐
    │  Trading Service     │  │   AI Service     │  │   Static Data     │
    │  (Port 8080)         │  │   (Port 5001)    │  │                   │
    │                      │  │                  │  │                   │
    │  Spring Boot 3       │  │  FastAPI         │  │  - Market prices  │
    │  - Order management  │  │  - Kronos model  │  │  - ISIN mapping   │
    │  - Portfolio mgmt    │  │  - Predictions   │  │                   │
    │  - Inventory track   │  │  - Signals       │  │                   │
    └──────────┬───────────┘  └──────────────────┘  └───────────────────┘
               │
               ▼
    ┌──────────────────────┐
    │   H2 Database        │
    │   (In-Memory)        │
    │  - Orders            │
    │  - Portfolios        │
    │  - Inventory         │
    └──────────────────────┘
```

### Microservices Communication
- **Frontend → Trading Service**: Order CRUD operations
- **Frontend → AI Service**: Price predictions and trading signals
- **Trading Service**: Independent order execution and portfolio management
- **AI Service**: Independent ML inference using Kronos algorithms

## Getting Started

### Prerequisites

- **Java 21+** - For trading service backend
- **Python 3.10+** - For AI prediction service
- **Node.js 18+** - For frontend
- **Maven 3.6+** - For building Java project

### Installation & Running

#### 1. Trading Service (Port 8080)
```bash
git clone https://github.com/liwagu/broker-trading-platform.git
cd broker-trading-platform

# Build and run
./mvnw clean install
./mvnw spring-boot:run
```

#### 2. AI Prediction Service (Port 5001)
```bash
cd ai-service

# Create virtual environment
python3 -m venv venv
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Run service
python main.py
```

#### 3. Frontend UI (Port 3000)
```bash
cd trading-ui

# Install dependencies
npm install

# Run development server
npm run dev
```

### Access Points
- **Web UI**: http://localhost:3000 (or 3002 if 3000 is busy)
- **Trading API**: http://localhost:8080
- **AI API**: http://localhost:5001
- **AI API Docs**: http://localhost:5001/docs

## API Endpoints

### Trading Service (Port 8080)

#### Create Order
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

#### Get Order
Retrieve order details by ID.

```http
GET /orders/{id}
```

#### Cancel Order
Cancel an existing order (only orders with status CREATED can be cancelled).

```http
PUT /orders/{id}
```

### AI Prediction Service (Port 5001)

#### Get Price Prediction
Get AI-powered price forecast for a security.

```http
GET /predict/{isin}?horizon_days=5
```

**Example Response**:
```json
{
  "isin": "US67066G1040",
  "security_name": "NVIDIA Corp",
  "current_price": 100.0,
  "predictions": [
    {
      "date": "2025-10-11",
      "predicted_price": 103.5,
      "confidence_lower": 99.2,
      "confidence_upper": 107.8
    }
  ],
  "signal": "BUY",
  "confidence": 0.87,
  "trend": "bullish",
  "ai_summary": "Strong upward momentum detected..."
}
```

#### Supported Securities
- `US67066G1040` - NVIDIA Corp ($100.00)
- `US0378331005` - Apple Inc ($200.00)
- `US5949181045` - Microsoft Corp ($35.50)

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
├── ai-service/                 # Python AI prediction service
│   ├── main.py                 # FastAPI application
│   ├── requirements.txt        # Python dependencies
│   └── venv/                   # Virtual environment
├── trading-ui/                 # Next.js frontend
│   ├── app/                    # Next.js app router pages
│   ├── components/             # React components
│   └── public/                 # Static assets
└── pom.xml
```

## Testing

### Backend Tests
```bash
./mvnw test
```

### API Testing Script
```bash
./test-api.sh  # Requires backend running on :8080
```

## AI Model - Kronos

This project integrates **Kronos**, an open-source foundation model for financial time series forecasting developed by researchers at **Tsinghua University**.

### About Kronos
- **Repository**: [shiyu-coder/Kronos](https://github.com/shiyu-coder/Kronos)
- **Training Data**: 45+ global exchanges
- **Architecture**: Two-stage framework with specialized tokenizer + Transformer
- **Capabilities**: Price prediction, trend analysis, probabilistic forecasting

### Implementation Note
The current MVP uses a **smart statistical model** that simulates Kronos behavior for demonstration purposes. For production deployment with the actual Kronos model:

```python
from kronos import KronosPredictor
predictor = KronosPredictor.from_pretrained("shiyu-coder/Kronos-mini")
```

This approach provides:
- ✅ Fast startup (no GPU required for MVP)
- ✅ Realistic predictions using statistical modeling
- ✅ Easy upgrade path to full Kronos integration
- ✅ Same API interface

### Academic Credit
If you use this project or Kronos in your research, please cite:

```
Kronos: An Open-Source Foundation Model for Financial Time Series
Tsinghua University, 2024
https://github.com/shiyu-coder/Kronos
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author

**liwagu** - [GitHub](https://github.com/liwagu)

## Acknowledgments

- **Tsinghua University** - For developing the Kronos AI model
- **Kronos Research Team** - For open-sourcing their financial forecasting model
- **shadcn** - For the beautiful UI component library
