package Suppenknecht.SasaWeather.Weather;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WeatherEndEvent extends Event {


    private final WeatherType weather;

    public WeatherEndEvent(WeatherType weather) {
        this.weather = weather;
    }

    public WeatherType getWeather() {
        return weather;
    }


    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
