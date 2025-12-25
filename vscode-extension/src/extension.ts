import * as vscode from 'vscode';
import * as path from 'path';
import * as cp from 'child_process';
import * as fs from 'fs';

let outputChannel: vscode.OutputChannel;
let isLlamaReady = false;
let statusProvider: LlamaStatusProvider;

export async function activate(context: vscode.ExtensionContext) {
    outputChannel = vscode.window.createOutputChannel("Test-Hae-Llama 🦙");
    outputChannel.show(true);
    outputChannel.appendLine("🦙 테스트해라마 25.1 가동!");

    statusProvider = new LlamaStatusProvider();
    const actionProvider = new LlamaActionProvider();
    vscode.window.registerTreeDataProvider('llama-status', statusProvider);
    vscode.window.registerTreeDataProvider('llama-actions', actionProvider);

    const venvPath = path.join(context.globalStorageUri.fsPath, 'venv');
    const pythonPath = process.platform === 'win32' 
        ? path.join(venvPath, 'Scripts', 'python.exe') 
        : path.join(venvPath, 'bin', 'python');

    initializeLlama(context, venvPath, pythonPath);

    context.subscriptions.push(
        vscode.commands.registerCommand('test-hae-llama.ingest', (uri: vscode.Uri) => {
            const projectPath = uri ? uri.fsPath : vscode.workspace.workspaceFolders?.[0].uri.fsPath;
            if (projectPath) runTask("ingest", "🦙 프로젝트 공부 중라마", ['--project-path', projectPath], projectPath, pythonPath, context);
        }),
        vscode.commands.registerCommand('test-hae-llama.generate', (uri: vscode.Uri) => {
            let targetFile = uri ? uri.fsPath : vscode.window.activeTextEditor?.document.uri.fsPath;
            if (targetFile?.endsWith('.java')) {
                const projectPath = vscode.workspace.getWorkspaceFolder(vscode.Uri.file(targetFile))?.uri.fsPath || path.dirname(targetFile);
                runTask("generate", "🚀 테스트 제작 중라마", ['--target-file', targetFile, '--project-path', projectPath], projectPath, pythonPath, context);
            }
        }),
        vscode.commands.registerCommand('test-hae-llama.showLogs', () => outputChannel.show(true))
    );
}

async function initializeLlama(context: vscode.ExtensionContext, venvPath: string, pythonPath: string) {
    try {
        if (!fs.existsSync(pythonPath)) {
            statusProvider.updateStatus("Setting up uv... 🚀");
            await execCmd(`uv venv "${venvPath}" --python 3.13`);
            const reqPath = path.join(context.extensionPath, 'python-core', 'requirements.txt');
            await execCmd(`uv pip install --python "${pythonPath}" -r "${reqPath}"`);
        }
        isLlamaReady = true;
        statusProvider.updateStatus("Ready ✅");
    } catch (err: any) {
        statusProvider.updateStatus("Error ❌");
        outputChannel.appendLine(`❌ Setup Error: ${err.message}`);
    }
}

async function runTask(type: string, label: string, args: string[], cwd: string, pythonPath: string, context: vscode.ExtensionContext) {
    if (!isLlamaReady) { vscode.window.showWarningMessage("🦙 라마가 준비 중라마!"); return; }

    await vscode.window.withProgress({
        location: vscode.ProgressLocation.Notification,
        title: label,
        cancellable: false
    }, async (progress) => {
        try {
            const config = vscode.workspace.getConfiguration('test-hae-llama');
            const model = config.get<string>('model') || 'qwen2.5-coder:7b';
            const rules = config.get<string>('customRules') || '';
            const apiKey = config.get<string>('context7ApiKey') || '';
            
            const finalArgs = [...args, '--model', model];
            if (type === 'generate') {
                if (rules) finalArgs.push('--custom-rules', rules);
                if (apiKey) finalArgs.push('--context7-api-key', apiKey);
            }

            const scriptPath = path.join(context.extensionPath, 'python-core', 'src', 'main.py');
            const pythonCoreDir = path.join(context.extensionPath, 'python-core');
            const env = { ...process.env, PYTHONPATH: pythonCoreDir };

            const res = await new Promise<string>((resolve, reject) => {
                const proc = cp.spawn(pythonPath, [scriptPath, type, ...finalArgs], { cwd, env });
                let out = '', err = '';
                
                proc.stdout?.on('data', (d: Buffer) => {
                    const line = d.toString();
                    // 💡 실시간 상태 업데이트 (모델 다운로드 진행률 포함) 파싱라마!
                    if (line.includes("[STATUS]")) {
                        const statusMsg = line.split("[STATUS]")[1].trim();
                        progress.report({ message: statusMsg });
                    }
                    if (line.includes("💾 [SAVED]")) {
                        vscode.window.showInformationMessage(line.trim());
                    }
                    out += line;
                    outputChannel.append(line);
                });
                
                proc.stderr?.on('data', (d: Buffer) => {
                    outputChannel.append(d.toString());
                    err += d.toString();
                });
                
                proc.on('close', (code) => code === 0 ? resolve(out) : reject(new Error(err || out)));
            });

            if (type === 'generate') {
                const cleanCode = res.split("[RESULT_START]")[1]?.split("[RESULT_END]")[0]?.trim() || res;
                const doc = await vscode.workspace.openTextDocument({ content: cleanCode, language: 'java' });
                await vscode.window.showTextDocument(doc, { preview: false, viewColumn: vscode.ViewColumn.Beside });
            }
        } catch (err: any) {
            outputChannel.appendLine(`❌ Error: ${err.message}`);
        }
    });
}

function execCmd(cmd: string): Promise<void> {
    return new Promise((resolve, reject) => {
        cp.exec(cmd, (err, out, serr) => err ? reject(new Error(serr || out)) : resolve());
    });
}

class LlamaActionProvider implements vscode.TreeDataProvider<LlamaItem> {
    getTreeItem(el: LlamaItem): vscode.TreeItem { return el; }
    getChildren(): LlamaItem[] {
        return [
            new LlamaItem("🦙 프로젝트 공부시키기", "test-hae-llama.ingest", new vscode.ThemeIcon("book")),
            new LlamaItem("🚀 테스트 코드 짜기", "test-hae-llama.generate", new vscode.ThemeIcon("beaker")),
            new LlamaItem("📋 로그 확인하기", "test-hae-llama.showLogs", new vscode.ThemeIcon("output"))
        ];
    }
}

class LlamaStatusProvider implements vscode.TreeDataProvider<vscode.TreeItem> {
    private _status: string = "Initializing... ⏳";
    private _onDidChangeTreeData = new vscode.EventEmitter<void>();
    readonly onDidChangeTreeData = this._onDidChangeTreeData.event;
    updateStatus(s: string) { this._status = s; this._onDidChangeTreeData.fire(); }
    getTreeItem(el: vscode.TreeItem): vscode.TreeItem { return el; }
    getChildren(): vscode.TreeItem[] { return [new vscode.TreeItem(`Status: ${this._status}`)]; }
}

class LlamaItem extends vscode.TreeItem {
    constructor(label: string, commandId: string, icon: vscode.ThemeIcon) {
        super(label); this.iconPath = icon; this.command = { command: commandId, title: label };
    }
}

export function deactivate() {}
