package xyz.upperlevel.quakecraft.profile;

import org.dizitart.no2.Cursor;
import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.WriteResult;
import org.dizitart.no2.filters.Filters;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NitriteDbProfileController extends ProfileController {
    private final Nitrite connection;

    public NitriteDbProfileController(NitriteDbConnection connection) {
        this.connection = connection.getHandle();
    }

    @Override
    public boolean createProfile(UUID id, String name, Profile profile) {
        Map<String, Object> data = new HashMap<>(profile.data);
        data.put("id", id.toString());
        data.put("name", name);

        WriteResult result = connection.getCollection("profiles").insert(new Document(data));
        return result.getAffectedCount() > 0;
    }

    @Override
    public Profile getProfile(UUID id) {
        Cursor cursor = connection.getCollection("profiles").find(Filters.eq("id", id.toString()));
        return cursor.size() > 0 ? new Profile(cursor.firstOrDefault()) : null;
    }

    @Override
    public Profile getProfile(String name) {
        Cursor cursor = connection.getCollection("profiles").find(Filters.eq("name", name));
        return cursor.size() > 0 ? new Profile(cursor.firstOrDefault()) : null;
    }

    @Override
    public boolean updateProfile(UUID id, Profile profile) {
        HashMap<String, Object> data = new HashMap<>(profile.data);
        data.remove("id");

        WriteResult result = connection.getCollection("profiles").update(Filters.eq("id", id.toString()), new Document(data));
        return result.getAffectedCount() > 0;
    }

    @Override
    public boolean deleteProfile(UUID id) {
        WriteResult result = connection.getCollection("profiles").remove(Filters.eq("id", id.toString()));
        return result.getAffectedCount() > 0;
    }
}
