package Suppenknecht.SasaWeather;

import Suppenknecht.SasaWeather.Weather.WeatherEndEvent;
import Suppenknecht.SasaWeather.Weather.WeatherType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.LinkedList;
import java.util.Random;

public class WeatherHandler implements Listener {

    private final Main MAIN;


    //--config stuff--
    private int minTime;
    private int maxTime;
    private boolean random;
    private double weatherChancePerInterval;
    //---

    private LinkedList<WeatherType> weathertypes;
    private WeatherType currentWeather;
    private long weatherChancePeriod = 200;
    private boolean weatherRunning = false;


    WeatherHandler() {
        MAIN = Main.getMainInstance();

        setPossibleWeathers();

        MAIN.getServer().getPluginManager().registerEvents(this, MAIN);

        //in ticks; 20 ticks = 1 second

        MAIN.getServer().getScheduler().scheduleSyncRepeatingTask(MAIN, new Runnable() {
            @Override
            public void run() {
                if (!weatherRunning) {
                    double chance = Math.random();
                    System.out.println(chance);
                    if (chance < weatherChancePerInterval) determineRandomWeather();
                }

            }
        }, 0L, 20L);
        //doesn't repeat stuff
    }

    private void setPossibleWeathers() {

    }

    private void determineRandomWeather() {
        weatherRunning = true;
        int randomIndex = new Random().nextInt(weathertypes.size());
        currentWeather = weathertypes.get(randomIndex);
        currentWeather.start();
    }


    @EventHandler
    public void onWeatherEnd(WeatherEndEvent event) {
        weatherRunning = false;
        System.out.println("end event");
    }
}
