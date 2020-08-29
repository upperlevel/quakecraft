package xyz.upperlevel.quakecraft.profile;

import lombok.Getter;
import org.dizitart.no2.Nitrite;

import java.io.File;

public class NitriteDbConnection implements DbConnection {
    @Getter
    private final Nitrite handle;

    @Getter
    private final NitriteDbProfileController profileController;

    public NitriteDbConnection(Nitrite handle) {
        this.handle = handle;

        this.profileController = new NitriteDbProfileController(this);
    }

    @Override
    public void close() {
        this.handle.close();
    }

    public static NitriteDbConnection create(File file) {
        Nitrite connection = Nitrite.builder().compressed().filePath(file).openOrCreate();
        return new NitriteDbConnection(connection);
    }
}
