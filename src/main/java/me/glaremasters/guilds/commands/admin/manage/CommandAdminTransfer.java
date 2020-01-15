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

package me.glaremasters.guilds.commands.admin.manage;

import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.annotation.Values;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
import me.glaremasters.guilds.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@CommandAlias("%guilds")
public class CommandAdminTransfer extends BaseCommand {

    @Dependency private GuildHandler guildHandler;

    /**
     * Transfer a guild to another player
     * @param player The player executing the command
     * @param guild the name of the guild that's being modified
     * @param newMaster The new master of the guild
     */
    @Subcommand("admin transfer")
    @CommandPermission(Constants.ADMIN_PERM)
    @Description("{@@descriptions.admin-transfer}")
    @CommandCompletion("@guilds @members-admin")
    @Syntax("<%syntax> <new master>")
    public void execute(Player player, @Flags("admin") @Values("@guilds") @Single Guild guild, @Values("@members-admin") @Single String newMaster) {

        if (guild == null) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__GUILD_NO_EXIST));
        }

        OfflinePlayer transferPlayer = PlayerUtils.getPlayer(newMaster);

        if (transferPlayer == null) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__PLAYER_NOT_FOUND));
        }

        if (guild.getGuildMaster().getUuid().equals(transferPlayer.getUniqueId())) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__TRANSFER_SAME_PERSON));
        }

        guild.transferGuildAdmin(transferPlayer, guildHandler);

        getCurrentCommandIssuer().sendInfo(Messages.TRANSFER__SUCCESS);
    }

}
