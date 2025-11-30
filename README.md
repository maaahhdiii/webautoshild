# 🛡️ AutoShield - Security Monitoring System

A complete, production-ready security monitoring and automated response system for Proxmox home lab environments. Built with **Java 21**, **Spring Boot**, and **Vaadin Flow**.

## 🌟 Overview

AutoShield provides real-time security monitoring, automated threat detection, and centralized management for your Proxmox infrastructure. The system consists of two main components:

1. **Backend API** (Spring Boot) - Core business logic and data management
2. **Frontend Dashboard** (Vaadin Flow) - Modern web interface for monitoring and control

## 📸 Key Features

### 🎯 Real-time Monitoring
- Live system metrics (CPU, RAM, Disk usage)
- Active threat tracking
- Network traffic analysis
- 10-second auto-refresh intervals

### 🚨 Security Alerts
- Centralized alert management
- Severity-based filtering (LOW, MEDIUM, HIGH, CRITICAL)
- Alert status tracking (Active, Resolved, Ignored)
- Detailed alert information with source IP tracking

### 🔍 Security Scanning
- Integration with Python AI for automated scanning
- Support for Quick, Full, and Vulnerability scans
- Scan result tracking with threat scoring
- Historical scan analysis

### 🔥 Firewall Management
- Dynamic IP blocking/unblocking
- Temporary or permanent rules
- Automatic rule expiration
- Audit trail for all firewall changes

### 👥 Role-based Access Control
- **Admin**: Full system control
- **Viewer**: Read-only monitoring access
- Spring Security integration

### 📊 Data Persistence
- H2 database for development
- PostgreSQL support for production
- Automatic data retention policies
- Metrics history for trend analysis

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     User Browser                            │
│                  (http://localhost:8081)                    │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           │ HTTPS
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                  AutoShield UI (Vaadin)                     │
│                    Port: 8081                               │
│  ┌─────────────┐  ┌─────────────┐  ┌──────────────┐       │
│  │  Dashboard  │  │   Alerts    │  │  Security    │       │
│  │    View     │  │    View     │  │   Control    │       │
│  └─────────────┘  └─────────────┘  └──────────────┘       │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           │ REST API (HTTP Basic Auth)
                           ▼
┌─────────────────────────────────────────────────────────────┐
│              AutoShield Backend (Spring Boot)               │
│                    Port: 8080                               │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              REST Controllers                        │  │
│  │  /api/v1/alerts  /api/v1/scan  /api/v1/firewall   │  │
│  └────────────────────┬─────────────────────────────────┘  │
│                       │                                     │
│  ┌────────────────────▼─────────────────────────────────┐  │
│  │              Service Layer                          │  │
│  │  AlertService  FirewallService  MetricsService     │  │
│  └────────────────────┬─────────────────────────────────┘  │
│                       │                                     │
│  ┌────────────────────▼─────────────────────────────────┐  │
│  │              Repository Layer (JPA)                 │  │
│  └────────────────────┬─────────────────────────────────┘  │
│                       │                                     │
│  ┌────────────────────▼─────────────────────────────────┐  │
│  │                 H2 / PostgreSQL                     │  │
│  └──────────────────────────────────────────────────────┘  │
└──────────────┬─────────────────────────────┬────────────────┘
               │                             │
               │                             │
               ▼                             ▼
    ┌──────────────────┐         ┌──────────────────┐
    │  Proxmox API     │         │  Python AI       │
    │  (Port 8006)     │         │  (Port 8000)     │
    │                  │         │                  │
    │ • Node Metrics   │         │ • Nmap Scanning  │
    │ • VM Status      │         │ • Threat Analysis│
    │ • Health Check   │         │ • Firewall Rules │
    └──────────────────┘         └──────────────────┘
```

## 🚀 Quick Start

### Prerequisites

- Java 21+
- Maven 3.8+
- (Optional) Proxmox VE instance
- (Optional) Python AI service

### 1. Clone Repository

```powershell
git clone https://github.com/your-org/autoshield.git
cd autoshield
```

### 2. Start Backend

```powershell
cd autoshield-backend
mvn spring-boot:run
```

Backend runs on **http://localhost:8080**

### 3. Start Frontend

```powershell
# Open new terminal
cd autoshield-ui
mvn spring-boot:run
```

Frontend runs on **http://localhost:8081**

### 4. Access Dashboard

Open browser: **http://localhost:8081**

Login with:
- **Username**: `admin`
- **Password**: `admin123`

## 📁 Project Structure

```
webautoshild/
├── autoshield-backend/          # Spring Boot REST API
│   ├── src/main/java/
│   │   └── com/autoshield/
│   │       ├── controller/      # REST endpoints
│   │       ├── service/         # Business logic
│   │       ├── repository/      # Data access
│   │       ├── entity/          # JPA entities
│   │       ├── dto/             # Data transfer objects
│   │       └── config/          # Configuration
│   ├── pom.xml
│   └── BACKEND_README.md        # Detailed backend docs
│
├── autoshield-ui/               # Vaadin Flow UI
│   ├── src/main/java/
│   │   └── com/autoshield/
│   │       ├── views/           # Vaadin views
│   │       ├── components/      # UI components
│   │       ├── services/        # REST client
│   │       └── security/        # Security config
│   ├── pom.xml
│   └── UI_README.md             # Detailed UI docs
│
└── README.md                    # This file
```

## 🔌 API Endpoints

### Authentication
All endpoints require HTTP Basic Authentication (except webhooks).

### Core Endpoints

| Category | Endpoint | Method | Description |
|----------|----------|--------|-------------|
| **Alerts** | `/api/v1/alerts` | GET | List all alerts |
| | `/api/v1/alerts/{id}` | GET | Get alert details |
| | `/api/v1/alerts/recent` | GET | Recent alerts |
| **Scans** | `/api/v1/scan/trigger` | POST | Start security scan |
| | `/api/v1/scan/{scanId}` | GET | Get scan results |
| **Firewall** | `/api/v1/firewall/block` | POST | Block IP address |
| | `/api/v1/firewall/unblock/{ip}` | DELETE | Unblock IP |
| | `/api/v1/firewall/rules` | GET | List active rules |
| **Metrics** | `/api/v1/metrics/current` | GET | Current system metrics |
| | `/api/v1/metrics/history` | GET | Metrics history |
| **Health** | `/api/v1/health` | GET | System health status |

Full API documentation: **http://localhost:8080/swagger-ui.html**

## 🖥️ Dashboard Views

### 1. Dashboard (/)
- Real-time system metrics
- CPU, RAM, Disk usage graphs
- Active threats counter
- Live alert feed (last 10 alerts)
- Quick action buttons

### 2. Alerts (/alerts)
- Comprehensive alert grid
- Sortable and filterable columns
- Color-coded severity levels
- Detailed alert inspection
- Export capabilities

### 3. Security Control (/security) - Admin Only
- Manual network scanning
- IP blocking/unblocking
- Firewall rule management
- Service health monitoring

### 4. Settings (/settings)
- System configuration
- User preferences
- (Future expansion)

## 🔐 Security

### Authentication
- HTTP Basic Authentication for API
- Form-based login for UI
- BCrypt password hashing
- Session management

### Authorization
- Role-based access control (RBAC)
- Admin vs. User permissions
- Method-level security with `@PreAuthorize`

### Default Credentials

| Username | Password | Role | Access |
|----------|----------|------|--------|
| admin | admin123 | ADMIN | Full control |
| viewer | viewer123 | USER | Read-only |

**⚠️ Change these in production!**

## ⚙️ Configuration

### Backend Configuration

Edit `autoshield-backend/src/main/resources/application.yml`:

```yaml
server:
  port: 8080

proxmox:
  api:
    url: https://your-proxmox-host:8006
    token: YOUR_PROXMOX_API_TOKEN

python:
  ai:
    url: http://your-python-ai-host:8000
```

### Frontend Configuration

Edit `autoshield-ui/src/main/resources/application.yml`:

```yaml
server:
  port: 8081

autoshield:
  backend:
    url: http://localhost:8080
    username: admin
    password: admin123
```

### Environment Variables

```powershell
# Backend
$env:PROXMOX_TOKEN="your-token"
$env:PYTHON_AI_URL="http://python-ai:8000"

# Frontend
$env:BACKEND_URL="http://backend:8080"
$env:BACKEND_USERNAME="admin"
$env:BACKEND_PASSWORD="secure_password"
```

## 🗄️ Database

### Development (H2)
- Embedded database
- File: `./data/autoshield.mv.db`
- Console: http://localhost:8080/h2-console

### Production (PostgreSQL)

1. Create database:
```sql
CREATE DATABASE autoshield;
CREATE USER autoshield WITH ENCRYPTED PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE autoshield TO autoshield;
```

2. Update backend `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/autoshield
    username: autoshield
    password: password
  jpa:
    hibernate:
      ddl-auto: update
```

## 🐳 Docker Deployment

### Using Docker Compose

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  backend:
    build: ./autoshield-backend
    ports:
      - "8080:8080"
    environment:
      - PROXMOX_TOKEN=${PROXMOX_TOKEN}
      - PYTHON_AI_URL=http://python-ai:8000
    depends_on:
      - postgres
    
  frontend:
    build: ./autoshield-ui
    ports:
      - "8081:8081"
    environment:
      - BACKEND_URL=http://backend:8080
      - BACKEND_USERNAME=admin
      - BACKEND_PASSWORD=admin123
    depends_on:
      - backend
    
  postgres:
    image: postgres:16
    environment:
      - POSTGRES_DB=autoshield
      - POSTGRES_USER=autoshield
      - POSTGRES_PASSWORD=password
    volumes:
      - postgres-data:/var/lib/postgresql/data

volumes:
  postgres-data:
```

Run:
```powershell
docker-compose up -d
```

## 📊 Monitoring & Observability

### Metrics Collection
- Automatic collection every 30 seconds
- 7-day retention by default
- Configurable in `application.yml`

### Health Checks
- Backend: http://localhost:8080/actuator/health
- Monitors: Database, Proxmox API, Python AI

### Logging
```yaml
logging:
  level:
    com.autoshield: INFO
  file:
    name: logs/autoshield.log
```

## 🧪 Testing

### Backend Tests
```powershell
cd autoshield-backend
mvn test
```

### Frontend Tests
```powershell
cd autoshield-ui
mvn test
```

## 🚀 Production Deployment

### Checklist

- [ ] Change all default passwords
- [ ] Configure HTTPS/TLS certificates
- [ ] Switch to PostgreSQL database
- [ ] Set up database backups
- [ ] Configure reverse proxy (nginx, Traefik)
- [ ] Enable production mode in Vaadin
- [ ] Set up monitoring (Prometheus, Grafana)
- [ ] Configure log aggregation (ELK, Loki)
- [ ] Review security settings
- [ ] Set up automated backups
- [ ] Configure firewall rules
- [ ] Enable rate limiting
- [ ] Set up alerts for system failures

### Building for Production

```powershell
# Backend
cd autoshield-backend
mvn clean package -DskipTests

# Frontend
cd autoshield-ui
mvn clean package -Pproduction
```

## 🤝 Integration with External Systems

### Proxmox API
- Metrics collection
- VM/Container status
- Node information
- Health monitoring

### Python AI Service
- Network scanning (Nmap)
- Vulnerability assessment
- Threat detection
- Automated response actions

### Webhook Support
External systems can send events:
```bash
curl -X POST http://localhost:8080/api/v1/webhook/python \
  -H "Content-Type: application/json" \
  -d '{
    "eventType": "brute_force",
    "severity": "HIGH",
    "sourceIp": "192.168.1.100",
    "description": "Multiple failed login attempts"
  }'
```

## 📚 Documentation

- [Backend API Documentation](autoshield-backend/BACKEND_README.md)
- [Frontend UI Documentation](autoshield-ui/UI_README.md)
- API Docs: http://localhost:8080/swagger-ui.html

## 🐛 Troubleshooting

### Backend won't start
- Check Java version: `java -version` (should be 21+)
- Verify port 8080 is available
- Check logs in `logs/autoshield.log`

### Frontend can't connect to backend
- Verify backend is running: http://localhost:8080/actuator/health
- Check `application.yml` backend URL
- Verify credentials are correct

### Database errors
- For H2: Ensure `./data/` directory exists and is writable
- For PostgreSQL: Verify database exists and credentials are correct

### Authentication failures
- Check username/password
- Verify Spring Security configuration
- Check browser console for errors

## 🎯 Future Enhancements

- [ ] Email/Slack notifications for critical alerts
- [ ] Custom alert rules and thresholds
- [ ] Advanced analytics and reporting
- [ ] Integration with more security tools
- [ ] Machine learning for anomaly detection
- [ ] Mobile app for remote monitoring
- [ ] Multi-tenant support
- [ ] Audit log viewer
- [ ] Customizable dashboards
- [ ] Export reports (PDF, CSV)

## 📄 License

MIT License

Copyright (c) 2025 AutoShield Security Systems

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

## 👥 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 💬 Support

For questions, issues, or feature requests:
- Open a GitHub issue
- Contact: security@autoshield.com
- Documentation: See README files in each project

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Vaadin team for the powerful UI framework
- Proxmox community for API documentation
- Security research community for best practices

---

**Built with ❤️ for home lab security enthusiasts**
