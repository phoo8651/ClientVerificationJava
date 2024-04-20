package net.rorum.minecraft;

import com.mongodb.client.*;
import org.bson.Document;
import org.bukkit.Bukkit;

import java.util.HashMap;

public class MongoHandler {
    private final ConfigHandler configHandler;
    private MongoClient mongoClient;
    private MongoCollection<Document> mongoCollection;

    public MongoHandler(ConfigHandler configHandler) {
        this.configHandler = configHandler;
    }

    public void connectMongo() {
        HashMap<String, Object> data = configHandler.onLoadConfig();
        try {
            Bukkit.getLogger().info("call connectMongo");
            Bukkit.getLogger().info("MongoUri : " + data.get("MongoUri").toString());
            Bukkit.getLogger().info("MongoDatabase : " + data.get("MongoDatabase").toString());
            Bukkit.getLogger().info("MongoCollection : " + data.get("MongoCollection").toString());
            this.mongoClient = MongoClients.create(data.get("MongoUri").toString());
            MongoDatabase mongoDatabase = mongoClient.getDatabase(data.get("MongoDatabase").toString());
            this.mongoCollection = mongoDatabase.getCollection(data.get("MongoCollection").toString());
        } catch (Exception error) {
            // 예외 처리 수정: 에러 로깅
            Bukkit.getLogger().severe("Failed to connect to MongoDB: " + error.getMessage());
            error.fillInStackTrace();
        }
    }

    public void closeMongo() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
            mongoCollection = null;
        }
    }

    protected boolean isDocumentFindById(String Id) {
        if (mongoCollection == null) {
            Bukkit.getLogger().severe("MongoCollection is not initialized. Make sure to call connectMongo() first.");
            return false;
        }

        Document document = mongoCollection.find(new Document("_id", Id)).first();
        return document != null;
    }
}
