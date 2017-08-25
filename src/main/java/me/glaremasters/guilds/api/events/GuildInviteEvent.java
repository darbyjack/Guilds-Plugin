package me.glaremasters.guilds.api.events;

import me.glaremasters.guilds.api.events.base.GuildEvent;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;

public class GuildInviteEvent extends GuildEvent {

	private Player invitedPlayer;

	public GuildInviteEvent(Player player, Guild guild, Player invitedPlayer) {
		super(player, guild);

		this.invitedPlayer = invitedPlayer;
	}

	public Player getInvitedPlayer() {
		return invitedPlayer;
	}
}
