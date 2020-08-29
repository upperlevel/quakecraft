package xyz.upperlevel.quakecraft.profile;

public interface DbConnection {
    void close();

    ProfileController getProfileController();
}
