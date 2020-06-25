package Suppenknecht.SasaWeather.Weather;

import Suppenknecht.SasaWeather.Main;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitScheduler;

public abstract class WeatherType implements Listener {
    protected final Main MAIN;
    protected final World OVERWORLD;
    protected final BukkitScheduler SCHEDULER;
    protected final FileConfiguration CONFIG;
    protected int minDuration;
    protected int maxDuration;

    public WeatherType() {
        MAIN = Main.getMainInstance();
        OVERWORLD = MAIN.getServer().getWorlds().get(0);
        SCHEDULER = MAIN.getServer().getScheduler();
        CONFIG = MAIN.getConfig();
        getConfig();
    }

    public void start() {
        scheduleThreads();
        MAIN.getServer().getPluginManager().registerEvents(this, MAIN);
    }

    public void stop() {
        HandlerList.unregisterAll(this);
        MAIN.getServer().getPluginManager().callEvent(new WeatherEndEvent(this));
    }

    protected abstract void getConfig();

    protected void getDurationBounds(final int defaultMin, final int defaultMax) {
        final String name = toString();
        minDuration = CONFIG.getInt(name + ".MinDuration", defaultMin);
        if (minDuration < 30 || minDuration > 1200) {
            minDuration = defaultMin;
        }
        maxDuration = CONFIG.getInt(name + ".MaxDuration", defaultMax);
        if (maxDuration < 30 || maxDuration > 1200) {
            maxDuration = defaultMax;
        }
    }

    protected abstract void scheduleThreads();

    @Override
    public abstract String toString();

    public class StopThread extends Thread {
        @Override
        public void run() {
            WeatherType.this.stop();
        }
    }
}
