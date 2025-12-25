import os
import glob
from langchain_community.document_loaders import TextLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter, Language
from langchain_chroma import Chroma
from langchain_ollama import OllamaEmbeddings
from src.utils import get_chroma_dir

def classify_component(content, filename):
    # Determine Type (Source vs Test)
    prefix = "t" if any(x in filename for x in ["Test.java", "Tests.java", "IT.java"]) else "c"
    
    # Determine Layer
    if "@RestController" in content or "@Controller" in content: layer = "controller"
    elif "@Service" in content: layer = "service"
    elif "Repository" in content or "@Repository" in content: layer = "repository"
    elif "@Entity" in content or "Dto" in filename: layer = "entity"
    elif "@Component" in content: layer = "component"
    else: layer = "common"
    
    return f"{prefix}_{layer}"

def ingest_codebase(project_path, collection_name_prefix="spring_project", embedding_model="nomic-embed-text"):
    """
    Scans the project and ingests source/test files into layered collections.
    """
    print(f"Scanning for Java files in: {project_path}")
    java_files = glob.glob(os.path.join(project_path, "**/*.java"), recursive=True)
    
    # Filter out our own data dir if it's inside the project
    java_files = [f for f in java_files if ".ai-test-gen" not in f]
    
    buckets = {}
    for file_path in java_files:
        try:
            loader = TextLoader(file_path, encoding='utf-8')
            docs = loader.load()
            for doc in docs:
                filename = os.path.basename(file_path)
                doc.metadata["source"] = file_path
                category = classify_component(doc.page_content, filename)
                if category not in buckets: buckets[category] = []
                buckets[category].append(doc)
        except: pass

    if not buckets:
        print("No Java files found to index.")
        return

    splitter = RecursiveCharacterTextSplitter.from_language(language=Language.JAVA, chunk_size=1000, chunk_overlap=200)
    embedding_function = OllamaEmbeddings(model=embedding_model)
    persist_dir = get_chroma_dir(project_path)

    for category, documents in buckets.items():
        splits = splitter.split_documents(documents)
        full_collection_name = f"{collection_name_prefix}_{category}"
        print(f"   -> Indexing {len(splits)} chunks into '{full_collection_name}'...")
        Chroma.from_documents(documents=splits, embedding=embedding_function, collection_name=full_collection_name, persist_directory=persist_dir)
    
    print(f"Ingestion complete. Data stored in: {persist_dir}")

def ingest_url(url, project_path, collection_name, embedding_model="nomic-embed-text"):
    import requests
    from bs4 import BeautifulSoup
    from langchain_core.documents import Document
    proxies = { "http": os.environ.get("HTTP_PROXY"), "https": os.environ.get("HTTPS_PROXY") }
    try:
        response = requests.get(url, proxies={k:v for k,v in proxies.items() if v}, timeout=15)
        soup = BeautifulSoup(response.text, 'html.parser')
        main_body = soup.find('main') or soup.find('article') or soup.find('body')
        content_parts = [e.get_text().strip() for e in main_body.find_all(['h1', 'h2', 'h3', 'p', 'pre', 'code'])]
        doc = Document(page_content="\n".join(content_parts), metadata={"source": url})
        splitter = RecursiveCharacterTextSplitter(chunk_size=1000, chunk_overlap=200)
        splits = splitter.split_documents([doc])
        embedding_function = OllamaEmbeddings(model=embedding_model)
        persist_dir = get_chroma_dir(project_path)
        Chroma.from_documents(documents=splits, embedding=embedding_function, collection_name=collection_name, persist_directory=persist_dir)
    except Exception as e: print(f"Error: {e}")