# Rebuild Backend with Automated Threat Response
Write-Host ""
Write-Host "================================" -ForegroundColor Cyan
Write-Host " REBUILD WITH AUTO-RESPONSE" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Stop backend
Write-Host "[1/4] Stopping backend..." -ForegroundColor Yellow
$javaProcs = Get-Process -Name java -ErrorAction SilentlyContinue
if ($javaProcs) {
    $javaProcs | Stop-Process -Force
    Write-Host "  Backend stopped" -ForegroundColor Green
    Start-Sleep -Seconds 3
} else {
    Write-Host "  No backend running" -ForegroundColor Gray
}

# Compile with Maven
Write-Host ""
Write-Host "[2/4] Compiling with Maven..." -ForegroundColor Yellow
cd autoshield-backend

# Find Maven
$mavenCmd = $null
if (Test-Path "C:\Program Files\apache-maven-3.9.11\bin\mvn.cmd") {
    $mavenCmd = "C:\Program Files\apache-maven-3.9.11\bin\mvn.cmd"
} elseif (Get-Command mvn -ErrorAction SilentlyContinue) {
    $mavenCmd = "mvn"
}

if ($mavenCmd) {
    Write-Host "  Using Maven: $mavenCmd" -ForegroundColor Gray
    & $mavenCmd clean package -DskipTests -q
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  Build successful!" -ForegroundColor Green
    } else {
        Write-Host "  Build failed!" -ForegroundColor Red
        cd ..
        exit 1
    }
} else {
    Write-Host "  Maven not found - using existing JAR" -ForegroundColor Yellow
}

# Start backend
Write-Host ""
Write-Host "[3/4] Starting backend..." -ForegroundColor Yellow
$jarFile = Get-ChildItem -Path target -Filter "autoshield-backend-*.jar" | Select-Object -First 1

if ($jarFile) {
    Write-Host "  JAR: $($jarFile.Name)" -ForegroundColor Gray
    Start-Process -NoNewWindow -FilePath "C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot\bin\java.exe" -ArgumentList "-jar", "target/$($jarFile.Name)"
    Write-Host "  Backend starting..." -ForegroundColor Cyan
} else {
    Write-Host "  JAR not found!" -ForegroundColor Red
    cd ..
    exit 1
}

# Wait and test
Write-Host ""
Write-Host "[4/4] Waiting for backend..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5
    if ($health.status -eq "UP") {
        Write-Host "  Backend is UP!" -ForegroundColor Green
    }
} catch {
    Write-Host "  Backend starting (wait a moment)..." -ForegroundColor Yellow
}

cd ..

Write-Host ""
Write-Host "================================" -ForegroundColor Green
Write-Host "  AUTOMATED THREAT RESPONSE" -ForegroundColor Green
Write-Host "  IS NOW ACTIVE!" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green
Write-Host ""
Write-Host "Les menaces CRITIQUES seront bloquees 24h" -ForegroundColor Red
Write-Host "Les menaces HIGH seront bloquees 4h" -ForegroundColor Yellow
Write-Host ""
