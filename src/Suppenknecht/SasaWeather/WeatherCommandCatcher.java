package Suppenknecht.SasaWeather;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class WeatherCommandCatcher implements Listener {

    private final Main MAIN;

    WeatherCommandCatcher() {
        MAIN = Main.getMainInstance();
        MAIN.getServer().getPluginManager().registerEvents(this, MAIN);
    }

    @EventHandler
    public void onWeatherCommand(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().startsWith("/weather")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("Please use this plugin's commands.");
        }
    }
}
