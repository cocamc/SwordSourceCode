package me.coca.swordsmp;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VoidListener implements Listener {

    private static final int COOLDOWN_BIG_SECONDS = 60;

    private Map<UUID, Long> cooldowns_BIG = new HashMap<>();

    private Map<UUID, BukkitRunnable> cooldownTasks_big = new HashMap<>();

    private static final int COOLDOWN_SECONDS_void = 600;

    private Map<UUID, Long> cooldowns_void = new HashMap<>();

    private Map<UUID, BukkitRunnable> cooldownTasks_void = new HashMap<>();






    private static final String DARK_SCYTHE_DISPLAY_NAME = "§8§lVoid Sword";
    private HashMap<Player, Boolean> cooldownStatus = new HashMap();

    @EventHandler
    public void switchy(PlayerItemHeldEvent e) {
        final Player player = e.getPlayer();
        ItemStack item = player.getInventory().getItem(e.getNewSlot());
        if (item != null && item.getItemMeta().getDisplayName().equals(DARK_SCYTHE_DISPLAY_NAME)) {
            cooldownStatus.put(player, true);
            BukkitRunnable updateTask = new BukkitRunnable() {
                public void run() {
                    long remainingSeconds = getBigRemainingCooldownSeconds(e.getPlayer());
                    String message;
                    message = "§8§lVoid Sword §r§a| §fMode: §eDragons Breath §f| §cCooldown Dragons Breath §e" + VoidListener.this.formatTime(remainingSeconds);
                    VoidListener.updateActionBar(e.getPlayer(), message);
                }
            };
            updateTask.runTaskTimer(SwordSMP.getInstance(), 0L, 1L);
        } else {
            cooldownStatus.remove(player);
            clearActionBar(player);
        }
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
                this.ticksInBreath++;
                double damageMultiplier = Math.min(this.ticksInBreath / durationTicks, 1.0D);
                double damage = damageMultiplier * 25.0D;
                for (Player p : loc.getWorld().getPlayers()) {
                    if (p.getLocation().distanceSquared(loc) <= 4.0D &&
                            p != player)
                        p.damage(damage, null);
                }
                cloud.teleport(loc);
                if (this.ticksInBreath >= durationTicks) {
                    cloud.remove();
                    Bukkit.getScheduler().cancelTask(((Integer) VoidListener.this.playerTasks.get(cloud.getUniqueId())).intValue());
                    VoidListener.this.playerTasks.remove(cloud.getUniqueId());
                }
            }
        },delay, 1L);
        this.playerTasks.put(cloud.getUniqueId(), Integer.valueOf(taskId));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.NETHERITE_SWORD && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals("§8§lVoid Sword")) {


            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
                if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK){
                    if(isBigOnCooldown(player)) {
                        return;
                    }
                    startBigCooldown(player);
                    Location loc = player.getEyeLocation();
                    Vector dir = loc.getDirection().normalize();
                    loc.add(dir.getX(), dir.getY(), dir.getZ());
                    createDragonBreath(player);
                }
        }
    }

    private void createDragonBreathEffect(Location location) {
        final World world = location.getWorld();
        final int durationTicks = 300;
        final AreaEffectCloud cloud = (AreaEffectCloud)world.spawnEntity(location, EntityType.AREA_EFFECT_CLOUD);
        cloud.setRadius(3.0F);
        cloud.setDuration(durationTicks);
        cloud.setParticle(Particle.SMOKE_LARGE);
        cloud.setColor(Color.fromRGB(50, 50, 50));
        cloud.setVelocity(new Vector(0, 0, 0));
        BukkitRunnable task = new BukkitRunnable() {
            int ticksInBreath = 0;

            public void run() {
                this.ticksInBreath++;
                if (this.ticksInBreath % 5 == 0) {
                    Location loc = cloud.getLocation();
                    for (Player p : world.getPlayers()) {
                        if (p.getLocation().distanceSquared(loc) <= 10.0D)
                            p.teleport(new Location(world, p.getLocation().getX(), -10000.0D, p.getLocation().getZ()));
                    }
                }
                if (this.ticksInBreath >= durationTicks) {
                    cloud.remove();
                    cancel();
                }
            }
        };
        task.runTaskTimer(SwordSMP.getInstance(), 0L, 1L);
    }

    private void createDragonBreathFireball(Player player) {
        World world = player.getWorld();
        final int durationTicks = 300;
        Location loc = player.getEyeLocation();
        Vector direction = loc.getDirection().normalize();
        final Fireball fireball = (Fireball)world.spawn(loc, Fireball.class);
        fireball.setDirection(direction);
        fireball.setIsIncendiary(false);
        fireball.setYield(0.0F);
        BukkitRunnable task = new BukkitRunnable() {
            int ticksInBreath = 0;

            public void run() {
                this.ticksInBreath++;
                if (fireball.isDead() || fireball.isOnGround()) {
                    Location impactLoc = fireball.getLocation();
                    VoidListener.this.createDragonBreathEffect(impactLoc);
                    cancel();
                }
                if (this.ticksInBreath >= durationTicks)
                    cancel();
            }
        };
        task.runTaskTimer(SwordSMP.getInstance(), 0L, 1L);
    }

    private void createDragonBreath2(final Player player) {
        final World world = player.getWorld();
        final int durationTicks = 300;
        Location loc = player.getEyeLocation();
        Vector direction = loc.getDirection().normalize();
        final Fireball fireball = (Fireball)world.spawn(loc, Fireball.class);
        fireball.setDirection(direction);
        fireball.setIsIncendiary(false);
        final AreaEffectCloud[] cloud = { null };
        BukkitRunnable task = new BukkitRunnable() {
            int ticksInBreath = 0;

            public void run() {
                this.ticksInBreath++;
                if (cloud[0] == null && (fireball.isDead() || fireball.isOnGround())) {
                    Location impactLoc = fireball.getLocation();
                    cloud[0] = (AreaEffectCloud)world.spawnEntity(impactLoc, EntityType.AREA_EFFECT_CLOUD);
                    cloud[0].setRadius(2.0F);
                    cloud[0].setDuration(durationTicks);
                    cloud[0].setParticle(Particle.SMOKE_LARGE);
                    cloud[0].setColor(Color.fromRGB(50, 50, 50));
                    cloud[0].setVelocity(new Vector(0, 0, 0));
                }
                if (cloud[0] != null) {
                    Location loc = cloud[0].getLocation();
                    for (Player p : world.getPlayers()) {
                        if (p.getLocation().distanceSquared(loc) <= 4.0D &&
                                !p.equals(player))
                            p.teleport(new Location(world, p.getLocation().getX(), -10000.0D, p.getLocation().getZ()));
                    }
                }
                if (this.ticksInBreath >= durationTicks) {
                    if (cloud[0] != null)
                        cloud[0].remove();
                    cancel();
                }
            }
        };
        task.runTaskTimer(SwordSMP.getInstance(), 0L, 1L);
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
        return (this.cooldowns_BIG.containsKey(player.getUniqueId()) && ((Long)this.cooldowns_BIG.get(player.getUniqueId())).longValue() > System.currentTimeMillis());
    }

    private long getBigRemainingCooldownSeconds(Player player) {
        if (isBigOnCooldown(player))
            return (((Long)this.cooldowns_BIG.get(player.getUniqueId())).longValue() - System.currentTimeMillis()) / 1000L + 1L;
        return 0L;
    }

    private void startBigCooldown(final Player player) {
        this.cooldowns_BIG.put(player.getUniqueId(), Long.valueOf(System.currentTimeMillis() + 60000L));
        BukkitRunnable task = new BukkitRunnable() {
            public void run() {
                VoidListener.this.cooldowns_BIG.remove(player.getUniqueId());
                VoidListener.this.cooldownTasks_big.remove(player.getUniqueId());
            }
        };
        task.runTaskLater(SwordSMP.getInstance(), 1200L);
        this.cooldownTasks_big.put(player.getUniqueId(), task);
    }

    private boolean isVoidOnCooldown(Player player) {
        return (this.cooldowns_void.containsKey(player.getUniqueId()) && ((Long)this.cooldowns_void.get(player.getUniqueId())).longValue() > System.currentTimeMillis());
    }

    private long getVoidRemainingCooldownSeconds(Player player) {
        if (isVoidOnCooldown(player))
            return (((Long)this.cooldowns_void.get(player.getUniqueId())).longValue() - System.currentTimeMillis()) / 1000L + 1L;
        return 0L;
    }

    private void startVoidCooldown(final Player player) {
        this.cooldowns_void.put(player.getUniqueId(), Long.valueOf(System.currentTimeMillis() + 600000L));
        BukkitRunnable task = new BukkitRunnable() {
            public void run() {
                VoidListener.this.cooldowns_void.remove(player.getUniqueId());
                VoidListener.this.cooldownTasks_void.remove(player.getUniqueId());
            }
        };
        task.runTaskLater(SwordSMP.getInstance(), 12000L);
        this.cooldownTasks_void.put(player.getUniqueId(), task);
    }

    private void clearActionBar(Player player) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(""));
    }

    public static void updateActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }
}
