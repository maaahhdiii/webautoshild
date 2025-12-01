Write-Host ""
Write-Host "AutoShield Quick Test" -ForegroundColor Cyan

$credentials = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("admin:admin123"))
$authHeaders = @{ "Authorization" = "Basic $credentials" }

Write-Host "[1/5] Backend API..." -NoNewline
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 3
    if ($health.status -eq "UP") { Write-Host " OK" -ForegroundColor Green }
} catch { Write-Host " FAIL" -ForegroundColor Red }

Write-Host "[2/5] Frontend UI..." -NoNewline
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081/" -TimeoutSec 3 -UseBasicParsing
    if ($response.StatusCode -eq 200) { Write-Host " OK" -ForegroundColor Green }
} catch { Write-Host " FAIL" -ForegroundColor Red }

Write-Host "[3/5] Python AI Service..." -NoNewline
try {
    $pythonHealth = Invoke-RestMethod -Uri "http://localhost:8000/" -TimeoutSec 3
    if ($pythonHealth.status -eq "running") { Write-Host " OK" -ForegroundColor Green }
} catch { Write-Host " FAIL" -ForegroundColor Red }

Write-Host "[4/5] API Endpoints..." -NoNewline
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/v1/alerts" -Headers $authHeaders -TimeoutSec 3 | Out-Null
    Write-Host " OK" -ForegroundColor Green
} catch { Write-Host " FAIL" -ForegroundColor Red }

Write-Host "[5/5] Swagger UI..." -NoNewline
try {
    $swagger = Invoke-WebRequest -Uri "http://localhost:8080/swagger-ui.html" -TimeoutSec 3 -UseBasicParsing
    if ($swagger.StatusCode -eq 200) { Write-Host " OK" -ForegroundColor Green }
} catch { Write-Host " FAIL" -ForegroundColor Red }

Write-Host ""
Write-Host "URLs: http://localhost:8081 | http://localhost:8080/swagger-ui.html" -ForegroundColor Yellow
Write-Host ""
