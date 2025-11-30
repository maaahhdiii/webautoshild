# AutoShield - One-Click Setup & Run Script
# This script installs everything and runs AutoShield

Write-Host @"

╔═══════════════════════════════════════════════════════════╗
║                                                           ║
║              🛡️  AUTOSHIELD ONE-CLICK SETUP               ║
║                                                           ║
╚═══════════════════════════════════════════════════════════╝

"@ -ForegroundColor Cyan

# Check if running as Administrator
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if (-not $isAdmin) {
    Write-Host "⚠️  This script needs Administrator privileges to install software.`n" -ForegroundColor Yellow
    Write-Host "Attempting to restart as Administrator...`n" -ForegroundColor Cyan
    
    $arguments = "-NoProfile -ExecutionPolicy Bypass -File `"$PSCommandPath`""
    Start-Process powershell -Verb RunAs -ArgumentList $arguments
    exit
}

Write-Host "✅ Running with Administrator privileges`n" -ForegroundColor Green

# Function to check if a command exists
function Test-Command($command) {
    try {
        if (Get-Command $command -ErrorAction Stop) { return $true }
    } catch {
        return $false
    }
}

# Check Chocolatey
Write-Host "[1/5] Checking Chocolatey..." -ForegroundColor Cyan
if (-not (Test-Command "choco")) {
    Write-Host "   Installing Chocolatey..." -ForegroundColor Yellow
    Set-ExecutionPolicy Bypass -Scope Process -Force
    [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
    iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
    Write-Host "   ✅ Chocolatey installed`n" -ForegroundColor Green
} else {
    Write-Host "   ✅ Chocolatey already installed`n" -ForegroundColor Green
}

# Refresh environment
$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")

# Check Java 21
Write-Host "[2/5] Checking Java 21..." -ForegroundColor Cyan
$needsJava = $false
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    if ($javaVersion -match '"21\.') {
        Write-Host "   ✅ Java 21 already installed`n" -ForegroundColor Green
    } else {
        $needsJava = $true
    }
} catch {
    $needsJava = $true
}

if ($needsJava) {
    Write-Host "   Installing Java 21 (this may take 2-3 minutes)..." -ForegroundColor Yellow
    choco install temurin21 -y --force
    $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
    Write-Host "   ✅ Java 21 installed`n" -ForegroundColor Green
}

# Check Maven
Write-Host "[3/5] Checking Maven..." -ForegroundColor Cyan
if (-not (Test-Command "mvn")) {
    Write-Host "   Installing Maven..." -ForegroundColor Yellow
    choco install maven -y
    $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
    Write-Host "   ✅ Maven installed`n" -ForegroundColor Green
} else {
    Write-Host "   ✅ Maven already installed`n" -ForegroundColor Green
}

# Verify installations
Write-Host "[4/5] Verifying installations..." -ForegroundColor Cyan
Start-Sleep -Seconds 2
$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")

try {
    $javaVer = java -version 2>&1 | Select-Object -First 1
    Write-Host "   Java: $javaVer" -ForegroundColor Gray
} catch {
    Write-Host "   ⚠️  Java verification failed" -ForegroundColor Red
}

try {
    $mavenVer = mvn -version 2>&1 | Select-Object -First 1
    Write-Host "   Maven: $mavenVer" -ForegroundColor Gray
} catch {
    Write-Host "   ⚠️  Maven verification failed" -ForegroundColor Red
}

Write-Host ""

# Ask to start application
Write-Host "[5/5] Ready to launch AutoShield!" -ForegroundColor Cyan
Write-Host ""
Write-Host "════════════════════════════════════════════════════════" -ForegroundColor Gray
Write-Host ""
Write-Host "The backend will start on:  http://localhost:8080" -ForegroundColor White
Write-Host "The dashboard will open on: http://localhost:8081" -ForegroundColor White
Write-Host ""
Write-Host "Login credentials:" -ForegroundColor Yellow
Write-Host "  Username: admin" -ForegroundColor White
Write-Host "  Password: admin123" -ForegroundColor White
Write-Host ""
Write-Host "════════════════════════════════════════════════════════" -ForegroundColor Gray
Write-Host ""

$start = Read-Host "Start AutoShield now? (Y/N)"

if ($start -eq "Y" -or $start -eq "y") {
    Write-Host ""
    Write-Host "🚀 Starting AutoShield Backend..." -ForegroundColor Cyan
    Write-Host "   (This will take 30-60 seconds on first run)" -ForegroundColor Gray
    Write-Host ""
    
    # Start backend in new window
    $backendPath = "D:\webautoshild\autoshield-backend"
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$backendPath'; Write-Host '🛡️  AutoShield Backend' -ForegroundColor Cyan; Write-Host 'Starting on http://localhost:8080' -ForegroundColor Yellow; Write-Host ''; mvn spring-boot:run"
    
    Write-Host "✅ Backend starting in new window..." -ForegroundColor Green
    Write-Host ""
    Write-Host "⏳ Waiting 45 seconds for backend to start..." -ForegroundColor Yellow
    
    # Wait for backend to start
    Start-Sleep -Seconds 45
    
    Write-Host ""
    Write-Host "🚀 Starting AutoShield Frontend..." -ForegroundColor Cyan
    Write-Host ""
    
    # Start frontend in new window
    $frontendPath = "D:\webautoshild\autoshield-ui"
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$frontendPath'; Write-Host '🖥️  AutoShield Dashboard' -ForegroundColor Cyan; Write-Host 'Starting on http://localhost:8081' -ForegroundColor Yellow; Write-Host ''; mvn spring-boot:run"
    
    Write-Host "✅ Frontend starting in new window..." -ForegroundColor Green
    Write-Host ""
    Write-Host "⏳ Waiting 30 seconds for frontend to start..." -ForegroundColor Yellow
    Start-Sleep -Seconds 30
    
    Write-Host ""
    Write-Host "🌐 Opening dashboard in browser..." -ForegroundColor Cyan
    Start-Process "http://localhost:8081"
    
    Write-Host ""
    Write-Host "════════════════════════════════════════════════════════" -ForegroundColor Green
    Write-Host "✅ AutoShield is now running!" -ForegroundColor Green
    Write-Host "════════════════════════════════════════════════════════" -ForegroundColor Green
    Write-Host ""
    Write-Host "Dashboard: http://localhost:8081" -ForegroundColor White
    Write-Host "Login:     admin / admin123" -ForegroundColor White
    Write-Host ""
    Write-Host "To stop: Close the Backend and Frontend PowerShell windows" -ForegroundColor Gray
    Write-Host ""
    
} else {
    Write-Host ""
    Write-Host "To start AutoShield manually:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Terminal 1 - Backend:" -ForegroundColor Cyan
    Write-Host "  cd D:\webautoshild\autoshield-backend" -ForegroundColor White
    Write-Host "  mvn spring-boot:run" -ForegroundColor White
    Write-Host ""
    Write-Host "Terminal 2 - Frontend:" -ForegroundColor Cyan
    Write-Host "  cd D:\webautoshild\autoshield-ui" -ForegroundColor White
    Write-Host "  mvn spring-boot:run" -ForegroundColor White
    Write-Host ""
}

Write-Host "Press any key to exit..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
