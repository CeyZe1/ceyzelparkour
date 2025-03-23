package net.ceyzel.parkour;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

public class LobbyHub {
    public static void RegisterCommands(CeyZelParkour plugin){

        BasicCommand hub = (commandSourceStack, strings) -> commandSourceStack.getExecutor().teleport(plugin.hub_location);
        BasicCommand lobby = (commandSourceStack, strings) -> commandSourceStack.getExecutor().teleport(plugin.lobby_location);

        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands ->{
            var reg = commands.registrar();
            reg.register("hub", hub);
            reg.register("lobby", lobby);
        });
    }

}
