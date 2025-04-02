package net.ceyzel.parkour;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class TimeCommand {
    private final CeyZelParkour plugin;

    public TimeCommand(CeyZelParkour plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Suggestions> suggestMaps(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        plugin.getMaps().keySet().stream()
                .filter(entry -> entry.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    public LiteralCommandNode<CommandSourceStack> asNode() {
        return Commands.literal("maptime").then(
                Commands.argument("map", StringArgumentType.string())
                        .suggests(this::suggestMaps)
                        .executes(ctx -> {
                            if (!(ctx.getSource().getSender() instanceof Player player)) {
                                ctx.getSource().getSender().sendMessage(ChatColor.RED + "Только игрок может использовать эту команду");
                                return 0;
                            }
                            String mapName = ctx.getArgument("map", String.class);
                            ParkourMap map = plugin.getMap(mapName);
                            if (map == null) {
                                player.sendMessage(ChatColor.RED + "Карта не найдена");
                                return 0;
                            }

                            Map<UUID, Long> bestTimes = new HashMap<>();
                            for (Map.Entry<UUID, Map<String, Long>> entry : plugin.getPlayerBestTimes().entrySet()) {
                                Long time = entry.getValue().get(mapName);
                                if (time != null) {
                                    bestTimes.put(entry.getKey(), time);
                                }
                            }

                            List<Map.Entry<UUID, Long>> sortedTimes = new ArrayList<>(bestTimes.entrySet());
                            sortedTimes.sort(Map.Entry.comparingByValue());

                            player.sendMessage(ChatColor.GOLD + "Топ 10 игроков на карте '" + mapName + "':");
                            for (int i = 0; i < Math.min(sortedTimes.size(), 10); i++) {
                                Map.Entry<UUID, Long> entry = sortedTimes.get(i);
                                Player topPlayer = Bukkit.getPlayer(entry.getKey());
                                String playerName = topPlayer != null ? topPlayer.getName() : "Неизвестно";
                                String time = plugin.getParkourTimer().formatDuration(Duration.ofMillis(entry.getValue()));
                                player.sendMessage(ChatColor.YELLOW + String.valueOf(i + 1) + ". " + playerName + " - " + time);
                            }

                            return 1;
                        })
        ).build();
    }
}