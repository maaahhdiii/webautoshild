# 🛡️ AutoShield - AI-Powered Security Automation Platform

A complete, production-ready security monitoring and **AI-powered automated threat response system** for Proxmox home lab environments. Built with **Java 21**, **Spring Boot**, **Vaadin Flow**, and **Python AI**.

## 🌟 Overview

AutoShield provides real-time security monitoring, **AI-driven threat detection**, and **automated response capabilities** for your Proxmox infrastructure. The system consists of three main components:

1. **Backend API** (Spring Boot) - Core business logic and data management
2. **Frontend Dashboard** (Vaadin Flow) - Modern web interface for monitoring and control
3. **Python AI Service** (FastAPI) - AI-powered threat analysis and automated Proxmox actions via SSH

## 📸 Key Features

### 🤖 AI-Powered Automated Threat Response
- **Intelligent threat analysis** with 7 pre-configured playbooks
- **Automated SSH execution** on Proxmox for immediate threat mitigation
- **Real-time monitoring** - processes threats as they occur
- **Automatic IP blocking** via iptables
- **Process termination** for malicious activities
- **VM isolation** for compromised systems
- **Rate limiting** for brute-force attacks
- **Rollback capabilities** for all automated actions
- **Dry-run mode** for safe testing before production

### 🎯 Real-time Monitoring
- Live system metrics (CPU, RAM, Disk usage)
- Active threat tracking with **AI threat scoring**
- Network traffic analysis
- 10-second auto-refresh intervals
- **Continuous alert monitoring** with auto-response

### 🚨 Security Alerts
- Centralized alert management
- Severity-based filtering (LOW, MEDIUM, HIGH, CRITICAL)
- Alert status tracking (Active, **Resolved with AI Actions**, Ignored)
- Detailed alert information with source IP tracking
- **actionTaken field** - records all automated responses
- **Real-time alert resolution** as threats are mitigated

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
│  │                 H2 Database                         │  │
│  │            (./data/autoshield)                      │  │
│  └──────────────────────────────────────────────────────┘  │
└──────────┬──────────────────────────────────┬───────────────┘
           │                                  │
           │ Webhook /                        │ REST API
           │ Alert Query                      │ /api/v1/analyze-threat
           ▼                                  ▼
┌─────────────────────┐         ┌──────────────────────────────┐
│  Python AI Service  │◄────────┤ auto-threat-monitor.py       │
│  (FastAPI)          │         │ (Continuous Monitoring)      │
│  Port: 8000         │         │                              │
│                     │         │ • Polls every 5s             │
│ • AI Decision Engine│         │ • Triggers AI analysis       │
│ • 7 Threat Playbooks│         │ • Updates alert status       │
│ • Threat Scoring    │         └──────────────────────────────┘
│ • SSH Orchestrator  │
└──────────┬──────────┘
           │
           │ SSH (Port 22)
           │ root@192.168.100.64
           ▼
┌─────────────────────────────────────────────────────────────┐
│                  Proxmox VE Host                            │
│                   192.168.100.64                            │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Automated Actions via SSH:                         │  │
│  │  • iptables -A INPUT -s <IP> -j DROP               │  │
│  │  • kill -9 <PID>                                    │  │
│  │  • chmod 000 /path/to/binary                       │  │
│  │  • qm shutdown <VMID>                              │  │
│  │  • qm set <VMID> -net0 model=e1000,bridge=vmbr999 │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Component Interactions:

1. **User** → Views dashboard/alerts in browser (Port 8081)
2. **Frontend** → Queries backend REST API for data
3. **Backend** → Stores alerts in H2 database
4. **auto-threat-monitor.py** → Continuously polls backend for ACTIVE alerts
5. **Monitor** → Submits threats to Python AI service for analysis
6. **Python AI** → Analyzes threat using playbooks, scores risk
7. **Python AI** → Executes SSH commands on Proxmox for mitigation
8. **Monitor** → Updates backend alert with actions taken + RESOLVED status

## 🚀 Quick Start

### Prerequisites

- Java 21+
- Maven 3.8+
- **Python 3.9+** (for AI service)
- **Proxmox VE instance** (recommended)
- **SSH access to Proxmox** (for automated actions)

### 1. Clone Repository

```powershell
git clone https://github.com/maaahhdiii/webautoshild.git
cd webautoshild
```

### 2. Configure Python AI Service

```powershell
cd python_ai_service
notepad .env
```

Edit `.env` with your Proxmox credentials:
```env
PROXMOX_HOST=192.168.100.64
PROXMOX_USER=root
PROXMOX_PASSWORD=your_proxmox_password
PROXMOX_PORT=22

# Safety settings
DRY_RUN_MODE=true          # Set to false for real actions
AUTO_EXECUTE_THREATS=false # Set to true for automatic execution
THREAT_SCORE_THRESHOLD=70
```

**⚠️ Important**: Start with `DRY_RUN_MODE=true` for testing!

### 3. Start Backend

```powershell
cd autoshield-backend
mvn spring-boot:run
```

Backend runs on **http://localhost:8080**

### 4. Start Frontend

```powershell
# Open new terminal
cd autoshield-ui
mvn spring-boot:run
```

Frontend runs on **http://localhost:8081**

### 5. Start Python AI Service (Optional but Recommended)

```powershell
# Open new terminal
cd python_ai_service
python app.py
```

Python AI runs on **http://localhost:8000**

### 6. Start Automated Threat Monitor (Optional)

```powershell
# Open new terminal
cd webautoshild
python auto-threat-monitor.py
```

This will:
- ✅ Process all existing active alerts
- ✅ Monitor for new threats every 5 seconds
- ✅ Automatically trigger AI analysis and response
- ✅ Update alerts with actions taken

### 7. Access Dashboard

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
| | `/api/v1/alerts/{id}/status` | PATCH | Update alert status/notes |
| **AI Threat Response** | `/api/v1/analyze-threat` | POST | Analyze threat with AI |
| | `/api/v1/threats/{id}/execute` | POST | Execute AI recommendations |
| | `/api/v1/threats/playbooks` | GET | List available playbooks |
| **Scans** | `/api/v1/scan/trigger` | POST | Start security scan |
| | `/api/v1/scan/{scanId}` | GET | Get scan results |
| **Firewall** | `/api/v1/firewall/block` | POST | Block IP address |
| | `/api/v1/firewall/unblock/{ip}` | DELETE | Unblock IP |
| | `/api/v1/firewall/rules` | GET | List active rules |
| **Metrics** | `/api/v1/metrics/current` | GET | Current system metrics |
| | `/api/v1/metrics/history` | GET | Metrics history |
| **Health** | `/api/v1/health` | GET | System health status |

Full API documentation: **http://localhost:8080/swagger-ui.html**

### AI Threat Analysis Example

```powershell
# Analyze threat from alert
curl -X POST http://localhost:8000/api/v1/analyze-threat `
  -H "Content-Type: application/json" `
  -d @"
{
  \"alertId\": 108,
  \"threatType\": \"MALICIOUS_PROCESS\",
  \"severity\": \"CRITICAL\",
  \"sourceIp\": \"192.168.100.64\",
  \"details\": {
    \"processName\": \"suspicious.exe\",
    \"pid\": 1234
  }
}
"@
```

Response:
```json
{
  "threat_score": 95,
  "recommended_actions": [
    "kill_process: Terminate malicious process",
    "block_binary: Prevent process restart",
    "scan_filesystem: Scan for malware artifacts"
  ],
  "analysis": "High-risk malicious process detected...",
  "playbook_used": "malicious_process"
}
```

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

spring:
  datasource:
    url: jdbc:h2:file:./data/autoshield
    driver-class-name: org.h2.Driver
    username: admin
    password: admin

webhook:
  secret: your-webhook-secret-key-here  # Change this!
```

### Frontend Configuration

Edit `autoshield-ui/src/main/resources/application.yml`:

```yaml
server:
  port: 8081

autoshield:
  backend:
    url: http://localhost:8080
```

### Python AI Service Configuration (Required for Automated Response)

Create `python_ai_service/.env`:

```env
# Proxmox Connection
PROXMOX_HOST=192.168.100.64      # Your Proxmox server IP
PROXMOX_USER=root                # SSH username
PROXMOX_PASSWORD=your_password   # SSH password (use key auth in production!)
PROXMOX_PORT=22

# Safety Configuration
DRY_RUN_MODE=true                # false to execute real actions
AUTO_EXECUTE_THREATS=false       # true for automatic execution
THREAT_SCORE_THRESHOLD=70        # Minimum score for automated response

# Logging
LOG_LEVEL=INFO                   # DEBUG for troubleshooting
```

**⚠️ Security Best Practices:**
- Use SSH key authentication instead of passwords in production
- Store credentials in secure vault (Azure Key Vault, HashiCorp Vault)
- Start with `DRY_RUN_MODE=true` to test without real changes
- Review AI recommendations before setting `AUTO_EXECUTE_THREATS=true`

### Environment Variables

```powershell
# Python AI Service
$env:PROXMOX_HOST="192.168.100.64"
$env:PROXMOX_USER="root"
$env:DRY_RUN_MODE="true"
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
- Verify port 8080 is available: `netstat -ano | findstr :8080`
- Check logs in `logs/autoshield.log`
- Rebuild: `mvn clean package`

### Frontend can't connect to backend
- Verify backend is running: http://localhost:8080/actuator/health
- Check `application.yml` backend URL
- Verify credentials are correct (default: admin/admin)

### Python AI Service won't start
- Check Python version: `python --version` (should be 3.9+)
- Install dependencies: `pip install -r requirements.txt`
- Verify port 8000 is available: `netstat -ano | findstr :8000`
- Check `.env` file exists in `python_ai_service/`
- Test connection: `curl http://localhost:8000/health`

### SSH to Proxmox fails
- Verify Proxmox host is reachable: `ping 192.168.100.64`
- Test SSH manually: `ssh root@192.168.100.64`
- Check credentials in `.env` file
- Verify SSH port: Default is 22
- Check firewall rules on Proxmox
- Test SSH script: `python test-ssh.py`

### auto-threat-monitor.py not processing alerts
- Verify backend is running and has ACTIVE alerts
- Check Python AI service is running on port 8000
- Verify URLs in script:
  ```python
  BACKEND_URL = "http://localhost:8080"
  PYTHON_AI_URL = "http://localhost:8000"
  ```
- Run in debug mode: Add `print()` statements
- Test single alert: `python execute-alert-108.py`

### Alerts created but no actions taken
- Verify `DRY_RUN_MODE=false` in `.env` to execute real actions
- Check `AUTO_EXECUTE_THREATS=true` if you want automatic execution
- Review alert notes for AI recommendations
- Check Proxmox logs for command execution
- Verify SSH permissions on Proxmox (root access required)

### Database errors
- For H2: Ensure `./data/` directory exists and is writable
- Check database file: `./data/autoshield.mv.db`
- If corrupted, delete and restart (data will be lost)
- For PostgreSQL: Verify database exists and credentials are correct

### Authentication failures
- Default credentials: `admin` / `admin`
- Check username/password in application.yml
- Verify Spring Security configuration
- Check browser console for errors
- Clear browser cache/cookies

## 🤖 Automated Threat Monitoring

### auto-threat-monitor.py

**Continuous monitoring system that automatically processes security alerts:**

```powershell
# Continuous monitoring mode (runs indefinitely)
python auto-threat-monitor.py

# Process current alerts once and exit
python auto-threat-monitor.py --once
```

**How it Works:**
1. 🔄 Polls backend every 5 seconds for ACTIVE alerts
2. 🤖 Submits each alert to Python AI service for analysis
3. 📝 Updates alert with AI recommendations and actions taken
4. ✅ Marks alert as RESOLVED with detailed action notes
5. 🔁 Repeats for new alerts (continuous mode)

**Output Example:**
```
[2025-01-23 14:30:15] Checking for active alerts...
[2025-01-23 14:30:15] Found 3 active alerts to process
[2025-01-23 14:30:16] Processing Alert #108 - MALICIOUS_PROCESS (CRITICAL)
[2025-01-23 14:30:17]   ✓ AI Analysis: Threat Score 95/100
[2025-01-23 14:30:17]   ✓ Actions: kill_process | block_binary | scan_filesystem
[2025-01-23 14:30:18]   ✓ Alert updated: RESOLVED
[2025-01-23 14:30:18] Processing Alert #109 - SSH_BRUTE_FORCE (HIGH)
...
```

**Configuration:**
```python
BACKEND_URL = "http://localhost:8080"
PYTHON_AI_URL = "http://localhost:8000"
POLL_INTERVAL = 5  # seconds
```

### Manual Threat Response

**Execute specific alert:**
```powershell
python execute-alert-108.py  # Processes alert #108
```

**Test SSH connectivity:**
```powershell
python test-ssh.py  # Verifies Proxmox SSH access
```

**Test automated response (dry-run):**
```powershell
python test-automated-response.py  # Simulates actions without execution
```

## 🎯 AI Threat Playbooks

The AI service includes 7 pre-configured threat response playbooks:

| Playbook | Threat Type | Automated Actions |
|----------|-------------|-------------------|
| **SSH Brute Force** | Login attacks | Rate limiting, IP blocking, audit logs |
| **Port Scan** | Network recon | Temporary IP block, increase monitoring |
| **Malicious Process** | Running malware | Kill process, block binary, scan filesystem |
| **DDoS Attack** | Traffic flood | Rate limiting, connection limits, IP blocking |
| **Unauthorized Access** | Access violation | Block user, isolate VM, capture forensics |
| **Privilege Escalation** | Privilege abuse | Revoke permissions, isolate VM, audit |
| **Data Exfiltration** | Data theft | Block IP, isolate VM, capture traffic |

**Each playbook provides:**
- 📊 Threat score (0-100)
- 📋 Recommended actions (prioritized)
- 📝 Detailed analysis and reasoning
- ⚙️ SSH commands for Proxmox execution
- 🔄 Rollback procedures if needed

## 🎯 Future Enhancements

- [x] ~~Machine learning for anomaly detection~~ ✅ **COMPLETED** (AI threat analysis)
- [x] ~~Integration with more security tools~~ ✅ **COMPLETED** (Proxmox SSH automation)
- [ ] Email/Slack notifications for critical alerts
- [ ] Custom alert rules and thresholds
- [ ] Advanced analytics and reporting
- [ ] Mobile app for remote monitoring
- [ ] Multi-tenant support
- [ ] Audit log viewer
- [ ] Customizable dashboards
- [ ] Export reports (PDF, CSV)
- [ ] Webhook integrations (PagerDuty, OpsGenie)
- [ ] AI model training on historical data

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
