package Suppenknecht.SasaWeather;

import Suppenknecht.SasaWeather.Weather.WeatherType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Commands implements CommandExecutor, TabCompleter
{
    private final Main MAIN;
    private final WeatherHandler WEATHER_HANDLER;
    private LinkedList<String> weatherStrings;
    private Set<WeatherType> weatherTypesSet;
    private Hashtable<WeatherType, Boolean> weatherEnabledMap;
    
    Commands(final WeatherHandler weatherHandler) {
        weatherStrings = new LinkedList<>();
        MAIN = Main.getMainInstance();
        WEATHER_HANDLER = weatherHandler;
        weatherEnabledMap = weatherHandler.getWeatherEnabledMap();
        weatherTypesSet = weatherEnabledMap.keySet();
        for (final WeatherType w : weatherTypesSet) {
            if (weatherEnabledMap.get(w)) {
                weatherStrings.add(w.toString());
            }
        }
        weatherStrings.add("Clear");
        try {
            MAIN.getServer().getPluginCommand("sweather").setExecutor(this);
            MAIN.getServer().getPluginCommand("sweather").setTabCompleter(this);
        }
        catch (NumberFormatException e) {
            System.err.println("[Sasa Weather] Could not enable /sweather command");
        }
        try {
            MAIN.getServer().getPluginCommand("enabledweather").setExecutor(this);
            MAIN.getServer().getPluginCommand("sweather").setTabCompleter(this);
        }
        catch (NumberFormatException e) {
            System.err.println("[Sasa Weather] Could not enable /enabledweather command");
        }
    }
    
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!command.getName().equals("sweather")) {
            if (command.getName().equals("enabledweather")) {
                for (final WeatherType w : weatherTypesSet) {
                    final String status = weatherEnabledMap.get(w) ? "enabled" : "disabled";
                    sender.sendMessage(w.toString() + ": " + status);
                }
            }
            return true;
        }
        if (args[0].equals("Clear")) {
            WEATHER_HANDLER.changeWeatherOnCommand(null);
            return true;
        }
        for (final WeatherType w : weatherTypesSet) {
            if (w.toString().equals(args[0]) && weatherEnabledMap.get(w)) {
                WEATHER_HANDLER.changeWeatherOnCommand(w);
                return true;
            }
        }
        return false;
    }
    
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        LinkedList<String> list;
        if (command.getName().equals("sweather") && args.length == 1) {
            list = weatherStrings;
        }
        else {
            list = new LinkedList<>();
        }
        return list;
    }
}
