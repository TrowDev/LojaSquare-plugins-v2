package br.com.lojasquare.qrcode.utils.bukkit;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class ConfigManager extends YamlConfiguration {

	private File bruteFile;
	private Plugin plugin;

	public ConfigManager(String name, Plugin plugin) {
		if (plugin == null) {
			Bukkit.broadcastMessage("§4[LSQrCode] §cConfigManager > Plugin null");
			return;
		}
		this.plugin = plugin;
		name = name.matches(".*(?i).yml$") ? name : name.concat(".yml");
		bruteFile = new File(plugin.getDataFolder(), name);

		try {
			if (!plugin.getDataFolder().exists()) {
				plugin.getDataFolder().mkdir();
			}
			if (!bruteFile.exists()) {
				bruteFile.createNewFile();
			}
			load(bruteFile);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}

	}

	public void saveDefault() {
		if (plugin.getResource(bruteFile.getName()) == null) {
			System.err.println("[" + plugin.getName()+ "] Nao foi possivel salvar o arquivo");
			System.err.println("[" + plugin.getName() + "] default da config " + bruteFile.getName() + " pois o jar nao");
			System.err.println("[" + plugin.getName()+ "] contem um arquivo com teste nome.");
		} else {
			if (!bruteFile.exists() || bruteFile.length() == 0) {
				plugin.saveResource(bruteFile.getName(), true);
			}
		}
	}

	@Override
	public void set(String path, Object obj) {
		super.set(path, obj);
		this.save();
	}

	public void save() {
		try {
			super.save(bruteFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void reload() {
		try {
			load(bruteFile);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

}