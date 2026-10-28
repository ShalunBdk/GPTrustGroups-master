package com.neoclar.gptrustgroups;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** Каждый ключ сообщения, упомянутый в коде, есть в поставляемом messages.yml. */
class ShippedMessagesTest {
    private static final Pattern KEY = Pattern.compile(
            "\"((?:General|Create|Delete|Add|Remove|Exclude|Unexclude|List|Info|My|Help|Trust|TrustList)(?:\\.[A-Z][A-Za-z]*)+)\"");
    // Ключи, собираемые в коде конкатенацией: "Trust.Level." + levelKey
    private static final Set<String> DYNAMIC = Set.of(
            "Trust.Level.Build", "Trust.Level.Containers", "Trust.Level.Access", "Trust.Level.Manage");

    @Test
    void allReferencedKeysExist() throws IOException {
        YamlConfiguration messages = YamlConfiguration.loadConfiguration(Path.of("src/main/resources/messages.yml").toFile());

        Set<String> referenced = new TreeSet<>(DYNAMIC);
        try (Stream<Path> files = Files.walk(Path.of("src/main/java"))) {
            for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                Matcher m = KEY.matcher(Files.readString(file));
                while (m.find()) referenced.add(m.group(1));
            }
        }

        assertTrue(referenced.size() > 20, "ключи не нашлись — сломан шаблон: " + referenced);
        Set<String> missing = new TreeSet<>();
        for (String key : referenced) if (messages.getString(key) == null) missing.add(key);
        assertEquals(Set.of(), missing);
    }
}
