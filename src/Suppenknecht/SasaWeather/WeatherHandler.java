package Suppenknecht.SasaWeather;

import Suppenknecht.SasaWeather.Weather.Types.*;
import Suppenknecht.SasaWeather.Weather.WeatherEndEvent;
import Suppenknecht.SasaWeather.Weather.WeatherType;
import org.bukkit.GameRule;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

public class WeatherHandler implements Listener
{
    private final Main MAIN;
    private FileConfiguration config;
    private int minTime;
    private int maxTime;
    private boolean random;
    private double weatherLikelihood;
    private boolean enableWeatherMessages;
    private Hashtable<WeatherType, Boolean> weatherEnabledMap;
    private Hashtable<WeatherType, Integer> weatherWeightMap;
    private LinkedList<WeatherType> weatherDistribution;
    private WeatherType currentWeather;
    private boolean interruptNext;
    
    WeatherHandler() {
        weatherEnabledMap = new Hashtable<>();
        weatherWeightMap = new Hashtable<>();
        weatherDistribution = new LinkedList<>();
        currentWeather = null;
        interruptNext = false;
        MAIN = Main.getMainInstance();
        config = MAIN.getConfig();
        loadConfig();
        fillWeatherDistributionTable();
        MAIN.getServer().getWorlds().get(0).setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        MAIN.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)MAIN);
        MAIN.getServer().getPluginManager().callEvent((Event)new WeatherEndEvent(null));
    }
    
    private void loadConfig() {
        minTime = config.getInt("General.Interval.MinTime", 180);
        if (minTime < 30 || minTime > 1800) {
            minTime = 180;
        }
        maxTime = config.getInt("General.Interval.MaxTime", 900);
        if (maxTime < 30 || maxTime > 1800) {
            maxTime = 900;
        }
        if (minTime > maxTime) {
            final int temp = maxTime;
            maxTime = minTime;
            minTime = temp;
        }
        random = config.getBoolean("General.Interval.Random", true);
        weatherLikelihood = config.getDouble("General.WeatherLikelihood", 1.0);
        enableWeatherMessages = config.getBoolean("General.WeatherMessages", false);

        //Hail
        final boolean hailEnabled = config.getBoolean("Hail.EnableWeather", false);
        setWeightInHashmap(hailEnabled, new Hail(), 2);
        //Heat
        final boolean heatAffectPlayers = config.getBoolean("Heat.AffectPlayers", true);
        final boolean heatRandomFire = config.getBoolean("Heat.RandomFire.EnableRandomFire", false);
        final boolean heatPlantsDying = config.getBoolean("Heat.DyingPlants.EnableDyingPlants", true);
        final boolean heatEnabled = (heatAffectPlayers || heatRandomFire || heatPlantsDying) &&
                config.getBoolean("Heat.EnableWeather", true);
        setWeightInHashmap(heatEnabled, new Heat(), 1);
        //Rain
        final boolean rainEnabled = config.getBoolean("Rain.EnableWeather", true);
        setWeightInHashmap(rainEnabled, new Rain(), 3);
        //Snow
        final boolean snowEnabled = config.getBoolean("Snow.EnableWeather", true);
        setWeightInHashmap(snowEnabled, new Snow(), 2);
        //Thunder
        final boolean thunderEnabled = config.getBoolean("Thunder.EnableWeather", true);
        setWeightInHashmap(thunderEnabled, new Thunder(), 2);
        //Windy
        final boolean windyAffectPlayers = config.getBoolean("Windy.AffectPlayers", true);
        final boolean windyEnableParticles = config.getBoolean("Windy.EnableParticles", true);
        final boolean windyEnabled = (windyAffectPlayers || windyEnableParticles) &&
                config.getBoolean("Windy.EnableWeather", true);
        setWeightInHashmap(windyEnabled, new Windy(), 2);
    }
    
    private void setWeightInHashmap(final boolean enabled, final WeatherType weatherType, final int def) {
        if (enabled) {
            weatherEnabledMap.put(weatherType, true);
            int weight = config.getInt(weatherType.toString() + ".Weight", def);
            if (weight < 1 || weight > 10) {
                weight = def;
            }
            weatherWeightMap.put(weatherType, weight);
        }
        else {
            weatherEnabledMap.put(weatherType, false);
        }
    }
    
    private void fillWeatherDistributionTable() {
        final Set<WeatherType> weathers = weatherWeightMap.keySet();
        for (final WeatherType w : weathers) {
            for (int weight = weatherWeightMap.get(w), i = 0; i < weight; ++i) {
                weatherDistribution.add(w);
            }
        }
    }
    
    @EventHandler
    public synchronized void onWeatherEnd(final WeatherEndEvent event) {
        if (event.getWeather() != null) {
            weatherMessage(false, event.getWeather());
        }
        if (interruptNext) {
            interruptNext = false;
            return;
        }
        final Random r = new Random();
        long interval;
        if (random) {
            interval = (r.nextInt(maxTime - minTime + 1) + minTime) * 20;
        }
        else {
            final int random1 = r.nextInt(maxTime - minTime + 1) + minTime;
            final int random2 = r.nextInt(maxTime - minTime + 1) + minTime;
            final int random3 = r.nextInt(maxTime - minTime + 1) + minTime;
            interval = (random1 + random2 + random3) / 3 * 20;
        }
        MAIN.getServer().getScheduler().scheduleSyncDelayedTask(MAIN, new Runnable() {
            @Override
            public void run() {
                final double chance = Math.random();
                if (chance < weatherLikelihood) {
                    determineRandomWeather();
                }
                else {
                    MAIN.getServer().getPluginManager().callEvent(new WeatherEndEvent(null));
                }
            }
        }, interval);
    }
    
    private void determineRandomWeather() {
        final int randomIndex = new Random().nextInt(weatherDistribution.size());
        (currentWeather = weatherDistribution.get(randomIndex)).start();
        weatherMessage(true, currentWeather);
    }
    
    public Hashtable<WeatherType, Boolean> getWeatherEnabledMap() {
        return weatherEnabledMap;
    }
    
    public synchronized void changeWeatherOnCommand(final WeatherType weatherType) {
        if (currentWeather != null) {
            interruptNext = true;
            currentWeather.stop();
        }
        currentWeather = weatherType;
        if (currentWeather != null) {
            currentWeather.start();
            weatherMessage(true, currentWeather);
        }
    }
    
    private void weatherMessage(final boolean started, final WeatherType weatherType) {
        if (enableWeatherMessages) {
            final String word = started ? "started" : "stopped";
            MAIN.getServer().broadcastMessage("The " + weatherType.toString() + "-Weather has " + word + ".");
        }
    }
}
