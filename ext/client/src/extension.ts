/*
import * as vscode from 'vscode';
import * as path from 'path';
import { LanguageClient, LanguageClientOptions, ServerOptions } from 'vscode-languageclient/node';

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

    console.log(`Starting Java LSP Server with command: java -jar ${jarPath}`);
    client = new LanguageClient('javaLspServer', 'Java LSP Server', serverOptions, clientOptions);
    await client.start();

    const disposable = vscode.commands.registerCommand('javaLspExtension.openSliders', async () => {
        const panel = vscode.window.createWebviewPanel(
            'javaSliders',
            'Java Sliders',
            vscode.ViewColumn.Beside,
            { enableScripts: true }
        );

        const htmlPath = vscode.Uri.file(path.join(context.extensionPath, 'media', 'sliders.html'));
        const buffer = await vscode.workspace.fs.readFile(htmlPath);
        panel.webview.html = buffer.toString();
    });

    context.subscriptions.push(disposable);
}



export function deactivate(): Thenable<void> | undefined {
    return client?.stop();
}
*/


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

    // Receive notification from server to update sliders
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

        // Listen for slider changes from the WebView
        sliderPanel.webview.onDidReceiveMessage(message => {
            if (message.type === 'sliderChange') {
                console.log(`Slider changed: ${message.id} = ${message.value}`);

                // Send slider value to the Java language server
                client.sendNotification('custom/sliderChanged', {
                    id: message.id,
                    value: message.value
                });
            }
        });

        sliderPanel.onDidDispose(() => {
            sliderPanel = undefined;
        });
    });

    context.subscriptions.push(disposable);
}

