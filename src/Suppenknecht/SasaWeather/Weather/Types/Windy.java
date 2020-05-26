package Suppenknecht.SasaWeather.Weather.Types;

import Suppenknecht.SasaWeather.Main;
import Suppenknecht.SasaWeather.Weather.WeatherEndEvent;
import Suppenknecht.SasaWeather.Weather.WeatherType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;


import java.util.*;
import java.util.function.Consumer;

public class Windy implements WeatherType, Listener {

    private final static int IN = 1;
    private final static int PARALLEL = 0;
    private final static int AGAINST = -1;

    //--config stuff--
    private int minDuration;
    private int maxDuration;
    private boolean affectPlayers;
    private boolean enableParticles;
    //---

    private final Main MAIN;
    private final World OVERWORLD;
    private final List<WindDirection> windList = Collections.unmodifiableList(Arrays.asList(WindDirection.values()));
    private final int WINDLIST_SIZE = windList.size();
    private final BukkitScheduler SCHEDULER;

    private static Hashtable<UUID, Integer> playerDirTable;

    private int weatherThreadID;
    private WindDirection windFrom;

    public Windy() {
        MAIN = Main.getMainInstance();
        OVERWORLD = MAIN.getServer().getWorlds().get(0);
        SCHEDULER = MAIN.getServer().getScheduler();
        getConfig();
    }

    private void getConfig() {
        FileConfiguration config = MAIN.getConfig();
        int min = config.getInt("Windy.MinDuration", 120);
        minDuration = min < 30 ? 210 : min;
        int max = config.getInt("Windy.MaxDuration", 210);
        maxDuration = max < 30 ? 210 : max;

        affectPlayers = config.getBoolean("Windy.AffectPlayers", true);
        enableParticles = config.getBoolean("Windy.EnableParticles", true);
    }

    @Override
    public void start() {
        Collection<? extends Player> players = MAIN.getServer().getOnlinePlayers();
        playerDirTable = new Hashtable<>(MAIN.getServer().getMaxPlayers() * 2);

        Random r = new Random();
        int randomInt = r.nextInt(WINDLIST_SIZE);
        windFrom = windList.get(randomInt);

        for (Player p : players) {
            if (p.getWorld() == MAIN.getServer().getWorlds().get(0))
                playerDirTable.put(p.getUniqueId(), PARALLEL);
        }

        Thread thread = new Thread() {
            @Override
            public void run() {
                for (Player p : players) {
                    weatherEffects(p);
                }
            }
        };

        weatherThreadID = SCHEDULER.scheduleSyncRepeatingTask(MAIN, thread, 0, 4);

        int duration = (r.nextInt(maxDuration + 1) + minDuration) * 20;

        SCHEDULER.scheduleSyncDelayedTask(MAIN, new Runnable() {
            @Override
            public void run() {
                stop();
            }
        }, duration);

        MAIN.getServer().getPluginManager().registerEvents(this, MAIN);
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
        SCHEDULER.cancelTask(weatherThreadID);
        MAIN.getServer().getPluginManager().callEvent(new WeatherEndEvent(this));
    }

    private void weatherEffects(Player player) {
        if (player.getLocation().getY() >= 58) {
            if (enableParticles) spawnParticles(player);
            if (affectPlayers) affectEntities(player);
        }
    }

    private void spawnParticles(Player player) {
        final Random r = new Random();
        final Location loc = player.getLocation();
        for (int i = 0; i < 100; i++) {
            final double dx = 20 * r.nextDouble() * (r.nextBoolean() ? 1 : -1);
            final double dz = 20 * r.nextDouble() * (r.nextBoolean() ? 1 : -1);
            loc.add(dx, 0.0, dz);
            loc.setY(loc.getY() + 5.0 * r.nextDouble());
            if (loc.getY() <= 60.0 || loc.getBlock().getType() == Material.WATER) {     //still spawn particles in houses
                continue;
            }

            player.spawnParticle(Particle.SMOKE_NORMAL, loc, 0, windFrom.X * 3, -0.5, windFrom.Z * 3, 0.3);
        }
    }

    private void affectEntities(Player player) {
        if (isAffectable(player)) {
            int dir = playerDirTable.get(player.getUniqueId());
            if (dir == IN) {
                player.setWalkSpeed(0.25f);
            } else if (dir == AGAINST) {
                player.setWalkSpeed(0.175f);
            } else {
                player.setWalkSpeed(0.2f);
            }
        }
    }

    private boolean isAffectable(Player player) {
        Location loc = player.getLocation();
        int x = loc.getBlockX();
        int z = loc.getBlockZ();

        if (OVERWORLD.getBlockAt(x - windFrom.X, loc.getBlockY() + 1, z - windFrom.Z).getType() != Material.AIR) return false;

        for (int i = 1; i <= 4; i++) {
            int y = loc.getBlockY() + 1 + i;
            if (OVERWORLD.getBlockAt(x, y, z).getType() != Material.AIR) {
                int count = 0;
                if (OVERWORLD.getBlockAt(x + 2, y, z + 2).getType() != Material.AIR) count++;
                if (OVERWORLD.getBlockAt(x - 2, y, z + 2).getType() != Material.AIR) count++;
                if (OVERWORLD.getBlockAt(x + 2, y, z - 2).getType() != Material.AIR) count++;
                if (OVERWORLD.getBlockAt(x - 2, y, z - 2).getType() != Material.AIR) count++;

                if (count >= 3) return false;
            }
        }

        return true;
    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Location to = event.getTo();
        if (to != null) {
                Location from = event.getFrom();

                //1 -> 3: 3 - 1 = 2 => in x
                //3 -> 1: 1 - 3 = -2 => in -x
                //-3 -> -1: -1 -(-3) = 2 => in x
                //-1 -> -3: -3 -(-1) = -2 => in -x
                //1 -> -1: -1 - 1 = -2 => in -x
                //-1 -> 1: 1 -(-1) = 2 => in x

                double deltaX = to.getX() - from.getX();
                double deltaZ = to.getZ() - from.getZ();

                Player p = event.getPlayer();
                UUID id = p.getUniqueId();

                if (!p.isSneaking() && !p.isFlying()) {
                    windFrom.METHOD.accept(new DirectionFinder(deltaX, deltaZ, id, windFrom.FIRST_NAMED_WIND));
                }

                System.out.println();
        }


    }



    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        playerDirTable.put(id, PARALLEL);
    }

    @EventHandler
    public void onPayerQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        playerDirTable.remove(id);
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player p = event.getPlayer();
        if (p.getWorld() == OVERWORLD) {
            playerDirTable.put(p.getUniqueId(), PARALLEL);
        } else {
            playerDirTable.remove(p.getUniqueId());
        }

    }

    enum WindDirection {

        //where the wind is coming from
        //the numbers determine the direction

        NORTH(0,1, DirectionFinder::northAndSouthWind, true),
        NORTHEAST(-1,1, DirectionFinder::northEastAndSouthWestWind, true),
        EAST(-1,0, DirectionFinder::eastAndWestWind, true),
        SOUTHEAST(-1,-1, DirectionFinder::northWestAndSouthEastWind, false),
        SOUTH(0,-1, DirectionFinder::northAndSouthWind, false),
        SOUTHWEST(1,-1, DirectionFinder::northEastAndSouthWestWind, false),
        WEST(1,0, DirectionFinder::eastAndWestWind, false),
        NORTHWEST(1,1, DirectionFinder::northWestAndSouthEastWind, true);

        public final int X;
        public final int Z;
        public final Consumer<DirectionFinder> METHOD;
        public final boolean FIRST_NAMED_WIND;

        WindDirection(int x, int z, Consumer<DirectionFinder> func, boolean firstNamed) {
            X = x;
            Z = z;
            METHOD = func;
            FIRST_NAMED_WIND = firstNamed;
        }
    }


    private static class DirectionFinder {

        private final double deltaX;
        private final double deltaZ;
        private final UUID id;
        private final boolean firstNamedWind;

        DirectionFinder(double deltaX, double deltaZ, UUID id, boolean firstNamedWind) {
            this.deltaX = deltaX;
            this.deltaZ = deltaZ;
            this.id = id;
            this.firstNamedWind = firstNamedWind;
        }

        static void northAndSouthWind(DirectionFinder d) {
            int dir1 = d.firstNamedWind ? AGAINST : IN;
            int dir2 = dir1 == IN ? AGAINST : IN;

            if (d.deltaZ < -Math.abs(d.deltaX)) {
                playerDirTable.put(d.id, dir1);
            } else if (d.deltaZ > Math.abs(d.deltaX)) {
                playerDirTable.put(d.id, dir2);
            } else {
                playerDirTable.put(d.id, PARALLEL);
            }
        }

        static void eastAndWestWind(DirectionFinder d) {
            int dir1 = d.firstNamedWind ? AGAINST : IN;
            int dir2 = dir1 == IN ? AGAINST : IN;

            if (d.deltaX > Math.abs(d.deltaZ)) {
                playerDirTable.put(d.id, dir1);
            } else if (d.deltaX < -Math.abs(d.deltaZ)) {
                playerDirTable.put(d.id, dir2);
            } else {
                playerDirTable.put(d.id, PARALLEL);
            }
        }

        static void northEastAndSouthWestWind(DirectionFinder d) {
            int dir1 = d.firstNamedWind ? AGAINST : IN;
            int dir2 = dir1 == IN ? AGAINST : IN;

            if (d.deltaX > 0 && d.deltaZ < 0) {
                playerDirTable.put(d.id, dir1);
            } else if (d.deltaX < 0 && d.deltaZ > 0) {
                playerDirTable.put(d.id, dir2);
            } else {
                playerDirTable.put(d.id, PARALLEL);
            }
        }

        static void northWestAndSouthEastWind(DirectionFinder d) {
            int dir1 = d.firstNamedWind ? AGAINST : IN;
            int dir2 = dir1 == IN ? AGAINST : IN;

            if (d.deltaX < 0 && d.deltaZ < 0) {
                playerDirTable.put(d.id, dir1);
            } else if (d.deltaX > 0 && d.deltaZ > 0) {
                playerDirTable.put(d.id, dir2);
            } else {
                playerDirTable.put(d.id, PARALLEL);
            }
        }



    }

}



