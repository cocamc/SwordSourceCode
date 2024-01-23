package me.coca.swordsmp;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Wither implements Listener {
    private final Map<Player, ModeType> playerModes = new HashMap<>();

    private static final int COOLDOWN_BIG_SECONDS = 300;

    private Map<UUID, Long> cooldowns_BIG = new HashMap<>();

    private Map<UUID, BukkitRunnable> cooldownTasks_big = new HashMap<>();

    private static final int COOLDOWN_SECONDS_FIREBALL = 60;

    private Map<UUID, Long> cooldowns_fireball = new HashMap<>();

    private Map<UUID, BukkitRunnable> cooldownTasks_fireball = new HashMap<>();

    private enum ModeType {
        BIG_FIREBALL, FIREBALL;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.NETHERITE_SWORD && item.getItemMeta().hasDisplayName() && item.isSimilar(CustomItems.createWither())) {
            if (!playerModes.containsKey(player))
                playerModes.put(player, ModeType.BIG_FIREBALL);
            ModeType currentMode = playerModes.get(player);
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
                if (event.getPlayer().isSneaking()) {
                    if (currentMode == ModeType.BIG_FIREBALL) {
                        playerModes.put(player, ModeType.FIREBALL);
                    } else if (currentMode == ModeType.FIREBALL) {
                        playerModes.put(player, ModeType.BIG_FIREBALL);
                    }
                } else if (currentMode == ModeType.BIG_FIREBALL) {
                    if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        if (player instanceof Player) {
                            if (!isBigOnCooldown(player)) {
                                startBigCooldown(player);
                                Location loc = player.getLocation();
                                loc.setY(loc.getY() + 1.5D);
                                Vector direction = loc.getDirection().multiply(2);
                                Fireball fireball = (Fireball)player.getWorld().spawn(loc, Fireball.class);
                                fireball.setDirection(direction);
                                fireball.setYield(5.0F);
                                fireball.setIsIncendiary(false);
                                for (Entity entity : fireball.getNearbyEntities(3.0D, 3.0D, 3.0D)) {
                                    if (entity instanceof Player &&
                                            entity != player) {
                                        Player target = (Player)entity;
                                        double damage = 3.0D;
                                        target.damage(damage);
                                    }
                                }
                            }
                        } else {
                            player.sendMessage("error");
                        }
                    } else {
                        clearActionBar(player);
                    }
                } else if (currentMode == ModeType.FIREBALL) {
                    if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        if (!isFireballOnCooldown(player)) {
                            startFireballCooldown(player);
                            Location loc = player.getLocation();
                            loc.setY(loc.getY() + 1.5D);
                            Vector direction = loc.getDirection().multiply(2);
                            Fireball fireball = (Fireball)player.getWorld().spawn(loc, Fireball.class);
                            fireball.setDirection(direction);
                            fireball.setYield(1.0F);
                            fireball.setIsIncendiary(false);
                            for (Entity entity : fireball.getNearbyEntities(3.0D, 3.0D, 3.0D)) {
                                if (entity instanceof Player &&
                                        entity != event.getPlayer()) {
                                    Player target = (Player)entity;
                                    double damage = 3.0D;
                                    target.damage(damage);
                                }
                            }
                        }
                    } else {
                        clearActionBar(player);
                    }
                }
        }
    }

    private String formatTime(long seconds) {
        if (seconds == 0) {
            return "§2§lREADY";
        }

        long minutes = seconds / 60L;
        long remainingSeconds = seconds % 60L;
        return String.format("%02d:%02d", Long.valueOf(minutes), Long.valueOf(remainingSeconds));
    }

    private boolean isBigOnCooldown(Player player) {
        return (cooldowns_BIG.containsKey(player.getUniqueId()) && ((Long)cooldowns_BIG.get(player.getUniqueId())).longValue() > System.currentTimeMillis());
    }

    private static final String DARK_SCYTHE_DISPLAY_NAME = "§c§lWither Sword";
    private HashMap<Player, Boolean> cooldownStatus = new HashMap();

    @EventHandler
    public void switchy(PlayerItemHeldEvent e) {
        final Player player = e.getPlayer();
        ItemStack item = player.getInventory().getItem(e.getNewSlot());
        if (item != null && item.getItemMeta().getDisplayName().equals(DARK_SCYTHE_DISPLAY_NAME)) {
            cooldownStatus.put(player, true);
            BukkitRunnable updateTask = new BukkitRunnable() {
                public void run() {
                    long remainingSecondswave = getFireballRemainingCooldownSeconds(e.getPlayer());
                    long remainingSeconds = getBigRemainingCooldownSeconds(e.getPlayer());
                    ModeType currentMode = playerModes.get(e.getPlayer());
                    String message;
                    if (currentMode == ModeType.BIG_FIREBALL) {
                        message = "§c§lWither Sword §r§a| §fMode: §eBig Fireball §f| §cCooldown Big Fireball §e" + formatTime(remainingSeconds);
                        updateActionBar(e.getPlayer(), message);
                    } else if (currentMode == ModeType.FIREBALL) {
                        message = "§c§lWither Sword §r§a| §fMode: §eFireball §f| §cCooldown Fireball §e" + formatTime(remainingSecondswave);
                        updateActionBar(e.getPlayer(), message);
                    }
                }
            };
            updateTask.runTaskTimer(SwordSMP.getInstance(), 0L, 1L);
        } else {
            cooldownStatus.remove(player);
            clearActionBar(player);
        }
    }
    private long getBigRemainingCooldownSeconds(Player player) {
        if (isBigOnCooldown(player))
            return (((Long)cooldowns_BIG.get(player.getUniqueId())).longValue() - System.currentTimeMillis()) / 1000L + 1L;
        return 0L;
    }

    private void startBigCooldown(final Player player) {
        cooldowns_BIG.put(player.getUniqueId(), Long.valueOf(System.currentTimeMillis() + 300000L));
        BukkitRunnable task = new BukkitRunnable() {
            public void run() {
                cooldowns_BIG.remove(player.getUniqueId());
                cooldownTasks_big.remove(player.getUniqueId());
            }
        };
        task.runTaskLater(SwordSMP.getInstance(), 6000L);
        cooldownTasks_big.put(player.getUniqueId(), task);
    }

    private boolean isFireballOnCooldown(Player player) {
        return (cooldowns_fireball.containsKey(player.getUniqueId()) && ((Long)cooldowns_fireball.get(player.getUniqueId())).longValue() > System.currentTimeMillis());
    }

    private long getFireballRemainingCooldownSeconds(Player player) {
        if (isFireballOnCooldown(player))
            return (((Long)cooldowns_fireball.get(player.getUniqueId())).longValue() - System.currentTimeMillis()) / 1000L + 1L;
        return 0L;
    }

    private void startFireballCooldown(final Player player) {
        cooldowns_fireball.put(player.getUniqueId(), Long.valueOf(System.currentTimeMillis() + 60000L));
        BukkitRunnable task = new BukkitRunnable() {
            public void run() {
                cooldowns_fireball.remove(player.getUniqueId());
                cooldownTasks_fireball.remove(player.getUniqueId());
            }
        };
        task.runTaskLater((Plugin)SwordSMP.getInstance(), 1200L);
        cooldownTasks_fireball.put(player.getUniqueId(), task);
    }

    private void clearActionBar(Player player) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(""));
    }

    private void updateActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }




}
