package net.ceyzel.parkour;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MapInfoCommand {
    private final CeyZelParkour plugin;

    public MapInfoCommand(CeyZelParkour plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Suggestions> suggestMaps(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        plugin.getMaps().keySet().stream()
                .filter(entry -> entry.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    public LiteralCommandNode<CommandSourceStack> asNode() {
        return Commands.literal("mapinfo").then(
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

                            UUID playerId = player.getUniqueId();
                            int completions = plugin.getMapCompletions(playerId, map.getName());
                            long bestTime = plugin.getBestTime(playerId, map.getName());

                            player.sendMessage("§6Информация о карте '" + map.getName() + "':");
                            player.sendMessage("§eПройдена: " + completions + " раз");
                            player.sendMessage("§eСложность: " + map.getDifficulty().name()); // Отображение сложности
                            player.sendMessage("§eЛучшее время: " + (bestTime == Long.MAX_VALUE ? "No data" : plugin.getParkourTimer().formatDuration(Duration.ofMillis(bestTime))));
                            return 1;
                        })
        ).build();
    }
}