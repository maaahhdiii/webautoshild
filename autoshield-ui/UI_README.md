# AutoShield UI - Vaadin Flow Dashboard

Modern, responsive web dashboard for the AutoShield Security Monitoring System. Built with **Java 21**, **Vaadin Flow 24**, and **Spring Security** - 100% Java, no HTML/CSS/JavaScript required.

## 🎯 Features

- **Real-time Monitoring**: Live system metrics with auto-refresh
- **Security Alerts**: Interactive grid with filtering and sorting
- **Firewall Management**: Block/unblock IPs with visual feedback
- **Security Scanning**: Trigger and monitor network scans
- **Role-based Access**: Admin and Viewer roles with different permissions
- **Responsive Design**: Works on desktop, tablet, and mobile
- **Modern UI**: Lumo theme with professional styling
- **Real-time Updates**: Push notifications and WebSocket support

## 📋 Prerequisites

- **Java 21** or higher
- **Maven 3.8+**
- **AutoShield Backend** running on http://localhost:8080

## 🚀 Quick Start

### 1. Clone and Build

```powershell
cd autoshield-ui
mvn clean install
```

### 2. Configure Backend Connection

Edit `src/main/resources/application.yml`:

```yaml
autoshield:
  backend:
    url: http://localhost:8080
    username: admin
    password: admin123
```

### 3. Run the Application

```powershell
mvn spring-boot:run
```

The UI will start on **http://localhost:8081**

### 4. Login

Open browser: **http://localhost:8081**

Default credentials:
- **Admin**: `admin` / `admin123`
- **Viewer**: `viewer` / `viewer123`

## 📁 Project Structure

```
autoshield-ui/
├── src/main/java/com/autoshield/
│   ├── views/                    # Vaadin views
│   │   ├── MainLayout.java       # App shell with navigation
│   │   ├── LoginView.java        # Authentication
│   │   ├── DashboardView.java    # Main monitoring screen
│   │   ├── AlertsView.java       # Security alerts grid
│   │   ├── SecurityControlView.java  # Admin controls
│   │   └── SettingsView.java     # Configuration
│   ├── components/               # Reusable components
│   │   ├── MetricCard.java       # Metric display widget
│   │   ├── AlertFeed.java        # Live alert stream
│   │   └── ThreatIndicator.java  # Threat level badge
│   ├── services/                 # Business logic
│   │   └── BackendService.java   # REST client
│   ├── security/                 # Security config
│   │   ├── SecurityConfig.java   # Spring Security
│   │   └── SecurityService.java  # Auth helpers
│   └── dto/                      # Data transfer objects
└── src/main/resources/
    └── application.yml           # Configuration
```

## 🖥️ Views Overview

### 1. Dashboard View (`/`)

**Access**: All authenticated users

**Features**:
- 4 metric cards: CPU, RAM, Disk, Active Threats
- Real-time updates every 10 seconds
- Live alert feed (last 10 alerts)
- Quick action buttons:
  - Scan Network
  - Block IP
  - System Health Check

**Components**:
```java
MetricCard cpuCard = new MetricCard("CPU Usage", "cpu");
cpuCard.updateValue(75.5); // Updates with percentage

AlertFeed alertFeed = new AlertFeed();
alertFeed.updateAlerts(recentAlerts);
```

### 2. Alerts View (`/alerts`)

**Access**: All authenticated users

**Features**:
- Paginated data grid with sorting
- Color-coded severity badges
- Status indicators (Active, Resolved, Ignored)
- Click to view detailed alert information
- Refresh button

**Grid Columns**:
- Timestamp
- Severity (with color badges)
- Type
- Source IP
- Status
- Action Taken
- Actions (View button)

### 3. Security Control View (`/security`)

**Access**: Admin only (`@RolesAllowed("ADMIN")`)

**Features**:
- **Manual Scan Section**:
  - Target IP/range input
  - Scan type selector (Quick, Full, Vulnerability)
  - Start scan button
  
- **Firewall Rules Management**:
  - Add new blocking rules
  - View active rules in grid
  - Set duration or permanent blocks
  - Remove rules with one click
  
- **Service Health**:
  - Check Proxmox API status
  - Check Python AI status
  - Check database connectivity

### 4. Settings View (`/settings`)

**Access**: All authenticated users

**Status**: Placeholder for future configuration options

### 5. Login View (`/login`)

**Access**: Public

**Features**:
- Professional gradient background
- AutoShield branding
- Username/password form
- Error message display
- Default credentials hint

## 🎨 UI Components

### MetricCard

Displays system metrics with progress bars and color coding:

```java
MetricCard card = new MetricCard("CPU Usage", "cpu");
card.updateValue(85.0); // Red if >90%, Orange if >75%, Green otherwise
card.setDetails("Node: pve");
```

### AlertFeed

Shows live security alerts:

```java
AlertFeed feed = new AlertFeed();
feed.updateAlerts(alertList); // Auto-scrollable, max 10 items
```

### ThreatIndicator

Displays threat level badge:

```java
ThreatIndicator indicator = new ThreatIndicator();
indicator.updateThreatLevel(5); // LOW, MEDIUM, HIGH, or CRITICAL
```

## 🔐 Security

### Authentication

Configured in `SecurityConfig.java`:
- Extends `VaadinWebSecurity`
- Form-based login with Vaadin integration
- BCrypt password encoding

### Roles

| Role | Permissions |
|------|-------------|
| ADMIN | Full access to all views and features |
| USER | Read-only access to Dashboard and Alerts |

### Role-based Access Control

```java
@Route(value = "security", layout = MainLayout.class)
@RolesAllowed("ADMIN")  // Only admins can access
public class SecurityControlView extends VerticalLayout {
    // ...
}
```

### Session Management

```java
SecurityService securityService;

// Get current user
String username = securityService.getCurrentUsername();

// Check admin role
boolean isAdmin = securityService.isAdmin();

// Logout
securityService.logout();
```

## 🌐 Backend Integration

### BackendService

REST client for AutoShield Backend API:

```java
@Service
public class BackendService {
    @Value("${autoshield.backend.url}")
    private String backendUrl; // http://localhost:8080
    
    // Get recent alerts
    List<AlertDto> getRecentAlerts(int hours);
    
    // Get current metrics
    SystemMetricDto getCurrentMetrics();
    
    // Trigger security scan
    Map<String, Object> triggerScan(String targetIp, String scanType);
    
    // Block IP address
    boolean blockIp(String ipAddress, String reason, Integer durationMinutes, Boolean permanent);
    
    // Get firewall rules
    List<FirewallRuleDto> getFirewallRules();
    
    // Get system health
    Map<String, Object> getHealthStatus();
}
```

### Authentication

Uses HTTP Basic Auth with Base64 encoding:
```java
private HttpHeaders createHeaders() {
    String auth = username + ":" + password;
    byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
    String authHeader = "Basic " + new String(encodedAuth);
    headers.set("Authorization", authHeader);
    return headers;
}
```

## 🔄 Real-time Updates

### Push Annotations

Application uses `@Push` for WebSocket support:

```java
@SpringBootApplication
@Theme(value = "autoshield")
@Push  // Enables WebSocket for real-time updates
public class AutoShieldUiApplication implements AppShellConfigurator {
    // ...
}
```

### Scheduled Updates

Dashboard auto-refreshes every 10 seconds:

```java
scheduler.scheduleAtFixedRate(() -> {
    updateDashboard();
}, 10, 10, TimeUnit.SECONDS);
```

### UI.access() for Thread Safety

```java
UI ui = getUI().orElse(null);
if (ui != null) {
    ui.access(() -> {
        cpuCard.updateValue(metrics.getCpuPercent());
        alertFeed.updateAlerts(recentAlerts);
    });
}
```

## ⚙️ Configuration

### application.yml

```yaml
server:
  port: 8081

spring:
  application:
    name: autoshield-ui
  security:
    user:
      name: admin
      password: admin123

vaadin:
  launch-browser: false
  productionMode: false  # Set to true for production

autoshield:
  backend:
    url: ${BACKEND_URL:http://localhost:8080}
    username: ${BACKEND_USERNAME:admin}
    password: ${BACKEND_PASSWORD:admin123}
```

### Environment Variables

```powershell
$env:BACKEND_URL="http://192.168.1.100:8080"
$env:BACKEND_USERNAME="admin"
$env:BACKEND_PASSWORD="secure_password"
```

## 🎨 Theming

### Lumo Theme

AutoShield uses Vaadin's modern Lumo theme with custom styling:

```java
@Theme(value = "autoshield")
public class AutoShieldUiApplication implements AppShellConfigurator {
    // ...
}
```

### Color Scheme

- **Primary**: Blue (#667eea)
- **Critical Alerts**: Red (Lumo error color)
- **High Alerts**: Orange (#ff6b35)
- **Medium Alerts**: Yellow (Lumo warning color)
- **Low Alerts**: Gray (Lumo contrast)
- **Success**: Green (Lumo success color)

### Custom Styles

Components use inline styles with Lumo CSS variables:

```java
card.getStyle()
    .set("background", "var(--lumo-contrast-5pct)")
    .set("border-radius", "var(--lumo-border-radius-m)")
    .set("padding", "var(--lumo-space-m)");
```

## 📱 Responsive Design

All views are responsive and work on:
- **Desktop**: Full feature set with multi-column layouts
- **Tablet**: Adapted layouts with touch-friendly controls
- **Mobile**: Stacked layouts with drawer navigation

```java
HorizontalLayout metricsLayout = new HorizontalLayout(cpuCard, ramCard, diskCard, threatCard);
metricsLayout.setWidthFull(); // Auto-wraps on smaller screens
```

## 🧪 Testing

```powershell
# Run all tests
mvn test

# Run in development mode with hot reload
mvn spring-boot:run
```

## 🏗️ Production Build

### 1. Enable Production Mode

Edit `application.yml`:
```yaml
vaadin:
  productionMode: true
```

### 2. Build Production Package

```powershell
mvn clean package -Pproduction
```

This will:
- Compile frontend resources
- Optimize JavaScript bundles
- Minify CSS
- Create executable JAR in `target/`

### 3. Run Production Build

```powershell
java -jar target/autoshield-ui-1.0.0.jar
```

## 🐳 Docker Deployment

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/autoshield-ui-1.0.0.jar app.jar
EXPOSE 8081
ENV BACKEND_URL=http://autoshield-backend:8080
ENV BACKEND_USERNAME=admin
ENV BACKEND_PASSWORD=admin123
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run:
```powershell
mvn clean package -Pproduction
docker build -t autoshield-ui .
docker run -p 8081:8081 `
  -e BACKEND_URL=http://backend:8080 `
  -e BACKEND_USERNAME=admin `
  -e BACKEND_PASSWORD=secure_password `
  autoshield-ui
```

## 🔧 Development Tips

### Hot Reload

Spring Boot DevTools enables hot reload:
```powershell
mvn spring-boot:run
# Make changes to Java files
# Application auto-restarts
```

### Debug Mode

Run with debug logging:
```yaml
logging:
  level:
    com.autoshield: DEBUG
    com.vaadin: DEBUG
```

### Browser DevTools

Vaadin provides debugging tools:
- Press `Ctrl+Shift+D` in browser
- Shows component hierarchy
- Inspects data bindings

## 🚀 Production Checklist

- [ ] Set `vaadin.productionMode=true`
- [ ] Change default passwords
- [ ] Configure HTTPS backend URL
- [ ] Set secure session timeout
- [ ] Enable CSRF protection
- [ ] Configure production logging
- [ ] Set up reverse proxy (nginx, Traefik)
- [ ] Enable compression
- [ ] Configure caching headers
- [ ] Set up monitoring

## 📊 Performance Optimization

### Lazy Loading

Grid uses lazy loading for large datasets:
```java
grid.setPageSize(50);
grid.setItems(query -> {
    return backendService.getAlerts(query.getPage(), query.getPageSize()).stream();
});
```

### Caching

Consider caching backend responses:
```java
@Cacheable("metrics")
public SystemMetricDto getCurrentMetrics() {
    // ...
}
```

## 🐛 Troubleshooting

### Backend Connection Issues

Check logs for:
```
Error fetching alerts: Connection refused
```

**Solution**: Ensure backend is running on configured URL

### Authentication Failures

```
401 Unauthorized
```

**Solution**: Verify backend username/password in `application.yml`

### UI Not Updating

**Solution**: Check if `@Push` annotation is present and WebSocket connection is established

## 📄 License

MIT License - See LICENSE file for details

## 👥 Support

For issues or questions, please open a GitHub issue or contact the development team.
