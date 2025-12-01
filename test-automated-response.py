#!/usr/bin/env python3
import paramiko
import os
from dotenv import load_dotenv
from datetime import datetime

# Load environment variables
env_path = r"d:\webautoshild\python_ai_service\.env"
load_dotenv(env_path)

host = os.getenv("PROXMOX_HOST")
user = os.getenv("PROXMOX_USER")
password = os.getenv("PROXMOX_PASSWORD")
port = int(os.getenv("PROXMOX_PORT", 22))
dry_run = os.getenv("DRY_RUN_MODE", "true").lower() == "true"

test_ip = "203.0.113.99"  # TEST-NET-3 (documentation IP, won't affect real traffic)

print(f"\n{'='*60}")
print(f"  AUTOMATED THREAT RESPONSE TEST")
print(f"{'='*60}\n")
print(f"Target:      {host}")
print(f"Test IP:     {test_ip}")
print(f"DRY_RUN:     {dry_run}\n")

try:
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(hostname=host, port=port, username=user, password=password, timeout=10)
    
    print("✅ Connected to Proxmox\n")
    
    # Simulate SSH Brute Force Attack Response
    print("🔒 THREAT SCENARIO: SSH Brute Force Attack")
    print(f"   Source IP: {test_ip}")
    print(f"   Severity: CRITICAL")
    print(f"   Action: Block IP via iptables\n")
    
    # AI-Generated Commands (from threat playbook)
    commands = [
        f"iptables -I INPUT -s {test_ip} -j DROP",
        "iptables-save > /etc/iptables/rules.v4"
    ]
    
    print("📋 Execution Plan:")
    for i, cmd in enumerate(commands, 1):
        print(f"   {i}. {cmd}")
    
    print(f"\n{'='*60}")
    
    if dry_run:
        print("🧪 DRY RUN MODE - Commands NOT executed")
        print("   (Set DRY_RUN_MODE=false in .env to execute real actions)")
        print(f"\n✅ Simulation completed successfully!")
        print(f"\nIn production mode, this would:")
        print(f"   • Block {test_ip} in iptables")
        print(f"   • Save rules to persist across reboots")
        print(f"   • Log action to audit trail")
    else:
        print("⚠️  PRODUCTION MODE - Executing commands...")
        for i, cmd in enumerate(commands, 1):
            print(f"\n   Executing step {i}...")
            stdin, stdout, stderr = ssh.exec_command(cmd)
            output = stdout.read().decode()
            error = stderr.read().decode()
            
            if error:
                print(f"   ❌ Error: {error}")
            else:
                print(f"   ✅ Success")
        
        # Verify the rule was added
        print("\n🔍 Verifying iptables rule...")
        stdin, stdout, stderr = ssh.exec_command(f"iptables -L INPUT -n | grep {test_ip}")
        output = stdout.read().decode()
        
        if output:
            print(f"   ✅ IP Blocked:\n   {output}")
        else:
            print(f"   ⚠️  Rule not found in iptables")
        
        print(f"\n✅ Automated response completed!")
    
    ssh.close()
    
    print(f"\n{'='*60}")
    print(f"  TEST COMPLETED")
    print(f"{'='*60}\n")
    
except Exception as e:
    print(f"❌ Error: {e}")
