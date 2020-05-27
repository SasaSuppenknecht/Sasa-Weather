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
    private LinkedList<WeatherType> weatherTypes = new LinkedList<>();


    Commands(WeatherHandler weatherHandler) {
        MAIN = Main.getMainInstance();
        WEATHER_HANDLER = weatherHandler;
        for (WeatherType w : weatherHandler.getEnabledWeatherTypes()) {
            weatherTypes.add(w);
            weatherStrings.add(w.toString());
        }

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
            for (WeatherType w : weatherTypes) {
                if (w.toString().equals(args[0])) {
                    WEATHER_HANDLER.changeWeatherOnCommand(w);
                    return true;
                }
            }
            return false;
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
