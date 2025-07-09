import * as vscode from 'vscode';
import * as path from 'path';
import * as fs from 'fs';
import * as fsp from 'fs/promises';
import { LanguageClient, LanguageClientOptions, ServerOptions } from 'vscode-languageclient/node';

let sliderPanel: vscode.WebviewPanel | undefined;
let client: LanguageClient;

/**
 * Copies all files from server/custom_progs/lib to workspace/lib
 */
async function installCustomLibs(context: vscode.ExtensionContext) {
    const sourceLibDir = context.asAbsolutePath(path.join('..', 'server', 'custom_progs'));
    const workspaceFolder = vscode.workspace.workspaceFolders?.[0]?.uri.fsPath;

    if (!workspaceFolder) {
        console.warn('No workspace folder found.');
        return;
    }

    const destLibDir = path.join(workspaceFolder, 'lib');

    try {
        await fsp.mkdir(destLibDir, { recursive: true });

        const files = await fsp.readdir(sourceLibDir);

        for (const file of files) {
            const sourcePath = path.join(sourceLibDir, file);
            const destPath = path.join(destLibDir, file);

            const stat = await fsp.stat(sourcePath);
            if (stat.isFile()) {
                console.log(`Copying file: ${file}`);
                await fsp.copyFile(sourcePath, destPath);
            } else {
                console.log(`Skipping non-file: ${file}`);
            }
        }

    } catch (error) {
        console.error(`Failed to copy files to workspace/lib: ${error}`);
    }
}

export async function activate(context: vscode.ExtensionContext) {
    const jarDir = context.asAbsolutePath(path.join('..', 'server'));
    const mainJar = path.join(jarDir, 'energy_prediction-1.0-SNAPSHOT-jar-with-dependencies.jar');
    //const helperJar = path.join(jarDir, 'BinaryTrees.jar');

    //const sep = process.platform === 'win32' ? ';' : ':';
    const classpath = `${mainJar}`;//${sep}${helperJar}

    const serverOptions: ServerOptions = {
        command: 'java',
        args: ['-cp', classpath, 'com.tool.App']
    };

    const clientOptions: LanguageClientOptions = {
        documentSelector: [{ scheme: 'file', language: 'java' }],
    };

    client = new LanguageClient('javaLspServer', 'Java LSP Server', serverOptions, clientOptions);
    await client.start();

    // Install ALL files into workspace/lib from server/custom_progs/lib
    await installCustomLibs(context);

    // Server → Webview: update sliders
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

    // Server → Webview: update energy values
    client.onNotification('custom/updateEnergy', (params) => {
        console.log('Received custom/updateEnergy notification:', params);
        if (sliderPanel) {
            sliderPanel.webview.postMessage({
                type: 'updateEnergy',
                energy: params.totalEnergyUsed,
                methodsEnergy: params.methodsEnergy
            });
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

        // Webview → Server
        sliderPanel.webview.onDidReceiveMessage(message => {
            switch (message.type) {
                case 'sliderChange':
                    console.log(`Slider changed: ${message.id} = ${message.value}`);
                    client.sendNotification('custom/sliderChanged', {
                        id: message.id,
                        value: message.value
                    });
                    break;

                case 'calculateEnergy':
                    console.log('Received calculateEnergy request from webview');
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
