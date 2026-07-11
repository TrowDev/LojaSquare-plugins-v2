package br.com.lojasquare.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.yaml.snakeyaml.Yaml;

public class ConfigManager {

    private final Path configFile;
    private final Path defaultResourcePath;
    private Map<String, Object> data;
    private final Yaml yaml;

    public ConfigManager(String name, Path configDir) {
        String fileName = name.endsWith(".yml") ? name : name + ".yml";
        this.configFile = configDir.resolve(fileName);
        this.defaultResourcePath = Path.of(fileName);
        this.yaml = new Yaml();
        load();
    }

    @SuppressWarnings("unchecked")
    private void load() {
        try {
            if (!Files.exists(configFile.getParent())) {
                Files.createDirectories(configFile.getParent());
            }
            if (!Files.exists(configFile)) {
                InputStream is = getClass().getClassLoader()
                        .getResourceAsStream(defaultResourcePath.toString());
                if (is != null) {
                    Files.copy(is, configFile);
                } else {
                    try (FileWriter fw = new FileWriter(configFile.toFile())) {
                        yaml.dump(Map.of(), fw);
                    }
                }
            }
            try (InputStream is = new FileInputStream(configFile.toFile())) {
                data = yaml.load(is);
            }
            if (data == null) {
                data = new java.util.LinkedHashMap<>();
            }
        } catch (IOException e) {
            System.err.println("[LojaSquare] Erro ao carregar config " + configFile + ": " + e.getMessage());
            data = new java.util.LinkedHashMap<>();
        }
    }

    @SuppressWarnings("unchecked")
    private Object getNested(String path) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = data;
        for (int i = 0; i < parts.length - 1; i++) {
            Object val = current.get(parts[i]);
            if (!(val instanceof Map)) return null;
            current = (Map<String, Object>) val;
        }
        return current.get(parts[parts.length - 1]);
    }

    @SuppressWarnings("unchecked")
    private void setNested(String path, Object value) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = data;
        for (int i = 0; i < parts.length - 1; i++) {
            Object val = current.get(parts[i]);
            if (!(val instanceof Map)) {
                Map<String, Object> newMap = new java.util.LinkedHashMap<>();
                current.put(parts[i], newMap);
                current = newMap;
            } else {
                current = (Map<String, Object>) val;
            }
        }
        current.put(parts[parts.length - 1], value);
    }

    public String getString(String path) {
        Object val = getNested(path);
        return val != null ? val.toString() : null;
    }

    public String getString(String path, String defaultValue) {
        String val = getString(path);
        return val != null ? val : defaultValue;
    }

    public boolean getBoolean(String path) {
        return getBoolean(path, false);
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        Object val = getNested(path);
        if (val instanceof Boolean) return (Boolean) val;
        if (val instanceof String) return Boolean.parseBoolean((String) val);
        return defaultValue;
    }

    public int getInt(String path) {
        return getInt(path, 0);
    }

    public int getInt(String path, int defaultValue) {
        Object val = getNested(path);
        if (val instanceof Number) return ((Number) val).intValue();
        return defaultValue;
    }

    public double getDouble(String path) {
        return getDouble(path, 0.0);
    }

    public double getDouble(String path, double defaultValue) {
        Object val = getNested(path);
        if (val instanceof Number) return ((Number) val).doubleValue();
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    public List<String> getStringList(String path) {
        Object val = getNested(path);
        if (val instanceof List) {
            List<String> result = new ArrayList<>();
            for (Object item : (List<Object>) val) {
                result.add(item.toString());
            }
            return result;
        }
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public Set<String> getKeys(String path) {
        Object val = getNested(path);
        if (val instanceof Map) {
            return ((Map<String, Object>) val).keySet();
        }
        return Set.of();
    }

    public void set(String path, Object value) {
        setNested(path, value);
        save();
    }

    public void save() {
        try (FileWriter fw = new FileWriter(configFile.toFile())) {
            yaml.dump(data, fw);
        } catch (IOException e) {
            System.err.println("[LojaSquare] Erro ao salvar config " + configFile + ": " + e.getMessage());
        }
    }

    public void reload() {
        load();
    }
}
