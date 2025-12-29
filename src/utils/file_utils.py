import os

def get_project_data_dir(project_path):
    """
    [CENTRAL PATH] Returns the official .test-hea-llama directory.
    All engine data MUST stay within this folder.
    """
    data_dir = os.path.join(project_path, ".test-hea-llama")
    os.makedirs(data_dir, exist_ok=True)
    return data_dir

def get_chroma_dir(project_path):
    return os.path.join(get_project_data_dir(project_path), "chroma_db")

def get_log_dir(project_path, target_file="unknown"):
    """Returns a directory named after the class for session grouping."""
    file_base = os.path.basename(target_file).replace(".", "_")
    log_root = os.path.join(get_project_data_dir(project_path), "logs", file_base)
    os.makedirs(log_root, exist_ok=True)
    return log_root

def get_chroma_dir(project_path):
    return os.path.join(get_project_data_dir(project_path), "chroma_db")

def ensure_gitignore(project_path):
    """
    Adds .test-hea-llama/ to the .gitignore file.
    """
    gitignore_path = os.path.join(project_path, ".gitignore")
    if not os.path.exists(gitignore_path):
        with open(gitignore_path, "w", encoding="utf-8") as f:
            f.write("# Test-Hae-Llama Data\n.test-hea-llama/\n")
        return

    try:
        with open(gitignore_path, "r", encoding="utf-8") as f:
            content = f.read()
        
        if ".test-hea-llama/" not in content:
            with open(gitignore_path, "a", encoding="utf-8") as f:
                f.write("\n# Test-Hae-Llama Data\n.test-hea-llama/\n")
    except Exception:
        pass

def get_log_dir(project_path, target_file="unknown"):
    """Returns a directory named after the class being tested."""
    import os
    file_base = os.path.basename(target_file).replace(".", "_")
    log_root = os.path.join(get_project_data_dir(project_path), "logs", file_base)
    os.makedirs(log_root, exist_ok=True)
    return log_root

def write_audit_log(project_path, filename, content, target_file="unknown"):
    """This function is now legacy. Agents should write directly to their assigned session file."""
    pass

def write_audit_log(project_path, filename, content, target_file="unknown"):
    """Writes to a structured session log."""
    # 💡 Redirects to the unique session directory
    session_dir = get_log_dir(project_path, target_file)
    log_path = os.path.join(session_dir, filename)
    with open(log_path, "a", encoding="utf-8") as f:
        f.write(content + "\n")
    return session_dir

def write_audit_log(project_path, filename, content, target_file="unknown"):
    """Writes to a structured session log using the target filename for categorization."""
    session_dir = get_log_dir(project_path, target_file)
    log_path = os.path.join(session_dir, filename)
    with open(log_path, "a", encoding="utf-8") as f:
        f.write(content + "\n")
    return session_dir
