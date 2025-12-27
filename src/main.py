import click
import os
import sys
import re
from pathlib import Path
from dotenv import load_dotenv

# 💡 최상단에서 .env 로드라마!
load_dotenv()

# Ensure the parent directory is in sys.path so 'src.xxx' imports work
current_dir = os.path.dirname(os.path.abspath(__file__))
parent_dir = os.path.dirname(current_dir)
if parent_dir not in sys.path:
    sys.path.insert(0, parent_dir)

from src.ingest import ingest_codebase
from src.rag_engine import generate_test
from src.dependency import ensure_ollama_models
from src.utils import get_project_data_dir

@click.group()
@click.option('--proxy', help='HTTP/HTTPS Proxy URL')
def cli(proxy):
    """🦙 Test-Hae-Llama: Local AI Test Generator"""
    if proxy:
        os.environ['HTTP_PROXY'] = proxy
        os.environ['HTTPS_PROXY'] = proxy
        click.echo(f"🔒 Proxy configured: {proxy}")

@cli.command()
@click.option('--project-path', required=True, help='Path to the Java project')
@click.option('--model', default='nomic-embed-text', help='Ollama model for embeddings')
def ingest(project_path, model):
    """Scan and ingest Java codebase."""
    from src.ingest import ingest_codebase
    ingest_codebase(project_path, embedding_model=model)
    click.echo(f"✅ Codebase ingestion complete.")

@cli.command()
@click.option('--project-path', required=True, help='Path to the Java project')
@click.option('--model', default='nomic-embed-text', help='Embedding model')
def ingest_deps(project_path, model):
    """Deep study: Ingest Javadocs for all project dependencies."""
    from src.ingest import ingest_dependencies_javadocs
    ingest_dependencies_javadocs(project_path, embedding_model=model)
    click.echo(f"✅ Library wisdom absorption complete.")

@cli.command()
@click.option('--target-file', prompt='Target Java File', help='Path to the file to test')
@click.option('--project-path', default='.', help='Workspace root')
@click.option('--prefix', default='spring_project', help='Prefix for vector collections')
@click.option('--model', default='qwen2.5-coder:7b', help='Ollama model to use')
@click.option('--custom-rules', default='', help='Custom rules from IDE settings')
@click.option('--context7-api-key', default='', help='API Key from VS Code settings')
def generate(target_file, project_path, prefix, model, custom_rules, context7_api_key):
    """Generate Unit Test and AUTO-SAVE to the correct location"""
    ensure_ollama_models(llm_model=model)
    
    # 💡 VS Code 설정값이 있으면 환경 변수를 강제로 덮어씀라마!
    if context7_api_key and context7_api_key.strip():
        os.environ['UPSTASH_CONTEXT7_API_KEY'] = context7_api_key
        os.environ['CONTEXT7_API_KEY'] = context7_api_key
        click.echo("🔑 Using API Key from VS Code Settings.")
        
    click.echo(f"[STATUS] Starting generation for {os.path.basename(target_file)}...")
    
    # Generate Code
    result = generate_test(target_file, project_path, prefix, "", llm_model=model, custom_rules=custom_rules)
    
    # Extract the actual code if wrapped in RESULT tags
    clean_result = result
    if "[RESULT_START]" in result:
        clean_result = result.split("[RESULT_START]")[1].split("[RESULT_END]")[0].strip()

    # 💡 자동 저장 및 검증
    save_path = _get_test_save_path(target_file, project_path)
    
    try:
        os.makedirs(os.path.dirname(save_path), exist_ok=True)
        with open(save_path, 'w', encoding='utf-8') as f:
            f.write(clean_result)
        
        # 🕵️‍♂️ 저장 검증
        if os.path.exists(save_path):
            size = os.path.getsize(save_path)
            click.echo(f"💾 [SAVED] {save_path} ({size} bytes)")
        else:
            click.echo(f"❌ [FAIL] File not found after write: {save_path}")
            
    except Exception as e:
        click.echo(f"⚠️ [ERROR] Save failed: {e}")

    # Output for IDE
    click.echo("\n[RESULT_START]")
    click.echo(clean_result)
    click.echo("[RESULT_END]")

def _get_test_save_path(target_file, project_path):
    """
    Spring Standard Path Resolver:
    1. Finds the module root by looking for pom.xml or build.gradle.
    2. Swaps src/main/{lang} to src/test/{lang}.
    3. Ensures the file ends with 'Test.java'.
    """
    abs_target = os.path.abspath(target_file)
    
    # 🔍 1. Find Module Root (nearest parent with pom.xml or build.gradle)
    module_root = os.path.dirname(abs_target)
    while module_root != os.path.dirname(module_root):
        if any(os.path.exists(os.path.join(module_root, f)) for f in ["pom.xml", "build.gradle", "build.gradle.kts"]):
            break
        module_root = os.path.dirname(module_root)
    
    # 🔍 2. Language-agnostic Path Swapping (java, kotlin, etc.)
    # pattern: src/main/(any_lang)/... -> src/test/(any_lang)/...
    test_path = re.sub(r'src([/\\+])main([/\\+])(\w+)', r'src\1test\2\3', abs_target)
    
    # Fallback if no src/main structure found
    if test_path == abs_target:
        test_path = os.path.join(module_root, "src/test/java", os.path.relpath(abs_target, os.path.join(module_root, "src/main/java") if "src/main/java" in abs_target else module_root))

    # 🔍 3. Suffix Integrity
    base, ext = os.path.splitext(test_path)
    if not base.endswith("Test"):
        return f"{base}Test{ext}"
    return f"{base}{ext}"

if __name__ == '__main__':
    cli()