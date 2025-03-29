package net.ceyzel.parkour;

import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.NumberConversions;

import javax.annotation.Nullable;
import java.util.*;

public class CeyZelParkour extends JavaPlugin {
    private Map<UUID, Double> playerScores = new HashMap<>();
    private FileConfiguration config;
    private Map<UUID, ParkourSession> activeSessions = new HashMap<>();
    private Map<String, ParkourMap> parkourMaps = new HashMap<>();
    private Map<UUID, Map<String, Integer>> playerMapCompletions = new HashMap<>();
    private Map<UUID, Map<String, Long>> playerBestTimes = new HashMap<>();

    public Location lobby_location;

    @Override
    public void onEnable() {
        loadPlayerScores();
        saveDefaultConfig();
        this.config = getConfig();
        loadMaps();
        for (var k : parkourMaps.values()) {
            System.out.println(k.getCheckpoints());
        }
        ;
        loadLocationsFromConfig();
        getCommand("ceyzel").setExecutor(new ParkourCommand(this));
        Bukkit.getPluginManager().registerEvents(new ParkourListener(this), this);
        Bukkit.getScheduler().runTaskTimer(this, this::updateActionBars, 0L, 20L);
        LobbyHub.RegisterCommands(this);
    }

    @Override
    public void onDisable() {
        savePlayerScores();
    }

    private void loadPlayerScores() {
        if (getConfig().contains("playerScores")) {
            for (String key : getConfig().getConfigurationSection("playerScores").getKeys(false)) {
                UUID playerId = UUID.fromString(key);
                double score = getConfig().getDouble("playerScores." + key);
                playerScores.put(playerId, score);
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
        String worldName = config.getString(path + ".world");
        if (worldName != null && Bukkit.getWorld(worldName) != null) {  // Add null check
            return new Location(
                    Bukkit.getWorld(worldName),
                    config.getDouble(path + ".x"),
                    config.getDouble(path + ".y"),
                    config.getDouble(path + ".z"),
                    (float) config.getDouble(path + ".yaw"),
                    (float) config.getDouble(path + ".pitch"));
        }
        this.getLogger().warning(String.format("%s не является валидной локацией", path));
        return null;
    }


    private static @Nullable Location locationFromMap(LinkedHashMap<String, Object> map) {
        if (map == null) {
            return null;
        }
        if (
                map.get("world") instanceof String worldname &&
                        map.get("x") instanceof Double x &&
                        map.get("y") instanceof Double y &&
                        map.get("z") instanceof Double z &&
                        Bukkit.getWorld(worldname) instanceof World world
        ) {
            return new Location(world, x, y, z);
        }
        return null;
    }


    private void loadLocationsFromConfig() {
        if (config.contains("lobby_location")) {
            this.lobby_location = locationByPath("lobby_location");
            if (this.lobby_location == null) {  // Add null check
                getLogger().warning("lobby_location не является валидной локацией.");
            }
        } else {
            getLogger().warning("lobby_location не найден в конфиге. Установите значение в конфиге.");
        }
    }


    public void loadMaps() {
        for (String mapName : config.getKeys(false)) {
            if (config.contains(mapName + ".start") && config.contains(mapName + ".finish")) {
                Location start = locationByPath(mapName + ".start");
                Location finish = locationByPath(mapName + ".finish");
                double score = config.getDouble(mapName + ".score");
                var checkpoints = (List<LinkedHashMap<String, Object>>) config.getList(mapName + ".checkpoints");
                var checkpointsList = new ArrayList<Location>();
                if (checkpoints != null) {
                    for (var a : checkpoints) {
                        checkpointsList.add(locationFromMap(a));
                    }
                }

                if (start != null && finish != null) {
                    ParkourMap map = new ParkourMap(mapName, start, finish, score, checkpointsList);
                    this.parkourMaps.put(mapName, map);
                } else {
                    getLogger().warning("Не удалось загрузить карту " + mapName + ": стартовая или конечная точка не найдены.");
                }
            }
        }
    }

    public void saveMap(ParkourMap map) {
        if (map == null) {
            getLogger().warning("null");
            return;
        }
        config.set(map.getName() + ".start", map.getStart());
        config.set(map.getName() + ".finish", map.getFinish());
        config.set(map.getName() + ".score", map.getScore());
        config.set(map.getName() + ".checkpoints", map.getCheckpoints());
        saveConfig();
    }


    public ParkourMap getMap(String name) {
        if (parkourMaps == null) {
            getLogger().warning("null");
            return null;
        }
        return parkourMaps.get(name);
    }


    public Map<String, ParkourMap> getMaps() {
        if (parkourMaps == null) {
            getLogger().warning("null");
            return new HashMap<>();
        }
        return parkourMaps;
    }


    public Map<UUID, ParkourSession> getActiveSessions() {
        if (activeSessions == null) {
            getLogger().warning("null");
            return new HashMap<>();
        }
        return this.activeSessions;
    }


    private void updateActionBars() {
        for (ParkourSession session : activeSessions.values()) {
            Player player = Bukkit.getPlayer(session.getPlayerId());
            if (player != null) {
                long time = (System.currentTimeMillis() - session.getStartTime()) / 1000;
                player.sendActionBar("§aВремя: §e" + time + " сек");
            }
        }
    }


    public Map<String, ParkourMap> getParkourMaps() {
        if (parkourMaps == null) {
            getLogger().warning("null");
            return new HashMap<>();
        }
        return parkourMaps;
    }


    public void setParkourMaps(Map<String, ParkourMap> parkourMaps) {
        this.parkourMaps = parkourMaps;
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
}