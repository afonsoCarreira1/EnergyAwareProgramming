package com.tool;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SetTraceParams;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.NotebookDocumentService;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;

public class Tool implements LanguageServer {

    private final TextDocumentService textDocumentService = new ToolTextDocumentService();
    private CustomLanguageClient client;

    @JsonNotification("custom/sliderChanged")
    public void onSliderChanged(Map<String, Object> params) {
        String id = (String) params.get("id");
        String value = (String) params.get("value");
        System.err.println("Slider changed: " + id + " = " + value);
        System.err.println("Updating slider [" + id + "] to value: " + value);
        Sliders.updateSliders(id, value);
    }

    @JsonNotification("custom/calculateEnergy")
    public void onCalculateEnergy(Object ignored) throws URISyntaxException {
        Path serverDir = Paths.get(Sliders.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        double totalEnergyUsed = CalculateEnergy.calculateEnergy(serverDir.toString() + "/collected_models/");
        Map<String,Object> message = Map.of("totalEnergyUsed",totalEnergyUsed);
        if (client != null) client.updateEnergy(message);
    }

    public void connect(LanguageClient client) {
        this.client = (CustomLanguageClient) client;
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return textDocumentService;
    }

    @Override
    public NotebookDocumentService getNotebookDocumentService() {
        return null;
    }

    @Override
    public void setTrace(SetTraceParams params) {
        System.err.println("Trace level set to: " + params);
    }

    private class ToolTextDocumentService implements TextDocumentService {
        private Map<String, String> openDocuments = new HashMap<>();

        @Override
        public void didOpen(DidOpenTextDocumentParams params) {
            String uri = params.getTextDocument().getUri();
            String text = params.getTextDocument().getText();
            openDocuments.put(uri, text);
            parseAndAnalyze(text);
            System.err.println("didOpen called");
        }

        @Override
        public void didChange(DidChangeTextDocumentParams params) {
            String uri = params.getTextDocument().getUri();
            String newText = params.getContentChanges().get(0).getText();
            openDocuments.put(uri, newText);
            parseAndAnalyze(newText);
            System.err.println("didChange called");
        }

        @Override
        public void didClose(DidCloseTextDocumentParams params) {
            String uri = params.getTextDocument().getUri();
            openDocuments.remove(uri);
            System.err.println("didClose called");
        }

        @Override
        public void didSave(DidSaveTextDocumentParams params) {
            String file = params.getTextDocument().getUri();
            // System.err.println("uri -> "+ file);
            // System.err.println("didSave called -> "+ params);
            try {
                Path serverDir = Paths.get(Sliders.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                        .getParent();
                HashSet<String> modelsSaved = Sliders.getModels(serverDir.toString() + "/" + "ModelsAvailable.txt");
                Map<String, Object> message = Sliders.getSlidersInfo(file.split("///")[1], modelsSaved);
                if (client != null) {
                    client.updateSliders(message);
                }

            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void parseAndAnalyze(String javaSource) {
        // Your parsing and logic here
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        ServerCapabilities capabilities = new ServerCapabilities();
        // Enable text document synchronization (open, change, close)
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
        InitializeResult result = new InitializeResult(capabilities);
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void exit() {
        System.exit(0);
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return null;
    }
}
