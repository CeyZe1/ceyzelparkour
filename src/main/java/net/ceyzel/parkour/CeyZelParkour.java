package net.ceyzel.parkour;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

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
        loadLocationsFromConfig();

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            var cmd = new ParkourCommand(this);
            var info = new MapInfoCommand(this);
            var reg = commands.registrar();
            reg.register(cmd.coolJoinCommand());
            reg.register(cmd.coolCeyzelCommand());
            reg.register(info.asNode());
        });

        Bukkit.getPluginManager().registerEvents(new ParkourListener(this), this);
        Bukkit.getScheduler().runTaskTimer(this, this::updateActionBars, 0L, 1L); // 1 тик = 50 мс
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
        if (worldName != null && Bukkit.getWorld(worldName) != null) {
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

    private void loadLocationsFromConfig() {
        if (config.contains("lobby_location")) {
            this.lobby_location = config.getLocation("lobby_location");
            if (this.lobby_location == null) {
                getLogger().warning("lobby_location не является валидной локацией.");
            }
        } else {
            getLogger().warning("lobby_location не найден в конфиге. Установите значение в конфиге.");
        }
    }

    public void loadMaps() {
        for (String mapName : config.getKeys(false)) {
            if (config.contains(mapName + ".start") && config.contains(mapName + ".finish")) {
                Block start = config.getLocation(mapName + ".start").getBlock();
                Block finish = config.getLocation(mapName + ".finish").getBlock();
                double score = config.getDouble(mapName + ".score");
                List<Location> checkpointLocations = (List<Location>) config.getList(mapName + ".checkpoints");

                if (start != null && finish != null) {
                    ParkourMap map = new ParkourMap(mapName, start, finish, score, checkpointLocations.stream()
                            .map(Location::getBlock)
                            .toList()
                    );
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
        if (map.getStart() != null) {
            config.set(map.getName() + ".start", map.getStart().getLocation());
        }
        if (map.getFinish() != null) {
            config.set(map.getName() + ".finish", map.getFinish().getLocation());
        }
        config.set(map.getName() + ".score", map.getScore());

        List<Location> checkpointLocations = new ArrayList<>();
        for (Block checkpoint : map.getCheckpoints()) {
            checkpointLocations.add(checkpoint.getLocation());
        }
        config.set(map.getName() + ".checkpoints", checkpointLocations);

        saveConfig();
    }

    public ParkourMap getMap(String name) {
        return parkourMaps.get(name);
    }

    public Map<String, ParkourMap> getMaps() {
        return parkourMaps;
    }

    public Map<UUID, ParkourSession> getActiveSessions() {
        return this.activeSessions;
    }

    private void updateActionBars() {
        for (ParkourSession session : activeSessions.values()) {
            Player player = Bukkit.getPlayer(session.getPlayerId());
            if (player != null) {
                long time = (System.currentTimeMillis() - session.getStartTime());
                player.sendActionBar("§aВремя: §e" + formatTime(time));
            }
        }
    }

    public String formatTime(long millis) {
        long hours = millis / 3600000;
        long minutes = (millis % 3600000) / 60000;
        long seconds = (millis % 60000) / 1000;
        long milliseconds = millis % 1000;

        return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds);
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