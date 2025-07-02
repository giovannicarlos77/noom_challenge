# Sleep Logger API

A REST API for logging and analyzing sleep data, developed with Kotlin, Spring Boot, and PostgreSQL.

## Features

The API supports the following functional requirements:

1. **Create last night’s sleep log**
   - Sleep date (today)
   - Time interval in bed (bedtime and wake-up time)
   - Total time in bed
   - How the user felt in the morning: BAD, OK, or GOOD

2. **Retrieve last night’s sleep information**

3. **Get 30-day averages**
   - Date range for which averages are calculated
   - Average total time in bed
   - Average bedtime and wake-up time
   - Frequency of how the user felt in the morning

## Technologies Used

- **Kotlin** - Programming language
- **Spring Boot 2.7.17** - Web framework
- **Spring JDBC** - Data access
- **PostgreSQL** - Database
- **Flyway** - Database migration
- **JUnit 5** - Unit testing
- **Mockito** - Mocking framework for testing
- **Docker** - Containerization

## Project Structure

```
sleep/
├── src/
│   ├── main/
│   │   ├── kotlin/com/noom/interview/fullstack/sleep/
│   │   │   ├── controller/          # REST Controllers
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── model/               # Data models
│   │   │   ├── repository/          # Data repositories
│   │   │   ├── service/             # Business services
│   │   │   └── SleepApplication.kt  # Main class
│   │   └── resources/
│   │       ├── db/migration/        # Flyway migration scripts
│   │       └── application.properties
│   └── test/                        # Unit tests
├── test_api.sh                     # API test script
└── Sleep_Logger_API.postman_collection.json  # Postman collection
```

## Database Schema

### Table `users`
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

### Table `sleep_logs`
```sql
CREATE TABLE sleep_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    sleep_date DATE NOT NULL,
    bedtime TIME NOT NULL,
    wake_time TIME NOT NULL,
    total_time_in_bed_minutes INTEGER NOT NULL,
    morning_feeling morning_feeling NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_sleep_date UNIQUE (user_id, sleep_date)
);
```

### Enum `morning_feeling`
```sql
CREATE TYPE morning_feeling AS ENUM ('BAD', 'OK', 'GOOD');
```

## API Endpoints

### User Management

#### Create User
```http
POST /api/users
Content-Type: application/json

{
    "username": "testuser",
    "email": "test@example.com"
}
```

**Response (201 Created):**
```json
{
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "createdAt": "2025-01-07T10:00:00Z",
    "updatedAt": "2025-01-07T10:00:00Z"
}
```

#### Get User by ID
```http
GET /api/users/{id}
```

#### Get User by Username
```http
GET /api/users/by-username/{username}
```

#### List All Users
```http
GET /api/users
```

### Sleep Log Management

#### Create Sleep Log
```http
POST /api/users/{userId}/sleep
Content-Type: application/json

{
    "sleepDate": "2025-01-07",
    "bedtime": "22:30:00",
    "wakeTime": "07:00:00",
    "morningFeeling": "GOOD"
}
```

**Response (201 Created):**
```json
{
    "id": 1,
    "userId": 1,
    "sleepDate": "2025-01-07",
    "bedtime": "22:30:00",
    "wakeTime": "07:00:00",
    "totalTimeInBedMinutes": 510,
    "morningFeeling": "GOOD",
    "createdAt": "2025-01-07T10:00:00Z",
    "updatedAt": "2025-01-07T10:00:00Z"
}
```

#### Get Last Night’s Sleep
```http
GET /api/users/{userId}/sleep/last-night
```

#### Get 30-Day Statistics
```http
GET /api/users/{userId}/sleep/statistics/30-days
```

**Response (200 OK):**
```json
{
    "dateRange": {
        "startDate": "2024-12-08",
        "endDate": "2025-01-07"
    },
    "averageTotalTimeInBedMinutes": 495.5,
    "averageBedtime": "22:45:00",
    "averageWakeTime": "07:15:00",
    "morningFeelingFrequencies": {
        "GOOD": 15,
        "OK": 10,
        "BAD": 5
    }
}
```

## How to Run

### Prerequisites
- Docker
- Docker Compose
- Ports 5432 (PostgreSQL) and 8080 (API) must be available

### Run with Docker Compose
```bash
docker-compose up
docker-compose up --build
```

### Run Tests
```bash
cd sleep
./gradlew test
./test_api.sh
```

## Testing

### Unit Tests
Includes comprehensive unit tests for:
- **UserRepository**
- **SleepLogRepository**
- **SleepLogService**

### API Tests
1. **Bash Script** (`test_api.sh`)
2. **Postman Collection** (`Sleep_Logger_API.postman_collection.json`)

## Business Rules

1. **Unique users**
2. **One entry per day**
3. **Auto calculation**
4. **Overnight sleep**
5. **Validation**

## HTTP Status Codes

- **200 OK**
- **201 Created**
- **400 Bad Request**
- **404 Not Found**
- **409 Conflict**

## Commit Structure

- Commits by feature
- Descriptive messages
- No temp/build files

## Next Steps

1. Authentication and authorization
2. More robust validation
3. Pagination
4. Statistics caching
5. Monitoring and metrics
6. Notification API
7. Wearable integration