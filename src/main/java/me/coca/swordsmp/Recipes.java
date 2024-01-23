package me.coca.swordsmp;



import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.Plugin;

public class Recipes {
    public static ShapelessRecipe unbanBeaconRecipe(Plugin plugin) {
        NamespacedKey key = new NamespacedKey(plugin, "fortnitemaybeaso");
        ShapelessRecipe sr = new ShapelessRecipe(key, CustomItems.createWarden());
        sr.addIngredient(1, Material.NETHERITE_INGOT);
        sr.addIngredient(4, Material.ECHO_SHARD);
        sr.addIngredient(4, CustomItems.createWardenshard().getType());
        return sr;
    }
    public static ShapelessRecipe WitherCreate(Plugin plugin) {
        ItemStack item = CustomItems.createWither();
        NamespacedKey key = new NamespacedKey(plugin, "withersssssasd");
        ShapelessRecipe sr = new ShapelessRecipe(key, item);
        sr.addIngredient(4, Material.NETHERITE_SCRAP);
        sr.addIngredient(1, Material.NETHER_STAR);
        sr.addIngredient(4, Material.WITHER_SKELETON_SKULL);
        return sr;
    }

    public static ShapelessRecipe unbanBeaconRecipe2(Plugin plugin) {
        NamespacedKey key = new NamespacedKey(plugin, "asdascz");
        ShapelessRecipe sr = new ShapelessRecipe(key, CustomItems.createGuardianSword());
        sr.addIngredient(1, Material.PRISMARINE_SHARD);
        sr.addIngredient(4, Material.SPONGE);
        sr.addIngredient(4, CustomItems.creaeguardianshard().getType());
        return sr;
    }

    public static ShapelessRecipe unbanBeaconRecipe22(Plugin plugin) {
        NamespacedKey key = new NamespacedKey(plugin, "sqasd");
        ShapelessRecipe sr = new ShapelessRecipe(key, CustomItems.createVoidSword());
        sr.addIngredient(1, Material.DRAGON_EGG);
        return sr;
    }
}

