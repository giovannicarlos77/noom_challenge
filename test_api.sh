#!/bin/bash

# Sleep Logger API Test Script
# This script tests all the endpoints of the Sleep Logger API

BASE_URL="http://localhost:8080/api"

echo "=== Sleep Logger API Test Script ==="
echo "Base URL: $BASE_URL"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print test results
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ PASS${NC}: $2"
    else
        echo -e "${RED}✗ FAIL${NC}: $2"
    fi
}

# Function to make HTTP requests and check status
test_endpoint() {
    local method=$1
    local url=$2
    local data=$3
    local expected_status=$4
    local description=$5
    
    echo -e "${YELLOW}Testing:${NC} $description"
    
    if [ -n "$data" ]; then
        response=$(curl -s -w "%{http_code}" -X $method \
            -H "Content-Type: application/json" \
            -d "$data" \
            "$url")
    else
        response=$(curl -s -w "%{http_code}" -X $method "$url")
    fi
    
    status_code="${response: -3}"
    body="${response%???}"
    
    if [ "$status_code" = "$expected_status" ]; then
        print_result 0 "$description (Status: $status_code)"
        if [ -n "$body" ] && [ "$body" != "null" ]; then
            echo "Response: $body" | jq . 2>/dev/null || echo "Response: $body"
        fi
    else
        print_result 1 "$description (Expected: $expected_status, Got: $status_code)"
        echo "Response: $body"
    fi
    
    echo ""
    return $([ "$status_code" = "$expected_status" ] && echo 0 || echo 1)
}

echo "=== 1. Testing User Management ==="

# Create a test user
USER_DATA='{"username": "testuser", "email": "test@example.com"}'
test_endpoint "POST" "$BASE_URL/users" "$USER_DATA" "201" "Create test user"

# Get user by username to retrieve ID
echo -e "${YELLOW}Getting user ID...${NC}"
USER_RESPONSE=$(curl -s "$BASE_URL/users/by-username/testuser")
USER_ID=$(echo $USER_RESPONSE | jq -r '.id' 2>/dev/null)

if [ "$USER_ID" = "null" ] || [ -z "$USER_ID" ]; then
    echo -e "${RED}Failed to get user ID. Exiting.${NC}"
    exit 1
fi

echo -e "${GREEN}User ID: $USER_ID${NC}"
echo ""

# Test getting user by ID
test_endpoint "GET" "$BASE_URL/users/$USER_ID" "" "200" "Get user by ID"

# Test getting all users
test_endpoint "GET" "$BASE_URL/users" "" "200" "Get all users"

echo "=== 2. Testing Sleep Log Management ==="

# Create sleep log for today
TODAY=$(date +%Y-%m-%d)
SLEEP_LOG_DATA="{
    \"sleepDate\": \"$TODAY\",
    \"bedtime\": \"22:30:00\",
    \"wakeTime\": \"07:00:00\",
    \"morningFeeling\": \"GOOD\"
}"
test_endpoint "POST" "$BASE_URL/users/$USER_ID/sleep" "$SLEEP_LOG_DATA" "201" "Create sleep log for today"

# Create sleep log for yesterday
YESTERDAY=$(date -d "yesterday" +%Y-%m-%d)
SLEEP_LOG_YESTERDAY="{
    \"sleepDate\": \"$YESTERDAY\",
    \"bedtime\": \"23:00:00\",
    \"wakeTime\": \"08:00:00\",
    \"morningFeeling\": \"OK\"
}"
test_endpoint "POST" "$BASE_URL/users/$USER_ID/sleep" "$SLEEP_LOG_YESTERDAY" "201" "Create sleep log for yesterday"

# Create sleep log for 2 days ago
TWO_DAYS_AGO=$(date -d "2 days ago" +%Y-%m-%d)
SLEEP_LOG_TWO_DAYS="{
    \"sleepDate\": \"$TWO_DAYS_AGO\",
    \"bedtime\": \"22:00:00\",
    \"wakeTime\": \"06:30:00\",
    \"morningFeeling\": \"BAD\"
}"
test_endpoint "POST" "$BASE_URL/users/$USER_ID/sleep" "$SLEEP_LOG_TWO_DAYS" "201" "Create sleep log for 2 days ago"

# Test duplicate sleep log (should fail)
test_endpoint "POST" "$BASE_URL/users/$USER_ID/sleep" "$SLEEP_LOG_DATA" "400" "Try to create duplicate sleep log (should fail)"

# Get last night's sleep
test_endpoint "GET" "$BASE_URL/users/$USER_ID/sleep/last-night" "" "200" "Get last night's sleep"

# Get 30-day statistics
test_endpoint "GET" "$BASE_URL/users/$USER_ID/sleep/statistics/30-days" "" "200" "Get 30-day statistics"

echo "=== 3. Testing Error Cases ==="

# Test with non-existent user
test_endpoint "GET" "$BASE_URL/users/99999" "" "404" "Get non-existent user"
test_endpoint "POST" "$BASE_URL/users/99999/sleep" "$SLEEP_LOG_DATA" "400" "Create sleep log for non-existent user"
test_endpoint "GET" "$BASE_URL/users/99999/sleep/last-night" "" "400" "Get last night's sleep for non-existent user"

# Test duplicate username
DUPLICATE_USER='{"username": "testuser", "email": "duplicate@example.com"}'
test_endpoint "POST" "$BASE_URL/users" "$DUPLICATE_USER" "409" "Try to create user with duplicate username"

echo "=== Test Summary ==="
echo "All tests completed. Check the results above."
echo "If all tests passed, the API is working correctly!"

