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

    protected abstract void scheduleThreads();

    public abstract String toString();

    public class StopThread extends Thread{
        @Override
        public void run() {
            WeatherType.this.stop();
        }
    }
}
