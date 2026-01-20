package com.hypixel.hytale.server.core.plugin;

import java.nio.file.Path;

import javax.annotation.Nonnull;

/**
 * Stub da classe JavaPlugin do Hytale Server API.
 * Esta classe será substituída pelo JAR real do servidor em runtime.
 */
public abstract class JavaPlugin {

    private final JavaPluginInit init;

    public JavaPlugin(@Nonnull JavaPluginInit init) {
        this.init = init;
    }

    protected void setup() {
        // Override in subclass
    }

    protected void start() {
        // Override in subclass
    }

    protected void shutdown() {
        // Override in subclass
    }

    public Path getPluginDataFolder() {
        return init.getDataFolder();
    }

    public CommandRegistry getCommandRegistry() {
        return new CommandRegistry();
    }
}
