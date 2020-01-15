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
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

/**
 * Created by Glare
 * Date: 4/4/2019
 * Time: 5:52 PM
 */
@CommandAlias("%guilds")
public class CommandAllyList extends BaseCommand {

    @Dependency private GuildHandler guildHandler;

    /**
     * List all the allies of your guild
     * @param player the player to check
     * @param guild the guild they are in
     */
    @Subcommand("ally list")
    @Description("{@@descriptions.ally-list}")
    @Syntax("")
    @CommandPermission(Constants.ALLY_PERM + "list")
    public void execute(Player player, Guild guild) {
        if (!guild.hasAllies()) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ALLY__NONE));
        }

        getCurrentCommandIssuer().sendInfo(Messages.ALLY__LIST,
                "{ally-list}", guild.getAllies().stream().map(m -> guildHandler.getGuild(m).getName()).collect(Collectors.joining(",")));
    }

}