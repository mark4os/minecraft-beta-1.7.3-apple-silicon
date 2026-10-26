#!/usr/bin/env python3
"""
Downloads Minecraft Beta 1.7.3 client jar and assets.
"""
import os
import sys
import json
import urllib.request

MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
TARGET_VERSION = "b1.7.3"

def main():
    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    cache_dir = os.path.join(base_dir, ".cache")
    os.makedirs(cache_dir, exist_ok=True)
    
    print(f"Fetching Mojang version manifest...")
    req = urllib.request.Request(MANIFEST_URL, headers={'User-Agent': 'Mozilla/5.0'})
    with urllib.request.urlopen(req) as resp:
        manifest = json.loads(resp.read().decode('utf-8'))
        
    version_info = next((v for v in manifest.get('versions', []) if v.get('id') == TARGET_VERSION), None)
    if not version_info:
        print(f"Version {TARGET_VERSION} not found in manifest!")
        sys.exit(1)
        
    version_url = version_info['url']
    print(f"Found {TARGET_VERSION} metadata: {version_url}")
    
    with urllib.request.urlopen(urllib.request.Request(version_url, headers={'User-Agent': 'Mozilla/5.0'})) as resp:
        version_data = json.loads(resp.read().decode('utf-8'))
        
    client_download = version_data.get('downloads', {}).get('client', {})
    client_url = client_download.get('url')
    if not client_url:
        print("Client download URL not found in version data!")
        sys.exit(1)
        
    dest_jar = os.path.join(cache_dir, f"minecraft-{TARGET_VERSION}.jar")
    print(f"Downloading client JAR from {client_url} to {dest_jar}...")
    urllib.request.urlretrieve(client_url, dest_jar)
    
    print(f"Successfully downloaded {dest_jar} ({os.path.getsize(dest_jar)} bytes)")

if __name__ == "__main__":
    main()

