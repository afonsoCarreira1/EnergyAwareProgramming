package com.tool;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import com.parse.ASTFeatureExtractor;
import com.parse.ModelInfo;

public class Sliders {

    public static HashMap<String,HashMap<String, Object>> sliders = new HashMap<>();
    

    public static Map<String, Object> getSlidersInfo(String fullPath,HashSet<String> modelsSaved) {
        StringBuilder path = new StringBuilder();
        String[] parts = fullPath.split("/");
        String file = parts[parts.length - 1].split("\\.")[0];
        for (int i = 2; i < parts.length - 1; i++) {
            path.append(parts[i] + "/");
        }
        ASTFeatureExtractor parser = new ASTFeatureExtractor(path.toString(), file, true);
        CalculateEnergy.modelInfos = parser.getMethodsForSliders(modelsSaved);

        //for every var associated with the method, put it in a set, then use it for the sliders, avoids repetitions
        HashSet<String> filteredSlidersName = new HashSet<>();
        for (ModelInfo modelInfo : CalculateEnergy.modelInfos) {
            filteredSlidersName.addAll(modelInfo.getIds());
        }
        ArrayList<String> l = new ArrayList<>(filteredSlidersName);

        List<HashMap<String, Object>> slidersTemp = new ArrayList<>();
        for (String id : l) {
            System.err.println("found important inputs -> "+ id);
            int val = 1000;
            if (sliders.get(id) != null) val = (int) sliders.get(id).get("val"); //get slider value if it already exists
            HashMap<String, Object> slider = new HashMap<>();
            slider.put("id", id);
            slider.put("label", "Value");
            slider.put("min", 1);
            slider.put("max", 2000);
            slider.put("val", val);
            slidersTemp.add(slider);
        }
        restartSlidersGlobalVar(slidersTemp);
        Map<String, Object> message = Map.of(
            "command", "updateSliders",
            "sliders", slidersTemp
        );

        return message;
    }

    private static void restartSlidersGlobalVar(List<HashMap<String, Object>> slidersTemp) {
        sliders.clear();
        for (HashMap<String, Object> sliderTemp: slidersTemp) {
            sliders.put((String) sliderTemp.get("id"),sliderTemp);
        }
    }

    public static void updateSliders(String id,String value) {
        HashMap<String,Object> slider = sliders.get(id);
        slider.put("val", Integer.parseInt(value));
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
