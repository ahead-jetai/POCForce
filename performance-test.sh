#!/bin/bash

# Performance Test Script for POCForce API
# Tests POC list and detail endpoints with various loads

BASE_URL="http://localhost:8080/api/v1"
RESULTS_FILE="performance-test-results.txt"

echo "POCForce Performance Test Results" > $RESULTS_FILE
echo "==================================" >> $RESULTS_FILE
echo "Date: $(date)" >> $RESULTS_FILE
echo "" >> $RESULTS_FILE

# Function to measure response time
measure_time() {
    local url=$1
    local description=$2
    
    echo "Testing: $description"
    echo "Testing: $description" >> $RESULTS_FILE
    
    # Measure response time
    local start=$(date +%s%3N)
    local response=$(curl -s -o /dev/null -w "%{http_code},%{time_total}" "$url")
    local end=$(date +%s%3N)
    
    local http_code=$(echo $response | cut -d',' -f1)
    local time_total=$(echo $response | cut -d',' -f2)
    
    echo "  HTTP Status: $http_code" | tee -a $RESULTS_FILE
    echo "  Response Time: ${time_total}s" | tee -a $RESULTS_FILE
    echo "" >> $RESULTS_FILE
}

# Function to create test data
create_test_data() {
    local count=$1
    echo "Creating $count test POCs..." | tee -a $RESULTS_FILE
    
    # Get an existing user (from DataInitializer seed data)
    if command -v jq &> /dev/null; then
        USER_ID=$(curl -s "$BASE_URL/users" | jq -r '.[0].id // empty' 2>/dev/null)
    else
        USER_ID=$(curl -s "$BASE_URL/users" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
    fi
    
    if [ -z "$USER_ID" ]; then
        echo "ERROR: Could not find any existing users" | tee -a $RESULTS_FILE
        exit 1
    fi
    
    echo "Using existing user with ID: $USER_ID"
    
    # Create POCs
    for i in $(seq 1 $count); do
        curl -s -X POST "$BASE_URL/pocs" \
            -H "Content-Type: application/json" \
            -d "{
                \"customerName\": \"Test Customer $i\",
                \"title\": \"Performance Test POC $i\",
                \"description\": \"Performance test POC for load testing\",
                \"dealValue\": 50000.00,
                \"projectedCloseDate\": \"2025-12-31\",
                \"kickoffDate\": \"2025-11-20\",
                \"endDate\": \"2025-12-15\",
                \"ownerId\": $USER_ID
            }" > /dev/null
        
        if [ $((i % 100)) -eq 0 ]; then
            echo "Created $i POCs..."
        fi
    done
    
    echo "Created $count POCs successfully" | tee -a $RESULTS_FILE
    echo "" >> $RESULTS_FILE
}

# Function to test concurrent requests
test_concurrent_requests() {
    local url=$1
    local concurrent=$2
    local description=$3
    
    echo "Testing: $description with $concurrent concurrent requests" | tee -a $RESULTS_FILE
    
    local start=$(date +%s)
    local start_ns=$(date +%N)
    
    # Run concurrent requests
    for i in $(seq 1 $concurrent); do
        curl -s "$url" > /dev/null &
    done
    
    # Wait for all background jobs to complete
    wait
    
    local end=$(date +%s)
    local end_ns=$(date +%N)
    
    # Calculate duration in seconds
    local duration=$((end - start))
    local duration_ns=$((end_ns - start_ns))
    if [ $duration_ns -lt 0 ]; then
        duration=$((duration - 1))
        duration_ns=$((duration_ns + 1000000000))
    fi
    local duration_sec=$(echo "scale=3; $duration + $duration_ns / 1000000000" | bc)
    
    echo "  Total Time: ${duration_sec}s for $concurrent concurrent requests" | tee -a $RESULTS_FILE
    echo "  Average Time: $(echo "scale=3; $duration_sec / $concurrent" | bc)s per request" | tee -a $RESULTS_FILE
    echo "" >> $RESULTS_FILE
}

echo "Starting performance tests..."
echo ""

# Test 1: Create test data
echo "Step 1: Creating test data (1000 POCs)"
create_test_data 1000

# Test 2: Test POC list endpoint with 1000 POCs
echo "Step 2: Testing POC list endpoint (NFR-1: must respond in < 2s)"
measure_time "$BASE_URL/pocs" "POC List Endpoint (1000 POCs)"

# Test 3: Test POC list endpoint with filters
echo "Step 3: Testing POC list endpoint with filters"
measure_time "$BASE_URL/pocs?phase=DISCOVERY" "POC List with Phase Filter"
measure_time "$BASE_URL/pocs?status=ACTIVE" "POC List with Status Filter"

# Test 4: Test POC detail endpoint
echo "Step 4: Testing POC detail endpoint (NFR-2: must respond in < 500ms)"
# Get first POC ID - use jq for proper JSON parsing, fallback to grep
if command -v jq &> /dev/null; then
    FIRST_POC_ID=$(curl -s "$BASE_URL/pocs" | jq -r '.[0].id // empty' 2>/dev/null)
else
    FIRST_POC_ID=$(curl -s "$BASE_URL/pocs" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
fi

if [ -n "$FIRST_POC_ID" ]; then
    measure_time "$BASE_URL/pocs/$FIRST_POC_ID" "POC Detail Endpoint"
else
    echo "Could not find POC ID for detail test" | tee -a $RESULTS_FILE
fi

# Test 5: Test concurrent POC list requests
echo "Step 5: Testing concurrent requests (NFR-4: support 10 concurrent users)"
test_concurrent_requests "$BASE_URL/pocs" 10 "POC List Endpoint"

echo ""
echo "Performance tests completed!"
echo "Results saved to: $RESULTS_FILE"
echo ""
echo "=== Summary ==="
cat $RESULTS_FILE
