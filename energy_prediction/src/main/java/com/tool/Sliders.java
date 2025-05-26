package com.tool;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.parse.ASTFeatureExtractor;

public class Sliders {

    public static Map<String, Object> getSlidersInfo(String fullPath,HashSet<String> modelsSaved) {
        StringBuilder path = new StringBuilder();
        String[] parts = fullPath.split("/");
        String file = parts[parts.length - 1].split("\\.")[0];
        for (int i = 2; i < parts.length - 1; i++) {
            path.append(parts[i] + "/");
        }
        System.err.println("path -> " + path);
        System.err.println("file -> " + file);
        ASTFeatureExtractor parser = new ASTFeatureExtractor(path.toString(), file, true);
        HashMap<String, Map<String, Object>> features = parser.getFeatures();
        //System.err.println("features -> " + features);
        HashSet<String> featuresToSlider = parser.getMethodsForSliders(modelsSaved);
        ArrayList<String> l = new ArrayList<>(featuresToSlider);
        List<Map<String, Object>> sliders = new ArrayList<>();
        for (String id : l) {
            System.err.println(id);
            sliders.add(Map.of("id", id, "label", "Value", "min", 0, "max", 2000));
        }

        Map<String, Object> message = Map.of(
            "command", "updateSliders",
            "sliders", sliders
        );

        return message;
    }

    public static HashSet<String> getModels(String path) {
        HashSet<String> models = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                models.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return models;
    }

}
