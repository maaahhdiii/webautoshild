# Create Test Security Alerts
Write-Host ""
Write-Host "Creating test security alerts..." -ForegroundColor Yellow
Write-Host ""

$backendUrl = "http://localhost:8080"

# Test alerts data
$events = @(
    @{
        eventType = "SUSPICIOUS_SCAN"
        severity = "HIGH"
        sourceIp = "192.168.1.100"
        description = "Port scan detected - 65535 ports scanned in 30 seconds"
    },
    @{
        eventType = "BRUTE_FORCE"
        severity = "CRITICAL"
        sourceIp = "203.0.113.42"
        description = "10 failed login attempts in 2 minutes"
    },
    @{
        eventType = "MALICIOUS_IP"
        severity = "MEDIUM"
        sourceIp = "198.51.100.50"
        description = "IP blocked - repeated scanning attempts"
    },
    @{
        eventType = "NETWORK_ANOMALY"
        severity = "HIGH"
        sourceIp = "192.168.1.101"
        description = "Suspicious network activity - multiple rapid connections"
    },
    @{
        eventType = "VULNERABILITY_FOUND"
        severity = "CRITICAL"
        sourceIp = "192.168.1.102"
        description = "Critical vulnerability detected - CVE-2023-12345"
    },
    @{
        eventType = "DDoS_ATTEMPT"
        severity = "HIGH"
        sourceIp = "10.0.0.50"
        description = "Potential DDoS - 500 requests in 10 seconds"
    },
    @{
        eventType = "SQL_INJECTION"
        severity = "CRITICAL"
        sourceIp = "172.16.0.100"
        description = "SQL injection detected in login form"
    },
    @{
        eventType = "XSS_ATTACK"
        severity = "MEDIUM"
        sourceIp = "192.168.1.103"
        description = "Cross-site scripting attempt blocked"
    }
)

$created = 0
foreach ($event in $events) {
    try {
        $body = $event | ConvertTo-Json
        Invoke-RestMethod -Uri "$backendUrl/api/v1/webhook/python" -Method POST -Body $body -ContentType "application/json" -ErrorAction Stop | Out-Null
        Write-Host " Created: [$($event.severity)] $($event.eventType) from $($event.sourceIp)" -ForegroundColor Green
        $created++
        Start-Sleep -Milliseconds 200
    }
    catch {
        Write-Host " Failed: $($event.eventType)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Total alerts created: $created" -ForegroundColor Cyan
Write-Host ""
Write-Host "View alerts at: http://localhost:8081/alerts" -ForegroundColor Gray
Write-Host "Or API: http://localhost:8080/api/v1/alerts" -ForegroundColor Gray
