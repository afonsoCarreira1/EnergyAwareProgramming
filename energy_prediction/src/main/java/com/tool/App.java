package com.tool;

import org.eclipse.lsp4j.jsonrpc.Launcher;

public class App 
{
    public static void main(String[] args) throws Exception
    {
        System.err.println("Tool started");
        Tool server = new Tool();

        Launcher<CustomLanguageClient> launcher = new Launcher.Builder<CustomLanguageClient>()
        .setRemoteInterface(CustomLanguageClient.class)
        .setInput(System.in)
        .setOutput(System.out)
        .setLocalService(server)
        .create();
        server.connect(launcher.getRemoteProxy());
        launcher.startListening();
    }
}
