package Suppenknecht.SasaWeather;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;

    private boolean enabled;
    private WeatherHandler weatherHandler;

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {

        enabled = getConfig().getBoolean("General.Enable", true);
        if (!enabled) {
            getServer().getPluginManager().disablePlugin(this);
        }

        instance = this;
        this.saveDefaultConfig();
        weatherHandler = new WeatherHandler();
    }



    public static Main getMainInstance() {
        return instance;
    }

}
