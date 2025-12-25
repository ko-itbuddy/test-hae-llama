import os
import subprocess
import sys

def check_ollama_installed():
    """Checks if Ollama CLI is installed."""
    try:
        subprocess.run(['ollama', '--version'], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        return True
    except (subprocess.CalledProcessError, FileNotFoundError):
        return False

def ensure_ollama_models(llm_model="qwen2.5-coder:7b", embedding_model="nomic-embed-text"):
    """
    Checks and pulls required Ollama models with REAL-TIME status reporting.
    """
    if not check_ollama_installed():
        print("❌ Ollama is not installed. Please install it from https://ollama.com")
        sys.exit(1)

    try:
        result = subprocess.run(['ollama', 'list'], capture_output=True, text=True, check=True)
        installed_models = result.stdout
    except Exception as e:
        print(f"❌ Failed to list Ollama models: {e}")
        return

    for model in [llm_model, embedding_model]:
        if model not in installed_models:
            # 💡 0.8.1: Stream download progress with [STATUS] tag
            print(f"[STATUS] ⚠️ Model '{model}' not found. Downloading...")
            try:
                # Use Popen to stream stdout/stderr lines
                process = subprocess.Popen(['ollama', 'pull', model], stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
                for line in process.stdout:
                    clean_line = line.strip()
                    if clean_line:
                        # Forward progress to IDE
                        print(f"[STATUS] {clean_line}")
                process.wait()
                if process.returncode == 0:
                    print(f"✅ Successfully pulled {model}")
                else:
                    print(f"❌ Failed to pull {model}")
            except Exception as e:
                print(f"❌ Error pulling {model}: {e}")