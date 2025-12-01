#!/usr/bin/env python3
import paramiko
import os
from dotenv import load_dotenv

# Load environment variables
env_path = r"d:\webautoshild\python_ai_service\.env"
load_dotenv(env_path)

host = os.getenv("PROXMOX_HOST")
user = os.getenv("PROXMOX_USER")
password = os.getenv("PROXMOX_PASSWORD")
port = int(os.getenv("PROXMOX_PORT", 22))

print(f"\n{'='*50}")
print(f"  PROXMOX SSH CONNECTION TEST")
print(f"{'='*50}\n")
print(f"Host:     {host}:{port}")
print(f"User:     {user}")
print(f"Password: {'*' * len(password)}\n")

try:
    print("Connecting to Proxmox...")
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    
    ssh.connect(
        hostname=host,
        port=port,
        username=user,
        password=password,
        timeout=10
    )
    
    print("✅ SSH Connection: SUCCESS\n")
    
    # Test command execution
    print("Testing command execution...")
    stdin, stdout, stderr = ssh.exec_command("hostname && uname -a")
    output = stdout.read().decode()
    error = stderr.read().decode()
    
    if output:
        print(f"✅ Command Output:\n{output}")
    if error:
        print(f"⚠️  Error Output:\n{error}")
    
    # Test iptables access (check if root can run iptables)
    print("\nTesting iptables access...")
    stdin, stdout, stderr = ssh.exec_command("iptables -L -n | head -5")
    output = stdout.read().decode()
    error = stderr.read().decode()
    
    if output:
        print(f"✅ iptables accessible:\n{output}")
    elif error:
        print(f"❌ iptables error:\n{error}")
    
    ssh.close()
    
    print(f"\n{'='*50}")
    print(f"  ✅ ALL TESTS PASSED")
    print(f"{'='*50}\n")
    print("Proxmox SSH is ready for automated threat response!")
    
except paramiko.AuthenticationException:
    print("❌ Authentication Failed: Invalid username or password")
except paramiko.SSHException as e:
    print(f"❌ SSH Error: {e}")
except Exception as e:
    print(f"❌ Connection Error: {e}")
