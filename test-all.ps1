# AutoShield System Test Script
# Tests all components: Backend API, Frontend UI, Python AI Service, and Proxmox integration

Write-Host ""
Write-Host "================================" -ForegroundColor Cyan
Write-Host "  AutoShield System Tests" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

$testResults = @()
$backendUrl = "http://localhost:8080"
$frontendUrl = "http://localhost:8081"
$pythonUrl = "http://localhost:8000"
$credentials = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("admin:admin123"))

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Url,
        [string]$Method = "GET",
        [hashtable]$Headers = @{},
        [string]$Body = $null,
        [int]$ExpectedStatus = 200
    )
    
    Write-Host "Testing: $Name..." -ForegroundColor Yellow -NoNewline
    
    try {
        $params = @{
            Uri = $Url
            Method = $Method
            Headers = $Headers
            TimeoutSec = 10
            UseBasicParsing = $true
        }
        
        if ($Body) {
            $params.Body = $Body
            $params.ContentType = "application/json"
        }
        
        $response = Invoke-WebRequest @params -ErrorAction Stop
        
        if ($response.StatusCode -eq $ExpectedStatus) {
            Write-Host " PASS" -ForegroundColor Green
            return @{ Name = $Name; Status = "PASS"; StatusCode = $response.StatusCode }
        } else {
            Write-Host " FAIL (Status: $($response.StatusCode))" -ForegroundColor Red
            return @{ Name = $Name; Status = "FAIL"; StatusCode = $response.StatusCode }
        }
    }
    catch {
        Write-Host " FAIL ($($_.Exception.Message))" -ForegroundColor Red
        return @{ Name = $Name; Status = "FAIL"; Error = $_.Exception.Message }
    }
}

# 1. Test Backend Health
Write-Host ""
Write-Host "=== Backend API Tests ===" -ForegroundColor Cyan
$testResults += Test-Endpoint -Name "Backend Health Check" -Url "$backendUrl/actuator/health"
$testResults += Test-Endpoint -Name "Backend Swagger UI" -Url "$backendUrl/swagger-ui.html"
$testResults += Test-Endpoint -Name "Backend API Docs" -Url "$backendUrl/v3/api-docs"

# 2. Test Backend Endpoints with Authentication
$authHeaders = @{
    "Authorization" = "Basic $credentials"
}

$testResults += Test-Endpoint -Name "Get All Alerts" -Url "$backendUrl/api/v1/alerts" -Headers $authHeaders
$testResults += Test-Endpoint -Name "Get Recent Alerts" -Url "$backendUrl/api/v1/alerts/recent?limit=5" -Headers $authHeaders
$testResults += Test-Endpoint -Name "Get Current Metrics" -Url "$backendUrl/api/v1/metrics/current" -Headers $authHeaders
$testResults += Test-Endpoint -Name "Get Firewall Rules" -Url "$backendUrl/api/v1/firewall/rules" -Headers $authHeaders
$testResults += Test-Endpoint -Name "Get System Health" -Url "$backendUrl/api/v1/health" -Headers $authHeaders

# 3. Test Frontend
Write-Host ""
Write-Host "=== Frontend UI Tests ===" -ForegroundColor Cyan
$testResults += Test-Endpoint -Name "Frontend Home Page" -Url "$frontendUrl/"
$testResults += Test-Endpoint -Name "Frontend Login Page" -Url "$frontendUrl/login" -ExpectedStatus 200

# 4. Test Python AI Service
Write-Host ""
Write-Host "=== Python AI Service Tests ===" -ForegroundColor Cyan
$testResults += Test-Endpoint -Name "Python AI Health Check" -Url "$pythonUrl/"

# Skip scan status test as it requires an actual scan ID
Write-Host "Testing: Python AI Scan Status..." -ForegroundColor Yellow -NoNewline
Write-Host " SKIP (Requires active scan)" -ForegroundColor Gray
$testResults += @{ Name = "Python AI Scan Status"; Status = "SKIP" }

# 5. Test Backend-to-Python Integration
Write-Host ""
Write-Host "=== Integration Tests ===" -ForegroundColor Cyan

try {
    Write-Host "Testing: Backend can reach Python AI..." -ForegroundColor Yellow -NoNewline
    
    # First check if Python AI is available
    try {
        Invoke-RestMethod -Uri "$pythonUrl/" -TimeoutSec 2 | Out-Null
    } catch {
        Write-Host " SKIP (Python AI not running)" -ForegroundColor Gray
        $testResults += @{ Name = "Backend-to-Python Integration"; Status = "SKIP" }
        throw "Python AI not available"
    }
    
    $scanBody = @{
        targetIp = "127.0.0.1"
        scanType = "quick"
    } | ConvertTo-Json
    
    $scanResponse = Invoke-RestMethod -Uri "$backendUrl/api/v1/scan/trigger" -Method POST -Headers $authHeaders -Body $scanBody -ContentType "application/json" -TimeoutSec 15
    Write-Host " PASS (Scan ID: $($scanResponse.scanId))" -ForegroundColor Green
    $testResults += @{ Name = "Backend-to-Python Integration"; Status = "PASS"; ScanId = $scanResponse.scanId }
}
catch {
    if ($_.Exception.Message -notlike "*Python AI not available*") {
        Write-Host " FAIL ($($_.Exception.Message))" -ForegroundColor Red
        $testResults += @{ Name = "Backend-to-Python Integration"; Status = "FAIL"; Error = $_.Exception.Message }
    }
}

# 6. Test Proxmox Integration
Write-Host ""
Write-Host "=== Proxmox Integration Tests ===" -ForegroundColor Cyan

try {
    Write-Host "Testing: Proxmox Metrics Collection..." -ForegroundColor Yellow -NoNewline
    # The metrics endpoint will attempt to fetch from Proxmox
    $metricsResponse = Invoke-RestMethod -Uri "$backendUrl/api/v1/metrics/current" -Method GET -Headers $authHeaders -TimeoutSec 10
    
    if ($metricsResponse.cpuUsage -ne $null) {
        Write-Host " PASS (CPU: $($metricsResponse.cpuUsage)%, RAM: $($metricsResponse.ramUsage)%)" -ForegroundColor Green
        $testResults += @{ Name = "Proxmox Metrics Collection"; Status = "PASS" }
    } else {
        Write-Host " PARTIAL (No data from Proxmox)" -ForegroundColor Yellow
        $testResults += @{ Name = "Proxmox Metrics Collection"; Status = "PARTIAL" }
    }
}
catch {
    Write-Host " FAIL ($($_.Exception.Message))" -ForegroundColor Red
    $testResults += @{ Name = "Proxmox Metrics Collection"; Status = "FAIL"; Error = $_.Exception.Message }
}

# 7. Test Database Operations
Write-Host ""
Write-Host "=== Database Tests ===" -ForegroundColor Cyan

# Skip alert creation test as backend doesn't support POST to /api/v1/alerts
Write-Host "Testing: Database Read Operations..." -ForegroundColor Yellow -NoNewline
try {
    $alertsResponse = Invoke-RestMethod -Uri "$backendUrl/api/v1/alerts" -Method GET -Headers $authHeaders -TimeoutSec 5
    Write-Host " PASS (Found $($alertsResponse.content.Count) alerts)" -ForegroundColor Green
    $testResults += @{ Name = "Database Read Operations"; Status = "PASS" }
}
catch {
    Write-Host " FAIL ($($_.Exception.Message))" -ForegroundColor Red
    $testResults += @{ Name = "Database Read Operations"; Status = "FAIL"; Error = $_.Exception.Message }
}

# 8. Summary
Write-Host ""
Write-Host "================================" -ForegroundColor Cyan
Write-Host "  Test Summary" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

$passCount = ($testResults | Where-Object { $_.Status -eq "PASS" }).Count
$failCount = ($testResults | Where-Object { $_.Status -eq "FAIL" }).Count
$partialCount = ($testResults | Where-Object { $_.Status -eq "PARTIAL" }).Count
$skipCount = ($testResults | Where-Object { $_.Status -eq "SKIP" }).Count
$totalCount = $testResults.Count

Write-Host "Total Tests: $totalCount" -ForegroundColor White
Write-Host "Passed:      $passCount" -ForegroundColor Green
Write-Host "Failed:      $failCount" -ForegroundColor Red
if ($partialCount -gt 0) {
    Write-Host "Partial:     $partialCount" -ForegroundColor Yellow
}
if ($skipCount -gt 0) {
    Write-Host "Skipped:     $skipCount" -ForegroundColor Gray
}
Write-Host ""

# Detailed Results
Write-Host "Detailed Results:" -ForegroundColor Cyan
foreach ($result in $testResults) {
    $statusColor = switch ($result.Status) {
        "PASS" { "Green" }
        "FAIL" { "Red" }
        "PARTIAL" { "Yellow" }
        default { "White" }
    }
    
    Write-Host "  [$($result.Status)]" -ForegroundColor $statusColor -NoNewline
    Write-Host " $($result.Name)" -ForegroundColor White
    
    if ($result.Error) {
        Write-Host "        Error: $($result.Error)" -ForegroundColor Gray
    }
}

Write-Host ""

# Overall Status
if ($failCount -eq 0 -and $partialCount -eq 0) {
    Write-Host "Overall Status: ALL TESTS PASSED" -ForegroundColor Green
    exit 0
} elseif ($failCount -eq 0) {
    Write-Host "Overall Status: PASSED WITH WARNINGS" -ForegroundColor Yellow
    exit 0
} else {
    Write-Host "Overall Status: SOME TESTS FAILED" -ForegroundColor Red
    exit 1
}
