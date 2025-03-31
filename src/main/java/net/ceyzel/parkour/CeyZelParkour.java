package net.ceyzel.parkour;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.*;

public class CeyZelParkour extends JavaPlugin {
    private final Map<UUID, Double> playerScores = new HashMap<>();
    @Getter
    private final Map<UUID, ParkourSession> activeSessions = new HashMap<>();
    private final Map<String, ParkourMap> parkourMaps = new HashMap<>();
    private final Map<UUID, Map<String, Integer>> playerMapCompletions = new HashMap<>();
    private final Map<UUID, Map<String, Long>> playerBestTimes = new HashMap<>();
    private ParkourTimer parkourTimer;
    private Location lobbyLocation;

    @Override
    public void onEnable() {
        parkourTimer = new ParkourTimer(this);
        loadPlayerScores();
        saveDefaultConfig();
        loadMaps();
        loadLocationsFromConfig();

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            var cmd = new ParkourCommand(this);
            var info = new MapInfoCommand(this);
            var reg = commands.registrar();
            reg.register(cmd.createJoinCommand());
            reg.register(cmd.createCeyzelCommand());
            reg.register(info.asNode());
        });

        Bukkit.getPluginManager().registerEvents(new ParkourListener(this), this);
        LobbyHub.registerCommands(this);
    }

    @Override
    public void onDisable() {
        savePlayerScores();
    }

    private void loadPlayerScores() {
        if (getConfig().contains("playerScores")) {
            var section = getConfig().getConfigurationSection("playerScores");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    UUID playerId = UUID.fromString(key);
                    double score = getConfig().getDouble("playerScores." + key);
                    playerScores.put(playerId, score);
                }
            }
        }
    }

    private void savePlayerScores() {
        for (Map.Entry<UUID, Double> entry : playerScores.entrySet()) {
            getConfig().set("playerScores." + entry.getKey().toString(), entry.getValue());
        }
        saveConfig();
    }

    public void addPlayerScore(UUID playerId, double score) {
        playerScores.put(playerId, playerScores.getOrDefault(playerId, 0.0) + score);
        savePlayerScores();
    }

    public double getPlayerTotalScore(UUID playerId) {
        return playerScores.getOrDefault(playerId, 0.0);
    }

    private @Nullable Location locationByPath(String path) {
        String worldName = getConfig().getString(path + ".world");
        if (worldName != null && Bukkit.getWorld(worldName) != null) {
            return new Location(
                    Bukkit.getWorld(worldName),
                    getConfig().getDouble(path + ".x"),
                    getConfig().getDouble(path + ".y"),
                    getConfig().getDouble(path + ".z"),
                    (float) getConfig().getDouble(path + ".yaw"),
                    (float) getConfig().getDouble(path + ".pitch"));
        }
        this.getLogger().warning(String.format("%s неправильное значение", path));
        return null;
    }

    private void loadLocationsFromConfig() {
        if (getConfig().contains("lobby_location")) {
            this.lobbyLocation = getConfig().getLocation("lobby_location");
            if (this.lobbyLocation == null) {
                getLogger().warning("lobby_location не валиден");
            }
        } else {
            getLogger().warning("lobby_location не найден");
        }
    }

    public void loadMaps() {
        for (String mapName : getConfig().getKeys(false)) {
            if (getConfig().contains(mapName + ".start") && getConfig().contains(mapName + ".finish")) {
                Block start = getConfig().getLocation(mapName + ".start").getBlock();
                Block finish = getConfig().getLocation(mapName + ".finish").getBlock();
                Difficulty difficulty = Difficulty.valueOf(getConfig().getString(mapName + ".difficulty", "EASY")); // Загрузка сложности
                List<Location> checkpointLocations = (List<Location>) getConfig().getList(mapName + ".checkpoints");

                if (start != null && finish != null) {
                    ParkourMap map = new ParkourMap(mapName, start, finish, difficulty, checkpointLocations != null ? checkpointLocations.stream()
                            .map(Location::getBlock)
                            .toList() : new ArrayList<>());
                    this.parkourMaps.put(mapName, map);
                } else {
                    getLogger().warning("Карта " + mapName + ": стартовая/финишная точка не найдена");
                }
            }
        }
    }

    public void saveMap(ParkourMap map) {
        if (map == null) {
            getLogger().warning("null");
            return;
        }
        if (map.getStart() != null) {
            getConfig().set(map.getName() + ".start", map.getStart().getLocation());
        }
        if (map.getFinish() != null) {
            getConfig().set(map.getName() + ".finish", map.getFinish().getLocation());
        }
        getConfig().set(map.getName() + ".difficulty", map.getDifficulty().name());

        List<Location> checkpointLocations = new ArrayList<>();
        for (Block checkpoint : map.getCheckpoints()) {
            checkpointLocations.add(checkpoint.getLocation());
        }
        getConfig().set(map.getName() + ".checkpoints", checkpointLocations);

        saveConfig();
    }

    public ParkourMap getMap(String name) {
        return parkourMaps.get(name);
    }

    public Map<String, ParkourMap> getMaps() {
        return parkourMaps;
    }

    public ParkourTimer getParkourTimer() {
        return parkourTimer;
    }

    public void addMapCompletion(UUID playerId, String mapName, long time) {
        playerMapCompletions.computeIfAbsent(playerId, k -> new HashMap<>()).merge(mapName, 1, Integer::sum);
        playerBestTimes.computeIfAbsent(playerId, k -> new HashMap<>()).merge(mapName, time, (oldTime, newTime) -> Math.min(oldTime, newTime));
    }

    public int getMapCompletions(UUID playerId, String mapName) {
        return playerMapCompletions.getOrDefault(playerId, new HashMap<>()).getOrDefault(mapName, 0);
    }

    public long getBestTime(UUID playerId, String mapName) {
        return playerBestTimes.getOrDefault(playerId, new HashMap<>()).getOrDefault(mapName, Long.MAX_VALUE);
    }

    public Location getLobbyLocation() {
        return lobbyLocation;
    }
}