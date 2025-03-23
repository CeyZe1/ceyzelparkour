package net.ceyzel.parkour;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class CeyZelParkour extends JavaPlugin {
    private Map<UUID, Double> playerScores = new HashMap<>();
    private FileConfiguration config;
    private Map<UUID, ParkourSession> activeSessions = new HashMap<>();
    private Map<String, ParkourMap> parkourMaps = new HashMap<>();

    public Location lobby_location;
    public Location hub_location;

    @Override
    public void onEnable() {
        loadPlayerScores();
        saveDefaultConfig();
        this.config = getConfig();
        loadMaps();
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

    private void loadLocationsFromConfig() {
        if (config.contains("lobby_location")) {
            lobby_location = config.getLocation("lobby_location");
        } else {
            getLogger().warning("lobby_location не найден в конфиге. Установите значение в конфиге.");
        }

        if (config.contains("hub_location")) {
            hub_location = config.getLocation("hub_location");
        } else {
            getLogger().warning("hub_location не найден в конфиге. Установите значение в конфиге.");
        }
    }

    public void loadMaps() {
        for (String mapName : config.getKeys(false)) {
            if (config.contains(mapName + ".start") && config.contains(mapName + ".finish")) {
                Location start = config.getLocation(mapName + ".start");
                Location finish = config.getLocation(mapName + ".finish");
                double score = config.getDouble(mapName + ".score");
                List<Location> checkpoints = (List<Location>) config.getList(mapName + ".checkpoints");

                if (start != null && finish != null) {
                    ParkourMap map = new ParkourMap(mapName, start, finish, score, checkpoints);
                    this.parkourMaps.put(mapName, map);
                } else {
                    getLogger().warning("Не удалось загрузить карту " + mapName + ": стартовая или конечная точка не найдены.");
                }
            }
        }
    }

    public void saveMap(ParkourMap map) {
        config.set(map.getName() + ".start", map.getStart());
        config.set(map.getName() + ".finish", map.getFinish());
        config.set(map.getName() + ".score", map.getScore());
        config.set(map.getName() + ".checkpoints", map.getCheckpoints());
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
                long time = (System.currentTimeMillis() - session.getStartTime()) / 1000;
                player.sendActionBar("§aВремя: §e" + time + " сек");
            }
        }
    }

    public Map<String, ParkourMap> getParkourMaps() {
        return parkourMaps;
    }

    public void setParkourMaps(Map<String, ParkourMap> parkourMaps) {
        this.parkourMaps = parkourMaps;
    }
}