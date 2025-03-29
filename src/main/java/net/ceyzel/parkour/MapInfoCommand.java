package net.ceyzel.parkour;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MapInfoCommand {
    private final CeyZelParkour plugin;

    public MapInfoCommand(CeyZelParkour plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Suggestions> mapsSuggestions(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        plugin.getParkourMaps().keySet().stream()
                .filter(entry -> entry.startsWith(builder.getRemainingLowerCase()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    public LiteralCommandNode<CommandSourceStack> asNode() {
        return Commands.literal("mapinfo").then(
                Commands.argument("map", StringArgumentType.string())
                        .suggests(this::mapsSuggestions).executes(ctx -> {
                            if (!(ctx.getSource().getSender() instanceof Player player)) {
                                ctx.getSource().getSender().sendMessage(ChatColor.RED + "Только игроки могут использовать эту команду!");
                                return 0;
                            }
                            ParkourMap map = plugin.getParkourMaps().get(ctx.getArgument("map", String.class));
                            if (map == null) {
                                player.sendMessage(ChatColor.RED + "Карта не найдена!");
                                return 0;
                            }

                            UUID playerId = player.getUniqueId();
                            double totalScore = plugin.getPlayerTotalScore(playerId);
                            int completions = plugin.getMapCompletions(playerId, map.getName());
                            long bestTime = plugin.getBestTime(playerId, map.getName());

                            player.sendMessage(ChatColor.GOLD + "Информация о карте '" + map.getName() + "':");
                            player.sendMessage(ChatColor.YELLOW + "Награда: " + map.getScore() + " очков");
                            player.sendMessage(ChatColor.YELLOW + "Ваш общий счет: " + totalScore + " очков");
                            player.sendMessage(ChatColor.YELLOW + "Количество прохождений: " + completions);
                            player.sendMessage(ChatColor.YELLOW + "Лучшее время: " + (bestTime == Long.MAX_VALUE ? "Нет данных" : bestTime + " сек"));
                            return 1;
                        })
        ).build();
    }
}