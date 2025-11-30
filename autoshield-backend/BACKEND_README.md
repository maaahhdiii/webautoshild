# AutoShield Backend API

Production-ready REST API for the AutoShield Security Monitoring System. Built with **Java 21**, **Spring Boot 3.2**, and designed to integrate with Proxmox hypervisors and Python AI security services.

## 🎯 Features

- **Real-time Security Monitoring**: Collect and analyze system metrics from Proxmox
- **Alert Management**: Track, filter, and respond to security events
- **Automated Scanning**: Trigger network security scans via Python AI integration
- **Firewall Management**: Block/unblock IPs with temporary or permanent rules
- **Health Monitoring**: Check status of all integrated services
- **RESTful API**: Comprehensive endpoints with OpenAPI/Swagger documentation
- **Scheduled Tasks**: Automatic metrics collection and rule expiration

## 📋 Prerequisites

- **Java 21** or higher
- **Maven 3.8+**
- **Proxmox VE** (optional, for production)
- **Python AI Service** (optional, for security scanning)

## 🚀 Quick Start

### 1. Clone and Build

```powershell
cd autoshield-backend
mvn clean install
```

### 2. Configure Environment

Create `.env` file or set environment variables:

```properties
PROXMOX_URL=https://192.168.100.50:8006
PROXMOX_TOKEN=your-proxmox-api-token
PYTHON_AI_URL=http://192.168.100.51:8000
```

### 3. Run the Application

```powershell
mvn spring-boot:run
```

The API will start on **http://localhost:8080**

### 4. Access Swagger UI

Open browser: **http://localhost:8080/swagger-ui.html**

### 5. Test with Default Credentials

```
Username: admin
Password: admin123
```

## 📁 Project Structure

```
autoshield-backend/
├── src/main/java/com/autoshield/
│   ├── controller/          # REST API endpoints
│   │   ├── AlertController.java
│   │   ├── ScanController.java
│   │   ├── FirewallController.java
│   │   ├── MetricsController.java
│   │   ├── WebhookController.java
│   │   └── HealthController.java
│   ├── service/             # Business logic
│   │   ├── AlertService.java
│   │   ├── ProxmoxApiService.java
│   │   ├── PythonAiClient.java
│   │   ├── FirewallService.java
│   │   └── MetricsCollectionService.java
│   ├── repository/          # Data access layer
│   │   ├── AlertRepository.java
│   │   ├── ScanResultRepository.java
│   │   ├── SystemMetricRepository.java
│   │   └── FirewallRuleRepository.java
│   ├── entity/              # JPA entities
│   │   ├── Alert.java
│   │   ├── ScanResult.java
│   │   ├── SystemMetric.java
│   │   └── FirewallRule.java
│   ├── dto/                 # Data transfer objects
│   ├── config/              # Configuration classes
│   │   ├── SecurityConfig.java
│   │   ├── RestClientConfig.java
│   │   └── OpenApiConfig.java
│   └── exception/           # Exception handling
└── src/main/resources/
    └── application.yml      # Configuration
```

## 🔌 API Endpoints

### Alerts

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/alerts` | Get all alerts (paginated) | Basic |
| GET | `/api/v1/alerts/{id}` | Get alert by ID | Basic |
| PATCH | `/api/v1/alerts/{id}/status` | Update alert status | Basic |
| GET | `/api/v1/alerts/recent?hours=24` | Get recent alerts | Basic |
| GET | `/api/v1/alerts/stats` | Get alert statistics | Basic |

### Scans

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/scan/trigger` | Trigger security scan | Admin |
| GET | `/api/v1/scan/{scanId}` | Get scan result | Basic |
| GET | `/api/v1/scan/history` | Get scan history | Basic |

### Firewall

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/firewall/block` | Block an IP address | Admin |
| DELETE | `/api/v1/firewall/unblock/{ip}` | Unblock an IP | Admin |
| GET | `/api/v1/firewall/rules` | Get active rules | Admin |

### Metrics

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/metrics/current` | Get current metrics | Basic |
| GET | `/api/v1/metrics/history?hours=24` | Get metrics history | Basic |
| GET | `/api/v1/metrics/average?hours=24` | Get average metrics | Basic |

### Health

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/health` | System health check | Basic |

### Webhooks

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/webhook/python` | Receive Python AI events | None |
| POST | `/api/v1/webhook/test` | Test webhook | None |

## 📊 Example API Calls

### Trigger a Scan

```bash
curl -X POST http://localhost:8080/api/v1/scan/trigger \
  -H "Content-Type: application/json" \
  -u admin:admin123 \
  -d '{
    "targetIp": "192.168.1.100",
    "scanType": "quick",
    "description": "Routine security check"
  }'
```

### Block an IP Address

```bash
curl -X POST http://localhost:8080/api/v1/firewall/block \
  -H "Content-Type: application/json" \
  -u admin:admin123 \
  -d '{
    "ipAddress": "192.168.1.200",
    "reason": "Brute force attack detected",
    "durationMinutes": 60,
    "permanent": false
  }'
```

### Get Recent Alerts

```bash
curl -X GET "http://localhost:8080/api/v1/alerts/recent?hours=24" \
  -u admin:admin123
```

## ⚙️ Configuration

Edit `src/main/resources/application.yml`:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:file:./data/autoshield
    # For PostgreSQL:
    # url: jdbc:postgresql://localhost:5432/autoshield
    # username: postgres
    # password: password

proxmox:
  api:
    url: https://192.168.100.50:8006
    token: ${PROXMOX_TOKEN}

python:
  ai:
    url: http://192.168.100.51:8000
    timeout: 30000

autoshield:
  metrics:
    retention-days: 7
  security:
    max-failed-logins: 5
```

## 🗄️ Database

### Development (H2)
- Embedded database stored in `./data/autoshield.mv.db`
- H2 Console: **http://localhost:8080/h2-console**
- JDBC URL: `jdbc:h2:file:./data/autoshield`

### Production (PostgreSQL)

1. Create database:
```sql
CREATE DATABASE autoshield;
```

2. Update `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/autoshield
    username: postgres
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

## 🔒 Security

### Authentication
- HTTP Basic Authentication
- Default users defined in `SecurityConfig.java`
- **Production**: Replace with database-backed user store

### Users

| Username | Password | Roles |
|----------|----------|-------|
| admin | admin123 | ADMIN, USER |
| viewer | viewer123 | USER |

### Change Passwords

In `SecurityConfig.java`:
```java
@Bean
public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
    UserDetails admin = User.builder()
            .username("admin")
            .password(passwordEncoder.encode("NEW_PASSWORD"))
            .roles("ADMIN", "USER")
            .build();
    // ...
}
```

## 📈 Scheduled Tasks

| Task | Schedule | Description |
|------|----------|-------------|
| Metrics Collection | Every 30 seconds | Fetch Proxmox metrics |
| Firewall Rule Expiration | Every minute | Deactivate expired rules |
| Metrics Cleanup | Daily at 2 AM | Delete old metrics |

## 🧪 Testing

```powershell
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## 🐳 Docker Deployment

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/autoshield-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run:
```powershell
mvn clean package -DskipTests
docker build -t autoshield-backend .
docker run -p 8080:8080 `
  -e PROXMOX_TOKEN=your-token `
  -e PYTHON_AI_URL=http://python-ai:8000 `
  autoshield-backend
```

## 🔍 Monitoring

### Actuator Endpoints

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Application health |
| `/actuator/info` | Application info |
| `/actuator/metrics` | Application metrics |

## 🤝 Integration with Python AI

The backend communicates with a Python AI service for:
- Security scanning (Nmap, Nikto)
- Firewall rule enforcement
- Anomaly detection

### Expected Python AI Endpoints

- `POST /api/v1/scan` - Trigger scan
- `POST /api/v1/firewall/block` - Block IP
- `DELETE /api/v1/firewall/unblock/{ip}` - Unblock IP
- `GET /api/v1/health` - Health check

### Webhook Integration

Python AI can send events to:
```
POST http://autoshield-backend:8080/api/v1/webhook/python
{
  "eventType": "brute_force_detected",
  "severity": "HIGH",
  "sourceIp": "192.168.1.100",
  "description": "Multiple failed SSH attempts"
}
```

## 📝 Logging

Logs are written to:
- Console (INFO level)
- `logs/autoshield.log` (all levels)

Configure in `application.yml`:
```yaml
logging:
  level:
    com.autoshield: DEBUG
    org.springframework: INFO
  file:
    name: logs/autoshield.log
```

## 🚀 Production Checklist

- [ ] Change default passwords
- [ ] Switch to PostgreSQL
- [ ] Configure real Proxmox API token
- [ ] Set up HTTPS/TLS
- [ ] Configure backup strategy
- [ ] Set up monitoring (Prometheus, Grafana)
- [ ] Review security configurations
- [ ] Set up log aggregation
- [ ] Configure CORS for production domains
- [ ] Enable CSRF protection for non-REST endpoints

## 📄 License

MIT License - See LICENSE file for details

## 👥 Support

For issues or questions, please open a GitHub issue or contact the development team.
