package me.coca.swordsmp;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class SwordSMP extends JavaPlugin implements Listener {
    private static SwordSMP instance;

    private File eggFile;

    private FileConfiguration eggConfig;

    private boolean eggFound;

    public void onLoad() {
        instance = this;
    }

    public static SwordSMP getInstance() {
        return instance;
    }
    

    public void onEnable() {
        Save();
        register();
        Bukkit.addRecipe(Recipes.unbanBeaconRecipe(this));
        Bukkit.addRecipe(Recipes.unbanBeaconRecipe2(this));
        Bukkit.addRecipe(Recipes.unbanBeaconRecipe22(this));
        Bukkit.addRecipe(Recipes.WitherCreate(this));
    }

    public void register() {
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new VoidListener(), this);
        getServer().getPluginManager().registerEvents(new Wither(), this);
        getServer().getPluginManager().registerEvents(new Guardian(), this);
        getServer().getPluginManager().registerEvents(new Warden(), this);
        getCommand("sword").setExecutor(new command());
    }

    public void Save() {
        this.eggFile = new File(getDataFolder(), "egg.yml");
        if (!this.eggFile.exists())
            saveResource("egg.yml", false);
        this.eggConfig = (FileConfiguration)YamlConfiguration.loadConfiguration(this.eggFile);
        this.eggFound = this.eggConfig.getBoolean("found", false);
    }







    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();
        ItemStack sword = CustomItems.createVoidSword();
        if (item.getItemStack().getType() == Material.DRAGON_EGG)
            if (!this.eggFound) {
                this.eggFound = true;
                this.eggConfig.set("found", Boolean.valueOf(this.eggFound));
                saveEggConfig();
            } else {
                player.sendMessage("The dragon egg has already been found.");
            }
    }

    private void saveEggConfig() {
        try {
            this.eggConfig.save(this.eggFile);
        } catch (IOException e) {
            getLogger().warning("Failed to save egg config file: " + e.getMessage());
        }
    }
}
