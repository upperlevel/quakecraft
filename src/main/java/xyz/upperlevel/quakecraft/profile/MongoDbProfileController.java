package xyz.upperlevel.quakecraft.profile;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MongoDbProfileController extends ProfileController {
    private final MongoDatabase database;

    public MongoDbProfileController(MongoDbConnection connection) {
        this.database = connection.getDatabase();
    }

    @Override
    public boolean createProfile(UUID id, String name, Profile profile) {
        try {
            Map<String, Object> data = new HashMap<>(profile.data);
            data.put("id", id.toString());
            data.put("name", name);

            database.getCollection("profiles").insertOne(new Document(data));
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    public Profile getProfile(UUID id) {
        Map<String, Object> data = database.getCollection("profiles").find(new Document("_id", id.toString())).first();
        return data != null ? new Profile(data) : null;
    }

    @Override
    public Profile getProfile(String name) {
        Map<String, Object> data = database.getCollection("profiles").find(new Document("name", name)).first();
        return data != null ? new Profile(data) : null;
    }

    @Override
    public boolean updateProfile(UUID id, Profile profile) {
        UpdateResult result = database.getCollection("profiles").updateOne(new Document("_id", id.toString()), new Document(profile.data));
        return result.getModifiedCount() > 0;
    }

    @Override
    public boolean deleteProfile(UUID id) {
        DeleteResult result = database.getCollection("profiles").deleteOne(new Document("_id", id.toString()));
        return result.getDeletedCount() > 0;
    }
}
