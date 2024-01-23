package me.coca.swordsmp;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Warden implements Listener {
    private final Map<Player, ModeType> playerModes = new HashMap<>();

    private static final int COOLDOWN_BIG_SECONDS = 300;

    private Map<UUID, Long> cooldowns_BIG = new HashMap<>();

    private Map<UUID, BukkitRunnable> cooldownTasks_big = new HashMap<>();

    private static final int COOLDOWN_SECONDS_WAVE = 60;

    private Map<UUID, Long> cooldowns_WAVE = new HashMap<>();

    private Map<UUID, BukkitRunnable> cooldownTasks_WAVE = new HashMap<>();

    private ItemStack previousItem;

    private enum ModeType {
        SHRIEK, WAVE;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.NETHERITE_SWORD && item.getItemMeta().hasDisplayName() && item.isSimilar(CustomItems.createWarden())) {
            if (!playerModes.containsKey(player))
                playerModes.put(player, ModeType.SHRIEK);
            ModeType currentMode = playerModes.get(player);
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
                if (event.getPlayer().isSneaking()) {
                    if (currentMode == ModeType.SHRIEK) {
                        playerModes.put(player, ModeType.WAVE);
                    } else if (currentMode == ModeType.WAVE) {
                        playerModes.put(player, ModeType.SHRIEK);
                    }
                } else if (currentMode == ModeType.SHRIEK) {
                    if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                            player instanceof Player &&
                            !isBigOnCooldown(player)) {
                        startBigCooldown(player);
                        sendParticleLineAndDamage(player, 20.0D);
                    }
                } else if (currentMode == ModeType.WAVE && (
                        event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                        !isWAVEOnCooldown(player)) {
                    startWAVECooldown(player);
                    createDragonBreath(player);
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
        task.runTaskLater((Plugin)SwordSMP.getInstance(), 6000L);
        cooldownTasks_big.put(player.getUniqueId(), task);
    }

    private boolean isWAVEOnCooldown(Player player) {
        return (cooldowns_WAVE.containsKey(player.getUniqueId()) && ((Long)cooldowns_WAVE.get(player.getUniqueId())).longValue() > System.currentTimeMillis());
    }

    private long getWAVERemainingCooldownSeconds(Player player) {
        if (isWAVEOnCooldown(player))
            return (((Long)cooldowns_WAVE.get(player.getUniqueId())).longValue() - System.currentTimeMillis()) / 1000L + 1L;
        return 0L;
    }

    private void startWAVECooldown(final Player player) {
        cooldowns_WAVE.put(player.getUniqueId(), Long.valueOf(System.currentTimeMillis() + 60000L));
        BukkitRunnable task = new BukkitRunnable() {
            public void run() {
                cooldowns_WAVE.remove(player.getUniqueId());
                cooldownTasks_WAVE.remove(player.getUniqueId());
            }
        };
        task.runTaskLater((Plugin)SwordSMP.getInstance(), 1200L);
        cooldownTasks_WAVE.put(player.getUniqueId(), task);
    }

    private void clearActionBar(Player player) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(""));
    }

    private void updateActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    @EventHandler
    public void dea(EntityDeathEvent e) {
        if (e.getEntity() instanceof org.bukkit.entity.Warden) {
            ItemStack shard = CustomItems.createWardenshard();
            e.getDrops().add(shard);
        }
    }





    private void applyEffects(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 2147483647, 0));
    }

    private Map<UUID, Integer> playerTasks = new HashMap<>();

    private void createDragonBreath(final Player player) {
        World world = player.getWorld();
        final int durationTicks = 300;
        final AreaEffectCloud cloud = (AreaEffectCloud)world.spawnEntity(player.getLocation(), EntityType.AREA_EFFECT_CLOUD);
        cloud.setRadius(2.0F);
        cloud.setDuration(durationTicks);
        cloud.setParticle(Particle.DRAGON_BREATH);
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        int delay = 0;
        int taskId = scheduler.scheduleSyncRepeatingTask(SwordSMP.getInstance(), new Runnable() {
            int ticksInBreath = 0;

            public void run() {
                Location loc = player.getLocation();
                ticksInBreath++;
                double damageMultiplier = Math.min(ticksInBreath / durationTicks, 1.0D);
                double damage = damageMultiplier * 3.0D;
                for (Player p : loc.getWorld().getPlayers()) {
                    if (p.getLocation().distanceSquared(loc) <= 4.0D &&
                            p != player)
                        p.damage(damage, null);
                }
                cloud.teleport(loc);
                if (ticksInBreath >= durationTicks) {
                    cloud.remove();
                    Bukkit.getScheduler().cancelTask(playerTasks.get(cloud.getUniqueId()).intValue());
                    playerTasks.remove(cloud.getUniqueId());
                }
            }
        }, delay, 1L);
        playerTasks.put(cloud.getUniqueId(), taskId);
    }

    private static final String DARK_SCYTHE_DISPLAY_NAME = "§5§lWarden Sword";
    private HashMap<Player, Boolean> cooldownStatus = new HashMap();

    @EventHandler
    public void switchy(PlayerItemHeldEvent e) {
        final Player player = e.getPlayer();
        ItemStack item = player.getInventory().getItem(e.getNewSlot());
        if (item != null && item.getItemMeta().getDisplayName().equals(DARK_SCYTHE_DISPLAY_NAME)) {
            cooldownStatus.put(player, true);
            BukkitRunnable updateTask = new BukkitRunnable() {
                public void run() {
                    long remainingSecondswave = getWAVERemainingCooldownSeconds(e.getPlayer());
                    long remainingSeconds = getBigRemainingCooldownSeconds(e.getPlayer());
                    ModeType currentMode = playerModes.get(e.getPlayer());
                    String message;
                    if (currentMode == ModeType.WAVE) {
                        message = "§5§lWarden Sword §r§a| §fMode: §eShockwave §f| §cCooldown Shockwave §e" + formatTime(remainingSecondswave);
                        updateActionBar(e.getPlayer(), message);
                    } else if (currentMode == ModeType.SHRIEK) {
                        message = "§5§lWarden Sword §r§a| §fMode: §eShriek §f| §cCooldown Shirek §e" + formatTime(remainingSeconds);
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

    public void sendParticleLineAndDamage(Player player, double damageAmount) {
        World world = player.getWorld();
        Location start = player.getEyeLocation();
        Vector direction = start.getDirection().normalize();
        double distance = 20.0D;
        double i;
        for (i = 0.0D; i < distance; i += 0.1D) {
            Location currentLocation = start.clone().add(direction.clone().multiply(i));
            world.spawnParticle(Particle.SONIC_BOOM, currentLocation, 1, 0.0D, 0.0D, 0.0D);
            List<Entity> entities = (List<Entity>)world.getNearbyEntities(currentLocation, 1.0D, 1.0D, 1.0D);
            for (Entity entity : entities) {
                if (entity instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity)entity;
                    if (livingEntity != player)
                        livingEntity.damage(damageAmount);
                }
            }
        }
    }

    private void removeEffects(Player player) {
        player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
    }
}
