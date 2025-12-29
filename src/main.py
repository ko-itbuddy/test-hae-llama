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
from src.rag_engine import generate_test, validate_and_fix
from src.dependency import ensure_ollama_models
from src.utils.file_utils import get_project_data_dir

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
@click.option('--reset', is_flag=True, help='Clear existing database before ingestion')
def ingest(project_path, model, reset):
    """Scan and ingest Java codebase."""
    from src.ingest import ingest_codebase
    ingest_codebase(project_path, embedding_model=model, reset=reset)
    click.echo(f"✅ Codebase ingestion complete (Reset={reset}).")

@cli.command()
@click.option('--project-path', required=True, help='Path to the Java project')
@click.option('--model', default='nomic-embed-text', help='Embedding model')
def ingest_deps(project_path, model):
    """Deep study: Ingest Javadocs for all project dependencies."""
    from src.ingest import ingest_dependencies_javadocs
    ingest_dependencies_javadocs(project_path, embedding_model=model)
    click.echo(f"✅ Library wisdom absorption complete.")

@cli.command()
@click.option('--project-path', default='.', help='Path to initialize')
def init(project_path):
    """[BOOTSTRAP] Initialize the .test-hea-llama environment."""
    from src.utils.file_utils import get_project_data_dir
    data_dir = get_project_data_dir(project_path)
    
    # 1. Create directory structure
    for sub in ["config", "rules", "logs"]:
        os.makedirs(os.path.join(data_dir, sub), exist_ok=True)
    
    # 2. Generate default philosophy (The Law)
    phi_path = os.path.join(data_dir, "rules", "coding_philosophy.md")
    if not os.path.exists(phi_path):
        with open(phi_path, "w", encoding="utf-8") as f:
            f.write("# 🦙 Coding Philosophy\n- Use Fluent Assertions\n- Prefer MethodSource\n- Failure-First (1:N Rule)\n")
        click.echo(f"📜 [Init] Created philosophy lawbook at {phi_path}")

    # 3. Generate default config
    cfg_path = os.path.join(data_dir, "config", "engine_config.yml")
    if not os.path.exists(cfg_path):
        with open(cfg_path, "w", encoding="utf-8") as f:
            f.write("llm:\n  model: \"qwen2.5-coder:14b\"\n  temperature: 0.3\npaths:\n  data_root: \".test-hea-llama\"\n")
        click.echo(f"⚙️ [Init] Created default configuration at {cfg_path}")

    click.echo(f"✅ [Init] Engine environment ready in {data_dir}")

@cli.command()
@click.option('--target-file', prompt='Target Java File', help='Path to the file to test')
@click.option('--project-path', default='.', help='Workspace root')
@click.option('--prefix', default='spring_project', help='Prefix for vector collections')
@click.option('--model', default=None, help='Ollama model to use')
@click.option('--custom-rules', default='', help='Custom rules from IDE settings')
def generate(target_file, project_path, prefix, model, custom_rules):
    """Generate Unit Test with Full Environment Check"""
    from src.dependency import run_full_health_check, ensure_ollama_models
    
    # 💡 [v6.3] Health Check First!
    click.echo(f"🔍 [Health] Checking environment...")
    report = run_full_health_check(project_path)
    if report["status"] == "CRITICAL":
        for issue in report["issues"]: click.echo(issue)
        sys.exit(1)
    
    # Ensure model is ready
    target_model = model or "qwen2.5-coder:14b"
    ensure_ollama_models(llm_model=target_model)
    
    click.echo(f"[STATUS] 🚀 Mission Start: {os.path.basename(target_file)}")
    
    # Code generation...
    result = generate_test(target_file, project_path, prefix, "", llm_model=target_model, custom_rules=custom_rules)
    
    # (Rest of the saving logic remains same)
    if "[RESULT_START]" in result:
        clean_result = result.split("[RESULT_START]")[1].split("[RESULT_END]")[0].strip()
        save_path = _get_test_save_path(target_file, project_path)
        os.makedirs(os.path.dirname(save_path), exist_ok=True)
        with open(save_path, 'w', encoding='utf-8') as f:
            f.write(clean_result)
        click.echo(f"💾 [SAVED] {save_path}")
        import asyncio
        asyncio.run(validate_and_fix(save_path, project_path, llm_model=target_model))

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
