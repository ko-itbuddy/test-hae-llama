import os
import subprocess
import sys
import xml.etree.ElementTree as ET

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
            print(f"[STATUS] ⚠️ Pulling {llm_model}...")
            subprocess.run(['ollama', 'pull', llm_model], check=True)
    except: pass

def get_java_version(project_path):
    """Detects Java version using simple string parsing (No Regex)."""
    # 1. Check build.gradle
    gradle_path = os.path.join(project_path, "build.gradle")
    if os.path.exists(gradle_path):
        try:
            lines = open(gradle_path, 'r').readlines()
            for line in lines:
                if "sourceCompatibility" in line:
                    # Parse: sourceCompatibility = '17' or 17
                    parts = line.split("=")
                    if len(parts) > 1:
                        ver = parts[1].strip().replace("'", "").replace('"', "")
                        return "1.8" if ver == "8" else ver
        except: pass

    # 2. Check pom.xml (XML parser is safe)
    pom_path = os.path.join(project_path, "pom.xml")
    if os.path.exists(pom_path):
        try:
            tree = ET.parse(pom_path); root = tree.getroot()
            ns = {'mvn': 'http://maven.apache.org/POM/4.0.0'}
            props = root.find(".//mvn:properties", ns)
            if props is not None:
                ver = props.find("mvn:java.version", ns)
                if ver is not None: return ver.text
        except: pass
        
    return "17" # Default

def get_all_dependency_versions(project_path):
    # Simplified dependency check without regex
    versions = {'spring-boot': '3.0.0', 'has-testcontainers': False, 'has-awaitility': False}
    gradle_path = os.path.join(project_path, "build.gradle")
    if os.path.exists(gradle_path):
        content = open(gradle_path, 'r').read()
        if "testcontainers" in content: versions['has-testcontainers'] = True
        if "awaitility" in content: versions['has-awaitility'] = True
    return versions
