/*
 * MIT License
 *
 * Copyright (c) 2019 Glare
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.glaremasters.guilds.commands.war;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.annotation.Values;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.arena.Arena;
import me.glaremasters.guilds.arena.ArenaHandler;
import me.glaremasters.guilds.challenges.ChallengeHandler;
import me.glaremasters.guilds.configuration.sections.WarSettings;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.exceptions.InvalidPermissionException;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildChallenge;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.tasks.GuildWarChallengeCheckTask;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.entity.Player;

@CommandAlias("%guilds")
public class CommandWarChallenge extends BaseCommand {

    @Dependency private GuildHandler guildHandler;
    @Dependency private ChallengeHandler challengeHandler;
    @Dependency private ArenaHandler arenaHandler;
    @Dependency private SettingsManager settingsManager;
    @Dependency private Guilds guilds;

    @Subcommand("war challenge")
    @Description("{@@descriptions.war-challenge}")
    @Syntax("<%syntax>")
    @CommandPermission(Constants.WAR_PERM + "challenge")
    @CommandCompletion("@guilds")
    public void execute(Player player, Guild guild, GuildRole role, @Values("@guilds") @Single String target) {
        if (!role.isInitiateWar()) {
            ACFUtil.sneaky(new InvalidPermissionException());
        }

        // Make sure they aren't already challenging someone / being challenged
        if (challengeHandler.getChallenge(guild) != null) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.WAR__ALREADY_CHALLENGING));
        }

        // Get an arena
        Arena arena = arenaHandler.getAvailableArena();

        // Check if there are any open arenas
        if (arena == null) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ARENA__ALL_FULL));
        }

        // Get the guild
        Guild targetGuild = guildHandler.getGuild(target);

        // Check if null
        if (targetGuild == null) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__GUILD_NO_EXIST));
        }

        // Check if same guild
        if (guildHandler.isSameGuild(guild, targetGuild)) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.WAR__NO_SELF_CHALLENGE));
        }

        // Make sure the defending guild is not on cooldown
        if (!challengeHandler.notOnCooldown(targetGuild, settingsManager)) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.WAR__DEFEND_COOLDOWN,
                    "{guild}", targetGuild.getName()));
        }

        // Check for online defenders to accept challenge
        if (challengeHandler.getOnlineDefenders(targetGuild).isEmpty()) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.WAR__NO_DEFENDERS));
        }

        // Min players
        int minPlayers = settingsManager.getProperty(WarSettings.MIN_PLAYERS);
        // Max players
        int maxPlayers = settingsManager.getProperty(WarSettings.MAX_PLAYERS);

        // Check to make sure both guilds have enough players on for a war
        if (!challengeHandler.checkEnoughOnline(guild, targetGuild, minPlayers)) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.WAR__NOT_ENOUGH_ON));
        }

        // Create the new guild challenge
        GuildChallenge challenge = challengeHandler.createNewChallenge(guild, targetGuild, minPlayers, maxPlayers, arena);

        // Add the new challenge to the handler
        challengeHandler.addChallenge(challenge);

        // Reserve the arena
        arena.setInUse(true);

        // Set an int to the amount of time allowed to accept it an invite
        int acceptTime = settingsManager.getProperty(WarSettings.ACCEPT_TIME);

        // Send message to challenger saying that they've sent the challenge.
        getCurrentCommandIssuer().sendInfo(Messages.WAR__CHALLENGE_SENT, "{guild}", targetGuild.getName(), "{amount}", String.valueOf(acceptTime));

        // Send message to defending guild
        challengeHandler.pingOnlineDefenders(targetGuild, getCurrentCommandManager(), guild.getName(), acceptTime);

        // After acceptTime is up, check if the challenge has been accepted or not
        new GuildWarChallengeCheckTask(guilds, challenge, challengeHandler).runTaskLater(guilds, (acceptTime * 20));
    }

}
