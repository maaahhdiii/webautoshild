#!/usr/bin/env python3
import requests
import json
from datetime import datetime

# Alert from Dashboard
alert = {
    "id": 108,
    "timestamp": "2025-12-01 02:43:44",
    "severity": "CRITICAL",
    "type": "MALICIOUS_PROCESS",
    "source_ip": "192.168.100.64",
    "status": "ACTIVE",
    "details": "AI DETECTED: Suspicious process detected - crypto miner activity"
}

print(f"\n{'='*70}")
print(f"  🚨 TRIGGERING AUTOMATED THREAT RESPONSE")
print(f"{'='*70}\n")
print(f"Alert ID:     {alert['id']}")
print(f"Type:         {alert['type']}")
print(f"Source IP:    {alert['source_ip']}")
print(f"Severity:     {alert['severity']}")
print(f"Details:      {alert['details']}\n")

# Submit to AI Threat Response Orchestrator
url = "http://localhost:8000/api/v1/analyze-threat"

payload = {
    "event_type": alert['type'].lower(),
    "source_ip": alert['source_ip'],
    "metadata": {
        "alert_id": alert['id'],
        "severity": alert['severity'],
        "details": alert['details'],
        "timestamp": alert['timestamp'],
        "threat_indicators": [
            "crypto_miner_detected",
            "high_cpu_usage",
            "unauthorized_process"
        ]
    }
}

print("📡 Submitting to AI Threat Response Engine...")
print(f"   Endpoint: {url}\n")

try:
    response = requests.post(url, json=payload, timeout=10)
    
    if response.status_code == 200:
        result = response.json()
        
        print("✅ AI Analysis Complete\n")
        print(f"{'='*70}")
        print(f"  AI THREAT ASSESSMENT")
        print(f"{'='*70}\n")
        
        print(f"Threat Level:    {result.get('threat_level', 'N/A').upper()}")
        print(f"Threat Score:    {result.get('threat_score', 0)}/100")
        print(f"Action Taken:    {result.get('action_taken', 'N/A').upper()}\n")
        
        # Details
        details = result.get('details', {})
        if details:
            print("📊 Analysis Details:")
            for key, value in details.items():
                if key != 'message':
                    print(f"   • {key}: {value}")
            print()
        
        # Recommendations
        recommendations = result.get('recommendations', [])
        if recommendations:
            print("💡 AI Recommendations:")
            for i, rec in enumerate(recommendations, 1):
                print(f"   {i}. {rec}")
            print()
        
        print(f"{'='*70}\n")
        
        # Now trigger full automated response with SSH actions
        print("🤖 Triggering Automated Response with Proxmox Actions...\n")
        
        ai_url = f"http://localhost:8000/api/v1/ai/threat-response?event_type={alert['type'].lower()}&source_ip={alert['source_ip']}"
        
        ai_payload = {
            "alert_id": alert['id'],
            "process_name": "xmrig",
            "cpu_usage": 95,
            "threat_indicators": ["crypto_mining", "unauthorized_process"]
        }
        
        ai_response = requests.post(ai_url, json=ai_payload, timeout=30)
        
        if ai_response.status_code == 200:
            ai_result = ai_response.json()
            
            print("✅ Automated Response Triggered\n")
            print(f"{'='*70}")
            print(f"  EXECUTION PLAN")
            print(f"{'='*70}\n")
            
            # Display AI decision
            ai_decision = ai_result.get('response', {}).get('ai_decision', {})
            exec_plan = ai_decision.get('execution_plan', {})
            
            if exec_plan:
                steps = exec_plan.get('steps', [])
                print(f"Total Steps:     {len(steps)}")
                print(f"Estimated Time:  {exec_plan.get('estimated_duration', 0)}s\n")
                
                print("📋 Actions to Execute:")
                for i, step in enumerate(steps, 1):
                    print(f"\n   Step {i}: {step.get('action', 'N/A').upper()}")
                    print(f"   Priority: {step.get('priority', 'N/A')}")
                    print(f"   Description: {step.get('description', 'N/A')}")
                    
                    commands = step.get('commands', [])
                    if commands:
                        print(f"   Commands:")
                        for cmd in commands:
                            print(f"      → {cmd}")
                
                print(f"\n{'='*70}\n")
                
                # Execution status
                exec_result = ai_result.get('response', {}).get('execution_result', {})
                status = exec_result.get('status', 'unknown')
                message = exec_result.get('message', '')
                
                if status == 'pending_approval':
                    print("⚠️  STATUS: PENDING MANUAL APPROVAL")
                    print(f"   {message}\n")
                    print("   Reason: AUTO_EXECUTE_THREATS=false in configuration")
                    print("   To enable auto-execution, set AUTO_EXECUTE_THREATS=true in .env\n")
                elif status == 'dry_run':
                    print("🧪 STATUS: DRY RUN MODE")
                    print("   Commands simulated but NOT executed on Proxmox\n")
                    print("   To enable real execution:")
                    print("   1. Set DRY_RUN_MODE=false in .env")
                    print("   2. Set AUTO_EXECUTE_THREATS=true in .env\n")
                elif status == 'executed':
                    print("✅ STATUS: EXECUTED ON PROXMOX")
                    print(f"   {message}\n")
                    print("   Threat has been automatically mitigated!\n")
                
            # Rollback plan
            rollback = ai_decision.get('rollback_plan', {})
            if rollback:
                print("🔄 Rollback Available:")
                print(f"   Strategy: {rollback.get('strategy', 'N/A')}")
                print(f"   Timeout: {rollback.get('timeout_hours', 0)} hours")
                print(f"   Requires Approval: {rollback.get('requires_approval', True)}\n")
        
        print(f"{'='*70}")
        print(f"  ✅ AUTOMATED RESPONSE COMPLETE")
        print(f"{'='*70}\n")
        
        print(f"🎯 Next Steps:")
        print(f"   • Check Alert ID {alert['id']} status in dashboard")
        print(f"   • Review execution logs")
        print(f"   • Monitor Proxmox for blocked IP: {alert['source_ip']}\n")
        
    else:
        print(f"❌ Error: HTTP {response.status_code}")
        print(f"   {response.text}")

except requests.exceptions.ConnectionError:
    print("❌ Error: Cannot connect to Python AI service")
    print("   Make sure the service is running on http://localhost:8000")
except Exception as e:
    print(f"❌ Error: {e}")
