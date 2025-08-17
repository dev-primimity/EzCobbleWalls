package me.primimity.ezCobbleWalls;

import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        Cobblestone cobble = new Cobblestone(this);
        getServer().getPluginManager().registerEvents(cobble, this);
        getCommand("ezcobblestone").setExecutor(cobble);
        getCommand("ezcobblestone").setTabCompleter(cobble);

        Obsidian obby = new Obsidian(this);
        getServer().getPluginManager().registerEvents(obby, this);
        getCommand("ezobsidian").setExecutor(obby);
        getCommand("ezobsidian").setTabCompleter(obby);

        System.out.println("\n" +
                getDescription().getName() + " v" + getDescription().getVersion() + "\n" +
                "Created by " + getDescription().getAuthors() + "\n" +
                "Need a custom plugin? Discord me @primimity\n");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
