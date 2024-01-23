package me.coca.swordsmp;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class command implements CommandExecutor {
    private Map<String, ItemStack> scythes = new HashMap<>();

    public command() {
        this.scythes.put("wither", CustomItems.createWither());
        this.scythes.put("void", CustomItems.createVoidSword());
        this.scythes.put("guardian", CustomItems.createGuardianSword());
        this.scythes.put("warden", CustomItems.createWarden());
    }


    

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, org.bukkit.command.@NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        int amount;
        if (!commandSender.isOp()) {
            commandSender.sendMessage(ChatColor.RED + "This command can only be executed by a operator.");
            return true;
        }
        Player player = (Player)commandSender;
        if (strings.length != 3) {
            player.sendMessage(ChatColor.RED + "Usage: /sword <player> <sword_name>(wither void guardian warden) <amount>");
            return true;
        }
        try {
            amount = Integer.parseInt(strings[2]);
            if (amount <= 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "The amount must be a positive integer.");
            return true;
        }
        String scytheName = strings[1].toLowerCase();
        if (!this.scythes.containsKey(scytheName)) {
            player.sendMessage(ChatColor.RED + "Unknown swordname name: " + scytheName);
            return true;
        }
        ItemStack scythe = ((ItemStack)this.scythes.get(scytheName)).clone();
        scythe.setAmount(amount);
        Player target = Bukkit.getPlayer(strings[0]);
        if (target != null) {
            HashMap<Integer, ItemStack> excessItems = target.getInventory().addItem(new ItemStack[] { scythe });
            if (!excessItems.isEmpty()) {
                for (ItemStack excessItem : excessItems.values())
                    target.getWorld().dropItemNaturally(target.getLocation(), excessItem);
                player.sendMessage(ChatColor.YELLOW + "Some items were dropped on the ground because the player's inventory was full.");
            }
            player.sendMessage(ChatColor.GREEN + "Successfully gave " + target.getName() + " " + amount + " " + scytheName + " swords.");
            target.sendMessage(ChatColor.GREEN + "You received " + amount + " " + scytheName + " swords from " + player.getName() + ".");
        } else {
            player.sendMessage(ChatColor.RED + "Player not found: " + strings[0]);
        }
        return false;
    }
}
