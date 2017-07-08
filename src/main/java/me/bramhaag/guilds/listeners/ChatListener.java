package me.bramhaag.guilds.listeners;

import me.bramhaag.guilds.guild.Guild;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import static me.bramhaag.guilds.placeholders.Placeholders.getGuildMemberCount;
import static me.bramhaag.guilds.placeholders.Placeholders.getGuildMembersOnline;

public class ChatListener implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Guild guild = Guild.getGuild(player.getUniqueId());
        event.setFormat(event.getFormat().replace("{ESSENTIALS_GUILD}", guild.getName()));
        event.setFormat(event.getFormat().replace("{ESSENTIALS_GUILD_PREFIX}", guild.getPrefix()));
        event.setFormat(event.getFormat().replace("{ESSENTIALS_GUILD_MASTER}", Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName()));
        event.setFormat(event.getFormat().replace("{ESSENTIALS_GUILD_MEMBER_COUNT}", getGuildMemberCount(event.getPlayer())));
        event.setFormat(event.getFormat().replace("{ESSENTIALS_GUILD_MEMBERS_ONLINE}", getGuildMembersOnline(event.getPlayer())));
    }
}

