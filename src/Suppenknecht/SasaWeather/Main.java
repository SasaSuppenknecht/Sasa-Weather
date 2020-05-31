package Suppenknecht.SasaWeather;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin
{
    private static Main instance;
    
    public void onDisable() {
        super.onDisable();
    }
    
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        final boolean enabled = this.getConfig().getBoolean("General.Enable", true);
        if (!enabled) {
            this.getServer().getPluginManager().disablePlugin(this);
        }
        final WeatherHandler weatherHandler = new WeatherHandler();
        new Commands(weatherHandler);
        new WeatherPropertiesDisabler();
    }
    
    public static Main getMainInstance() {
        return Main.instance;
    }
}
