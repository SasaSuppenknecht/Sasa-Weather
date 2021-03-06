package Suppenknecht.SasaWeather.Weather.Types;

import Suppenknecht.SasaWeather.Weather.WeatherType;
import org.bukkit.plugin.Plugin;

import java.util.Random;

public class Rain extends WeatherType
{
    private int duration;
    
    @Override
    public void start() {
        OVERWORLD.setStorm(true);
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
        final int defaultMin = 90;
        final int defaultMax = 240;
        super.getDurationBounds(90, 240);
    }
    
    @Override
    protected void scheduleThreads() {
        SCHEDULER.scheduleSyncDelayedTask((Plugin)MAIN, (Runnable)new StopThread(), (long)duration);
    }
    
    @Override
    public String toString() {
        return "Rain";
    }
}
