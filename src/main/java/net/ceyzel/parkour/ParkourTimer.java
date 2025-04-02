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
        Instant startTime = startTimes.get(playerId);
        if (startTime != null) {
            return Duration.between(startTime, Instant.now());
        }
        return Duration.ZERO;
    }

    private void startTimerTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<UUID, Instant> entry : startTimes.entrySet()) {
                    Player player = Bukkit.getPlayer(entry.getKey());
                    if (player != null) {
                        Duration elapsedTime = Duration.between(entry.getValue(), Instant.now());
                        player.sendActionBar(net.kyori.adventure.text.Component.text("§aВремя: §e" + formatDuration(elapsedTime)));
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L); // Обновление каждый тик
    }

    public String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        long millis = duration.toMillis() % 1000;

        return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis);
    }
}