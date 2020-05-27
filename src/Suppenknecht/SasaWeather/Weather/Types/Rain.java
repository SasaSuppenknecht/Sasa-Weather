package Suppenknecht.SasaWeather.Weather.Types;

import Suppenknecht.SasaWeather.Weather.WeatherType;

import java.util.Random;

public class Rain extends WeatherType {

    //--config stuff--
    private int minDuration;
    private int maxDuration;
    //---

    private int duration;

    public Rain() {
        super();
    }

    @Override
    public void start() {
        OVERWORLD.setStorm(true); //probably has to be done different
        duration = (new Random().nextInt(maxDuration - minDuration + 1) + minDuration) * 20;
        OVERWORLD.setWeatherDuration(duration);
        super.start();
    }

    @Override
    public void stop() {
        OVERWORLD.setStorm(false);
        super.stop();
    }

    @Override
    protected void getConfig() {
        minDuration = CONFIG.getInt("Windy.MinDuration", 90);
        if (minDuration < 30 || minDuration > 1200) minDuration = 90;
        maxDuration = CONFIG.getInt("Windy.MaxDuration", 240);
        if (maxDuration < 30 || maxDuration > 1200) maxDuration = 240;
    }

    @Override
    protected void scheduleThreads() {
        SCHEDULER.scheduleSyncDelayedTask(MAIN, new StopThread(), duration);
    }

    @Override
    public String toString() {
        return "Rain";
    }


}
