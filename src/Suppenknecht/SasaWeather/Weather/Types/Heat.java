// 
// Decompiled by Procyon v0.5.36
// 

package Suppenknecht.SasaWeather.Weather.Types;

import Suppenknecht.SasaWeather.Weather.WeatherType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class Heat extends WeatherType implements Listener
{
    //--config stuff---
    private boolean affectPlayer;
    private boolean enableRandomFire;
    private double fireChance;
    private boolean considerTemperatureForFire;
    private boolean enablePlantsDying;
    private double plantsDyingChance;
    //---

    private HashMap<Player, Boolean> playersOutside;
    private int threadID;
    private boolean enabledSprintChecker;

    private final Material[] plantsAndCrops = {Material.WHEAT_SEEDS, Material.DANDELION, Material.POTATO, Material.CARROT,
        Material.OXEYE_DAISY, Material.AZURE_BLUET, Material.CORNFLOWER, Material.ALLIUM, Material.BLUE_ORCHID,
        Material.ORANGE_TULIP, Material.MELON_STEM, Material.PUMPKIN_STEM, Material.MELON_SEEDS, Material.PUMPKIN_SEEDS,
        Material.PEONY, Material.PINK_TULIP, Material.POPPY, Material.RED_TULIP, Material.WHITE_TULIP, Material.ROSE_BUSH,
        Material.SWEET_BERRY_BUSH, Material.WHEAT, Material.BEETROOT, Material.BEETROOT_SEEDS };
    
    @Override
    public void start() {
        playersOutside = new HashMap<Player, Boolean>(MAIN.getServer().getMaxPlayers() * 2);
        for (final Player p : OVERWORLD.getPlayers()) {
            playersOutside.put(p, false);
        }
        if (affectPlayer) {
            enabledSprintChecker = true;
        }
        super.start();
    }
    
    @Override
    public void stop() {
        super.stop();
        enabledSprintChecker = false;
        SCHEDULER.cancelTask(threadID);
        for (final Player p : playersOutside.keySet()) {
            playersOutside.put(p, false);
        }
    }
    
    @Override
    protected void getConfig() {
        final int minDefault = 150, maxDefault = 300;
        super.getDurationBounds(minDefault, maxDefault);
        affectPlayer = CONFIG.getBoolean("Heat.AffectPlayers", true);
        enableRandomFire = CONFIG.getBoolean("Heat.RandomFire.EnableRandomFire", false);
        fireChance = CONFIG.getDouble("Heat.RandomFire.RandomFireChance", 0.05);
        considerTemperatureForFire = CONFIG.getBoolean("Heat.RandomFire.ConsiderBiomeTemperature", true);
        enablePlantsDying = CONFIG.getBoolean("Heat.DyingPlants.EnableDyingPlants", true);
        plantsDyingChance = CONFIG.getDouble("Heat.DyingPlants.DyingPlantsChance", 0.5);
    }
    
    @Override
    protected void scheduleThreads() {
        final Thread thread = new Thread() {
            @Override
            public void run() {
                for (final Player p : playersOutside.keySet()) {
                    final Location loc = p.getLocation();
                    if (OVERWORLD.getTime() > 6000L && OVERWORLD.getTime() < 18000L) {
                        if (loc.getY() <= 45.0) {
                            continue;
                        }
                        affectPlayer(p);
                        randomFire(p);
                        plantsDying(p);
                    }
                    else {
                        enabledSprintChecker = false;
                    }
                }
            }
        };
        threadID = SCHEDULER.scheduleSyncRepeatingTask(MAIN, thread, 0L, 30L);
        final int duration = (new Random().nextInt(maxDuration - minDuration + 1) + minDuration) * 20;
        SCHEDULER.scheduleSyncDelayedTask(MAIN, new StopThread(), duration);
    }
    
    private void affectPlayer(final Player p) {
        if (affectPlayer) {
            final Location loc = p.getLocation();
            final Block b = OVERWORLD.getHighestBlockAt(loc);
            if (b.getY() > loc.getY()) {
                final double temperature = loc.getBlock().getTemperature();
                playersOutside.put(p, false);
                if (temperature > 1.1) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 40, 0, false, false));
                }
                if (temperature > 1.75 && Math.random() < 0.1) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 70, 0, false, false));
                }
            }
            else {
                playersOutside.put(p, true);
            }
        }
    }
    
    private void randomFire(final Player p) {
        if (enableRandomFire) {
            final Random r = new Random();
            final Location loc = p.getLocation();
            final int dx = r.nextInt(50) * (r.nextBoolean() ? 1 : -1);
            final int dz = r.nextInt(50) * (r.nextBoolean() ? 1 : -1);
            loc.add(dx, 0.0, dz);
            final Block b = OVERWORLD.getHighestBlockAt(loc);
            double multiplier = 1.0;
            if (considerTemperatureForFire) {
                final double temperature = b.getTemperature();
                if (temperature > 1.1) {
                    multiplier = 2.0;
                }
                else if (temperature < 0.5) {
                    multiplier = 0.5;
                }
            }
            if (Math.random() < fireChance * multiplier) {
                OVERWORLD.getBlockAt(b.getX(), b.getY() + 1, b.getZ()).setType(Material.FIRE);
            }
        }
    }
    
    private void plantsDying(final Player p) {
        if (enablePlantsDying) {
            final Random r = new Random();
            final Location loc = p.getLocation();
            final int dx = r.nextInt(50) * (r.nextBoolean() ? 1 : -1);
            final int dz = r.nextInt(50) * (r.nextBoolean() ? 1 : -1);
            loc.add(dx, 0.0, dz);
            Block b = OVERWORLD.getHighestBlockAt(loc);
            if (Math.random() < plantsDyingChance) {
                b = OVERWORLD.getBlockAt(b.getX(), b.getY() + 1, b.getZ());
                if (Arrays.asList(plantsAndCrops).contains(b.getType())) {
                    b.breakNaturally();
                }
            }
        }
    }
    
    @Override
    public String toString() {
        return "Heat";
    }
    
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player p = event.getPlayer();
        if (p.getWorld() == OVERWORLD) {
            playersOutside.put(p, false);
        }
    }
    
    @EventHandler
    public void onPayerQuit(final PlayerQuitEvent event) {
        final Player p = event.getPlayer();
        if (p.getWorld() == OVERWORLD) {
            playersOutside.remove(p);
        }
    }
    
    @EventHandler
    public void onPlayerChangedWorld(final PlayerChangedWorldEvent event) {
        final Player p = event.getPlayer();
        if (p.getWorld() == OVERWORLD) {
            playersOutside.put(p, false);
        }
        else {
            playersOutside.remove(p);
        }
    }
    
    @EventHandler
    public void onPlayerSprinting(final PlayerToggleSprintEvent event) {
        if (enabledSprintChecker && affectPlayer && playersOutside.get(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}
