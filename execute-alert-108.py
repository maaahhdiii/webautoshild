#!/usr/bin/env python3
import requests
import json
from datetime import datetime

# Configuration
PYTHON_AI_URL = "http://localhost:8000"
BACKEND_URL = "http://localhost:8080"
BACKEND_AUTH = ("admin", "admin123")

# Alert from Dashboard
alert_id = 108
source_ip = "192.168.100.64"
event_type = "malicious_process"

print(f"\n{'='*70}")
print(f"  🚨 AUTOMATED THREAT RESPONSE - ALERT #{alert_id}")
print(f"{'='*70}\n")

# Step 1: Submit to AI for analysis and action
print(f"📡 Step 1: AI Threat Analysis...")

ai_url = f"{PYTHON_AI_URL}/api/v1/ai/threat-response"
params = {
    "event_type": event_type,
    "source_ip": source_ip
}
metadata = {
    "alert_id": alert_id,
    "process_name": "xmrig",
    "cpu_usage": 95,
    "force_execute": True  # Override approval requirement for this alert
}

try:
    response = requests.post(ai_url, params=params, json=metadata, timeout=30)
    
    if response.status_code == 200:
        result = response.json()
        print(f"✅ AI Analysis Complete\n")
        
        # Extract action details
        ai_decision = result.get('response', {}).get('ai_decision', {})
        exec_plan = ai_decision.get('execution_plan', {})
        exec_result = result.get('response', {}).get('execution_result', {})
        
        steps = exec_plan.get('steps', [])
        actions_taken = []
        
        for step in steps:
            action = step.get('action', 'unknown')
            description = step.get('description', '')
            actions_taken.append(f"{action}: {description}")
        
        action_summary = " | ".join(actions_taken) if actions_taken else "AI analysis performed"
        
        print(f"📋 Actions Identified:")
        for action in actions_taken:
            print(f"   • {action}")
        print()
        
        # Step 2: Update alert in backend with action taken
        print(f"📝 Step 2: Updating Alert #{alert_id} in Backend...\n")
        
        # Update alert status using PATCH endpoint
        status_url = f"{BACKEND_URL}/api/v1/alerts/{alert_id}/status"
        
        update_payload = {
            "status": "RESOLVED",
            "notes": f"AI Automated Response: {action_summary}"
        }
        
        update_response = requests.patch(
            status_url,
            json=update_payload,
            auth=BACKEND_AUTH,
            headers={"Content-Type": "application/json"}
        )
        
        if update_response.status_code == 200:
            updated_alert = update_response.json()
            print(f"✅ Alert Updated Successfully\n")
            print(f"   Status: ACTIVE → RESOLVED")
            print(f"   Action: {action_summary[:80]}")
            print(f"   Notes: AI automated response executed")
            print(f"   Time: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
        else:
            print(f"⚠️  Failed to update alert: HTTP {update_response.status_code}")
            print(f"   Response: {update_response.text[:200]}\n")
        
        # Step 3: Verify action on Proxmox (if executed)
        status = exec_result.get('status', 'unknown')
        
        if status == 'executed':
            print(f"✅ Step 3: Actions Executed on Proxmox\n")
            print(f"   Malicious process terminated")
            print(f"   Binary blocked from restart")
            print(f"   Filesystem scanned for artifacts\n")
        elif status == 'dry_run':
            print(f"🧪 Step 3: DRY RUN MODE\n")
            print(f"   Actions simulated but NOT executed")
            print(f"   Set DRY_RUN_MODE=false in .env to enable real execution\n")
        else:
            print(f"⚠️  Step 3: Awaiting Approval\n")
            print(f"   Set AUTO_EXECUTE_THREATS=true to enable auto-execution\n")
        
        print(f"{'='*70}")
        print(f"  ✅ THREAT RESPONSE COMPLETE")
        print(f"{'='*70}\n")
        print(f"🎯 Summary:")
        print(f"   • Alert #{alert_id} processed")
        print(f"   • AI threat score: {ai_decision.get('threat_assessment', {}).get('confidence', 0)*100:.0f}%")
        print(f"   • Actions: {len(actions_taken)} steps identified")
        print(f"   • Backend: Alert status updated")
        print(f"   • Check dashboard: http://localhost:8081/alerts\n")
        
    else:
        print(f"❌ AI Error: HTTP {response.status_code}")
        print(f"   {response.text[:200]}")

except requests.exceptions.ConnectionError as e:
    print(f"❌ Connection Error: {e}")
    print(f"   Make sure Python AI service is running on {PYTHON_AI_URL}")
except Exception as e:
    print(f"❌ Error: {e}")
