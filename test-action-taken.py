#!/usr/bin/env python3
import requests

BACKEND_URL = "http://localhost:8080"
BACKEND_AUTH = ("admin", "admin123")

# Create a new test alert
print("\n" + "="*70)
print("  🧪 TESTING ACTION TAKEN FIELD UPDATE")
print("="*70 + "\n")

print("Step 1: Creating new test alert...")

webhook_payload = {
    "eventType": "SSH_BRUTE_FORCE",
    "severity": "CRITICAL",
    "sourceIp": "192.168.100.64",
    "description": "TEST: SSH brute force attack detected - 50 failed attempts"
}

response = requests.post(
    f"{BACKEND_URL}/api/v1/webhook/python",
    json=webhook_payload
)

if response.status_code == 200:
    print(f"✅ Alert created\n")
    
    # Get the latest alert
    alerts = requests.get(
        f"{BACKEND_URL}/api/v1/alerts/recent?hours=1",
        auth=BACKEND_AUTH
    ).json()
    
    latest_alert = alerts[0]
    alert_id = latest_alert['id']
    
    print(f"Alert ID: {alert_id}")
    print(f"Type: {latest_alert['type']}")
    print(f"Status: {latest_alert['status']}")
    print(f"Action Taken: {latest_alert.get('actionTaken', 'None')}\n")
    
    # Step 2: Update with AI response
    print("Step 2: Updating alert with AI automated response...\n")
    
    status_update = {
        "status": "RESOLVED",
        "notes": "AI Automated Response: block_ip: IP blocked via iptables | rate_limit: SSH rate limited | audit_logs: Logs checked"
    }
    
    update_response = requests.patch(
        f"{BACKEND_URL}/api/v1/alerts/{alert_id}/status",
        json=status_update,
        auth=BACKEND_AUTH,
        headers={"Content-Type": "application/json"}
    )
    
    if update_response.status_code == 200:
        updated_alert = update_response.json()
        
        print("✅ Alert updated successfully\n")
        print(f"{'='*70}")
        print(f"  UPDATED ALERT #{alert_id}")
        print(f"{'='*70}\n")
        print(f"Status: {updated_alert['status']}")
        print(f"Action Taken: {updated_alert.get('actionTaken', 'None')}")
        print(f"\nDetails:\n{updated_alert['details']}\n")
        
        if updated_alert.get('actionTaken'):
            print(f"✅ SUCCESS! actionTaken field is now populated!")
        else:
            print(f"❌ FAILED: actionTaken is still None")
        
        print(f"\n{'='*70}\n")
    else:
        print(f"❌ Update failed: {update_response.status_code}")
        print(f"Response: {update_response.text}")
else:
    print(f"❌ Failed to create alert: {response.status_code}")
