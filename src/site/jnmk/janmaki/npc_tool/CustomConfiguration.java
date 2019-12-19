package site.jnmk.janmaki.npc_tool;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class CustomConfiguration{
    private FileConfiguration config = null;
    private final File file;
    private final String fileName;
    private final Plugin plugin;

    public CustomConfiguration(Plugin plugin) {
        this(plugin, "config.yml");
    }

    CustomConfiguration(Plugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        file = new File(plugin.getDataFolder(), fileName);
    }

    void saveDefaultConfig() {
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
    }

    FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }

    void saveConfig() {
        if (config == null) {
            return;
        }
        try {
            getConfig().save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + file, e);
        }
    }

    void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(file);
        final InputStream defConfigStream = plugin.getResource(fileName);
        if (defConfigStream == null) {
            return;
        }
        config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8)));
    }
}