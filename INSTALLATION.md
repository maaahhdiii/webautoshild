# AutoShield - Installation & Setup Guide for Windows

## Current Status
❌ Java 8 detected (Java 21 required)
❌ Maven not installed

## Quick Setup Instructions

### 1. Install Java 21

**Option A: Using Chocolatey (Easiest)**
```powershell
# Install Chocolatey first if not installed (run as Administrator)
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# Then install Java 21 and Maven
choco install temurin21 maven -y

# Restart PowerShell and verify
java -version
mvn -version
```

**Option B: Manual Installation**

1. **Download Java 21**:
   - Visit: https://adoptium.net/temurin/releases/?version=21
   - Download: Windows x64 MSI installer
   - Run installer (it will set PATH automatically)

2. **Download Maven**:
   - Visit: https://maven.apache.org/download.cgi
   - Download: apache-maven-3.9.6-bin.zip
   - Extract to: `C:\Program Files\Apache\maven`
   
3. **Add Maven to PATH**:
   ```powershell
   # Run as Administrator
   [Environment]::SetEnvironmentVariable("Path", $env:Path + ";C:\Program Files\Apache\maven\bin", "Machine")
   ```

4. **Restart PowerShell** and verify:
   ```powershell
   java -version    # Should show "21.x.x"
   mvn -version     # Should show Maven 3.9.x
   ```

### 2. Run AutoShield

Once Java 21 and Maven are installed:

```powershell
# Terminal 1: Start Backend
cd D:\webautoshild\autoshield-backend
mvn spring-boot:run
# Wait for "Started AutoShieldApplication" message
# Runs on http://localhost:8080

# Terminal 2: Start Frontend
cd D:\webautoshild\autoshield-ui
mvn spring-boot:run
# Wait for "Started AutoShieldUiApplication" message
# Runs on http://localhost:8081
```

### 3. Access the Dashboard

Open browser: **http://localhost:8081**

Login:
- Username: `admin`
- Password: `admin123`

## Alternative: Use Docker (No Java/Maven needed)

If you have Docker Desktop installed:

1. Create `docker-compose.yml` in `D:\webautoshild\`:

```yaml
version: '3.8'
services:
  backend:
    build: ./autoshield-backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
  
  frontend:
    build: ./autoshield-ui
    ports:
      - "8081:8081"
    environment:
      - BACKEND_URL=http://backend:8080
    depends_on:
      - backend
```

2. Build and run:
```powershell
cd D:\webautoshild
docker-compose up --build
```

## Troubleshooting

### "mvn not recognized"
- Maven not installed or not in PATH
- Restart PowerShell after installation
- Check: `$env:Path -split ';' | Select-String maven`

### "JAVA_HOME not set"
```powershell
# Find Java installation
Get-ChildItem "C:\Program Files\Eclipse Adoptium" -Recurse -Filter java.exe

# Set JAVA_HOME (adjust path to your installation)
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Eclipse Adoptium\jdk-21.0.1.12-hotspot", "Machine")
```

### Port Already in Use
```powershell
# Find process using port 8080 or 8081
netstat -ano | findstr :8080
netstat -ano | findstr :8081

# Kill process (replace PID)
taskkill /PID <PID> /F
```

### Build Errors
```powershell
# Clean and rebuild
mvn clean install -DskipTests
```

## Next Steps After Installation

1. ✅ Install Java 21
2. ✅ Install Maven
3. ✅ Start backend (port 8080)
4. ✅ Start frontend (port 8081)
5. ✅ Login to dashboard
6. 🔧 Configure Proxmox connection (optional)
7. 🔧 Set up Python AI service (optional)

## Need Help?

Check the detailed documentation:
- Backend: `autoshield-backend/BACKEND_README.md`
- Frontend: `autoshield-ui/UI_README.md`
- Main: `README.md`
