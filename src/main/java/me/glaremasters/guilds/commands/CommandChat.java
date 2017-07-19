package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.message.Message;
import org.bukkit.entity.Player;

public class CommandChat extends CommandBase {

    public CommandChat() {
        super("chat", "Send a message to your guild members", "guilds.command.chat", false,
            new String[] {"c"}, "<message>", 1, -1);
    }

    public void execute(Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());

        if (guild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }

        GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());
        if (!role.canChat()) {
            Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
            return;
        }

        String message = String.join(" ", args);
        guild.sendMessage(Message.COMMAND_CHAT_MESSAGE.replace("{role}",
            GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole()).getName(),
            "{player}", player.getName(), "{message}", message));
    }
}
