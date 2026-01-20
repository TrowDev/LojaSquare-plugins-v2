package com.hypixel.hytale.server.core.plugin;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Stub da classe JavaPluginInit do Hytale Server API.
 * Fornecida pelo servidor ao carregar o plugin.
 */
public class JavaPluginInit {

    private final String pluginName;

    public JavaPluginInit(String pluginName) {
        this.pluginName = pluginName;
    }

    public Path getDataFolder() {
        return Paths.get("plugins", pluginName);
    }

    public String getPluginName() {
        return pluginName;
    }
}
