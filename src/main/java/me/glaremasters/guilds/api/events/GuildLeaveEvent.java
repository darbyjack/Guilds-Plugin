package me.glaremasters.guilds.api.events;

import me.glaremasters.guilds.api.events.base.GuildEvent;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;

public class GuildLeaveEvent extends GuildEvent {

    public GuildLeaveEvent(Player player, Guild guild) {
        super(player, guild);
    }
}