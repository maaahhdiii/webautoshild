# Real Threat Test Against Proxmox Server
# Target: 192.168.100.64:8006

Write-Host ""
Write-Host "========================================" -ForegroundColor Red
Write-Host "  REAL THREAT TEST - PROXMOX SERVER" -ForegroundColor Red
Write-Host "  Target: 192.168.100.64:8006" -ForegroundColor Red
Write-Host "========================================" -ForegroundColor Red
Write-Host ""

$target = "192.168.100.64"
$backendUrl = "http://localhost:8080"
$credentials = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("admin:admin123"))
$authHeaders = @{ "Authorization" = "Basic $credentials" }

# Get initial alert count
Write-Host "[INFO] Getting baseline alert count..." -ForegroundColor Gray
$initialAlerts = Invoke-RestMethod -Uri "$backendUrl/api/v1/alerts" -Headers $authHeaders
$initialCount = $initialAlerts.totalElements
Write-Host "Initial Alerts: $initialCount" -ForegroundColor Cyan
Write-Host ""

# THREAT 1: Quick Port Scan
Write-Host "=== THREAT 1: Quick Port Scan ===" -ForegroundColor Yellow
Write-Host "Scanning common ports on $target..." -ForegroundColor Gray

try {
    $scanBody = @{
        targetIp = $target
        scanType = "quick"
    } | ConvertTo-Json
    
    $scan1 = Invoke-RestMethod -Uri "$backendUrl/api/v1/scan/trigger" -Headers $authHeaders -Method POST -Body $scanBody -ContentType "application/json"
    Write-Host " Scan initiated: $($scan1.scanId)" -ForegroundColor Green
    Write-Host " Status: $($scan1.status)" -ForegroundColor Gray
}
catch {
    Write-Host " Error: $($_.Exception.Message)" -ForegroundColor Red
}

Start-Sleep -Seconds 5

# THREAT 2: Full Port Scan (Aggressive)
Write-Host ""
Write-Host "=== THREAT 2: Full Port Scan (Aggressive) ===" -ForegroundColor Yellow
Write-Host "Performing comprehensive scan on $target..." -ForegroundColor Gray

try {
    $scanBody = @{
        targetIp = $target
        scanType = "full"
    } | ConvertTo-Json
    
    $scan2 = Invoke-RestMethod -Uri "$backendUrl/api/v1/scan/trigger" -Headers $authHeaders -Method POST -Body $scanBody -ContentType "application/json"
    Write-Host " Scan initiated: $($scan2.scanId)" -ForegroundColor Green
    Write-Host " Status: $($scan2.status)" -ForegroundColor Gray
}
catch {
    Write-Host " Error: $($_.Exception.Message)" -ForegroundColor Red
}

Start-Sleep -Seconds 3

# THREAT 3: Vulnerability Scan
Write-Host ""
Write-Host "=== THREAT 3: Vulnerability Scan ===" -ForegroundColor Yellow
Write-Host "Scanning for vulnerabilities on $target..." -ForegroundColor Gray

try {
    $scanBody = @{
        targetIp = $target
        scanType = "vulnerability"
    } | ConvertTo-Json
    
    $scan3 = Invoke-RestMethod -Uri "$backendUrl/api/v1/scan/trigger" -Headers $authHeaders -Method POST -Body $scanBody -ContentType "application/json"
    Write-Host " Scan initiated: $($scan3.scanId)" -ForegroundColor Green
    Write-Host " Status: $($scan3.status)" -ForegroundColor Gray
}
catch {
    Write-Host " Error: $($_.Exception.Message)" -ForegroundColor Red
}

Start-Sleep -Seconds 3

# THREAT 4: Simulated Brute Force Alert
Write-Host ""
Write-Host "=== THREAT 4: Simulated Brute Force Attack ===" -ForegroundColor Yellow
Write-Host "Creating brute force alert for $target..." -ForegroundColor Gray

$bruteForceEvent = @{
    eventType = "BRUTE_FORCE"
    severity = "CRITICAL"
    sourceIp = $target
    description = "Multiple failed SSH login attempts detected - 15 attempts in 60 seconds"
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "$backendUrl/api/v1/webhook/python" -Method POST -Body $bruteForceEvent -ContentType "application/json" | Out-Null
    Write-Host " Alert created: BRUTE_FORCE" -ForegroundColor Green
}
catch {
    Write-Host " Error: $($_.Exception.Message)" -ForegroundColor Red
}

Start-Sleep -Seconds 2

# THREAT 5: Suspicious Network Activity
Write-Host ""
Write-Host "=== THREAT 5: Network Anomaly Detection ===" -ForegroundColor Yellow
Write-Host "Simulating suspicious network patterns from $target..." -ForegroundColor Gray

$networkEvent = @{
    eventType = "NETWORK_ANOMALY"
    severity = "HIGH"
    sourceIp = $target
    description = "Unusual network traffic pattern detected - Multiple rapid connections to various ports"
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "$backendUrl/api/v1/webhook/python" -Method POST -Body $networkEvent -ContentType "application/json" | Out-Null
    Write-Host " Alert created: NETWORK_ANOMALY" -ForegroundColor Green
}
catch {
    Write-Host " Error: $($_.Exception.Message)" -ForegroundColor Red
}

Start-Sleep -Seconds 2

# THREAT 6: Port 8006 Specific Attack
Write-Host ""
Write-Host "=== THREAT 6: Proxmox Web Interface Attack ===" -ForegroundColor Yellow
Write-Host "Simulating attack on Proxmox web interface (port 8006)..." -ForegroundColor Gray

$proxmoxEvent = @{
    eventType = "WEB_ATTACK"
    severity = "HIGH"
    sourceIp = $target
    description = "Suspected attack on Proxmox web interface - Multiple authentication attempts on port 8006"
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "$backendUrl/api/v1/webhook/python" -Method POST -Body $proxmoxEvent -ContentType "application/json" | Out-Null
    Write-Host " Alert created: WEB_ATTACK" -ForegroundColor Green
}
catch {
    Write-Host " Error: $($_.Exception.Message)" -ForegroundColor Red
}

Start-Sleep -Seconds 2

# THREAT 7: Automated Exploit Attempt
Write-Host ""
Write-Host "=== THREAT 7: Exploit Attempt Detection ===" -ForegroundColor Yellow
Write-Host "Detecting potential exploit against $target..." -ForegroundColor Gray

$exploitEvent = @{
    eventType = "EXPLOIT_ATTEMPT"
    severity = "CRITICAL"
    sourceIp = $target
    description = "Potential exploit detected - CVE-2023-XXXX targeting Proxmox VE service"
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "$backendUrl/api/v1/webhook/python" -Method POST -Body $exploitEvent -ContentType "application/json" | Out-Null
    Write-Host " Alert created: EXPLOIT_ATTEMPT" -ForegroundColor Green
}
catch {
    Write-Host " Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Wait for scans to process
Write-Host ""
Write-Host "[INFO] Waiting 10 seconds for scans to complete..." -ForegroundColor Gray
Start-Sleep -Seconds 10

# Summary
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  DETECTION SUMMARY" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Get final alert count
$finalAlerts = Invoke-RestMethod -Uri "$backendUrl/api/v1/alerts" -Headers $authHeaders
$finalCount = $finalAlerts.totalElements
$newAlerts = $finalCount - $initialCount

Write-Host "Initial Alerts: $initialCount" -ForegroundColor Gray
Write-Host "Final Alerts:   $finalCount" -ForegroundColor Gray
Write-Host "New Threats:    $newAlerts" -ForegroundColor $(if ($newAlerts -gt 0) { "Red" } else { "Green" })
Write-Host ""

# Show recent threats related to target
Write-Host "Recent Threats from $target :" -ForegroundColor Yellow
Write-Host ""

$recentAlerts = Invoke-RestMethod -Uri "$backendUrl/api/v1/alerts/recent?hours=1" -Headers $authHeaders
$targetAlerts = $recentAlerts | Where-Object { $_.sourceIp -eq $target }

if ($targetAlerts.Count -gt 0) {
    $targetAlerts | ForEach-Object {
        $color = switch ($_.severity) {
            "CRITICAL" { "Red" }
            "HIGH" { "DarkYellow" }
            "MEDIUM" { "Yellow" }
            default { "Gray" }
        }
        Write-Host " [$($_.severity)] $($_.type)" -ForegroundColor $color
        Write-Host "    $($_.details)" -ForegroundColor Gray
        Write-Host "    Time: $($_.timestamp)" -ForegroundColor DarkGray
        Write-Host ""
    }
}
else {
    Write-Host " No alerts found for this IP yet" -ForegroundColor Gray
    Write-Host ""
}

# Check scan results
Write-Host "Scan Results:" -ForegroundColor Yellow
Write-Host ""

try {
    $scanHistory = Invoke-RestMethod -Uri "$backendUrl/api/v1/scan/history?targetIp=$target" -Headers $authHeaders
    
    if ($scanHistory.Count -gt 0) {
        $scanHistory | Select-Object -First 3 | ForEach-Object {
            Write-Host " Scan ID: $($_.scanId)" -ForegroundColor Cyan
            Write-Host "   Status: $($_.status)" -ForegroundColor Gray
            Write-Host "   Tool: $($_.toolUsed)" -ForegroundColor Gray
            Write-Host "   Threat Score: $($_.threatScore)" -ForegroundColor $(if ($_.threatScore -gt 50) { "Red" } else { "Green" })
            if ($_.openPorts) {
                Write-Host "   Open Ports: $($_.openPorts)" -ForegroundColor Gray
            }
            Write-Host ""
        }
    }
    else {
        Write-Host " No scan results available yet (scans may still be running)" -ForegroundColor Gray
        Write-Host ""
    }
}
catch {
    Write-Host " Error retrieving scan history" -ForegroundColor Red
    Write-Host ""
}

# Show dashboard link
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host " View Live Threats:" -ForegroundColor White
Write-Host "   Dashboard: http://localhost:8081" -ForegroundColor Cyan
Write-Host "   Alerts:    http://localhost:8081/alerts" -ForegroundColor Cyan
Write-Host ""
Write-Host " API Endpoints:" -ForegroundColor White
Write-Host "   Alerts:    http://localhost:8080/api/v1/alerts" -ForegroundColor Gray
Write-Host "   Scans:     http://localhost:8080/api/v1/scan/history" -ForegroundColor Gray
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
