package Suppenknecht.SasaWeather;

import Suppenknecht.SasaWeather.Weather.WeatherType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

public class Commands implements CommandExecutor, TabCompleter {

    private final Main MAIN;
    private final WeatherHandler WEATHER_HANDLER;

    private LinkedList<String> weatherStrings = new LinkedList<>();
    private Set<WeatherType> weatherTypesSet;
    private Hashtable<WeatherType, Boolean> weatherEnabledMap;


    // todo TEST THIS

    Commands(WeatherHandler weatherHandler) {
        MAIN = Main.getMainInstance();
        WEATHER_HANDLER = weatherHandler;
        weatherEnabledMap = weatherHandler.getWeatherEnabledMap();
        weatherTypesSet = weatherEnabledMap.keySet();

        for (WeatherType w : weatherTypesSet) {
            if (weatherEnabledMap.get(w)) {
                weatherStrings.add(w.toString());
            }
        }
        weatherStrings.add("Clear");

        try {
            MAIN.getServer().getPluginCommand("sweather").setExecutor(this);
            MAIN.getServer().getPluginCommand("sweather").setTabCompleter(this);
        } catch (NumberFormatException e) {
            System.err.println("[Sasa Weather] Could not enable /sweather command");
        }
        try {
            MAIN.getServer().getPluginCommand("enabledweather").setExecutor(this);
            MAIN.getServer().getPluginCommand("sweather").setTabCompleter(this);
        } catch (NumberFormatException e) {
            System.err.println("[Sasa Weather] Could not enable /enabledweather command");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("sweather")) {
            if (args[0].equals("Clear")) {
                WEATHER_HANDLER.changeWeatherOnCommand(null);
                return true;
            }
            for (WeatherType w : weatherTypesSet) {
                if (w.toString().equals(args[0]) && weatherEnabledMap.get(w)) {
                    WEATHER_HANDLER.changeWeatherOnCommand(w);
                    return true;
                }
            }
            return false;
        } else if (command.getName().equals("enabledweather")) {
            for (WeatherType w : weatherTypesSet) {
                String status = weatherEnabledMap.get(w) ? "enabled" : "disabled";
                sender.sendMessage(w.toString() + ": " + status);
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        LinkedList<String> list;
        if (command.getName().equals("sweather") && args.length == 1) {
            list = weatherStrings;
        } else {
            list = new LinkedList<>();
        }
        return list;
    }
}
