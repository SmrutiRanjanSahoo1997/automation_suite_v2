import os
import sys
import time
from datetime import datetime

# Reconfigure stdout for UTF-8 output in Windows console if needed
try:
    sys.stdout.reconfigure(encoding='utf-8')
except Exception:
    pass

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
BASE_DIR = os.path.abspath(os.path.join(SCRIPT_DIR, '..'))
DOC_DIR = os.path.join(SCRIPT_DIR, 'doc')

IGNORED_DIRS = {
    'node_modules', 'target', 'dist', 'build', '__pycache__',
    '.pytest_cache', '.git', '.idea', '.vscode', 'logs',
    'automation_profile', 'storage', '.mvn', '.mvnw'
}

IGNORED_EXTS = {
    '.png', '.jpg', '.jpeg', '.gif', '.ico', '.webp', '.pdf',
    '.zip', '.tar', '.gz', '.exe', '.dll', '.jar', '.war',
    '.class', '.pyc', '.pyo', '.db', '.sqlite', '.sqlite3',
    '.log', '.bak', '.tmp', '.DS_Store'
}

IGNORED_FILES = {
    'package-lock.json', 'yarn.lock', 'pnpm-lock.yaml',
    'frontend_detailed_code.md', 'frontent_detailed_code.md',
    'springboot_detailed_code.md', 'springboot_detailsed_code.md',
    'agent_detailed_code.md', 'copilot_detailed_code.md', 'ai_agent_detailed_code.md',
    'frontend_detailed_code.md.bak', 'current_plan.txt',
    'help.md', 'workflow.md',
    'agent-test-console.html',
}

EXT_LANG_MAP = {
    '.java': 'java',
    '.ts': 'typescript',
    '.tsx': 'tsx',
    '.js': 'javascript',
    '.jsx': 'jsx',
    '.py': 'python',
    '.html': 'html',
    '.css': 'css',
    '.scss': 'scss',
    '.sql': 'sql',
    '.xml': 'xml',
    '.json': 'json',
    '.yaml': 'yaml',
    '.yml': 'yaml',
    '.properties': 'properties',
    '.bat': 'cmd',
    '.cmd': 'cmd',
    '.sh': 'bash',
    '.ps1': 'powershell',
    '.md': 'markdown',
    '.txt': 'text',
    '.ini': 'ini',
    '.env': 'properties'
}

PROJECTS = [
    {
        'id': 'agent',
        'title': 'Automation Suite Agent',
        'root_dir': os.path.join(BASE_DIR, 'automation_suite_agent'),
        'root_display': 'automation_suite_agent',
        'output_files': ['agent_detailed_code.md'],
        'summary': 'Plain Java 21 generic CLI execution bridge. Owns one authenticated WebSocket to Spring Boot and at most 10 isolated CMD/PowerShell ConPTY terminals with atomic capacity/lifecycle management. No business logic, no persistence — it streams terminal stdout/stderr chunks and status over the single WebSocket.',
        'exclude_fn': lambda rel_path: False
    },
    {
        'id': 'springboot',
        'title': 'Automation Suite SpringBoot Orchestrator',
        'root_dir': os.path.join(BASE_DIR, 'automation_suite_springboot'),
        'root_display': 'automation_suite_springboot',
        'output_files': ['springboot_detailed_code.md'],
        'summary': 'Spring Boot 3.2 Orchestrator service. Connects outbound WebSocket clients to 34+ target agents on port 9010 with auto-reconnect loops, exposes global WebSocket hub at /ws/app for browser clients, persists session logs and command audit snapshots in SQLite (C:/Ranjan/Automation Suite/Database/automation.db), and enforces 7-day session log and 25-day command log retention schedules.',
        'exclude_fn': lambda rel_path: False
    },
    {
        'id': 'frontend',
        'title': 'Automation Suite React Frontend',
        'root_dir': os.path.join(BASE_DIR, 'automation_suite_frontend'),
        'root_display': 'automation_suite_frontend',
        'output_files': ['frontend_detailed_code.md'],
        'summary': 'React 18 + Vite + TypeScript Frontend module featuring single global WebSocket hub (/ws/app), multi-tab server dashboard, environment & server category filters, resizable floating terminal modal windows, custom tab renaming, dynamic prompt path displays (PS C:\\...>), command history navigation, and Ctrl+C interrupt support.',
        'exclude_fn': lambda rel_path: False
    },
    {
        'id': 'ai_agent',
        'title': 'Automation Suite AI Agent Server',
        'root_dir': os.path.join(BASE_DIR, 'automation_suite_ai_agent'),
        'root_display': 'automation_suite_ai_agent',
        'output_files': ['ai_agent_detailed_code.md'],
        'summary': 'AI Agent WebSocket Server module running on port 9020. Connects to SpringBoot orchestrator and Chrome Extension, resolves target host selection and remediation prompt tasks, and manages AI execution pipelines.',
        'exclude_fn': lambda rel_path: False
    }
]


def is_binary(file_path):
    try:
        with open(file_path, 'rb') as f:
            chunk = f.read(1024)
            if b'\x00' in chunk:
                return True
    except Exception:
        return True
    return False


def should_ignore_file(filename):
    fn_lower = filename.lower()
    if fn_lower == 'help.md' or fn_lower == 'workflow.md' or fn_lower.endswith('workflow.md'):
        return True
    if filename in IGNORED_FILES or any(filename.endswith(ext) for ext in IGNORED_EXTS):
        return True
    return False


def build_ascii_tree(root_name, rel_paths):
    tree = {}
    for rel_path in sorted(rel_paths):
        parts = rel_path.replace('\\', '/').split('/')
        curr = tree
        for part in parts:
            curr = curr.setdefault(part, {})

    def format_tree(node, prefix=''):
        lines = []
        items = sorted(node.keys())
        for index, item in enumerate(items):
            is_last = (index == len(items) - 1)
            connector = '└── ' if is_last else '├── '
            child_prefix = '    ' if is_last else '│   '
            is_dir = bool(node[item])
            lines.append(f"{prefix}{connector}{item}{'/' if is_dir else ''}")
            if is_dir:
                lines.extend(format_tree(node[item], prefix + child_prefix))
        return lines

    return f"{root_name}/\n" + "\n".join(format_tree(tree))


def get_fence(content):
    if '````' in content:
        return '`````'
    elif '```' in content:
        return '````'
    return '```'


def process_project(proj):
    root_dir = proj['root_dir']
    root_display = proj['root_display']
    title = proj['title']
    summary = proj['summary']
    exclude_fn = proj['exclude_fn']

    print(f"\n[+] Processing: {title}...")
    if not os.path.exists(root_dir):
        print(f"    [!] Error: Root directory does not exist: {root_dir}")
        return

    collected_files = []

    for dirpath, dirnames, filenames in os.walk(root_dir):
        # Filter excluded subdirectories
        dirnames[:] = [d for d in dirnames if d not in IGNORED_DIRS]
        
        for f in filenames:
            if should_ignore_file(f):
                continue
            
            full_path = os.path.join(dirpath, f)
            rel_path = os.path.relpath(full_path, root_dir)

            if exclude_fn(rel_path):
                continue

            if is_binary(full_path):
                continue

            collected_files.append((rel_path, full_path))

    collected_files.sort(key=lambda x: x[0].replace('\\', '/'))
    rel_paths = [x[0] for x in collected_files]

    total_files = len(collected_files)
    total_lines = 0
    total_bytes = 0
    file_contents = []

    for rel_path, full_path in collected_files:
        size_bytes = os.path.getsize(full_path)
        total_bytes += size_bytes
        
        content = ""
        lines_count = 0
        try:
            with open(full_path, 'r', encoding='utf-8', errors='replace') as fh:
                content = fh.read()
                lines_count = len(content.splitlines())
        except Exception as e:
            content = f"// Error reading file: {e}"
            lines_count = 0

        total_lines += lines_count
        file_contents.append({
            'rel_path': rel_path.replace('\\', '/'),
            'full_path': full_path,
            'size_bytes': size_bytes,
            'lines_count': lines_count,
            'content': content
        })

    tree_str = build_ascii_tree(root_display, rel_paths)
    gen_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    abs_root = os.path.abspath(root_dir).replace('\\', '/')

    # Build Markdown Content
    md = []
    md.append(f"# 🚀 {title} - Detailed Source Code")
    md.append("")
    md.append(f"> 📅 **Generated On:** `{gen_time}`  ")
    md.append(f"> 📂 **Source Root:** `{abs_root}`  ")
    md.append(f"> 📊 **Total Source Files:** `{total_files}`  ")
    md.append(f"> 📝 **Total Lines of Code:** `{total_lines:,}`  ")
    md.append(f"> 💾 **Total Size:** `{total_bytes / 1024:.2f} KB`  ")
    md.append("")
    md.append("---")
    md.append("")
    md.append("## 📌 Executive Summary")
    md.append("")
    md.append(summary)
    md.append("")
    md.append("---")
    md.append("")
    md.append("## 📂 Project Directory Structure")
    md.append("")
    md.append("```text")
    md.append(tree_str)
    md.append("```")
    md.append("")
    md.append("---")
    md.append("")
    md.append("## 📑 File Index")
    md.append("")
    for idx, item in enumerate(file_contents, 1):
        clean_path = item['rel_path']
        md.append(f"{idx}. [`{clean_path}`](#file-{idx})")
    md.append("")
    md.append("---")
    md.append("")
    md.append("## 💻 Comprehensive Source Code")
    md.append("")

    for idx, item in enumerate(file_contents, 1):
        clean_path = item['rel_path']
        ext = os.path.splitext(clean_path)[1].lower()
        lang = EXT_LANG_MAP.get(ext, 'text')
        fence = get_fence(item['content'])
        size_kb = item['size_bytes'] / 1024

        md.append(f"<a id=\"file-{idx}\"></a>")
        md.append(f"### {idx}. `{clean_path}`")
        md.append("")
        md.append(f"**Path:** `{clean_path}` | **Size:** `{size_kb:.2f} KB` | **Lines:** `{item['lines_count']}` | **Language:** `{lang}`")
        md.append("")
        md.append(f"{fence}{lang}")
        md.append(item['content'])
        md.append(f"{fence}")
        md.append("")
        md.append("---")
        md.append("")

    final_md_content = "\n".join(md)

    os.makedirs(DOC_DIR, exist_ok=True)

    for out_filename in proj['output_files']:
        out_path = os.path.join(DOC_DIR, out_filename)
        with open(out_path, 'w', encoding='utf-8') as f:
            f.write(final_md_content)
        print(f"    [✓] Generated: doc/{out_filename} ({total_files} files, {total_lines:,} lines, {os.path.getsize(out_path)/1024:.1f} KB)")


def main():
    start_time = time.time()
    print("===================================================")
    print("  Automation Suite - Codebase Details Generator")
    print("===================================================")

    for proj in PROJECTS:
        process_project(proj)

    # Clean up any leftover markdown files directly in workflow/ if present
    cleanup_files = [
        'frontend_detailed_code.md', 'frontent_detailed_code.md',
        'springboot_detailed_code.md', 'springboot_detailsed_code.md',
        'agent_detailed_code.md', 'copilot_detailed_code.md'
    ]
    for old_file in cleanup_files:
        old_path = os.path.join(SCRIPT_DIR, old_file)
        if os.path.exists(old_path):
            try:
                os.remove(old_path)
                print(f"    [-] Cleaned old root file: {old_file}")
            except Exception:
                pass

    elapsed = time.time() - start_time
    print(f"\n[SUCCESS] Completed detailed code generation for all projects in {elapsed:.2f} seconds!")


if __name__ == '__main__':
    main()
