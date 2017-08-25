package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.leaderboard.LeaderboardSorter;
import me.glaremasters.guilds.message.Message;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.TreeMap;


public class CommandLeaderboard extends CommandBase {

	private final LeaderboardSorter sorter;

	public CommandLeaderboard() {
		super("leaderboard", Main.getInstance().getConfig().getString("commands.description.leaderboard"),
				"guilds.command.leaderboard", false, null, null, 0, 1);
		this.sorter = new LeaderboardSorter(Main.getInstance());
	}

	@Override
	public void execute(Player sender, String[] args) {
		Main instance = Main.getInstance();
		int amount = instance.getConfig().getInt("leaderboard.amount");
		StringBuilder builder = new StringBuilder(instance.getConfig().getString("leaderboard.header").replace("{amount}", Integer.toString
				(amount)));
		TreeMap<Guild, Double> top = sorter.getTop(amount);
		if (!top.isEmpty()) {
			for (int i = 1; i <= amount; i++) {
				Map.Entry<Guild, Double> entry = top.pollFirstEntry();
				if (entry == null) {
					continue;
				}
				String tmp = "\n&r  #" + i + ": " + entry.getKey().getName() + " (" + entry.getValue() + ")";
				builder.append(tmp);
			}
		} else {
			builder.append(instance.getConfig().getString("leaderboard.error"));
		}
		Message.sendMessage(sender, builder.toString().trim());
	}

}
