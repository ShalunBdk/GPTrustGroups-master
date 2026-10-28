package com.neoclar.gptrustgroups;

import com.neoclar.gptrustgroups.storage.GroupManager;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TestSupport {
    private static final Logger SILENT = Logger.getAnonymousLogger();
    static { SILENT.setLevel(Level.OFF); }

    private TestSupport() {
    }

    public static File groupsFile(Path dir) {
        return dir.resolve("groups.yml").toFile();
    }

    public static GroupManager manager(Path dir) {
        return new GroupManager(groupsFile(dir), SILENT);
    }

    public static UUID player(int n) {
        return new UUID(1L, n);
    }
}
