import * as vscode from 'vscode';
import * as path from 'path';
import * as os from 'os';
import * as fs from 'fs/promises';
import { LanguageClient, LanguageClientOptions, ServerOptions } from 'vscode-languageclient/node';

let sliderPanel: vscode.WebviewPanel | undefined;
let client: LanguageClient;

export async function activate(context: vscode.ExtensionContext) {
    const jarPath = context.asAbsolutePath(path.join('..', 'server', 'energy_prediction-1.0-SNAPSHOT-jar-with-dependencies.jar'));

    const serverOptions: ServerOptions = {
        command: 'java',
        args: ['-jar', jarPath]
    };

    const clientOptions: LanguageClientOptions = {
        documentSelector: [{ scheme: 'file', language: 'java' }],
    };

    client = new LanguageClient('javaLspServer', 'Java LSP Server', serverOptions, clientOptions);
    await client.start();

    // Handle server notification to update sliders in webview
    client.onNotification('custom/updateSliders', (params) => {
        console.log('Received custom/updateSliders notification:', params);
        if (sliderPanel) {
            sliderPanel.webview.postMessage({
                type: 'updateSliders',
                sliders: params.sliders,
                methods: params.methods
            });
        } else {
            console.warn('Slider panel is not open, message not sent.');
        }
    });

    // Handle server notification to update energy in webview
    client.onNotification('custom/updateEnergy', async (params) => {
        console.log('Received custom/updateEnergy notification:', params);
        if (sliderPanel) {
            sliderPanel.webview.postMessage({
                type: 'updateEnergy',
                energy: params.totalEnergyUsed,
                methodsEnergy: params.methodsEnergy
            });

            // Wait 1 second before requesting HTML content
            setTimeout(() => {
                // Ask webview to send its current full HTML content
                sliderPanel?.webview.postMessage({ type: 'requestHtmlSnapshot' });
            }, 1000);
        } else {
            console.warn('Slider panel is not open, message not sent.');
        }
    });

    const disposable = vscode.commands.registerCommand('javaLspExtension.openEnergyExtension', async () => {
        sliderPanel = vscode.window.createWebviewPanel(
            'energyExtension',
            'Energy Extension',
            vscode.ViewColumn.Beside,
            { enableScripts: true }
        );

        const htmlPath = vscode.Uri.file(path.join(context.extensionPath, 'media', 'sliders.html'));
        const buffer = await vscode.workspace.fs.readFile(htmlPath);
        sliderPanel.webview.html = buffer.toString();

        // Listen for messages from the webview (sliders and button, plus HTML snapshot response)
        sliderPanel.webview.onDidReceiveMessage(async message => {
            switch (message.type) {
                case 'sliderChange':
                    console.log(`Slider changed: ${message.id} = ${message.value}`);
                    // Forward slider change to Java language server
                    client.sendNotification('custom/sliderChanged', {
                        id: message.id,
                        value: message.value
                    });
                    break;

                case 'calculateEnergy':
                    console.log('Received calculateEnergy request from webview');
                    // Send a notification to the Java language server to trigger energy estimation
                    client.sendNotification('custom/calculateEnergy');
                    break;

                case 'htmlSnapshot':
                    // message.html contains the full HTML string from the webview
                    console.log('Received HTML snapshot from webview, saving to temp file...');
                    const tempFilePath = path.join(os.tmpdir(), `energy_sliders_${Date.now()}.html`);
                    try {
                        await fs.writeFile(tempFilePath, message.html, 'utf8');
                        vscode.window.showInformationMessage(`Saved snapshot to ${tempFilePath}`);
                        console.log(`Snapshot saved: ${tempFilePath}`);
                    } catch (err) {
                        console.error('Error saving HTML snapshot:', err);
                        vscode.window.showErrorMessage('Failed to save HTML snapshot.');
                    }
                    break;

                default:
                    console.warn(`Unknown message type received from webview: ${message.type}`);
            }
        });

        sliderPanel.onDidDispose(() => {
            sliderPanel = undefined;
        });
    });

    context.subscriptions.push(disposable);
}

export function deactivate(): Thenable<void> | undefined {
    return client?.stop();
}
