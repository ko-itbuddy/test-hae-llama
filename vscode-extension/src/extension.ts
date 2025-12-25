import * as vscode from 'vscode';
import * as path from 'path';
import * as cp from 'child_process';
import * as fs from 'fs';

let outputChannel: vscode.OutputChannel;
let isLlamaReady = false;
let statusProvider: LlamaProvider;

export async function activate(context: vscode.ExtensionContext) {
    outputChannel = vscode.window.createOutputChannel("Test-Hae-Llama 🦙");
    outputChannel.show(true);
    outputChannel.appendLine("🦙 테스트해라마 기상 중...");

    const llamaProvider = new LlamaProvider();
    statusProvider = llamaProvider; // For backward compatibility in initializeLlama
    vscode.window.registerTreeDataProvider('llama-main', llamaProvider);

    const venvPath = path.join(context.globalStorageUri.fsPath, 'venv');
    const pythonPath = process.platform === 'win32' 
        ? path.join(venvPath, 'Scripts', 'python.exe') 
        : path.join(venvPath, 'bin', 'python');

    initializeLlama(context, venvPath, pythonPath);

    context.subscriptions.push(
        vscode.commands.registerCommand('test-hae-llama.ingest', async (uri: vscode.Uri) => {
            if (!checkReady()) return;
            const projectPath = uri ? uri.fsPath : vscode.workspace.workspaceFolders?.[0].uri.fsPath;
            if (projectPath) runTask("ingest", "🦙 공부 중라마", ['--project-path', projectPath], projectPath, pythonPath, context);
        }),
        vscode.commands.registerCommand('test-hae-llama.generate', async (uri: vscode.Uri) => {
            if (!checkReady()) return;
            let targetFile = uri ? uri.fsPath : vscode.window.activeTextEditor?.document.uri.fsPath;
            if (targetFile?.endsWith('.java')) {
                const projectPath = vscode.workspace.getWorkspaceFolder(vscode.Uri.file(targetFile))?.uri.fsPath || path.dirname(targetFile);
                runTask("generate", "🚀 테스트 제작 중라마", ['--target-file', targetFile, '--project-path', projectPath], projectPath, pythonPath, context);
            }
        }),
        vscode.commands.registerCommand('test-hae-llama.reinstall', () => {
            if (fs.existsSync(context.globalStorageUri.fsPath)) fs.rmSync(context.globalStorageUri.fsPath, { recursive: true });
            vscode.commands.executeCommand('workbench.action.reloadWindow');
        }),
        vscode.commands.registerCommand('test-hae-llama.showLogs', () => outputChannel.show(true))
    );
}

async function initializeLlama(context: vscode.ExtensionContext, venvPath: string, pythonPath: string) {
    try {
        let needsInstall = !fs.existsSync(pythonPath);
        if (!needsInstall) {
            try {
                await execCmd(`"${pythonPath}" -c "import click; import langchain"`);
            } catch { needsInstall = true; }
        }

        if (needsInstall) {
            statusProvider.updateStatus("Installing... 📦");
            if (!fs.existsSync(context.globalStorageUri.fsPath)) fs.mkdirSync(context.globalStorageUri.fsPath, { recursive: true });
            outputChannel.appendLine("📦 가상환경 구축 중라마 (uv 사용)...");
            await execCmd(`uv venv "${venvPath}" --python 3.13`);
            const reqPath = path.join(context.extensionPath, 'python-core', 'requirements.txt');
            outputChannel.appendLine("📥 라이브러리 설치 중라마...");
            await execCmd(`uv pip install --python "${pythonPath}" -r "${reqPath}"`);
        }
        isLlamaReady = true;
        statusProvider.updateStatus("Ready ✅");
        outputChannel.appendLine("✅ 라마 엔진 가동 준비 완료!");
    } catch (err: any) {
        isLlamaReady = false;
        statusProvider.updateStatus("Setup Error ❌");
        outputChannel.appendLine(`❌ 초기화 에러: ${err.message}`);
    }
}

async function runTask(type: string, label: string, args: string[], cwd: string, pythonPath: string, context: vscode.ExtensionContext) {
    await vscode.window.withProgress({ location: vscode.ProgressLocation.Notification, title: label }, async () => {
        try {
            const config = vscode.workspace.getConfiguration('test-hae-llama');
            const finalArgs = [...args, '--model', config.get<string>('model') || 'qwen2.5-coder:7b'];
            if (type === 'generate') {
                const rules = config.get<string>('customRules') || '';
                const apiKey = config.get<string>('context7ApiKey') || '';
                if (rules) finalArgs.push('--custom-rules', rules);
                if (apiKey) finalArgs.push('--context7-api-key', apiKey);
            }
            
            const scriptPath = path.join(context.extensionPath, 'python-core', 'src', 'main.py');
            const pythonCoreDir = path.join(context.extensionPath, 'python-core');
            
            const res = await new Promise<string>((resolve, reject) => {
                const proc = cp.spawn(pythonPath, [scriptPath, type, ...finalArgs], { cwd, env: { ...process.env, PYTHONPATH: pythonCoreDir } });
                let out = '', err = '';
                proc.stdout?.on('data', (d) => { out += d.toString(); outputChannel.append(d.toString()); });
                proc.stderr?.on('data', (d) => { err += d.toString(); outputChannel.append(d.toString()); });
                proc.on('close', (code) => code === 0 ? resolve(out) : reject(new Error(err || out)));
            });

            if (type === 'generate') {
                const cleanCode = res.split("[RESULT_START]")[1]?.split("[RESULT_END]")[0]?.trim() || res;
                const doc = await vscode.workspace.openTextDocument({ content: cleanCode, language: 'java' });
                await vscode.window.showTextDocument(doc, { preview: false, viewColumn: vscode.ViewColumn.Beside });
            }
        } catch (err: any) { outputChannel.appendLine(`❌ 에러: ${err.message}`); }
    });
}

function checkReady() {
    if (!isLlamaReady) { vscode.window.showWarningMessage("🦙 아직 준비 중라마!"); return false; }
    return true;
}

function execCmd(cmd: string): Promise<void> {
    return new Promise((resolve, reject) => {
        cp.exec(cmd, (err, out, serr) => err ? reject(new Error(serr || out)) : resolve());
    });
}

class LlamaProvider implements vscode.TreeDataProvider<vscode.TreeItem> {
    private _status: string = "Initializing... ⏳";
    private _onDidChangeTreeData = new vscode.EventEmitter<void>();
    readonly onDidChangeTreeData = this._onDidChangeTreeData.event;

    updateStatus(s: string) { 
        this._status = s; 
        this._onDidChangeTreeData.fire(); 
    }

    getTreeItem(el: vscode.TreeItem): vscode.TreeItem { return el; }

    getChildren(): vscode.TreeItem[] {
        const statusItem = new vscode.TreeItem(`상태: ${this._status}`);
        statusItem.iconPath = this._status.includes("Ready") ? new vscode.ThemeIcon("check") : new vscode.ThemeIcon("sync~spin");
        
        return [
            statusItem,
            new LlamaItem("🦙 프로젝트 공부시키기", "test-hae-llama.ingest", new vscode.ThemeIcon("book")),
            new LlamaItem("🚀 테스트 코드 짜기", "test-hae-llama.generate", new vscode.ThemeIcon("beaker")),
            new LlamaItem("📋 로그 확인하기", "test-hae-llama.showLogs", new vscode.ThemeIcon("output")),
            new LlamaItem("♻️ 환경 재구축하기", "test-hae-llama.reinstall", new vscode.ThemeIcon("refresh"))
        ];
    }
}

class LlamaItem extends vscode.TreeItem {
    constructor(label: string, commandId: string, icon: vscode.ThemeIcon) {
        super(label); 
        this.iconPath = icon; 
        this.command = { command: commandId, title: label };
    }
}

export function deactivate() {}