# Complete System Test - AutoShield with AI Threat Response
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  AUTOSHIELD COMPLETE SYSTEM TEST" -ForegroundColor Cyan
Write-Host "  AI + Detection + Response" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$backendUrl = "http://localhost:8080"
$pythonUrl = "http://localhost:8000"
$frontendUrl = "http://localhost:8081"
$proxmoxTarget = "192.168.100.64"
$credentials = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("admin:admin123"))
$authHeaders = @{ "Authorization" = "Basic $credentials" }

# TEST 1: Check all services
Write-Host "[1/6] Verification des services..." -ForegroundColor Yellow
Write-Host ""

try {
    $backend = Invoke-RestMethod -Uri "$backendUrl/actuator/health" -TimeoutSec 5
    Write-Host "  Backend (Java):  $($backend.status)" -ForegroundColor $(if ($backend.status -eq "UP") { "Green" } else { "Red" })
} catch {
    Write-Host "  Backend (Java):  DOWN" -ForegroundColor Red
}

try {
    $python = Invoke-RestMethod -Uri "$pythonUrl/health" -TimeoutSec 5
    Write-Host "  Python AI:       $($python.status)" -ForegroundColor $(if ($python.status -eq "healthy") { "Green" } else { "Red" })
} catch {
    Write-Host "  Python AI:       DOWN" -ForegroundColor Red
}

try {
    Invoke-WebRequest -Uri $frontendUrl -TimeoutSec 5 | Out-Null
    Write-Host "  Frontend (UI):   UP" -ForegroundColor Green
} catch {
    Write-Host "  Frontend (UI):   DOWN (demarrage...)" -ForegroundColor Yellow
}

Write-Host ""

# TEST 2: Test AI Decision Engine
Write-Host "[2/6] Test AI Decision Engine..." -ForegroundColor Yellow

$aiTestPayload = @{
    event_type = "brute_force_ssh"
    severity = "critical"
    source_ip = $proxmoxTarget
    target_ip = "192.168.100.1"
    description = "15 failed SSH login attempts in 30 seconds"
    timestamp = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
    metadata = @{
        failed_attempts = 15
        duration_seconds = 30
        usernames_tried = @("root", "admin", "user")
    }
} | ConvertTo-Json

try {
    $aiResponse = Invoke-RestMethod -Uri "$pythonUrl/api/v1/ai/threat-response" -Method POST -Body $aiTestPayload -ContentType "application/json"
    Write-Host "  Threat Score:    $($aiResponse.analysis.threat_score)/100" -ForegroundColor $(if ($aiResponse.analysis.threat_score -gt 70) { "Red" } else { "Yellow" })
    Write-Host "  Severity:        $($aiResponse.analysis.severity)" -ForegroundColor Yellow
    Write-Host "  Actions Planned: $($aiResponse.execution_plan.actions.Count)" -ForegroundColor Cyan
    Write-Host "  Execution ID:    $($aiResponse.execution_id)" -ForegroundColor Gray
    $executionId = $aiResponse.execution_id
} catch {
    Write-Host "  AI Engine Error: $($_.Exception.Message)" -ForegroundColor Red
    $executionId = $null
}

Write-Host ""

# TEST 3: Create alerts via webhook
Write-Host "[3/6] Creation d'alertes de test..." -ForegroundColor Yellow

$threats = @(
    @{
        eventType = "SSH_BRUTE_FORCE"
        severity = "CRITICAL"
        sourceIp = $proxmoxTarget
        description = "AI DETECTED: 15 failed SSH attempts from Proxmox server"
    },
    @{
        eventType = "PORT_SCAN_DETECTED"
        severity = "HIGH"
        sourceIp = $proxmoxTarget
        description = "AI DETECTED: Aggressive port scanning - 500 ports in 10 seconds"
    },
    @{
        eventType = "MALICIOUS_PROCESS"
        severity = "CRITICAL"
        sourceIp = $proxmoxTarget
        description = "AI DETECTED: Suspicious process detected - crypto miner activity"
    }
)

$alertsCreated = 0
foreach ($threat in $threats) {
    try {
        Invoke-RestMethod -Uri "$backendUrl/api/v1/webhook/python" -Method POST -Body ($threat | ConvertTo-Json) -ContentType "application/json" | Out-Null
        Write-Host "  Created: $($threat.eventType)" -ForegroundColor Green
        $alertsCreated++
        Start-Sleep -Milliseconds 500
    } catch {
        Write-Host "  Failed: $($threat.eventType)" -ForegroundColor Red
    }
}

Write-Host "  Total: $alertsCreated alertes creees" -ForegroundColor Cyan
Write-Host ""

# TEST 4: Check alerts and actions
Write-Host "[4/6] Verification des alertes..." -ForegroundColor Yellow

try {
    $alerts = Invoke-RestMethod -Uri "$backendUrl/api/v1/alerts/recent?hours=1" -Headers $authHeaders
    $proxmoxAlerts = $alerts | Where-Object { $_.sourceIp -eq $proxmoxTarget }
    
    Write-Host "  Total Alerts:    $($alerts.Count)" -ForegroundColor Cyan
    Write-Host "  From Proxmox:    $($proxmoxAlerts.Count)" -ForegroundColor Yellow
    
    $criticalCount = ($proxmoxAlerts | Where-Object { $_.severity -eq "CRITICAL" }).Count
    $highCount = ($proxmoxAlerts | Where-Object { $_.severity -eq "HIGH" }).Count
    
    Write-Host "  - CRITICAL:      $criticalCount" -ForegroundColor Red
    Write-Host "  - HIGH:          $highCount" -ForegroundColor Yellow
    
} catch {
    Write-Host "  Error checking alerts" -ForegroundColor Red
}

Write-Host ""

# TEST 5: Check AI execution history
Write-Host "[5/6] Historique des executions AI..." -ForegroundColor Yellow

try {
    $history = Invoke-RestMethod -Uri "$pythonUrl/api/v1/ai/execution-history?limit=5"
    Write-Host "  Total Executions: $($history.total)" -ForegroundColor Cyan
    Write-Host "  Recent:"
    
    $history.executions | Select-Object -First 3 | ForEach-Object {
        $status = if ($_.status -eq "success") { "Green" } elseif ($_.status -eq "failed") { "Red" } else { "Yellow" }
        Write-Host "    - $($_.threat_type) | Status: $($_.status) | Score: $($_.threat_score)" -ForegroundColor $status
    }
} catch {
    Write-Host "  No execution history yet" -ForegroundColor Gray
}

Write-Host ""

# TEST 6: System Statistics
Write-Host "[6/6] Statistiques du systeme..." -ForegroundColor Yellow

try {
    $stats = Invoke-RestMethod -Uri "$pythonUrl/api/v1/ai/orchestrator-status"
    Write-Host "  AI Status:       $($stats.status)" -ForegroundColor $(if ($stats.status -eq "operational") { "Green" } else { "Yellow" })
    Write-Host "  Mode:            $($stats.config.dry_run_mode)" -ForegroundColor $(if ($stats.config.dry_run_mode) { "Yellow" } else { "Green" })
    Write-Host "  Auto Execute:    $($stats.config.auto_execute_threats)" -ForegroundColor $(if ($stats.config.auto_execute_threats) { "Green" } else { "Yellow" })
    Write-Host "  Total Actions:   $($stats.stats.total_actions)" -ForegroundColor Cyan
    Write-Host "  Success Rate:    $($stats.stats.success_rate)%" -ForegroundColor Green
} catch {
    Write-Host "  AI Stats unavailable" -ForegroundColor Gray
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  TEST COMPLET!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Acces au systeme:" -ForegroundColor White
Write-Host "  Dashboard:  $frontendUrl" -ForegroundColor Cyan
Write-Host "  Alertes:    $frontendUrl/alerts" -ForegroundColor Cyan
Write-Host "  API Docs:   $backendUrl/swagger-ui.html" -ForegroundColor Cyan
Write-Host "  Python AI:  $pythonUrl/docs" -ForegroundColor Cyan
Write-Host ""
Write-Host "Credentials: admin / admin123" -ForegroundColor Yellow
Write-Host ""
Write-Host "Menaces detectees depuis votre serveur Proxmox:" -ForegroundColor Red
Write-Host "$proxmoxTarget - $($proxmoxAlerts.Count) alertes actives" -ForegroundColor Yellow
Write-Host ""
