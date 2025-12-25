import click
import os
import sys
from pathlib import Path

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

@main.command()
@click.option('--path', required=True, help='Path to documents folder')
@click.option('--model', default='nomic-embed-text', help='Embedding model')
def ingest_docs(path, model):
    """Bulk ingest all documents (.md, .txt, .pdf) in a directory."""
    from src.ingest import ingest_documentation
    ingest_documentation(path, embedding_model=model)
    click.echo(f"✅ Bulk ingestion for {path} complete.")

@cli.command()
@click.option('--target-file', prompt='Target Java File', help='Path to the file to test')
@click.option('--project-path', default='.', help='Workspace root')
@click.option('--prefix', default='spring_project', help='Prefix for vector collections')
@click.option('--model', default='qwen2.5-coder:7b', help='Ollama model to use')
@click.option('--custom-rules', default='', help='Custom rules from IDE settings')
@click.option('--context7-api-key', default='', help='API Key for Context7 MCP')
def generate(target_file, project_path, prefix, model, custom_rules, context7_api_key):
    """Generate Unit Test and AUTO-SAVE to the correct location"""
    ensure_ollama_models(llm_model=model)
    
    if context7_api_key:
        os.environ['CONTEXT7_API_KEY'] = context7_api_key
        
    click.echo(f"[STATUS] Starting generation for {os.path.basename(target_file)}...")
    
    # Generate Code
    result = generate_test(target_file, project_path, prefix, "", llm_model=model, custom_rules=custom_rules)
    
    # 💡 0.8.0: 자동 저장 경로 계산라마!
    save_path = _get_test_save_path(target_file, project_path)
    
    try:
        os.makedirs(os.path.dirname(save_path), exist_ok=True)
        with open(save_path, 'w', encoding='utf-8') as f:
            f.write(result)
        click.echo(f"💾 [SAVED] {save_path}")
    except Exception as e:
        click.echo(f"⚠️ [ERROR] Save failed: {e}")

    # Output the result anyway for the IDE to capture if needed
    click.echo("\n[RESULT_START]")
    click.echo(result)
    click.echo("[RESULT_END]")

def _get_test_save_path(target_file, project_path):
    """
    Converts src/main/java/.../File.java to src/test/java/.../FileTest.java
    """
    abs_target = os.path.abspath(target_file)
    if "src/main/java" in abs_target:
        test_path = abs_target.replace("src/main/java", "src/test/java")
    elif "src/" in abs_target:
        test_path = abs_target.replace("src/", "src/test/")
    else:
        test_path = abs_target
        
    base, ext = os.path.splitext(test_path)
    if not base.endswith("Test"):
        return f"{base}Test{ext}"
    return f"{base}{ext}"

if __name__ == '__main__':
    cli()
