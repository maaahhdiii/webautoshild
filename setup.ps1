# AutoShield Setup Script for Windows
# Run this script as Administrator

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   AutoShield Installation Script      " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if running as Administrator
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if (-not $isAdmin) {
    Write-Host "❌ This script must be run as Administrator!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Right-click PowerShell and select 'Run as Administrator', then run this script again." -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ Running as Administrator" -ForegroundColor Green
Write-Host ""

# Check current Java version
Write-Host "Checking Java installation..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "Current Java: $javaVersion" -ForegroundColor Cyan
    
    if ($javaVersion -match "1\.8" -or $javaVersion -match '"8\.') {
        Write-Host "⚠️  Java 8 detected. Java 21 is required." -ForegroundColor Yellow
        $needsJava = $true
    } elseif ($javaVersion -match '"21\.') {
        Write-Host "✅ Java 21 already installed!" -ForegroundColor Green
        $needsJava = $false
    } else {
        Write-Host "⚠️  Incompatible Java version. Java 21 is required." -ForegroundColor Yellow
        $needsJava = $true
    }
} catch {
    Write-Host "❌ Java not found!" -ForegroundColor Red
    $needsJava = $true
}

Write-Host ""

# Check Maven
Write-Host "Checking Maven installation..." -ForegroundColor Yellow
try {
    $mavenVersion = mvn -version 2>&1 | Select-String "Apache Maven"
    Write-Host "✅ Maven found: $mavenVersion" -ForegroundColor Green
    $needsMaven = $false
} catch {
    Write-Host "❌ Maven not found!" -ForegroundColor Red
    $needsMaven = $true
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan

# Check if Chocolatey is available
$hasChoco = $false
try {
    choco --version | Out-Null
    $hasChoco = $true
    Write-Host "✅ Chocolatey is installed" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Chocolatey not found" -ForegroundColor Yellow
}

Write-Host ""

if ($needsJava -or $needsMaven) {
    Write-Host "Installation Options:" -ForegroundColor Cyan
    Write-Host ""
    
    if ($hasChoco) {
        Write-Host "Option 1: Automatic Installation (Recommended)" -ForegroundColor Green
        Write-Host "  - Uses Chocolatey package manager" -ForegroundColor Gray
        Write-Host "  - Installs Java 21 and Maven automatically" -ForegroundColor Gray
        Write-Host ""
        Write-Host "Option 2: Manual Installation" -ForegroundColor Yellow
        Write-Host "  - Download and install manually" -ForegroundColor Gray
        Write-Host ""
        
        $choice = Read-Host "Choose option (1 or 2)"
        
        if ($choice -eq "1") {
            Write-Host ""
            Write-Host "Installing via Chocolatey..." -ForegroundColor Cyan
            
            if ($needsJava) {
                Write-Host "Installing Java 21..." -ForegroundColor Yellow
                choco install temurin21 -y
            }
            
            if ($needsMaven) {
                Write-Host "Installing Maven..." -ForegroundColor Yellow
                choco install maven -y
            }
            
            Write-Host ""
            Write-Host "✅ Installation complete!" -ForegroundColor Green
            Write-Host ""
            Write-Host "⚠️  IMPORTANT: Please restart PowerShell and run this script again to verify installation." -ForegroundColor Yellow
            exit 0
        }
    }
    
    # Manual installation instructions
    Write-Host ""
    Write-Host "Manual Installation Steps:" -ForegroundColor Cyan
    Write-Host ""
    
    if ($needsJava) {
        Write-Host "1. Install Java 21:" -ForegroundColor Yellow
        Write-Host "   Download from: https://adoptium.net/temurin/releases/?version=21" -ForegroundColor White
        Write-Host "   Choose: Windows x64 MSI installer" -ForegroundColor Gray
        Write-Host "   Run the installer (it will configure PATH automatically)" -ForegroundColor Gray
        Write-Host ""
    }
    
    if ($needsMaven) {
        Write-Host "2. Install Maven:" -ForegroundColor Yellow
        Write-Host "   Download from: https://maven.apache.org/download.cgi" -ForegroundColor White
        Write-Host "   Choose: apache-maven-3.9.6-bin.zip" -ForegroundColor Gray
        Write-Host "   Extract to: C:\Program Files\Apache\maven" -ForegroundColor Gray
        Write-Host ""
        Write-Host "   Then run this command to add to PATH:" -ForegroundColor Gray
        Write-Host '   [Environment]::SetEnvironmentVariable("Path", $env:Path + ";C:\Program Files\Apache\maven\bin", "Machine")' -ForegroundColor Cyan
        Write-Host ""
    }
    
    Write-Host "After installation, restart PowerShell and run this script again." -ForegroundColor Yellow
    
} else {
    Write-Host "✅ All requirements met!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Ready to run AutoShield!" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "To start the application:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Terminal 1 - Backend:" -ForegroundColor Cyan
    Write-Host "  cd D:\webautoshild\autoshield-backend" -ForegroundColor White
    Write-Host "  mvn spring-boot:run" -ForegroundColor White
    Write-Host ""
    Write-Host "Terminal 2 - Frontend:" -ForegroundColor Cyan
    Write-Host "  cd D:\webautoshild\autoshield-ui" -ForegroundColor White
    Write-Host "  mvn spring-boot:run" -ForegroundColor White
    Write-Host ""
    Write-Host "Then open: http://localhost:8081" -ForegroundColor Green
    Write-Host "Login: admin / admin123" -ForegroundColor Green
    Write-Host ""
    
    $startNow = Read-Host "Do you want to start the backend now? (Y/N)"
    
    if ($startNow -eq "Y" -or $startNow -eq "y") {
        Write-Host ""
        Write-Host "Starting AutoShield Backend..." -ForegroundColor Cyan
        Write-Host "Press Ctrl+C to stop" -ForegroundColor Gray
        Write-Host ""
        Set-Location "D:\webautoshild\autoshield-backend"
        mvn spring-boot:run
    }
}
