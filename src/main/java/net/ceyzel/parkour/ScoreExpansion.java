package net.ceyzel.parkour;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class ScoreExpansion extends PlaceholderExpansion {
    private final CeyZelParkour plugin;

    public ScoreExpansion(CeyZelParkour plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "ceyzelparkour";
    }

    @Override
    public @NotNull String getAuthor() {
        return "YourName";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.equalsIgnoreCase("player_score_maps")) {
            return String.valueOf(plugin.getPlayerTotalScore(player.getUniqueId()));
        }
        return null;
    }
}