import os
import glob
from langchain_community.document_loaders import TextLoader, DirectoryLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter, Language
from langchain_chroma import Chroma
from langchain_ollama import OllamaEmbeddings
from src.utils.file_utils import get_chroma_dir

def classify_component(content, filename):
    prefix = "t" if any(x in filename for x in ["Test.java", "Tests.java", "IT.java"]) else "c"
    if "@RestController" in content or "@Controller" in content: layer = "controller"
    elif "@Service" in content: layer = "service"
    elif "Repository" in content or "@Repository" in content: layer = "repository"
    elif "@Entity" in content or "Dto" in filename: layer = "entity"
    elif "@Component" in content: layer = "component"
    else: layer = "common"
    return f"{prefix}_{layer}"

def ingest_codebase(project_path, embedding_model="nomic-embed-text"):
    print(f"📦 [Ingest] Organizing Codebase into Isolated Collections...")
    java_files = glob.glob(os.path.join(project_path, "**/*.java"), recursive=True)
    # 💡 [v6.3] Updated to use the new project name
    java_files = [f for f in java_files if ".test-hea-llama" not in f]
    
    # (Rest of the categorization logic...)
    collections = {"source": [], "test": []}
    for f in java_files:
        loader = TextLoader(f, encoding='utf-8')
        docs = loader.load()
        category = "test" if "src/test/" in f else "source"
        for doc in docs:
            doc.metadata["source"] = f
            collections[category].append(doc)

    splitter = RecursiveCharacterTextSplitter.from_language(language=Language.JAVA, chunk_size=1500, chunk_overlap=300)
    embedding_function = OllamaEmbeddings(model=embedding_model)
    persist_dir = get_chroma_dir(project_path)

    for name, docs in collections.items():
        if not docs: continue
        print(f"   -> Saving {len(docs)} files to 'collection_{name}'...")
        splits = splitter.split_documents(docs)
        Chroma.from_documents(documents=splits, embedding=embedding_function, 
                             collection_name=f"collection_{name}", 
                             persist_directory=persist_dir)

def ingest_web_docs(project_path, url, library_name, embedding_model="nomic-embed-text"):
    """Fetches web documentation and stores it in a dedicated collection."""
    from langchain_community.document_loaders import WebBaseLoader
    print(f"🌍 [Ingest] Fetching Javadoc for '{library_name}' from {url}...")
    
    loader = WebBaseLoader(url)
    docs = loader.load()
    
    splitter = RecursiveCharacterTextSplitter(chunk_size=1000, chunk_overlap=200)
    embedding_function = OllamaEmbeddings(model=embedding_model)
    persist_dir = get_chroma_dir(project_path)
    
    splits = splitter.split_documents(docs)
    if splits:
        Chroma.from_documents(documents=splits, embedding=embedding_function, 
                             collection_name=f"collection_docs_{library_name.lower()}", 
                             persist_directory=persist_dir)
        print(f"✅ [Ingest] Library '{library_name}' is now vectorized.")

def ingest_dependencies_javadocs(project_path, embedding_model="nomic-embed-text"):
    from src.dependency import get_full_dependencies
    deps = get_full_dependencies(project_path)
    print(f"[STATUS] 🚀 Layered Learning: Ingesting API and Guides for {len(deps)} libraries.")
    for dep in deps:
        url = f"https://www.javadoc.io/doc/{dep['group']}/{dep['artifact']}/{dep['version']}"
        ingest_url(url, project_path, f"lib_{dep['artifact']}", embedding_model)

def ingest_url(url, project_path, collection_name, embedding_model="nomic-embed-text"):
    import requests
    from bs4 import BeautifulSoup
    from langchain_core.documents import Document
    from langchain_text_splitters import RecursiveCharacterTextSplitter
    try:
        response = requests.get(url, timeout=15)
        soup = BeautifulSoup(response.text, 'html.parser')
        main_body = soup.find('main') or soup.find('article') or soup.find('body')
        if not main_body: return
        text = "\n".join([e.get_text().strip() for e in main_body.find_all(['h1', 'h2', 'h3', 'p', 'pre', 'code'])])
        if not text.strip(): return
        
        doc = Document(page_content=text, metadata={"source": url})
        splitter = RecursiveCharacterTextSplitter(chunk_size=1000, chunk_overlap=200)
        splits = splitter.split_documents([doc])
        if not splits: return
        
        embedding_function = OllamaEmbeddings(model=embedding_model)
        persist_dir = get_chroma_dir(project_path)
        Chroma.from_documents(documents=splits, embedding=embedding_function, collection_name=collection_name, persist_directory=persist_dir)
    except Exception as e: print(f"Error: {e}")