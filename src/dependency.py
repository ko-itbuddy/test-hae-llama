import os
import subprocess
import sys
import xml.etree.ElementTree as ET
import re

def check_ollama_installed():
    try:
        subprocess.run(['ollama', '--version'], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        return True
    except: return False

def ensure_ollama_models(llm_model="qwen2.5-coder:7b", embedding_model="nomic-embed-text"):
    if not check_ollama_installed():
        print("[STATUS] ❌ Ollama not found.")
        return
    try:
        result = subprocess.run(['ollama', 'list'], capture_output=True, text=True)
        if llm_model not in result.stdout:
            print(f"[STATUS] ⚠️ Pulling {llm_model}... (This may take a while)")
            # Use Popen to filter out carriage returns that cause messy logs in VS Code
            process = subprocess.Popen(
                ['ollama', 'pull', llm_model],
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT,
                text=True,
                bufsize=1
            )
            last_percent = -1
            for line in process.stdout:
                # Extract percentage if exists (e.g., "6%")
                match = re.search(r'(\d+)%', line)
                if match:
                    percent = int(match.group(1))
                    if percent >= last_percent + 5: # Log every 5% to keep it clean
                        print(f"[STATUS] 📥 {llm_model} pulling... {percent}%")
                        last_percent = percent
                elif "pulling manifest" in line or "verifying sha256" in line:
                    if not any(x in line for x in ["\r", "▕"]): # Skip progress bars
                        print(f"[STATUS] 📥 {line.strip()}")
            process.wait()
            print(f"[STATUS] ✅ {llm_model} is ready.")
    except Exception as e:
        print(f"[STATUS] ❌ Error pulling model: {e}")

def get_all_dependency_versions(project_path):
    versions = {}
    pom_path = os.path.join(project_path, "pom.xml")
    if os.path.exists(pom_path):
        try:
            tree = ET.parse(pom_path); root = tree.getroot()
            ns = {'mvn': 'http://maven.apache.org/POM/4.0.0'}
            parent = root.find(".//mvn:parent/mvn:version", ns)
            if parent is not None: versions['spring-boot'] = parent.text
            for dep in root.findall(".//mvn:dependency", ns):
                aid = dep.find("mvn:artifactId", ns).text
                ver = dep.find("mvn:version", ns)
                if ver is not None: versions[aid] = ver.text
        except: pass
    return versions
