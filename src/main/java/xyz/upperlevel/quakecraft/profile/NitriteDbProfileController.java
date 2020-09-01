package xyz.upperlevel.quakecraft.profile;

import org.dizitart.no2.*;
import org.dizitart.no2.exceptions.UniqueConstraintException;
import org.dizitart.no2.filters.Filters;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NitriteDbProfileController extends ProfileController {
    private final Nitrite connection;
    private final NitriteCollection collection;

    public NitriteDbProfileController(NitriteDbConnection connection) {
        this.connection = connection.getHandle();

        collection = this.connection.getCollection("profiles");
        if (!collection.hasIndex("id")) collection.createIndex("id", IndexOptions.indexOptions(IndexType.Unique));
        if (!collection.hasIndex("name")) collection.createIndex("name", IndexOptions.indexOptions(IndexType.Unique));
    }

    @Override
    public boolean createProfile0(UUID id, String name, Profile profile) {
        Map<String, Object> data = new HashMap<>(profile.data);
        data.put("id", id.toString());
        data.put("name", name);

        try {
            collection.insert(new Document(data));
            return true;
        } catch (UniqueConstraintException ignored) {
            return false;
        }
    }

    @Override
    public Profile getProfile0(UUID id) {
        Cursor cursor = collection.find(Filters.eq("id", id.toString()));
        return cursor.size() > 0 ? new Profile(cursor.firstOrDefault()) : null;
    }

    @Override
    public Profile getProfile0(String name) {
        Cursor cursor = collection.find(Filters.eq("name", name));
        return cursor.size() > 0 ? new Profile(cursor.firstOrDefault()) : null;
    }

    @Override
    public boolean updateProfile0(UUID id, Profile profile) {
        HashMap<String, Object> data = new HashMap<>(profile.data);
        data.remove("id");

        WriteResult result = collection.update(Filters.eq("id", id.toString()), new Document(data));
        return result.getAffectedCount() > 0;
    }

    @Override
    public boolean deleteProfile0(UUID id) {
        WriteResult result = collection.remove(Filters.eq("id", id.toString()));
        return result.getAffectedCount() > 0;
    }
}
