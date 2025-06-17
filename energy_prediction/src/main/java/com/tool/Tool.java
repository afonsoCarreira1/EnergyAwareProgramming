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
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SetTraceParams;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.NotebookDocumentService;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import com.parse.ASTFeatureExtractor;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;

public class Tool implements LanguageServer {

    private final TextDocumentService textDocumentService = new ToolTextDocumentService();
    private CustomLanguageClient client;
    static public ASTFeatureExtractor parser = null;
    static private HashSet<String> modelsSaved = null;

    @JsonNotification("custom/sliderChanged")
    public void onSliderChanged(Map<String, Object> params) {
        String id = (String) params.get("id");
        String value = (String) params.get("value");
        System.err.println("Slider changed: " + id + " = " + value);
        System.err.println("Updating slider [" + id + "] to value: " + value);
        Sliders.updateSliders(id, value);
    }

    //bad solution, o melhor e so tirar isto dos update dos sliders e deixar so qd o bttn da energia e computado
    @JsonNotification("custom/calculateEnergy")
    public void onCalculateEnergy(Object ignored) throws URISyntaxException {
        Path serverDir = Paths.get(Sliders.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        double totalEnergyUsed = CalculateEnergy.calculateEnergy(serverDir.toString() + "/collected_models/");
        Map<String,Object> message = Map.of("totalEnergyUsed",totalEnergyUsed,"methodsEnergy",CalculateEnergy.getMethodsEnergy());
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
            System.err.println("didOpen called");
        }

        @Override
        public void didChange(DidChangeTextDocumentParams params) {
            String uri = params.getTextDocument().getUri();
            String newText = params.getContentChanges().get(0).getText();
            openDocuments.put(uri, newText);
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
                Path serverDir = Paths.get(Sliders.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
                if (modelsSaved == null) modelsSaved = Sliders.getModels(serverDir.toString() + "/" + "ModelsAvailable.txt");
                Map<String, Object> message = Sliders.getSlidersInfo(file.split("///")[1], modelsSaved);
                if (client != null) {
                    client.updateSliders(message);
                }

            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public CompletableFuture<Hover> hover(HoverParams params) {
            String uri = params.getTextDocument().getUri();
            Position pos = params.getPosition();

            String text = openDocuments.get(uri);
            if (text == null) return CompletableFuture.completedFuture(null);

            // Use the position to find the line being hovered
            String[] lines = text.split("\n");
            if (pos.getLine() >= lines.length) return CompletableFuture.completedFuture(null);
            String line = lines[pos.getLine()];

            //String hoveredToken = getHoveredToken(line, pos.getCharacter());
            String key = line.trim().replace(";", "") + " | " +(pos.getLine()+1);
            System.err.println("Available expression: "+CalculateEnergy.lineExpressions);
            System.err.println("Esta key -> "+key);
            String expression = CalculateEnergy.lineExpressions.getOrDefault(key, "No expression available");

            if (expression == null) return CompletableFuture.completedFuture(null);

            // Prepare hover content
            MarkupContent content = new MarkupContent();
            content.setKind(MarkupKind.MARKDOWN);
            content.setValue("🔋 **Energy Expression:**\n```java\n" + expression + "\n```");

            Hover hover = new Hover(content);
            return CompletableFuture.completedFuture(hover);
        }


    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        ServerCapabilities capabilities = new ServerCapabilities();
        // Enable text document synchronization (open, change, close)
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
        capabilities.setHoverProvider(true);
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
