# AutoShield Startup Script
# This script starts both backend and frontend applications

Write-Host ""
Write-Host "================================" -ForegroundColor Cyan
Write-Host "  AutoShield Application" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Set Java and Maven paths
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot"
$env:MAVEN_HOME = "C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.11"
$env:Path = "$env:JAVA_HOME\bin;$env:MAVEN_HOME\bin;$env:Path"

# Check if backend is already running
$backendRunning = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
if ($backendRunning) {
    Write-Host "Backend already running on port 8080" -ForegroundColor Green
} else {
    Write-Host "Starting Backend on http://localhost:8080..." -ForegroundColor Yellow
    $backendPath = Join-Path $PSScriptRoot "autoshield-backend"
    $jarPath = Join-Path $backendPath "target\autoshield-backend-1.0.0.jar"
    
    if (Test-Path $jarPath) {
        Start-Process -NoNewWindow -FilePath "$env:JAVA_HOME\bin\java.exe" -ArgumentList "-jar", $jarPath -WorkingDirectory $backendPath
        Write-Host "Backend started" -ForegroundColor Green
    } else {
        Write-Host "Backend JAR not found. Please build first: cd autoshield-backend && mvn clean package" -ForegroundColor Red
    }
}

Write-Host ""

# Check if frontend is already running
$frontendRunning = Get-NetTCPConnection -LocalPort 8081 -State Listen -ErrorAction SilentlyContinue
if ($frontendRunning) {
    Write-Host "Frontend already running on port 8081" -ForegroundColor Green
} else {
    Write-Host "Starting Frontend on http://localhost:8081..." -ForegroundColor Yellow
    $frontendPath = Join-Path $PSScriptRoot "autoshield-ui"
    
    # Create a startup script for the frontend
    $javaHome = $env:JAVA_HOME
    $mavenHome = $env:MAVEN_HOME
    $pathEnv = $env:Path
    
    $frontendScript = @"
`$env:JAVA_HOME = '$javaHome'
`$env:MAVEN_HOME = '$mavenHome'
`$env:Path = '$pathEnv'
Set-Location '$frontendPath'
Write-Host 'Starting AutoShield Frontend...' -ForegroundColor Cyan
mvn spring-boot:run
"@
    
    # Save script to temp file
    $tempScript = Join-Path $env:TEMP "start-frontend.ps1"
    $frontendScript | Out-File -FilePath $tempScript -Encoding UTF8
    
    # Start frontend in new window
    Start-Process powershell -ArgumentList "-NoExit", "-ExecutionPolicy", "Bypass", "-File", $tempScript
    Write-Host "Frontend starting (in new window)" -ForegroundColor Green
}

Write-Host ""
Write-Host "Waiting for applications to start..." -ForegroundColor Cyan
Start-Sleep -Seconds 10

Write-Host ""
Write-Host "================================" -ForegroundColor Green
Write-Host "  Applications Ready!" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green
Write-Host ""
Write-Host "Dashboard:  http://localhost:8081" -ForegroundColor Yellow
Write-Host "Backend:    http://localhost:8080" -ForegroundColor Yellow
Write-Host "Swagger:    http://localhost:8080/swagger-ui.html" -ForegroundColor Yellow
Write-Host ""
Write-Host "Login credentials:" -ForegroundColor Cyan
Write-Host "  admin / admin123" -ForegroundColor White
Write-Host ""
