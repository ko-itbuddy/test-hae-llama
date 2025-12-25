import * as vscode from 'vscode';
import * as path from 'path';
import * as cp from 'child_process';
import * as fs from 'fs';

export function activate(context: vscode.ExtensionContext) {
    const provider = new LlamaChatProvider(context.extensionUri, context);
    context.subscriptions.push(
        vscode.window.registerWebviewViewProvider('llama-chat-view', provider)
    );

    context.subscriptions.push(
        vscode.commands.registerCommand('test-hae-llama.generate', async () => {
            provider.postMessage({ type: 'addLog', text: '🚀 테스트 코드 생성을 시작한다라마!' });
            // 기존 생성 로직 호출...
        })
    );
}

class LlamaChatProvider implements vscode.WebviewViewProvider {
    private _view?: vscode.WebviewView;

    constructor(private readonly _extensionUri: vscode.Uri, private readonly _context: vscode.ExtensionContext) {}

    public resolveWebviewView(webviewView: vscode.WebviewView) {
        this._view = webviewView;
        webviewView.webview.options = { enableScripts: true };
        webviewView.webview.html = this._getHtml(webviewView.webview);

        webviewView.webview.onDidReceiveMessage(async (data) => {
            switch (data.type) {
                case 'userMessage':
                    this.postMessage({ type: 'addBotMessage', text: `생각 중라마... 🧠` });
                    const response = await this._getLlamaResponse(data.text);
                    this.postMessage({ type: 'addBotMessage', text: response });
                    break;
            }
        });
    }

    public postMessage(msg: any) { this._view?.webview.postMessage(msg); }

    private async _getLlamaResponse(msg: string): Promise<string> {
        // 실제로는 python-core/src/main.py를 chat 모드로 호출
        return `라마가 분석해본 결과, [${msg}] 문제는 테스트 환경 설정의 라이브러리 버전 충돌일 확률이 높라마! AbstractTestBase를 확인해봐라마.`;
    }

    private _getHtml(webview: vscode.Webview) {
        return `<html>
            <body style="display: flex; flex-direction: column; height: 100vh; padding: 10px;">
                <div id="chat" style="flex: 1; overflow-y: auto; margin-bottom: 10px; font-family: sans-serif;">
                    <div style="color: #aaa; margin-bottom: 10px;">🦙 안녕라마! 무엇을 도와줄까라마?</div>
                </div>
                <div style="display: flex; gap: 5px;">
                    <input id="input" style="flex: 1; padding: 5px; background: #333; color: white; border: 1px solid #555;" placeholder="에러 로그를 붙여넣어라마...">
                    <button onclick="send()" style="padding: 5px 10px; background: #007acc; color: white; border: none; cursor: pointer;">전송</button>
                </div>
                <script>
                    const vscode = acquireVsCodeApi();
                    function send() {
                        const input = document.getElementById('input');
                        const text = input.value;
                        if (!text) return;
                        appendMessage('나', text);
                        vscode.postMessage({ type: 'userMessage', text });
                        input.value = '';
                    }
                    window.addEventListener('message', event => {
                        const msg = event.data;
                        if (msg.type === 'addBotMessage') appendMessage('라마', msg.text);
                    });
                    function appendMessage(sender, text) {
                        const chat = document.getElementById('chat');
                        const div = document.createElement('div');
                        div.innerHTML = '<b>' + sender + ':</b> ' + text;
                        div.style.marginBottom = '8px';
                        chat.appendChild(div);
                        chat.scrollTop = chat.scrollHeight;
                    }
                </script>
            </body>
        </html>`;
    }
}

export function deactivate() {}
