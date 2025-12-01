# AutoShield Security Testing Script
# Simulates various attack scenarios to test threat detection and response

Write-Host ""
Write-Host "================================" -ForegroundColor Red
Write-Host "  AutoShield Security Test" -ForegroundColor Red
Write-Host "  Simulated Attack Scenarios" -ForegroundColor Red
Write-Host "================================" -ForegroundColor Red
Write-Host ""
Write-Host "WARNING: This script simulates attacks for testing purposes only!" -ForegroundColor Yellow
Write-Host "Only use on systems you own and have permission to test." -ForegroundColor Yellow
Write-Host ""

$backendUrl = "http://localhost:8080"
$frontendUrl = "http://localhost:8081"
$testResults = @()

# Attack Scenario 1: Brute Force Login Attack
Write-Host ""
Write-Host "=== Scenario 1: Brute Force Login Attack ===" -ForegroundColor Cyan
Write-Host "Simulating multiple failed login attempts..." -ForegroundColor Yellow

$commonPasswords = @("password", "123456", "admin", "letmein", "qwerty", "password123", "admin123", "root", "test")
$attemptCount = 0
$successCount = 0
$failedCount = 0

foreach ($password in $commonPasswords) {
    $attemptCount++
    Write-Host "Attempt $attemptCount : Testing password '$password'..." -NoNewline -ForegroundColor Gray
    
    try {
        $credentials = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("admin:$password"))
        $headers = @{ "Authorization" = "Basic $credentials" }
        
        $response = Invoke-RestMethod -Uri "$backendUrl/api/v1/health" -Headers $headers -Method GET -TimeoutSec 3 -ErrorAction Stop
        Write-Host " SUCCESS!" -ForegroundColor Green
        $successCount++
        break
    }
    catch {
        Write-Host " Failed" -ForegroundColor Red
        $failedCount++
        Start-Sleep -Milliseconds 500
    }
}

$testResults += @{
    Scenario = "Brute Force Login"
    Attempts = $attemptCount
    Failed = $failedCount
    Successful = $successCount
    Status = if ($successCount -gt 0) { "COMPROMISED" } else { "BLOCKED" }
}

# Attack Scenario 2: SQL Injection Attempts
Write-Host ""
Write-Host "=== Scenario 2: SQL Injection Attack ===" -ForegroundColor Cyan
Write-Host "Testing SQL injection payloads..." -ForegroundColor Yellow

$credentials = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("admin:admin123"))
$authHeaders = @{ "Authorization" = "Basic $credentials" }

$sqlPayloads = @(
    "' OR '1'='1",
    "admin'--",
    "' OR 1=1--",
    "1' UNION SELECT NULL--",
    "'; DROP TABLE alerts--"
)

$sqlBlocked = 0
$sqlPassed = 0

foreach ($payload in $sqlPayloads) {
    Write-Host "Testing payload: $payload..." -NoNewline -ForegroundColor Gray
    
    try {
        $url = "$backendUrl/api/v1/alerts?search=$([System.Web.HttpUtility]::UrlEncode($payload))"
        $response = Invoke-RestMethod -Uri $url -Headers $authHeaders -Method GET -TimeoutSec 3
        Write-Host " Passed (Not vulnerable)" -ForegroundColor Green
        $sqlPassed++
    }
    catch {
        if ($_.Exception.Response.StatusCode -eq 400 -or $_.Exception.Response.StatusCode -eq 403) {
            Write-Host " Blocked!" -ForegroundColor Green
            $sqlBlocked++
        }
        else {
            Write-Host " Error" -ForegroundColor Yellow
        }
    }
    Start-Sleep -Milliseconds 300
}

$testResults += @{
    Scenario = "SQL Injection"
    Blocked = $sqlBlocked
    Passed = $sqlPassed
    Status = if ($sqlBlocked -gt 0) { "PROTECTED" } else { "VULNERABLE" }
}

# Attack Scenario 3: Directory Traversal
Write-Host ""
Write-Host "=== Scenario 3: Directory Traversal Attack ===" -ForegroundColor Cyan
Write-Host "Testing path traversal payloads..." -ForegroundColor Yellow

$traversalPayloads = @(
    "../../../etc/passwd",
    "..\..\..\..\windows\system32\config\sam",
    "....//....//....//etc/passwd",
    "%2e%2e%2f%2e%2e%2f%2e%2e%2fetc%2fpasswd"
)

$traversalBlocked = 0
foreach ($payload in $traversalPayloads) {
    Write-Host "Testing: $payload..." -NoNewline -ForegroundColor Gray
    
    try {
        $response = Invoke-RestMethod -Uri "$backendUrl/api/v1/alerts/$payload" -Headers $authHeaders -Method GET -TimeoutSec 3
        Write-Host " Vulnerable!" -ForegroundColor Red
    }
    catch {
        Write-Host " Blocked" -ForegroundColor Green
        $traversalBlocked++
    }
    Start-Sleep -Milliseconds 300
}

$testResults += @{
    Scenario = "Directory Traversal"
    Blocked = $traversalBlocked
    Total = $traversalPayloads.Count
    Status = if ($traversalBlocked -eq $traversalPayloads.Count) { "PROTECTED" } else { "VULNERABLE" }
}

# Attack Scenario 4: XSS (Cross-Site Scripting)
Write-Host ""
Write-Host "=== Scenario 4: XSS Attack ===" -ForegroundColor Cyan
Write-Host "Testing XSS payloads..." -ForegroundColor Yellow

$xssPayloads = @(
    "<script>alert('XSS')</script>",
    "<img src=x onerror=alert('XSS')>",
    "javascript:alert('XSS')",
    "<svg/onload=alert('XSS')>"
)

$xssBlocked = 0
foreach ($payload in $xssPayloads) {
    Write-Host "Testing XSS: $($payload.Substring(0, [Math]::Min(30, $payload.Length)))..." -NoNewline -ForegroundColor Gray
    
    try {
        $body = @{ targetIp = $payload; scanType = "quick" } | ConvertTo-Json
        $response = Invoke-RestMethod -Uri "$backendUrl/api/v1/scan/trigger" -Headers $authHeaders -Method POST -Body $body -ContentType "application/json" -TimeoutSec 3
        Write-Host " Accepted (Check encoding)" -ForegroundColor Yellow
    }
    catch {
        if ($_.Exception.Response.StatusCode -eq 400) {
            Write-Host " Blocked" -ForegroundColor Green
            $xssBlocked++
        }
        else {
            Write-Host " Error" -ForegroundColor Yellow
        }
    }
    Start-Sleep -Milliseconds 300
}

$testResults += @{
    Scenario = "XSS Attack"
    Blocked = $xssBlocked
    Total = $xssPayloads.Count
    Status = if ($xssBlocked -gt $xssPayloads.Count / 2) { "PROTECTED" } else { "VULNERABLE" }
}

# Attack Scenario 5: Port Scanning Detection
Write-Host ""
Write-Host "=== Scenario 5: Port Scanning ===" -ForegroundColor Cyan
Write-Host "Triggering network scan to test detection..." -ForegroundColor Yellow

try {
    $scanBody = @{
        targetIp = "127.0.0.1"
        scanType = "quick"
    } | ConvertTo-Json
    
    Write-Host "Initiating scan on localhost..." -NoNewline -ForegroundColor Gray
    $scanResponse = Invoke-RestMethod -Uri "$backendUrl/api/v1/scan/trigger" -Headers $authHeaders -Method POST -Body $scanBody -ContentType "application/json" -TimeoutSec 10
    Write-Host " Scan initiated (ID: $($scanResponse.scanId))" -ForegroundColor Green
    
    Start-Sleep -Seconds 3
    
    # Check scan results
    Write-Host "Checking scan results..." -NoNewline -ForegroundColor Gray
    $scanStatus = Invoke-RestMethod -Uri "http://localhost:8000/api/v1/scan/$($scanResponse.scanId)" -Method GET -TimeoutSec 5
    
    if ($scanStatus.status -eq "completed") {
        Write-Host " DETECTED!" -ForegroundColor Green
        Write-Host "  Open Ports Found: $($scanStatus.results.open_ports.Count)" -ForegroundColor Yellow
        $testResults += @{
            Scenario = "Port Scanning"
            Detected = $true
            OpenPorts = $scanStatus.results.open_ports.Count
            Status = "DETECTED"
        }
    }
}
catch {
    Write-Host " Failed to scan" -ForegroundColor Red
    $testResults += @{
        Scenario = "Port Scanning"
        Detected = $false
        Status = "UNDETECTED"
    }
}

# Attack Scenario 6: API Rate Limiting Test
Write-Host ""
Write-Host "=== Scenario 6: API Rate Limiting (DoS) ===" -ForegroundColor Cyan
Write-Host "Testing rate limiting with rapid requests..." -ForegroundColor Yellow

$requestCount = 50
$blockedCount = 0
$successCount = 0
$startTime = Get-Date

for ($i = 1; $i -le $requestCount; $i++) {
    try {
        $response = Invoke-RestMethod -Uri "$backendUrl/api/v1/health" -Headers $authHeaders -Method GET -TimeoutSec 2 -ErrorAction Stop
        $successCount++
    }
    catch {
        if ($_.Exception.Response.StatusCode -eq 429) {
            $blockedCount++
        }
    }
    
    if ($i % 10 -eq 0) {
        Write-Host "  Progress: $i/$requestCount requests..." -ForegroundColor Gray
    }
}

$endTime = Get-Date
$duration = ($endTime - $startTime).TotalSeconds

Write-Host "Sent $requestCount requests in $([Math]::Round($duration, 2)) seconds" -ForegroundColor Yellow
Write-Host "Success: $successCount | Blocked: $blockedCount" -ForegroundColor Yellow

$testResults += @{
    Scenario = "Rate Limiting"
    Requests = $requestCount
    Blocked = $blockedCount
    Success = $successCount
    Status = if ($blockedCount -gt 0) { "PROTECTED" } else { "NO_LIMIT" }
}

# Attack Scenario 7: Session Hijacking Test
Write-Host ""
Write-Host "=== Scenario 7: Session Security Test ===" -ForegroundColor Cyan
Write-Host "Testing session handling..." -ForegroundColor Yellow

try {
    # Try to access protected endpoint without credentials
    Write-Host "Attempting access without authentication..." -NoNewline -ForegroundColor Gray
    $response = Invoke-RestMethod -Uri "$backendUrl/api/v1/alerts" -Method GET -TimeoutSec 3
    Write-Host " VULNERABLE (No auth required!)" -ForegroundColor Red
    $sessionStatus = "VULNERABLE"
}
catch {
    if ($_.Exception.Response.StatusCode -eq 401) {
        Write-Host " PROTECTED (401 Unauthorized)" -ForegroundColor Green
        $sessionStatus = "PROTECTED"
    }
    else {
        Write-Host " Error: $($_.Exception.Response.StatusCode)" -ForegroundColor Yellow
        $sessionStatus = "UNKNOWN"
    }
}

$testResults += @{
    Scenario = "Session Security"
    Status = $sessionStatus
}

# Summary Report
Write-Host ""
Write-Host "================================" -ForegroundColor Red
Write-Host "  Security Test Summary" -ForegroundColor Red
Write-Host "================================" -ForegroundColor Red
Write-Host ""

foreach ($result in $testResults) {
    $statusColor = switch ($result.Status) {
        "PROTECTED" { "Green" }
        "BLOCKED" { "Green" }
        "DETECTED" { "Green" }
        "VULNERABLE" { "Red" }
        "COMPROMISED" { "Red" }
        "NO_LIMIT" { "Yellow" }
        default { "Yellow" }
    }
    
    Write-Host "[$($result.Status)]" -ForegroundColor $statusColor -NoNewline
    Write-Host " $($result.Scenario)" -ForegroundColor White
    
    if ($result.Attempts) {
        Write-Host "    Attempts: $($result.Attempts) | Failed: $($result.Failed)" -ForegroundColor Gray
    }
    if ($result.Blocked) {
        Write-Host "    Blocked: $($result.Blocked)" -ForegroundColor Gray
    }
    if ($result.OpenPorts) {
        Write-Host "    Open Ports Detected: $($result.OpenPorts)" -ForegroundColor Gray
    }
}

Write-Host ""
$protectedCount = ($testResults | Where-Object { $_.Status -in @("PROTECTED", "BLOCKED", "DETECTED") }).Count
$vulnerableCount = ($testResults | Where-Object { $_.Status -in @("VULNERABLE", "COMPROMISED", "NO_LIMIT") }).Count

Write-Host "Security Score: " -NoNewline
$score = [Math]::Round(($protectedCount / $testResults.Count) * 100, 0)
$scoreColor = if ($score -ge 80) { "Green" } elseif ($score -ge 60) { "Yellow" } else { "Red" }
Write-Host "$score%" -ForegroundColor $scoreColor
Write-Host ""
Write-Host "Protected: $protectedCount | Vulnerable: $vulnerableCount" -ForegroundColor Gray
Write-Host ""

if ($vulnerableCount -eq 0) {
    Write-Host "Excellent! Your system is well protected." -ForegroundColor Green
}
elseif ($vulnerableCount -le 2) {
    Write-Host "Good security, but some improvements needed." -ForegroundColor Yellow
}
else {
    Write-Host "WARNING: Multiple vulnerabilities detected!" -ForegroundColor Red
}

Write-Host ""
