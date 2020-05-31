package Suppenknecht.SasaWeather.Weather;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class WeatherEndEvent extends Event
{
    private final WeatherType WEATHER;
    private static final HandlerList HANDLERS = new HandlerList();
    
    public WeatherEndEvent(final WeatherType weather) {
        WEATHER = weather;
    }
    
    public WeatherType getWeather() {
        return WEATHER;
    }
    
    public HandlerList getHandlers() {
        return WeatherEndEvent.HANDLERS;
    }
    
    public static HandlerList getHandlerList() {
        return WeatherEndEvent.HANDLERS;
    }
}
