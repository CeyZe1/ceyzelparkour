package net.ceyzel.parkour;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ParkourTimer {
    private final CeyZelParkour plugin;
    private final Map<UUID, Instant> startTimes = new HashMap<>();

    public ParkourTimer(CeyZelParkour plugin) {
        this.plugin = plugin;
        startTimerTask();
    }

    public void startTimer(UUID playerId) {
        startTimes.put(playerId, Instant.now());
    }

    public void stopTimer(UUID playerId) {
        startTimes.remove(playerId);
    }

    public Duration getElapsedTime(UUID playerId) {
        return startTimes.containsKey(playerId) ? Duration.between(startTimes.get(playerId), Instant.now()) : Duration.ZERO;
    }

    private void startTimerTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                startTimes.forEach((playerId, startTime) -> {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null) {
                        Duration elapsedTime = Duration.between(startTime, Instant.now());
                        player.sendActionBar(net.kyori.adventure.text.Component.text("§aВремя: §e" + formatDuration(elapsedTime)));
                    }
                });
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        long millis = duration.toMillisPart();

        return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis);
    }
}