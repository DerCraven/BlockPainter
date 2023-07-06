package dercraven.blockpainter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.logging.Logger;

public final class BlockPainter extends JavaPlugin {

    private FileConfiguration config;

    @Override
    public void onEnable() {

        // Plugin startup logic
        createConfig();
        getLogger().info("Loading config...");

        getServer().getPluginManager().registerEvents(new BlockManager(), this); //this enables the block manager listener

        //commands go down there somewhere

    }
    private void createConfig() {
        if (getDataFolder().exists()) {
            getLogger().info("Config found!");
        }
        if (!getDataFolder().exists()) {
            getLogger().info("No config found! creating a new one...");
            getDataFolder().mkdirs();
        }
        config = getConfig();
        config.options().copyDefaults(true);
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
