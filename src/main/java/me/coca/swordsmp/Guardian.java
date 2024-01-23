package me.coca.swordsmp;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Guardian implements Listener {
    private final Map<Player, ModeType> playerModes = new HashMap<>();

    private Map<UUID, Long> cooldowns_BIG = new HashMap<>();

    private Map<UUID, BukkitRunnable> cooldownTasks_big = new HashMap<>();

    private boolean cooldownOn = false;

    private static final int COOLDOWN_SECONDS = 500;

    private enum ModeType {
        TORNADO;
    }

    public void createTornado(final Player player, int duration, final double range, final double pullForce, final int damageTicks, final int damageAmount) {
        final Location playerLoc = player.getEyeLocation();
        player.getWorld().spawnParticle(Particle.SMOKE_LARGE, playerLoc, 100, 0.0D, 0.0D, 0.0D, 0.5D);
        final BukkitTask pullTask = Bukkit.getScheduler().runTaskTimer((Plugin)SwordSMP.getInstance(), new Runnable() {
            double t = 0.0D;

            public void run() {
                this.t += 0.05D;
                for (Entity entity : playerLoc.getWorld().getEntities()) {
                    if (entity.getLocation().distance(playerLoc) < range && entity != player) {
                        Vector direction = entity.getLocation().subtract(playerLoc).toVector().normalize();
                        double distance = entity.getLocation().distance(playerLoc);
                        double strength = pullForce / distance * this.t;
                        entity.setVelocity(direction.multiply(strength));
                        if (this.t % damageTicks == 0.0D &&
                                entity instanceof Player)
                            ((Player)entity).damage(damageAmount);
                    }
                }
            }
        },  0L, 1L);
        Bukkit.getScheduler().runTaskLater((Plugin)SwordSMP.getInstance(), new Runnable() {
            public void run() {
                pullTask.cancel();
            }
        },  duration * 20L);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.NETHERITE_SWORD && item.getItemMeta().hasDisplayName() && item.isSimilar(CustomItems.createGuardianSword())) {
            if (!this.playerModes.containsKey(player))
                this.playerModes.put(player, ModeType.TORNADO);
            ModeType currentMode = this.playerModes.get(player);
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
                if (event.getPlayer().isSneaking()) {
                    if (currentMode == ModeType.TORNADO)
                        this.playerModes.put(player, ModeType.TORNADO);
                } else if (currentMode == ModeType.TORNADO && (
                        event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                        !isBigOnCooldown(player)) {
                    startBigCooldown(player);
                    createTornado(player, 15, 10.0D, 7.0D, 10, 10);
                }
        }
    }

    private boolean isBigOnCooldown(Player player) {
        return (this.cooldowns_BIG.containsKey(player.getUniqueId()) && ((Long)this.cooldowns_BIG.get(player.getUniqueId())).longValue() > System.currentTimeMillis());
    }

    private void startBigCooldown(final Player player) {
        final UUID playerId = player.getUniqueId();
        if (this.cooldowns_BIG.containsKey(playerId))
            return;
        long cooldownEndTime = System.currentTimeMillis() + 500000L;
        this.cooldowns_BIG.put(playerId, Long.valueOf(cooldownEndTime));
        BukkitRunnable cooldownTask = new BukkitRunnable() {
            public void run() {
                Guardian.this.cooldowns_BIG.remove(playerId);
                Guardian.this.cooldownTasks_big.remove(playerId);
            }
        };
        cooldownTask.runTaskLater((Plugin)SwordSMP.getInstance(), 10000L);
        this.cooldownTasks_big.put(playerId, cooldownTask);
        BukkitRunnable updateTask = new BukkitRunnable() {
            public void run() {
                if (Guardian.this.isBigOnCooldown(player));
            }
        };
        updateTask.runTaskTimer((Plugin)SwordSMP.getInstance(), 0L, 1L);
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
                    long remainingSeconds = Guardian.this.getRemainingCooldownSeconds(e.getPlayer());
                    String message = "§6§lGuardian Sword §r§a| §fMode: §eInvincible §f| §cCooldown: §e" + formatTime(remainingSeconds);
                    updateActionBar(e.getPlayer(), message);
                }
            };
            updateTask.runTaskTimer(SwordSMP.getInstance(), 0L, 1L);
        } else {
            cooldownStatus.remove(player);
            clearActionBar(player);
        }
    }

    private void clearActionBar(Player player) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(""));
    }

    private long getRemainingCooldownSeconds(Player player) {
        UUID playerId = player.getUniqueId();
        if (this.cooldowns_BIG.containsKey(playerId)) {
            long cooldownEndTime = ((Long)this.cooldowns_BIG.get(playerId)).longValue();
            long currentTime = System.currentTimeMillis();
            long remainingMilliseconds = cooldownEndTime - currentTime;
            return Math.max(0L, remainingMilliseconds / 1000L + 1L);
        }
        return 0L;
    }

    private String formatTime(long seconds) {
        if (seconds == 0) {
            return "§2§lREADY";
        }

        long minutes = seconds / 60L;
        long remainingSeconds = seconds % 60L;
        return String.format("%02d:%02d", Long.valueOf(minutes), Long.valueOf(remainingSeconds));
    }


    private void updateActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }


    @EventHandler
    public void dea(EntityDeathEvent e) {
        if (e.getEntity() instanceof org.bukkit.entity.ElderGuardian) {
            ItemStack shard = CustomItems.creaeguardianshard();
            e.getDrops().add(shard);
        }
    }
}
