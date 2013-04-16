/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.nextbattle.quarry.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import net.nextbattle.quarry.entities.CommandHandler;
import net.nextbattle.quarry.entities.Configuration;
import net.nextbattle.quarry.entities.CustomItems;
import net.nextbattle.quarry.entities.Quarry;
import net.nextbattle.quarry.metrics.Metrics;
import net.nextbattle.quarry.metrics.Metrics.Graph;
import net.nextbattle.quarry.updater.Updater;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class MainClass extends JavaPlugin {

    public static Plugin plugin;
    public static Configuration config;
    public static CustomItems citems;
    public static int timer;
    public static CommandHandler ch;

    @Override
    public void onEnable() {
        //Initialize Custom Items
        citems = new CustomItems();
        citems.addRecipes();

        //Define plugin
        plugin = this;

        //Event Listener
        getServer().getPluginManager().registerEvents(new GeneralEventListener(), plugin);

        //Data Folders
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
        File quarrydir = new File(plugin.getDataFolder(), "/quarries");
        if (!quarrydir.exists()) {
            quarrydir.mkdir();
        }

        //Initialize Command Handler
        ch = new CommandHandler();
        getCommand("nextquarry").setExecutor(ch);

        //Config
        if (!(new File(plugin.getDataFolder(), "config.yml")).exists()) {
            plugin.saveDefaultConfig();
        }
        plugin.reloadConfig();
        Configuration.loadConfig();

        //Updater
        try {
            Updater updater = null;
            if (config.getAutoUpdate()) {
                updater = new Updater(this, "nextquarry", this.getFile(), Updater.UpdateType.DEFAULT, true); //TODO: NOTIFY UPDATE BUT NO UPDATE FEATURE
                Updater.updateResultCustom(updater);
            } else if (config.getUpdateNotify()) {
                updater = new Updater(this, "nextquarry", this.getFile(), Updater.UpdateType.NO_DOWNLOAD, true);
                Updater.updateResultCustom(updater);
            }
        } catch (Exception e) {
        }


        //Load Quarries
        File dir = new File(plugin.getDataFolder(), "/quarries");
        for (File child : dir.listFiles()) {
            Quarry.LoadQuarry(child);
        }

        //Plugin Metrics
        try {
            Metrics metrics = new Metrics(this);
            Metrics.initMetrics(metrics);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }

        //Main Timer
        timer = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for (Quarry q : Quarry.quarrylist) {
                    q.doTick();
                }
            }
        }, 0, 1L);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTask(timer);
        getServer().getLogger().log(Level.INFO, "NeXTQuarry: Saving quarries...");
        Quarry.saveAll();
        getServer().getLogger().log(Level.INFO, "NeXTQuarry: All quarries saved.");
    }
}