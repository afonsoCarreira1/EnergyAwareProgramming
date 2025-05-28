import * as vscode from 'vscode';
import * as path from 'path';
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
                sliders: params.sliders
            });
        } else {
            console.warn('Slider panel is not open, message not sent.');
        }
    });

    // Handle server notification to update energy in webview
    client.onNotification('custom/updateEnergy', (params) => {
        console.log('Received custom/updateEnergy notification:', params);
        if (sliderPanel) {
            sliderPanel.webview.postMessage({
                type: 'updateEnergy',
                energy: params.totalEnergyUsed
            });
        } else {
            console.warn('Slider panel is not open, message not sent.');
        }
    });

    const disposable = vscode.commands.registerCommand('javaLspExtension.openSliders', async () => {
        sliderPanel = vscode.window.createWebviewPanel(
            'javaSliders',
            'Java Sliders',
            vscode.ViewColumn.Beside,
            { enableScripts: true }
        );

        const htmlPath = vscode.Uri.file(path.join(context.extensionPath, 'media', 'sliders.html'));
        const buffer = await vscode.workspace.fs.readFile(htmlPath);
        sliderPanel.webview.html = buffer.toString();

        // Listen for messages from the webview (sliders and button)
        sliderPanel.webview.onDidReceiveMessage(message => {
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
