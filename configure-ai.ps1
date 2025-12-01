# Configuration AutoShield AI - Reponse Automatique
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  CONFIGURATION AI THREAT RESPONSE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$envFile = "d:\webautoshild\python_ai_service\.env"

Write-Host "Pour activer la reponse automatique, configurez:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. Editez le fichier: $envFile" -ForegroundColor White
Write-Host ""
Write-Host "2. Remplacez 'your_proxmox_password_here' par le vrai mot de passe" -ForegroundColor White
Write-Host ""
Write-Host "3. Parametres de securite:" -ForegroundColor Yellow
Write-Host "   - DRY_RUN_MODE=true        (mode test, pas d'actions reelles)" -ForegroundColor Gray
Write-Host "   - DRY_RUN_MODE=false       (mode production, actions reelles)" -ForegroundColor Gray
Write-Host ""
Write-Host "   - AUTO_EXECUTE_THREATS=true   (execution automatique)" -ForegroundColor Gray
Write-Host "   - AUTO_EXECUTE_THREATS=false  (approbation manuelle)" -ForegroundColor Gray
Write-Host ""
Write-Host "4. Redemarrez le Python AI service:" -ForegroundColor Yellow
Write-Host "   cd d:\webautoshild\python_ai_service" -ForegroundColor White
Write-Host "   python main.py" -ForegroundColor White
Write-Host ""
Write-Host "5. Testez la connexion:" -ForegroundColor Yellow
Write-Host "   curl http://localhost:8000/api/v1/ai/test-connection" -ForegroundColor White
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "IMPORTANT:" -ForegroundColor Red
Write-Host "Commencez TOUJOURS avec DRY_RUN_MODE=true pour tester!" -ForegroundColor Red
Write-Host ""

# Ouvrir le fichier pour edition
Write-Host "Voulez-vous ouvrir le fichier .env maintenant? (O/N)" -ForegroundColor Yellow
$response = Read-Host

if ($response -eq "O" -or $response -eq "o") {
    notepad $envFile
}
