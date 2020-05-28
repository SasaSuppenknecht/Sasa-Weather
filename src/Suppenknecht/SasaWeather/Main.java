package Suppenknecht.SasaWeather;

import org.bukkit.plugin.java.JavaPlugin;


public class Main extends JavaPlugin {

    private static Main instance = null;

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();

        boolean enabled = getConfig().getBoolean("General.Enable", true);
        if (!enabled) {
            getServer().getPluginManager().disablePlugin(this);
        }

        WeatherHandler weatherHandler = new WeatherHandler();
        new Commands(weatherHandler);
        new WeatherCommandCatcher();
    }

    public static Main getMainInstance() {
        return instance;
    }

}
