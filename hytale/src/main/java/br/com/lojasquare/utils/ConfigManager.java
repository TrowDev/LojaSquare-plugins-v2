package br.com.lojasquare.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gson.*;

import br.com.lojasquare.LojaSquarePlugin;

/**
 * Gerenciador de configurações JSON para Hytale.
 * Equivalente ao ConfigManager YAML do Spigot, mas usando JSON.
 */
public class ConfigManager {

    private final Path configFile;
    private final LojaSquarePlugin plugin;
    private JsonObject rootObject;
    private final Gson gson;

    public ConfigManager(String name, LojaSquarePlugin plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        String fileName = name.endsWith(".json") ? name : name + ".json";
        this.configFile = plugin.getDataFolder().resolve(fileName);

        load();
    }

    private void load() {
        try {
            if (!Files.exists(plugin.getDataFolder())) {
                Files.createDirectories(plugin.getDataFolder());
            }

            if (!Files.exists(configFile)) {
                // Tenta copiar do resources
                try (InputStream is = getClass().getClassLoader()
                        .getResourceAsStream(configFile.getFileName().toString())) {
                    if (is != null) {
                        Files.copy(is, configFile);
                    } else {
                        // Cria arquivo vazio
                        Files.writeString(configFile, "{}", StandardCharsets.UTF_8);
                    }
                }
            }

            String content = Files.readString(configFile, StandardCharsets.UTF_8);
            rootObject = JsonParser.parseString(content).getAsJsonObject();

        } catch (Exception e) {
            plugin.log("Erro ao carregar configuração " + configFile.getFileName() + ": " + e.getMessage());
            rootObject = new JsonObject();
        }
    }

    public void save() {
        try {
            Files.writeString(configFile, gson.toJson(rootObject), StandardCharsets.UTF_8);
        } catch (IOException e) {
            plugin.log("Erro ao salvar configuração: " + e.getMessage());
        }
    }

    public void set(String path, Object value) {
        String[] parts = path.split("\\.");
        JsonObject current = rootObject;

        for (int i = 0; i < parts.length - 1; i++) {
            if (!current.has(parts[i]) || !current.get(parts[i]).isJsonObject()) {
                current.add(parts[i], new JsonObject());
            }
            current = current.getAsJsonObject(parts[i]);
        }

        String lastKey = parts[parts.length - 1];

        if (value == null) {
            current.add(lastKey, JsonNull.INSTANCE);
        } else if (value instanceof Boolean) {
            current.addProperty(lastKey, (Boolean) value);
        } else if (value instanceof Number) {
            current.addProperty(lastKey, (Number) value);
        } else if (value instanceof String) {
            current.addProperty(lastKey, (String) value);
        } else if (value instanceof List) {
            JsonArray array = new JsonArray();
            for (Object item : (List<?>) value) {
                if (item instanceof String) {
                    array.add((String) item);
                }
            }
            current.add(lastKey, array);
        } else {
            current.addProperty(lastKey, value.toString());
        }

        save();
    }

    private JsonElement getElement(String path) {
        String[] parts = path.split("\\.");
        JsonObject current = rootObject;

        for (int i = 0; i < parts.length - 1; i++) {
            if (!current.has(parts[i]) || !current.get(parts[i]).isJsonObject()) {
                return null;
            }
            current = current.getAsJsonObject(parts[i]);
        }

        String lastKey = parts[parts.length - 1];
        return current.has(lastKey) ? current.get(lastKey) : null;
    }

    public String getString(String path) {
        JsonElement element = getElement(path);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
    }

    public String getString(String path, String defaultValue) {
        String value = getString(path);
        return value != null ? value : defaultValue;
    }

    public boolean getBoolean(String path) {
        return getBoolean(path, false);
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        JsonElement element = getElement(path);
        return element != null && element.isJsonPrimitive() ? element.getAsBoolean() : defaultValue;
    }

    public int getInt(String path) {
        return getInt(path, 0);
    }

    public int getInt(String path, int defaultValue) {
        JsonElement element = getElement(path);
        return element != null && element.isJsonPrimitive() ? element.getAsInt() : defaultValue;
    }

    public double getDouble(String path) {
        return getDouble(path, 0.0);
    }

    public double getDouble(String path, double defaultValue) {
        JsonElement element = getElement(path);
        return element != null && element.isJsonPrimitive() ? element.getAsDouble() : defaultValue;
    }

    public List<String> getStringList(String path) {
        List<String> list = new ArrayList<>();
        JsonElement element = getElement(path);

        if (element != null && element.isJsonArray()) {
            for (JsonElement item : element.getAsJsonArray()) {
                if (item.isJsonPrimitive()) {
                    list.add(item.getAsString());
                }
            }
        }

        return list;
    }

    public Set<String> getKeys(String path) {
        JsonElement element = getElement(path);
        if (element != null && element.isJsonObject()) {
            return element.getAsJsonObject().keySet();
        }
        return Set.of();
    }

    public JsonObject getSection(String path) {
        JsonElement element = getElement(path);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    public void reload() {
        load();
    }
}
