import os
import subprocess
import sys

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
        # Simple string check instead of regex
        result = subprocess.run(['ollama', 'list'], capture_output=True, text=True)
        if llm_model not in result.stdout:
            print(f"[STATUS] ⚠️ Pulling {llm_model}...")
            subprocess.run(['ollama', 'pull', llm_model], check=True)
    except: pass

def get_java_version(project_path):
    """Detects Java version by asking Gradle or Maven directly."""
    if os.path.exists(os.path.join(project_path, "gradlew")) or os.path.exists(os.path.join(project_path, "build.gradle")):
        try:
            cmd = ['./gradlew', '-q', 'properties'] if os.path.exists(os.path.join(project_path, "gradlew")) else ['gradle', '-q', 'properties']
            result = subprocess.run(cmd, cwd=project_path, capture_output=True, text=True)
            for line in result.stdout.split('\n'):
                if line.startswith("sourceCompatibility:"): return line.split(":")[1].strip()
        except: pass
    return "17"

def get_build_command(project_path):
    """
    Returns the appropriate build command (gradlew, mvnw, gradle, or mvn).
    """
    if os.path.exists(os.path.join(project_path, "gradlew")):
        return "./gradlew" if os.name != 'nt' else "gradlew.bat"
    if os.path.exists(os.path.join(project_path, "build.gradle")):
        return "gradle"
    if os.path.exists(os.path.join(project_path, "mvnw")):
        return "./mvnw" if os.name != 'nt' else "mvnw.cmd"
    if os.path.exists(os.path.join(project_path, "pom.xml")):
        return "mvn"
    return None # Default fallback

def get_project_classpath(project_path):
    """
    Finds and returns the project's classpath including source classes and dependencies.
    """
    paths = []
    
    # 1. Main classes
    main_classes = os.path.join(project_path, "build/classes/java/main")
    if os.path.exists(main_classes):
        paths.append(main_classes)
    
    # 2. Test dependencies (Attempt to find common library locations)
    # For now, we'll look into common Gradle cache patterns or project libs
    # A robust way is to ask Gradle, but for a quick check, we'll scan the build dir
    lib_dir = os.path.join(project_path, "build/libs")
    if os.path.exists(lib_dir):
        paths.append(os.path.join(lib_dir, "*"))

    # Fallback: Add current directory
    paths.append(".")
    
    # Join with system path separator
    separator = ";" if os.name == 'nt' else ":"
    return separator.join(paths)

def get_all_dependency_versions(project_path):
    # Simplified dependency check via text search (fast & safe)
    versions = {'spring-boot': '3.0.0', 'has-testcontainers': False, 'has-awaitility': False}
    
    # Check Gradle
    gradle_path = os.path.join(project_path, "build.gradle")
    if os.path.exists(gradle_path):
        content = open(gradle_path, 'r').read()
        if "testcontainers" in content: versions['has-testcontainers'] = True
        if "awaitility" in content: versions['has-awaitility'] = True
        
    # Check Maven
    pom_path = os.path.join(project_path, "pom.xml")
    if os.path.exists(pom_path):
        content = open(pom_path, 'r').read()
        if "testcontainers" in content: versions['has-testcontainers'] = True
        if "awaitility" in content: versions['has-awaitility'] = True
        
    return versions