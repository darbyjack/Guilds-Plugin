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

import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import me.glaremasters.guilds.challenges.ChallengeHandler;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.exceptions.InvalidPermissionException;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildChallenge;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.entity.Player;

@CommandAlias("%guilds")
public class CommandWarDeny extends BaseCommand {

    @Dependency private GuildHandler guildHandler;
    @Dependency private ChallengeHandler challengeHandler;

    @Subcommand("war deny")
    @Description("{@@descriptions.war-deny}")
    @CommandPermission(Constants.WAR_PERM + "deny")
    @Syntax("")
    public void execute(Player player, Guild guild, GuildRole role) {
        if (!role.isInitiateWar()) {
            ACFUtil.sneaky(new InvalidPermissionException());
        }

        GuildChallenge challenge = challengeHandler.getChallenge(guild);

        // Check to make sure they have a pending challenge
        if (challenge == null) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.WAR__NO_PENDING_CHALLENGE));
        }

        // Get the challenger guild cause we assume this is the defender
        Guild challenger = challenge.getChallenger();

        // Should never be null, but just in case
        if (challenger == null) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__GUILD_NO_EXIST));
        }

        // Send message to challenger saying that the challenge has been denied
        challenger.sendMessage(getCurrentCommandManager(), Messages.WAR__CHALLENGE_DENIED_CHALLENGER, "{guild}", guild.getName());
        // Send message to defender saying they've denied the challenge
        guild.sendMessage(getCurrentCommandManager(), Messages.WAR__CHALLENGE_DENIED_DEFENDER, "{guild}", challenger.getName());
        // Open the arena back up
        challenge.getArena().setInUse(false);
        // Remove the challenge
        challengeHandler.removeChallenge(challenge);
    }

}
