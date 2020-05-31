package Suppenknecht.SasaWeather.Weather.Types;

import Suppenknecht.SasaWeather.Weather.WeatherType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.function.Consumer;

public class Windy extends WeatherType {
    private static final int IN = 1;
    private static final int PARALLEL = 0;
    private static final int AGAINST = -1;

    //--config stuff--
    private boolean affectPlayers;
    private boolean enableParticles;
    //---

    private final List<WindDirection> windList;
    private final int WINDLIST_SIZE;

    private static Hashtable<Player, Integer> playerDirTable;
    private WindDirection windFrom;
    private int weatherThreadID;

    public Windy() {
        this.windList = Collections.unmodifiableList(Arrays.asList(WindDirection.values()));
        this.WINDLIST_SIZE = this.windList.size();
    }

    @Override
    protected void getConfig() {
        final int defaultMin = 120, defaultMax = 210;
        super.getDurationBounds(defaultMin, defaultMax);
        this.affectPlayers = this.CONFIG.getBoolean("Windy.AffectPlayers", true);
        this.enableParticles = this.CONFIG.getBoolean("Windy.EnableParticles", true);
    }

    @Override
    public void start() {
        final Collection<? extends Player> players = OVERWORLD.getPlayers();
        Windy.playerDirTable = new Hashtable<>(this.MAIN.getServer().getMaxPlayers() * 2);
        for (final Player p : players) {
            Windy.playerDirTable.put(p, PARALLEL);
        }
        final Random r = new Random();
        final int randomInt = r.nextInt(this.WINDLIST_SIZE);
        windFrom = windList.get(randomInt);
        super.start();
    }

    @Override
    protected void scheduleThreads() {
        final Thread thread = new Thread() {
            @Override
            public void run() {
               for (Player p : playerDirTable.keySet()) {
                   weatherEffects(p);
               }
            }
        };
        weatherThreadID = SCHEDULER.scheduleSyncRepeatingTask( MAIN,  thread, 0L, 4L);

        final int duration = (new Random().nextInt(maxDuration - minDuration + IN) + minDuration) * 20;
        SCHEDULER.scheduleSyncDelayedTask(MAIN, new StopThread(), duration);
    }

    @Override
    public void stop() {
        this.SCHEDULER.cancelTask(weatherThreadID);
        super.stop();
    }

    private void weatherEffects(final Player player) {
        if (player.getLocation().getY() >= 58.0) {
            if (enableParticles) {
                spawnParticles(player);
            }
            if (affectPlayers) {
                affectEntities(player);
            }
        }
    }

    private void spawnParticles(final Player player) {
        final Random r = new Random();
        final Location loc = player.getLocation();
        for (int i = 0; i < 100; ++i) {
            final double dx = 20.0 * r.nextDouble() * (r.nextBoolean() ? 1 : -1);
            final double dz = 20.0 * r.nextDouble() * (r.nextBoolean() ? 1 : -1);
            loc.add(dx, 0.0, dz);
            loc.setY(loc.getY() + 5.0 * r.nextDouble());
            if (loc.getY() > 60.0) {
                if (loc.getBlock().getType() != Material.WATER) {
                    player.spawnParticle(Particle.SMOKE_NORMAL, loc, 0,(windFrom.X * 3), -0.5, (windFrom.Z * 3), 0.3);
                }
            }
        }
    }

    private void affectEntities(final Player player) {
        if (this.isAffectable(player)) {
            final int dir = Windy.playerDirTable.get(player);
            if (dir == IN) {
                player.setWalkSpeed(0.25f);
            } else if (dir == AGAINST) {
                player.setWalkSpeed(0.175f);
            } else {
                player.setWalkSpeed(0.2f);
            }
        }
    }

    private boolean isAffectable(final Player player) {
        final Location loc = player.getLocation();
        final int x = loc.getBlockX();
        final int z = loc.getBlockZ();
        if (this.OVERWORLD.getBlockAt(x - windFrom.X, loc.getBlockY() + 1, z - windFrom.Z).getType() != Material.AIR) {
            return false;
        }
        for (int i = 1; i <= 4; ++i) {
            final int y = loc.getBlockY() + 1 + i;
            if (OVERWORLD.getBlockAt(x, y, z).getType() != Material.AIR) {
                int count = 0;
                if (OVERWORLD.getBlockAt(x + 2, y, z + 2).getType() != Material.AIR) {
                    ++count;
                }
                if (OVERWORLD.getBlockAt(x - 2, y, z + 2).getType() != Material.AIR) {
                    ++count;
                }
                if (OVERWORLD.getBlockAt(x + 2, y, z - 2).getType() != Material.AIR) {
                    ++count;
                }
                if (OVERWORLD.getBlockAt(x - 2, y, z - 2).getType() != Material.AIR) {
                    ++count;
                }
                if (count >= 2) {
                    return false;
                }
            }
        }
        return true;
    }

    @EventHandler
    public void onPlayerMoveEvent(final PlayerMoveEvent event) {
        final Location to = event.getTo();
        if (to != null) {
            final Location from = event.getFrom();
            final double deltaX = to.getX() - from.getX();
            final double deltaZ = to.getZ() - from.getZ();
            final Player p = event.getPlayer();
            if (!p.isSneaking() && !p.isFlying()) {
                windFrom.METHOD.accept(new DirectionFinder(deltaX, deltaZ, p, this.windFrom.FIRST_NAMED_WIND));
            } else {
                p.setWalkSpeed(2.0f);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player p = event.getPlayer();
        if (p.getWorld() == OVERWORLD) {
            Windy.playerDirTable.put(p, PARALLEL);
        }
    }

    @EventHandler
    public void onPayerQuit(final PlayerQuitEvent event) {
        final Player p = event.getPlayer();
        if (p.getWorld() == OVERWORLD) {
            Windy.playerDirTable.remove(p);
        }
    }

    @EventHandler
    public void onPlayerChangedWorld(final PlayerChangedWorldEvent event) {
        final Player p = event.getPlayer();
        if (p.getWorld() == OVERWORLD) {
            Windy.playerDirTable.put(p, PARALLEL);
        } else {
            Windy.playerDirTable.remove(p);
        }
    }

    @Override
    public String toString() {
        return "Windy";
    }

    enum WindDirection {
        NORTH(0, 1, DirectionFinder::northAndSouthWind, true),
        NORTHEAST( -1, 1, DirectionFinder::northEastAndSouthWestWind, true),
        EAST( -1, 0, DirectionFinder::eastAndWestWind, true),
        SOUTHEAST( -1,  -1, DirectionFinder::northWestAndSouthEastWind, false),
        SOUTH(0,  -1, DirectionFinder::northAndSouthWind, false),
        SOUTHWEST(1,  -1, DirectionFinder::northEastAndSouthWestWind, false),
        WEST(1, 0, DirectionFinder::eastAndWestWind, false),
        NORTHWEST(1, 1, DirectionFinder::northWestAndSouthEastWind, true);

        public final int X;
        public final int Z;
        public final Consumer<DirectionFinder> METHOD;
        public final boolean FIRST_NAMED_WIND;

        WindDirection(final int x, final int z, final Consumer<DirectionFinder> func, final boolean firstNamed) {
            X = x;
            Z = z;
            METHOD = func;
            FIRST_NAMED_WIND = firstNamed;
        }
    }

    private static class DirectionFinder {
        private final double deltaX;
        private final double deltaZ;
        private final Player p;
        private final boolean firstNamedWind;

        DirectionFinder(final double deltaX, final double deltaZ, final Player p, final boolean firstNamedWind) {
            this.deltaX = deltaX;
            this.deltaZ = deltaZ;
            this.p = p;
            this.firstNamedWind = firstNamedWind;
        }

        static void northAndSouthWind(final DirectionFinder d) {
            final int dir1 = d.firstNamedWind ? AGAINST : IN;
            final int dir2 = (dir1 == IN) ? AGAINST : IN;
            if (d.deltaZ < -Math.abs(d.deltaX)) {
                Windy.playerDirTable.put(d.p, dir1);
            } else if (d.deltaZ > Math.abs(d.deltaX)) {
                Windy.playerDirTable.put(d.p, dir2);
            } else {
                Windy.playerDirTable.put(d.p, PARALLEL);
            }
        }

        static void eastAndWestWind(final DirectionFinder d) {
            final int dir1 = d.firstNamedWind ? AGAINST : IN;
            final int dir2 = (dir1 == IN) ? AGAINST : IN;
            if (d.deltaX > Math.abs(d.deltaZ)) {
                Windy.playerDirTable.put(d.p, dir1);
            } else if (d.deltaX < -Math.abs(d.deltaZ)) {
                Windy.playerDirTable.put(d.p, dir2);
            } else {
                Windy.playerDirTable.put(d.p, PARALLEL);
            }
        }

        static void northEastAndSouthWestWind(final DirectionFinder d) {
            final int dir1 = d.firstNamedWind ? AGAINST : IN;
            final int dir2 = (dir1 == IN) ? AGAINST : IN;
            if (d.deltaX > 0.0 && d.deltaZ < 0.0) {
                Windy.playerDirTable.put(d.p, dir1);
            } else if (d.deltaX < 0.0 && d.deltaZ > 0.0) {
                Windy.playerDirTable.put(d.p, dir2);
            } else {
                Windy.playerDirTable.put(d.p, PARALLEL);
            }
        }

        static void northWestAndSouthEastWind(final DirectionFinder d) {
            final int dir1 = d.firstNamedWind ? AGAINST : IN;
            final int dir2 = (dir1 == IN) ? AGAINST : IN;
            if (d.deltaX < 0.0 && d.deltaZ < 0.0) {
                Windy.playerDirTable.put(d.p, dir1);
            } else if (d.deltaX > 0.0 && d.deltaZ > 0.0) {
                Windy.playerDirTable.put(d.p, dir2);
            } else {
                Windy.playerDirTable.put(d.p, PARALLEL);
            }
        }
    }
}
