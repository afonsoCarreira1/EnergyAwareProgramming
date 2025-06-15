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
import com.parse.MethodEnergyInfo;
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
        Tool.parser = new ASTFeatureExtractor(path.toString(), file, true);
        CalculateEnergy.methodsEnergyInfo = Tool.parser.getMethodsForSliders(modelsSaved);

        HashSet<String> methodsNotRepeated = new HashSet<>();// to create the methods containers in the UI
        for(MethodEnergyInfo m : CalculateEnergy.methodsEnergyInfo) {
            methodsNotRepeated.add(m.getMethodName());
        }
        
        ArrayList<String> slidersListNotRepeated = getSlidersNoRepetitions(); //gets the vars that can be sliders in the UI

        List<HashMap<String, Object>> slidersTemp = new ArrayList<>();
        populateSildersTemp(slidersListNotRepeated, slidersTemp);
        restartSlidersGlobalVar(slidersTemp);
        Map<String, Object> message = Map.of(
            "command", "updateSliders",
            "sliders", slidersTemp,
            "methods",joinMethodsWithEnergy(methodsNotRepeated)
        );

        return message;
    }

    private static HashMap<String, Double> joinMethodsWithEnergy(HashSet<String> methods) {
        HashMap<String, Double> methodsEnergy = new HashMap<>();
        for (String method : methods) {
            methodsEnergy.put(method, CalculateEnergy.methodsEnergyForContainer.getOrDefault(method, -1.0));//-1 pq n consigo enviar null para o .ts
        }
        System.err.println("methodsEnergy -> "+methodsEnergy);
        return methodsEnergy;
    }

    private static void populateSildersTemp(ArrayList<String> slidersListNotRepeated,List<HashMap<String, Object>> slidersTemp) {
        for (String id : slidersListNotRepeated) {
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
    }

    //for every var associated with the method, put it in a set, then use it for the sliders, avoids repetitions
    private static ArrayList<String> getSlidersNoRepetitions() {
        HashSet<String> filteredSlidersName = new HashSet<>();
        for (ModelInfo modelInfo : getAllVarsForSliders()) {
            filteredSlidersName.addAll(modelInfo.getIds());
        }
        return new ArrayList<>(filteredSlidersName);
    }

    private static ArrayList<ModelInfo> getAllVarsForSliders() {
        ArrayList<ModelInfo> modelInfos = new ArrayList<>();
        for (MethodEnergyInfo methodEnergyInfo : CalculateEnergy.methodsEnergyInfo) {
            modelInfos.addAll(methodEnergyInfo.getModelInfos());
        }
        return modelInfos;
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
