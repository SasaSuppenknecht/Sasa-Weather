package Suppenknecht.SasaWeather.Weather.Types;

import Suppenknecht.SasaWeather.Weather.WeatherType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.LinkedList;
import java.util.Random;

public class Snow extends WeatherType implements Listener
{
    private LinkedList<Player> playersInOverworld;
    private int threadID;
    
    @Override
    public void start() {
        playersInOverworld = (LinkedList<Player>)OVERWORLD.getPlayers();
        super.start();
    }
    
    @Override
    public void stop() {
        super.stop();
        SCHEDULER.cancelTask(threadID);
    }
    
    @Override
    protected void getConfig() {
        final int minDefault = 150, maxDefault = 300;
        super.getDurationBounds(minDefault, maxDefault);
    }
    
    @Override
    protected void scheduleThreads() {
        final Thread thread = new Thread() {
            @Override
            public void run() {
                for (final Player p : playersInOverworld) {
                    spawnParticles(p);
                    createSnow(p);
                }
            }
        };
        threadID = SCHEDULER.scheduleSyncRepeatingTask(MAIN, thread, 0L, 4L);
        final int duration = (new Random().nextInt(maxDuration - minDuration + 1) + minDuration) * 20;
        SCHEDULER.scheduleSyncDelayedTask(MAIN, new StopThread(), duration);
    }
    
    private void spawnParticles(final Player p) {
        final Location loc = p.getLocation();
        final double temperature = loc.getBlock().getTemperature();
        if (temperature > 1.5) {
            return;
        }
        int particleCount;
        if (temperature > 1.0 && temperature <= 1.5) {
            particleCount = 20;
        }
        else if (temperature > 0.5 && temperature <= 1.0) {
            particleCount = 50;
        }
        else {
            particleCount = 75;
        }
        for (int i = 0; i < particleCount; ++i) {
            final Random r = new Random();
            final int dx = loc.getBlockX() + r.nextInt(30) * (r.nextBoolean() ? 1 : -1);
            final int dy = loc.getBlockY() + 10;
            final int dz = loc.getBlockZ() + r.nextInt(30) * (r.nextBoolean() ? 1 : -1);
            final Location spawnLoc = new Location(OVERWORLD, dx, dy, dz);
            p.spawnParticle(Particle.FIREWORKS_SPARK, spawnLoc, 0, 0.0, 0.2, 0.0);
        }
    }
    
    private void createSnow(final Player p) {
        final Random r = new Random();
        final Location loc = p.getLocation();
        final int dx = r.nextInt(30) * (r.nextBoolean() ? 1 : -1);
        final int dz = r.nextInt(30) * (r.nextBoolean() ? 1 : -1);
        loc.add(dx, 0.0, dz);
        final Block b = OVERWORLD.getHighestBlockAt(loc);
        final double temperature = b.getTemperature();
        if (temperature > 1.5) {
            return;
        }
        double snowChance;
        if (temperature > 1.0 && temperature <= 1.5) {
            snowChance = 0.01;
        }
        else if (temperature > 0.5 && temperature <= 1.0) {
            snowChance = 0.05;
        }
        else {
            snowChance = 0.1;
        }
        if (Math.random() < snowChance && loc.add(0.0, 1.0, 0.0).getBlock().getType() == Material.AIR) {
            OVERWORLD.getBlockAt(b.getX(), b.getY() + 1, b.getZ()).setType(Material.SNOW);
        }
    }
    
    @Override
    public String toString() {
        return "Snow";
    }
    
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player p = event.getPlayer();
        if (p.getWorld() == OVERWORLD) {
            playersInOverworld.add(p);
        }
    }
    
    @EventHandler
    public void onPayerQuit(final PlayerQuitEvent event) {
        final Player p = event.getPlayer();
        if (p.getWorld() == OVERWORLD) {
            playersInOverworld.remove(p);
        }
    }
    
    @EventHandler
    public void onPlayerChangedWorld(final PlayerChangedWorldEvent event) {
        final Player p = event.getPlayer();
        if (p.getWorld() == OVERWORLD) {
            playersInOverworld.add(p);
        }
        else {
            playersInOverworld.remove(p);
        }
    }
}
