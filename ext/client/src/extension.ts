import * as vscode from 'vscode';
import * as path from 'path';
import * as fs from 'fs';
import * as fsp from 'fs/promises';
import { LanguageClient, LanguageClientOptions, ServerOptions } from 'vscode-languageclient/node';

let sliderPanel: vscode.WebviewPanel | undefined;
let client: LanguageClient;

async function installHelperJar(context: vscode.ExtensionContext, jarFileName: string) {
    const sourceJar = context.asAbsolutePath(path.join('..', 'server', jarFileName));
    const workspaceFolder = vscode.workspace.workspaceFolders?.[0]?.uri.fsPath;

    if (!workspaceFolder) {
        console.warn('No workspace folder found.');
        return;
    }

    const libDir = path.join(workspaceFolder, 'lib');
    const destJar = path.join(libDir, jarFileName);

    try {
        await fsp.mkdir(libDir, { recursive: true });
        await fsp.copyFile(sourceJar, destJar);

        const config = vscode.workspace.getConfiguration('java.project');
        const currentLibs: string[] = config.get('referencedLibraries') || [];

        if (!currentLibs.includes('lib/' + jarFileName)) {
            currentLibs.push('lib/' + jarFileName);
            await config.update('referencedLibraries', currentLibs, vscode.ConfigurationTarget.Workspace);
            console.log(`Added ${jarFileName} to java.project.referencedLibraries`);
        }
    } catch (error) {
        console.error(`Failed to install helper JAR: ${error}`);
    }
}

export async function activate(context: vscode.ExtensionContext) {
    const jarDir = context.asAbsolutePath(path.join('..', 'server'));
    const mainJar = path.join(jarDir, 'energy_prediction-1.0-SNAPSHOT-jar-with-dependencies.jar');
    const helperJar = path.join(jarDir, 'BinaryTrees.jar');

    const sep = process.platform === 'win32' ? ';' : ':';
    const classpath = `${mainJar}${sep}${helperJar}`;

    const serverOptions: ServerOptions = {
        command: 'java',
        args: ['-cp', classpath, 'com.tool.App']
    };

    const clientOptions: LanguageClientOptions = {
        documentSelector: [{ scheme: 'file', language: 'java' }],
    };

    client = new LanguageClient('javaLspServer', 'Java LSP Server', serverOptions, clientOptions);
    await client.start();

    // Install helper JAR into workspace and add to classpath
    await installHelperJar(context, 'BinaryTrees.jar');

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

        // Listen for messages from the webview (sliders and button)
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
