package Suppenknecht.SasaWeather;

import Suppenknecht.SasaWeather.Weather.Types.*;
import Suppenknecht.SasaWeather.Weather.WeatherEndEvent;
import Suppenknecht.SasaWeather.Weather.WeatherType;
import org.bukkit.GameRule;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;

public class WeatherHandler implements Listener {

    private final Main MAIN;

    //--config stuff--
    private FileConfiguration config;
    private int minTime;
    private int maxTime;
    private boolean random;
    private double weatherLikelihood;

    private boolean windyEnabled; //todo use this for a list of active weathers
    private boolean rainEnabled;
    //---

    private Hashtable<WeatherType, Integer> weathertypes = new Hashtable<>();
    private LinkedList<WeatherType> weatherDistribution = new LinkedList<>();
    private WeatherType currentWeather;
    private boolean interruptNext;


    WeatherHandler() {
        MAIN = Main.getMainInstance();
        config = MAIN.getConfig();
        loadConfig();
        fillWeatherDistributionTable();

        MAIN.getServer().getWorlds().get(0).setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        MAIN.getServer().getPluginManager().registerEvents(this, MAIN);
        MAIN.getServer().getPluginManager().callEvent(new WeatherEndEvent(null));
    }

    private void loadConfig() {
        minTime = config.getInt("General.Interval.MinTime", 180);
        if (minTime < 30 || minTime > 1800) minTime = 180;
        maxTime = config.getInt("General.Interval.MaxTime", 900);
        if (maxTime < 30 || maxTime > 1800) maxTime = 900;
        if (minTime > maxTime) {
            int temp = maxTime;
            maxTime = minTime;
            minTime = temp;
        }

        random = config.getBoolean("General.Interval.Random", true);
        weatherLikelihood = config.getDouble("General.WeatherLikelihood", 1.0);


        //enabling or disabling weathers
        windyEnabled = config.getBoolean("Windy.EnableWeather", true);
        setWeightInHashmap(windyEnabled, new Windy(), 1);
        rainEnabled = config.getBoolean("Rain.EnableWeather", true);
        setWeightInHashmap(rainEnabled, new Rain(), 2);
    }

    private void setWeightInHashmap(boolean enabled, WeatherType weatherType, int def) {
        if (enabled) {
            int weight = config.getInt( weatherType.toString() + ".Weight", def);
            if (weight < 1 || weight > 10) weight = def;
            weathertypes.put(weatherType, weight);
        }
    }

    private void fillWeatherDistributionTable() {
        Set<WeatherType> weathers = weathertypes.keySet();

        for (WeatherType w : weathers) {
            int weight = weathertypes.get(w);
            for (int i = 0; i < weight; i++) {
                weatherDistribution.add(w);
            }
        }
    }

    @EventHandler
    public synchronized void onWeatherEnd(WeatherEndEvent event) {

        if (interruptNext) {
            interruptNext = false;
            return;
        }

        long interval;
        Random r = new Random();
        if (random) {
            interval = (r.nextInt(maxTime - minTime + 1) + minTime) * 20 ;
        } else {
            int random1 = r.nextInt(maxTime - minTime + 1) + minTime;
            int random2 = r.nextInt(maxTime - minTime + 1) + minTime;
            int random3 = r.nextInt(maxTime - minTime + 1) + minTime;
            interval = (random1 + random2 + random3) / 3 * 20;
        }

        MAIN.getServer().getScheduler().scheduleSyncDelayedTask(MAIN, new Runnable() {
            @Override
            public void run() {
                double chance = Math.random();
                if (chance < weatherLikelihood) determineRandomWeather();
            }
        }, interval);
    }

    private void determineRandomWeather() {
        int randomIndex = new Random().nextInt(weatherDistribution.size());
        currentWeather = weatherDistribution.get(randomIndex);
        currentWeather.start();
    }

    public Set<WeatherType> getEnabledWeatherTypes() {
        return weathertypes.keySet(); //todo maybe this creates an error in executing the command
    }

    public synchronized void changeWeatherOnCommand(WeatherType weatherType) {
        interruptNext = true;
        currentWeather.stop();
        currentWeather = weatherType;
        currentWeather.start();
    }
}
