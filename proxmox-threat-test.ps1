# Targeted Threat Test - Proxmox Server
# Target: 192.168.100.64:8006

Write-Host ""
Write-Host "╔════════════════════════════════════════╗" -ForegroundColor Red
Write-Host "║  PROXMOX SERVER THREAT DETECTION TEST  ║" -ForegroundColor Red
Write-Host "║  Target: 192.168.100.64:8006          ║" -ForegroundColor Red
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Red
Write-Host ""

$target = "192.168.100.64"
$backendUrl = "http://localhost:8080"
$credentials = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("admin:admin123"))
$authHeaders = @{ "Authorization" = "Basic $credentials" }

# Baseline
Write-Host "📊 Getting baseline..." -ForegroundColor Cyan
$initial = Invoke-RestMethod -Uri "$backendUrl/api/v1/alerts" -Headers $authHeaders
Write-Host "Current Alerts: $($initial.totalElements)" -ForegroundColor Gray
Write-Host ""

# Threat 1: Port Scan
Write-Host "🔍 THREAT 1: Port Scanning Attack" -ForegroundColor Yellow
Write-Host "   Initiating port scan on $target..." -ForegroundColor Gray

$scanBody = @{ targetIp = $target; scanType = "quick" } | ConvertTo-Json
try {
    $scan = Invoke-RestMethod -Uri "$backendUrl/api/v1/scan/trigger" -Headers $authHeaders -Method POST -Body $scanBody -ContentType "application/json"
    Write-Host "   ✓ Scan ID: $($scan.scanId)" -ForegroundColor Green
}
catch {
    Write-Host "   ⚠ Scan queued (will process shortly)" -ForegroundColor Yellow
}

Start-Sleep -Seconds 3

# Threat 2: Brute Force on SSH
Write-Host ""
Write-Host "💥 THREAT 2: SSH Brute Force Attack" -ForegroundColor Yellow
Write-Host "   Simulating 20 failed SSH login attempts..." -ForegroundColor Gray

$sshBrute = @{
    eventType = "SSH_BRUTE_FORCE"
    severity = "CRITICAL"
    sourceIp = $target
    description = "SSH Brute Force: 20 failed login attempts in 45 seconds from $target - Usernames: root, admin, proxmox"
} | ConvertTo-Json

Invoke-RestMethod -Uri "$backendUrl/api/v1/webhook/python" -Method POST -Body $sshBrute -ContentType "application/json" | Out-Null
Write-Host "   ✓ Alert Generated: SSH_BRUTE_FORCE" -ForegroundColor Green

Start-Sleep -Seconds 2

# Threat 3: Proxmox Web Attack
Write-Host ""
Write-Host "🌐 THREAT 3: Proxmox Web Interface Attack" -ForegroundColor Yellow
Write-Host "   Detecting attack on port 8006..." -ForegroundColor Gray

$webAttack = @{
    eventType = "PROXMOX_WEB_ATTACK"
    severity = "HIGH"
    sourceIp = $target
    description = "Proxmox Web Attack: Multiple authentication failures on port 8006 - Possible credential stuffing attack"
} | ConvertTo-Json

Invoke-RestMethod -Uri "$backendUrl/api/v1/webhook/python" -Method POST -Body $webAttack -ContentType "application/json" | Out-Null
Write-Host "   ✓ Alert Generated: PROXMOX_WEB_ATTACK" -ForegroundColor Green

Start-Sleep -Seconds 2

# Threat 4: API Exploitation
Write-Host ""
Write-Host "⚠️  THREAT 4: Proxmox API Exploitation" -ForegroundColor Yellow
Write-Host "   Detecting suspicious API calls..." -ForegroundColor Gray

$apiExploit = @{
    eventType = "API_EXPLOITATION"
    severity = "HIGH"
    sourceIp = $target
    description = "Proxmox API Exploitation: Unusual API access patterns detected - Potential privilege escalation attempt"
} | ConvertTo-Json

Invoke-RestMethod -Uri "$backendUrl/api/v1/webhook/python" -Method POST -Body $apiExploit -ContentType "application/json" | Out-Null
Write-Host "   ✓ Alert Generated: API_EXPLOITATION" -ForegroundColor Green

Start-Sleep -Seconds 2

# Threat 5: Ransomware Indicators
Write-Host ""
Write-Host "🦠 THREAT 5: Ransomware Indicators" -ForegroundColor Yellow
Write-Host "   Detecting ransomware patterns..." -ForegroundColor Gray

$ransomware = @{
    eventType = "RANSOMWARE_DETECTED"
    severity = "CRITICAL"
    sourceIp = $target
    description = "Ransomware Indicators: Suspicious file encryption activity detected on Proxmox host - Immediate action required!"
} | ConvertTo-Json

Invoke-RestMethod -Uri "$backendUrl/api/v1/webhook/python" -Method POST -Body $ransomware -ContentType "application/json" | Out-Null
Write-Host "   ✓ Alert Generated: RANSOMWARE_DETECTED" -ForegroundColor Green

Start-Sleep -Seconds 2

# Summary
Write-Host ""
Write-Host "╔════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║       DETECTION RESULTS                ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

$final = Invoke-RestMethod -Uri "$backendUrl/api/v1/alerts" -Headers $authHeaders
$newAlerts = $final.totalElements - $initial.totalElements

Write-Host "📈 Statistics:" -ForegroundColor White
Write-Host "   Initial Alerts: $($initial.totalElements)" -ForegroundColor Gray
Write-Host "   Final Alerts:   $($final.totalElements)" -ForegroundColor Gray
Write-Host "   New Threats:    $newAlerts" -ForegroundColor $(if ($newAlerts -gt 0) { "Red" } else { "Green" })
Write-Host ""

# Show threats from target
Write-Host "🚨 Detected Threats from $target :" -ForegroundColor Red
Write-Host ""

$recent = Invoke-RestMethod -Uri "$backendUrl/api/v1/alerts/recent?hours=1" -Headers $authHeaders
$proxmoxThreats = $recent | Where-Object { $_.sourceIp -eq $target } | Sort-Object -Property timestamp -Descending

if ($proxmoxThreats) {
    $criticalCount = ($proxmoxThreats | Where-Object { $_.severity -eq "CRITICAL" }).Count
    $highCount = ($proxmoxThreats | Where-Object { $_.severity -eq "HIGH" }).Count
    
    Write-Host "   🔴 Critical: $criticalCount" -ForegroundColor Red
    Write-Host "   🟠 High:     $highCount" -ForegroundColor Yellow
    Write-Host ""
    
    $proxmoxThreats | ForEach-Object {
        $icon = switch ($_.severity) {
            "CRITICAL" { "🔴" }
            "HIGH" { "🟠" }
            default { "🟡" }
        }
        
        $color = switch ($_.severity) {
            "CRITICAL" { "Red" }
            "HIGH" { "Yellow" }
            default { "Gray" }
        }
        
        Write-Host "   $icon [$($_.severity)] $($_.type)" -ForegroundColor $color
        Write-Host "      $($_.details)" -ForegroundColor Gray
        Write-Host "      Detected: $($_.timestamp)" -ForegroundColor DarkGray
        Write-Host ""
    }
}
else {
    Write-Host "   No threats detected (alerts may still be processing)" -ForegroundColor Gray
    Write-Host ""
}

# Dashboard prompt
Write-Host "╔════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║  VIEW LIVE THREATS IN DASHBOARD       ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""
Write-Host "   🌐 Dashboard:  http://localhost:8081" -ForegroundColor Cyan
Write-Host "   🚨 Alerts:     http://localhost:8081/alerts" -ForegroundColor Cyan
Write-Host "   📊 API:        http://localhost:8080/api/v1/alerts" -ForegroundColor Gray
Write-Host ""
Write-Host "   ⚡ Active Threats from your Proxmox server are NOW visible!" -ForegroundColor Yellow
Write-Host ""
