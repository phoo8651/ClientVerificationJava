package net.rorum.minecraft;


import com.google.gson.*;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class ConfigHandler {
    private final HashMap<String, Object> configData;
    private final File config;

    protected ConfigHandler(File config) {
        this.config = config;
        configData = new HashMap<>();
        loadData();
    }

    public void onGeneration() {
        configData.put("ServerName", "Minecraft Server");
        configData.put("MongoUri", null);
        configData.put("MongoDatabase", null);
        configData.put("MongoCollection", null);
        configData.put("isVerification", false);
        saveData();
    }

    public HashMap<String, Object> onLoadConfig() {
        return configData;
    }

    public void updateConfig(String name, Object value) {
        configData.put(name, value);
        saveData();
    }

    private void loadData() {
        try {
            if (config.exists()) {
                String content = new String(Files.readAllBytes(Paths.get(config.toURI())));
                JsonObject dataObject = JsonParser.parseString(content).getAsJsonObject();
                configData.put("ServerName", dataObject.get("ServerName").getAsString());
                configData.put("MongoUri", dataObject.get("MongoUri").getAsString());
                configData.put("MongoDatabase", dataObject.get("MongoDatabase").getAsString());
                configData.put("MongoCollection", dataObject.get("MongoCollection").getAsString());
                configData.put("isVerification", dataObject.get("isVerification").getAsBoolean());
            } else {
                onGeneration(); // 파일이 없으면 기본값으로 초기화
            }
        } catch (IOException error) {
            Bukkit.getLogger().severe("Failed to load configuration: " + error.getMessage());
            error.printStackTrace();
        }
    }

    private void saveData() {
        JsonObject dataObject = new JsonObject();
        dataObject.addProperty("ServerName", configData.get("ServerName").toString());
        dataObject.addProperty("MongoUri", configData.get("MongoUri").toString());
        dataObject.addProperty("MongoDatabase", configData.get("MongoDatabase").toString());
        dataObject.addProperty("MongoCollection", configData.get("MongoCollection").toString());
        dataObject.addProperty("isVerification", (boolean) configData.get("isVerification"));

        try {
            if (!config.exists()) {
                config.getParentFile().mkdirs();
                config.createNewFile();
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String content = gson.toJson(dataObject);
            Files.write(Paths.get(config.toURI()), content.getBytes());
        } catch (IOException error) {
            Bukkit.getLogger().severe("Failed to save configuration: " + error.getMessage());
            error.printStackTrace();
        }
    }
}
