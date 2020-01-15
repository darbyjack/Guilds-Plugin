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

package me.glaremasters.guilds.commands.ally;

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
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.exceptions.InvalidPermissionException;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.entity.Player;

/**
 * Created by Glare
 * Date: 4/4/2019
 * Time: 5:59 PM
 */
@CommandAlias("%guilds")
public class CommandAllyAccept extends BaseCommand {

    @Dependency private GuildHandler guildHandler;

    /**
     * Accept a guild ally request
     * @param player the player to check
     * @param guild the guild they are in
     * @param role the role of the player
     * @param name the guild name they are accepting
     */
    @Subcommand("ally accept")
    @Description("{@@descriptions.ally-accept}")
    @CommandPermission(Constants.ALLY_PERM + "accept")
    @CommandCompletion("@allyInvites")
    @Syntax("<%syntax>")
    public void execute(Player player, Guild guild, GuildRole role, @Values("@allyInvites") @Single String name) {
        if (!role.isAddAlly()) {
            ACFUtil.sneaky(new InvalidPermissionException());
        }

        Guild target = guildHandler.getGuild(name);

        if (target == null) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__GUILD_NO_EXIST));
        }

        if (!guild.isAllyPending(target)) {
            return;
        }

        guild.removePendingAlly(target);
        guildHandler.addAlly(guild, target);

        guild.sendMessage(getCurrentCommandManager(), Messages.ALLY__CURRENT_ACCEPTED,
                "{guild}", target.getName());

        target.sendMessage(getCurrentCommandManager(), Messages.ALLY__TARGET_ACCEPTED,
                "{guild}", guild.getName());
    }

}