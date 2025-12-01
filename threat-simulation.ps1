# Active Threat Simulation
Write-Host ""
Write-Host "Active Threat Simulation" -ForegroundColor Red
Write-Host ""

$backendUrl = "http://localhost:8080"
$pythonUrl = "http://localhost:8000"
$credentials = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("admin:admin123"))
$authHeaders = @{ "Authorization" = "Basic $credentials" }

# Get initial alerts
$initialAlerts = Invoke-RestMethod -Uri "$backendUrl/api/v1/alerts" -Headers $authHeaders
$initialCount = $initialAlerts.content.Count
Write-Host "Initial Alerts: $initialCount" -ForegroundColor Gray
Write-Host ""

# THREAT 1: Port Scanning
Write-Host "=== THREAT 1: Port Scanning ===" -ForegroundColor Red
$scanBody = @{ targetIp = "192.168.1.100"; scanType = "full" } | ConvertTo-Json
$scan1 = Invoke-RestMethod -Uri "$backendUrl/api/v1/scan/trigger" -Headers $authHeaders -Method POST -Body $scanBody -ContentType "application/json"
Write-Host "Scan initiated: $($scan1.scanId)" -ForegroundColor Green
Start-Sleep -Seconds 2

# THREAT 2: Multiple IPs
Write-Host ""
Write-Host "=== THREAT 2: Multiple Scans ===" -ForegroundColor Red
$ips = @("192.168.1.101", "192.168.1.102", "192.168.1.103")
foreach ($ip in $ips) {
    $body = @{ targetIp = $ip; scanType = "quick" } | ConvertTo-Json
    $result = Invoke-RestMethod -Uri "$backendUrl/api/v1/scan/trigger" -Headers $authHeaders -Method POST -Body $body -ContentType "application/json"
    Write-Host "Scanning $ip... $($result.scanId)" -ForegroundColor Yellow
    Start-Sleep -Milliseconds 500
}

Start-Sleep -Seconds 2

# THREAT 3: Block Malicious IP
Write-Host ""
Write-Host "=== THREAT 3: Block Malicious IP ===" -ForegroundColor Red
$blockBody = @{ ipAddress = "198.51.100.50"; reason = "Suspicious activity"; duration = 3600 } | ConvertTo-Json
Invoke-RestMethod -Uri "$backendUrl/api/v1/firewall/block" -Headers $authHeaders -Method POST -Body $blockBody -ContentType "application/json" | Out-Null
Write-Host "IP 198.51.100.50 blocked!" -ForegroundColor Green

Start-Sleep -Seconds 2

# Summary
Write-Host ""
Write-Host "=== Detection Summary ===" -ForegroundColor Cyan
$finalAlerts = Invoke-RestMethod -Uri "$backendUrl/api/v1/alerts" -Headers $authHeaders
$finalCount = $finalAlerts.content.Count
Write-Host "Initial Alerts: $initialCount" -ForegroundColor Gray
Write-Host "Final Alerts: $finalCount" -ForegroundColor Gray
Write-Host "New Alerts: $($finalCount - $initialCount)" -ForegroundColor Green
Write-Host ""

Write-Host "Recent Threats:" -ForegroundColor Yellow
$recent = Invoke-RestMethod -Uri "$backendUrl/api/v1/alerts/recent?limit=5" -Headers $authHeaders
$recent | ForEach-Object {
    Write-Host "  [$($_.severity)] $($_.message)" -ForegroundColor Yellow
    Write-Host "    Source: $($_.sourceIp)" -ForegroundColor Gray
}

Write-Host ""
Write-Host "Firewall Rules:" -ForegroundColor Yellow
$rules = Invoke-RestMethod -Uri "$backendUrl/api/v1/firewall/rules" -Headers $authHeaders
$rules | ForEach-Object {
    Write-Host "  Blocked: $($_.ipAddress) - $($_.reason)" -ForegroundColor Red
}

Write-Host ""
Write-Host "Check dashboard: http://localhost:8081" -ForegroundColor Cyan
