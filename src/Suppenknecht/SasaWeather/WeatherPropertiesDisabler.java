package Suppenknecht.SasaWeather;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.world.TimeSkipEvent;

public class WeatherPropertiesDisabler implements Listener
{
    private final Main MAIN;
    
    WeatherPropertiesDisabler() {
        MAIN = Main.getMainInstance();
        MAIN.getServer().getPluginManager().registerEvents(this, MAIN);
    }
    
    @EventHandler
    public void onWeatherCommand(final PlayerCommandPreprocessEvent event) {
        if (event.getMessage().startsWith("/weather")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("Please use this plugin's commands.");
        }
    }
    
    @EventHandler
    public void onServerLoaded(final ServerLoadEvent event) {
        MAIN.getServer().dispatchCommand(MAIN.getServer().getConsoleSender(), "weather clear");
    }

    @EventHandler
    public void onTimeSkip(TimeSkipEvent event) {
        if (event.getSkipReason() == TimeSkipEvent.SkipReason.NIGHT_SKIP) {
            final boolean hasStorm = event.getWorld().hasStorm();
            final boolean isThundering = event.getWorld().isThundering();

            if (hasStorm) event.getWorld().setStorm(true);
            else if (isThundering) event.getWorld().setThundering(true);
        }
    }
}
