/*
 * MIT License
 *
 * Copyright (c) 2018 Glare
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

package me.glaremasters.guilds.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.Messages;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildCode;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildRole;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by GlareMasters
 * Date: 3/15/2019
 * Time: 2:17 PM
 */
@AllArgsConstructor
@CommandAlias("guild|guilds")
public class CommandCodes extends BaseCommand {

    private GuildHandler guildHandler;

    /**
     * Create an invite code for your guild
     * @param player the player creating the invite code
     * @param guild the guild the invite is being created for
     * @param role the guild role of the user
     */
    @Subcommand("code create")
    @Description("{@@descriptions.code-create}")
    @CommandPermission("guilds.command.codecreate")
    public void onCreate(Player player, Guild guild, GuildRole role, Integer uses) {

        if (!role.isCreateCode()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        //todo Cleanup and make config values for size and maybe check for existing amount
        if (guild.getCodes() == null) {
            guild.setCodes(new ArrayList<>(Collections.singletonList(new GuildCode(RandomStringUtils.randomAlphabetic(10), uses))));
        } else {
            guild.getCodes().add(new GuildCode(RandomStringUtils.randomAlphabetic(10), uses));
        }

    }

    /**
     * Delete an invite code from the guild
     * @param player the player deleting the invite code
     * @param guild the guild the invite is being deleted from
     * @param role the role of the user
     */
    @Subcommand("code delete")
    @Description("{@@descriptions.code-delete}")
    @CommandPermission("guilds.command.codedelete")
    public void onDelete(Player player, Guild guild, GuildRole role) {

    }

    /**
     * List all the current invite codes in your guild
     * @param player the player fetching the list
     * @param guild the guild the player is in
     */
    @Subcommand("code list")
    @Description("{@@descriptions.code-list}")
    @CommandPermission("guilds.command.codelist")
    public void onList(Player player, Guild guild) {

    }

    /**
     * Redeem an invite code to join a guild
     * @param player the player redeeming the code
     * @param code the code being redeemed
     */
    @Subcommand("code redeem")
    @Description("{@@descriptions.code-redeem")
    @CommandPermission("guilds.command.coderedeem")
    public void onRedeem(Player player, String code) {

    }

}
