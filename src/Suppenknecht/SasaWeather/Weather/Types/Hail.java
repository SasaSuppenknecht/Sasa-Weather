package Suppenknecht.SasaWeather.Weather.Types;

import Suppenknecht.SasaWeather.Weather.WeatherType;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class Hail extends WeatherType implements Listener
{
    private LinkedList<Player> playersInOverworld;
    private int duration;
    
    @Override
    public void start() {
        playersInOverworld = (LinkedList<Player>) OVERWORLD.getPlayers();
        OVERWORLD.setStorm(true);
        duration = (new Random().nextInt(maxDuration - minDuration + 1) + minDuration) * 20;
        OVERWORLD.setWeatherDuration(duration);
        super.start();
    }
    
    @Override
    public void stop() {
        super.stop();
    }
    
    @Override
    protected void getConfig() {
        final int defaultMin = 60, defaultMax = 120;
        super.getDurationBounds(defaultMin, defaultMax);
    }
    
    @Override
    protected void scheduleThreads() {
        final Thread thread = new Thread() {
            @Override
            public void run() {
                for (final Player p : playersInOverworld) {
                    hail(p);
                }
            }
        };
        SCHEDULER.scheduleSyncRepeatingTask(MAIN, thread, 0L, 2L);
        SCHEDULER.scheduleSyncDelayedTask(MAIN, new StopThread(), duration);
    }
    
    private void hail(final Player player) {
        final Random r = new Random();
        final Location loc = player.getLocation();
        final int dx = r.nextInt(11) * (r.nextBoolean() ? 1 : -1);
        final int dy = 6 + r.nextInt(5);
        final int dz = r.nextInt(11) * (r.nextBoolean() ? 1 : -1);
        loc.add(dx, dy, dz);
        OVERWORLD.spawnEntity(loc, EntityType.SNOWBALL);
    }
    
    @Override
    public String toString() {
        return "Hail";
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
