import os

def get_project_data_dir(project_path):
    """
    Returns the project-local .ai-test-gen directory.
    Example: ./sample-project/.ai-test-gen/
    """
    data_dir = os.path.join(project_path, ".ai-test-gen")
    os.makedirs(data_dir, exist_ok=True)
    ensure_gitignore(project_path)
    return data_dir

def get_chroma_dir(project_path):
    return os.path.join(get_project_data_dir(project_path), "chroma_db")

def ensure_gitignore(project_path):
    """
    Adds .ai-test-gen/ to the .gitignore file.
    """
    gitignore_path = os.path.join(project_path, ".gitignore")
    if not os.path.exists(gitignore_path):
        with open(gitignore_path, "w", encoding="utf-8") as f:
            f.write("# Local AI Test Generator Data\n.ai-test-gen/\n")
        return

    try:
        with open(gitignore_path, "r", encoding="utf-8") as f:
            content = f.read()
        
        if ".ai-test-gen/" not in content:
            with open(gitignore_path, "a", encoding="utf-8") as f:
                f.write("\n# Local AI Test Generator Data\n.ai-test-gen/\n")
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
