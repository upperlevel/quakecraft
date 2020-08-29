package xyz.upperlevel.quakecraft.profile;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;

public class MongoDbConnection implements DbConnection {
    @Getter
    private final MongoClient client;

    @Getter
    private final MongoDatabase database;

    @Getter
    private final MongoDbProfileController profileController;

    public MongoDbConnection(MongoClient client, MongoDatabase database) {
        this.client = client;
        this.database = database;

        this.profileController = new MongoDbProfileController(this);
    }

    @Override
    public void close() {
        this.client.close();
    }

    public static MongoDbConnection create(String host, int port, String database, String username, String password) {
        try {
            Class.forName("com.mongodb.MongoClient");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
        ServerAddress address = new ServerAddress(host, port);
        MongoClient client;
        if (database != null && username != null && password != null) {
            MongoCredential credential = MongoCredential.createCredential(username, database, password.toCharArray());
            client = new MongoClient(address, credential, MongoClientOptions.builder().build());
        } else {
            client = new MongoClient(address);
        }
        return new MongoDbConnection(client, client.getDatabase(database));
    }
}
