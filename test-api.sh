#!/bin/bash

# API Testing Script for Trading Platform
# Run Spring Boot first: ./mvnw spring-boot:run

API_URL="http://localhost:8080"

echo "=== Trading API Test Suite ==="
echo ""

# Test 1: Create BUY Order (Success)
echo "TEST 1: Create BUY Order (should succeed)"
curl -X POST "$API_URL/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "portfolioId": "portfolio-id-1",
    "isin": "US67066G1040",
    "side": "BUY",
    "quantity": 10.00
  }' | json_pp
echo ""
echo "Expected: Order created with status CREATED"
echo "---"

# Test 2: Create BUY Order (Insufficient Funds)
echo "TEST 2: Create BUY Order with insufficient funds"
curl -X POST "$API_URL/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "portfolioId": "portfolio-id-1",
    "isin": "US67066G1040",
    "side": "BUY",
    "quantity": 500.00
  }' | json_pp
echo ""
echo "Expected: 400 error - Insufficient buying power"
echo "---"

# Test 3: Get Order by ID
echo "TEST 3: Get Order by ID"
curl -X GET "$API_URL/orders/1" \
  -H "Content-Type: application/json" | json_pp
echo ""
echo "Expected: Order details returned"
echo "---"

# Test 4: Get Non-existent Order
echo "TEST 4: Get non-existent order"
curl -X GET "$API_URL/orders/999" \
  -H "Content-Type: application/json" | json_pp
echo ""
echo "Expected: 404 error - Order not found"
echo "---"

# Test 5: Create SELL Order (Success)
echo "TEST 5: Create SELL Order"
# First create a BUY order to have inventory
curl -X POST "$API_URL/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "portfolioId": "portfolio-id-2",
    "isin": "US0378331005",
    "side": "BUY",
    "quantity": 5.00
  }' > /dev/null 2>&1

# Then sell some shares
curl -X POST "$API_URL/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "portfolioId": "portfolio-id-2",
    "isin": "US0378331005",
    "side": "SELL",
    "quantity": 3.00
  }' | json_pp
echo ""
echo "Expected: SELL order created"
echo "---"

# Test 6: SELL Order (Insufficient Inventory)
echo "TEST 6: SELL Order with insufficient inventory"
curl -X POST "$API_URL/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "portfolioId": "portfolio-id-3",
    "isin": "US5949181045",
    "side": "SELL",
    "quantity": 100.00
  }' | json_pp
echo ""
echo "Expected: 400 error - Insufficient inventory"
echo "---"

# Test 7: Cancel Order
echo "TEST 7: Cancel an order"
# Create an order first
ORDER_RESPONSE=$(curl -s -X POST "$API_URL/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "portfolioId": "portfolio-id-4",
    "isin": "US67066G1040",
    "side": "BUY",
    "quantity": 2.00
  }')

ORDER_ID=$(echo $ORDER_RESPONSE | grep -o '"id":[0-9]*' | cut -d: -f2)

curl -X PUT "$API_URL/orders/$ORDER_ID" \
  -H "Content-Type: application/json" | json_pp
echo ""
echo "Expected: Order status changed to CANCELLED"
echo "---"

# Test 8: Cancel Already Cancelled Order
echo "TEST 8: Cancel already cancelled order"
curl -X PUT "$API_URL/orders/$ORDER_ID" \
  -H "Content-Type: application/json" | json_pp
echo ""
echo "Expected: 400 error - Order cannot be cancelled"
echo "---"

echo "=== Test Suite Complete ==="