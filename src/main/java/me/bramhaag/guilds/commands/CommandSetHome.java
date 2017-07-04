package me.bramhaag.guilds.commands;

import me.bramhaag.guilds.Main;
import me.bramhaag.guilds.commands.base.CommandBase;
import me.bramhaag.guilds.guild.Guild;
import me.bramhaag.guilds.guild.GuildRole;
import me.bramhaag.guilds.message.Message;
import org.bukkit.entity.Player;

import java.util.HashMap;

/**
 * Created by GlareMasters on 6/11/2017.
 */
public class CommandSetHome extends CommandBase {
    public HashMap<String, Long> cooldowns = new HashMap<String, Long>();

    public CommandSetHome() {
        super("sethome", "Set your guild's home!", "guilds.command.sethome", false, null, null, 0, 0);
    }

    @Override
    public void execute(Player player, String[] args) {
        int cooldownTime = Main.getInstance().getConfig().getInt("sethome.cool-down"); // Get number of seconds from wherever you want
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }
        if (cooldowns.containsKey(player.getName())) {
            long secondsLeft = ((cooldowns.get(player.getName()) / 1000) + cooldownTime) - (System.currentTimeMillis() / 1000);
            if (secondsLeft > 0) {
                // Still cooling down
                Message.sendMessage(player, Message.COMMAND_ERROR_SETHOME_COOLDOWN.replace("{time}", String.valueOf(secondsLeft)));
                return;
            }
        }

        GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());
        if (!role.canChangeHome()) {
            Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
            return;
        }

        String world = player.getWorld().getName();
        double xloc = player.getLocation().getX();
        double yloc = player.getLocation().getY();
        double zloc = player.getLocation().getZ();
        float yaw = player.getLocation().getYaw();
        float pitch = player.getLocation().getPitch();

        Main.getInstance().guildhomesconfig.set(Guild.getGuild(player.getUniqueId()).getName(), world + ":" + xloc + ":" + yloc + ":" + zloc + ":" + yaw + ":" + pitch);
        Main.getInstance().saveGuildhomes();
        Message.sendMessage(player, Message.COMMAND_CREATE_GUILD_HOME);
        cooldowns.put(player.getName(), System.currentTimeMillis());
    }
}