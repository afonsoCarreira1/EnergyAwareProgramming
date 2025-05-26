package com.tool;

import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;

import java.util.Map;

public interface CustomLanguageClient extends LanguageClient {
    @JsonNotification("custom/updateSliders")
    void updateSliders(Map<String, Object> params);
}
