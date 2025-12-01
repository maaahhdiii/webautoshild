#!/usr/bin/env python3
"""
AutoShield - Automated Threat Response Monitor
Monitors alerts and automatically triggers AI responses
"""
import requests
import time
import json
from datetime import datetime
from typing import Set

# Configuration
BACKEND_URL = "http://localhost:8080"
PYTHON_AI_URL = "http://localhost:8000"
BACKEND_AUTH = ("admin", "admin123")
POLL_INTERVAL = 5  # Check for new alerts every 5 seconds
PROCESSED_ALERTS: Set[int] = set()

def get_active_alerts():
    """Get all active (unresolved) alerts"""
    try:
        response = requests.get(
            f"{BACKEND_URL}/api/v1/alerts/recent?hours=24",
            auth=BACKEND_AUTH,
            timeout=5
        )
        if response.status_code == 200:
            alerts = response.json()
            # Filter for ACTIVE or unprocessed alerts
            return [a for a in alerts if a.get('status') == 'ACTIVE']
        return []
    except Exception as e:
        print(f"Error fetching alerts: {e}")
        return []

def trigger_ai_response(alert):
    """Trigger AI threat response for an alert"""
    alert_id = alert['id']
    event_type = alert['type'].lower()
    source_ip = alert['sourceIp']
    severity = alert['severity']
    
    print(f"\n{'='*70}")
    print(f"🚨 Processing Alert #{alert_id}")
    print(f"{'='*70}")
    print(f"Type:        {alert['type']}")
    print(f"Severity:    {severity}")
    print(f"Source IP:   {source_ip}")
    print(f"Time:        {alert['timestamp']}")
    print()
    
    # Submit to AI for analysis
    try:
        ai_url = f"{PYTHON_AI_URL}/api/v1/analyze-threat"
        
        payload = {
            "event_type": event_type,
            "source_ip": source_ip,
            "metadata": {
                "alert_id": alert_id,
                "severity": severity,
                "details": alert.get('details', ''),
                "auto_response": True
            }
        }
        
        print("📡 Submitting to AI Threat Response Engine...")
        response = requests.post(ai_url, json=payload, timeout=30)
        
        if response.status_code == 200:
            result = response.json()
            
            threat_level = result.get('threat_level', 'unknown')
            threat_score = result.get('threat_score', 0)
            action_taken = result.get('action_taken', 'monitored')
            recommendations = result.get('recommendations', [])
            
            print(f"✅ AI Analysis Complete")
            print(f"   Threat Level: {threat_level.upper()}")
            print(f"   Score: {threat_score}/100")
            print(f"   Action: {action_taken.upper()}")
            
            # Build action summary from recommendations
            if recommendations:
                action_summary = " | ".join(recommendations[:3])  # First 3 recommendations
            else:
                action_summary = f"AI analyzed: {threat_level} threat (score: {threat_score})"
            
            # Update alert in backend
            print(f"\n📝 Updating Alert #{alert_id}...")
            
            status_update = {
                "status": "RESOLVED",
                "notes": f"AI Automated Response: {action_summary}"
            }
            
            update_response = requests.patch(
                f"{BACKEND_URL}/api/v1/alerts/{alert_id}/status",
                json=status_update,
                auth=BACKEND_AUTH,
                headers={"Content-Type": "application/json"}
            )
            
            if update_response.status_code == 200:
                updated = update_response.json()
                print(f"✅ Alert Resolved")
                print(f"   Action Taken: {updated.get('actionTaken', 'N/A')}")
                print(f"   Status: {updated.get('status')}")
                return True
            else:
                print(f"⚠️  Failed to update alert: {update_response.status_code}")
                return False
                
        else:
            print(f"❌ AI Response Error: {response.status_code}")
            return False
            
    except requests.exceptions.ConnectionError:
        print(f"❌ Cannot connect to AI service at {PYTHON_AI_URL}")
        return False
    except Exception as e:
        print(f"❌ Error: {e}")
        return False

def process_all_active_alerts():
    """Process all currently active alerts"""
    print(f"\n{'='*70}")
    print(f"  🔍 SCANNING FOR ACTIVE ALERTS")
    print(f"{'='*70}\n")
    
    alerts = get_active_alerts()
    
    if not alerts:
        print("No active alerts found.")
        return 0
    
    print(f"Found {len(alerts)} active alert(s)\n")
    
    processed = 0
    for alert in alerts:
        alert_id = alert['id']
        
        if alert_id in PROCESSED_ALERTS:
            print(f"⏭️  Alert #{alert_id} already processed, skipping...")
            continue
        
        if trigger_ai_response(alert):
            PROCESSED_ALERTS.add(alert_id)
            processed += 1
            time.sleep(1)  # Brief pause between processing
    
    return processed

def monitor_continuous():
    """Continuously monitor for new alerts"""
    print("\n" + "="*70)
    print("  🤖 AUTOSHIELD AUTOMATED THREAT RESPONSE")
    print("  Continuous Monitoring Mode")
    print("="*70 + "\n")
    print(f"Backend:     {BACKEND_URL}")
    print(f"AI Service:  {PYTHON_AI_URL}")
    print(f"Poll Rate:   Every {POLL_INTERVAL} seconds")
    print(f"Started:     {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("\n" + "="*70)
    print("  MONITORING ACTIVE - Press Ctrl+C to stop")
    print("="*70 + "\n")
    
    # First pass: Process all existing active alerts
    print("🔄 Initial Scan: Processing existing active alerts...")
    initial_count = process_all_active_alerts()
    print(f"\n✅ Initial scan complete: {initial_count} alert(s) processed")
    print(f"\n{'='*70}")
    print("  👀 Now monitoring for new alerts...")
    print(f"{'='*70}\n")
    
    try:
        while True:
            time.sleep(POLL_INTERVAL)
            
            alerts = get_active_alerts()
            new_alerts = [a for a in alerts if a['id'] not in PROCESSED_ALERTS]
            
            if new_alerts:
                print(f"\n🆕 {len(new_alerts)} new alert(s) detected!")
                
                for alert in new_alerts:
                    if trigger_ai_response(alert):
                        PROCESSED_ALERTS.add(alert['id'])
                        time.sleep(1)
                
                print(f"\n{'='*70}")
                print(f"  ✅ Processed {len(new_alerts)} new alert(s)")
                print(f"  📊 Total processed: {len(PROCESSED_ALERTS)}")
                print(f"{'='*70}\n")
            else:
                # Silent monitoring - print status every minute
                if int(time.time()) % 60 == 0:
                    print(f"[{datetime.now().strftime('%H:%M:%S')}] Monitoring... ({len(PROCESSED_ALERTS)} total processed)")
    
    except KeyboardInterrupt:
        print("\n\n" + "="*70)
        print("  🛑 MONITORING STOPPED")
        print("="*70)
        print(f"\nSession Summary:")
        print(f"  Total alerts processed: {len(PROCESSED_ALERTS)}")
        print(f"  Duration: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        print(f"\nProcessed Alert IDs: {sorted(PROCESSED_ALERTS)}")
        print("\n" + "="*70 + "\n")

if __name__ == "__main__":
    import sys
    
    if len(sys.argv) > 1 and sys.argv[1] == "--once":
        # Process once and exit
        count = process_all_active_alerts()
        print(f"\n✅ Session complete: {count} alert(s) processed\n")
    else:
        # Continuous monitoring mode
        monitor_continuous()
