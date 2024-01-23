package me.coca.swordsmp;
import java.util.Collections;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CustomItems {
    public static ItemStack createVoidSword() {
        ItemStack voidSword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = voidSword.getItemMeta();
        meta.setDisplayName("§8§lVoid Sword");
        meta.setLore(Collections.singletonList("§eThis sword embodies the ancient untapped power of the VOID"));
        meta.setUnbreakable(true);
        voidSword.setItemMeta(meta);
        voidSword.addEnchantment(Enchantment.DAMAGE_ALL, 5);
        voidSword.addEnchantment(Enchantment.DURABILITY, 3);
        voidSword.addEnchantment(Enchantment.MENDING, 1);
        return voidSword;
    }

    public static ItemStack unbanBeacon() {
        ItemStack itm = new ItemStack(Material.BEACON);
        ItemMeta met = itm.getItemMeta();
        met.setDisplayName(ChatColor.GOLD + "Unban Beacon");
        met.setLore(Collections.singletonList(ChatColor.BLUE + "Use me to unban somebody"));
        itm.setItemMeta(met);
        return itm;
    }

    public static ItemStack createGuardianSword() {
        ItemStack voidSword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = voidSword.getItemMeta();
        meta.setDisplayName("§2§lGuardian Sword");
        meta.setLore(Collections.singletonList("§eThe guardians ancient treasure, has the power to destory worlds"));
        meta.setUnbreakable(true);
        voidSword.setItemMeta(meta);
        voidSword.addEnchantment(Enchantment.DAMAGE_ALL, 5);
        voidSword.addEnchantment(Enchantment.DURABILITY, 3);
        voidSword.addEnchantment(Enchantment.MENDING, 1);
        return voidSword;
    }

    public static ItemStack createWither() {
        ItemStack voidSword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = voidSword.getItemMeta();
        meta.setDisplayName("§c§lWither Sword");
        meta.setLore(Collections.singletonList("§eThe withers lost sword, lost and shattered decades ago"));
        meta.setUnbreakable(true);
        voidSword.setItemMeta(meta);
        voidSword.addEnchantment(Enchantment.DAMAGE_ALL, 5);
        voidSword.addEnchantment(Enchantment.DURABILITY, 3);
        voidSword.addEnchantment(Enchantment.MENDING, 1);
        return voidSword;
    }

    public static ItemStack createWarden() {
        ItemStack voidSword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = voidSword.getItemMeta();
        meta.setDisplayName("§5§lWarden Sword");
        meta.setLore(Collections.singletonList("§eThe fragments of the warden come together to make this powerful ancient relic"));
        meta.setUnbreakable(true);
        voidSword.setItemMeta(meta);
        voidSword.addEnchantment(Enchantment.DAMAGE_ALL, 5);
        voidSword.addEnchantment(Enchantment.DURABILITY, 3);
        voidSword.addEnchantment(Enchantment.MENDING, 1);
        return voidSword;
    }

    public static ItemStack createWardenshard() {
        ItemStack shard = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta meta = shard.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Warden Shard");
        shard.setItemMeta(meta);
        return shard;
    }

    public static ItemStack creaeguardianshard() {
        ItemStack shard = new ItemStack(Material.COMMAND_BLOCK_MINECART);
        ItemMeta meta = shard.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Guardian Shard");
        shard.setItemMeta(meta);
        return shard;
    }
}