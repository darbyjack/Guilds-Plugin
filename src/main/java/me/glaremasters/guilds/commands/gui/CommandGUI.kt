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

package me.glaremasters.guilds.commands.gui

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.exceptions.InvalidPermissionException
import me.glaremasters.guilds.exceptions.InvalidTierException
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildRole
import me.glaremasters.guilds.utils.Constants
import org.bukkit.entity.Player

@CommandAlias("%guilds")
internal class CommandGUI() : BaseCommand() {
    @Dependency lateinit var guilds: Guilds

    @Subcommand("buff")
    @Description("{@@descriptions.buff}")
    @Syntax("")
    @CommandPermission(Constants.BASE_PERM + "buff")
    fun buff(player: Player, guild: Guild, role: GuildRole) {
        if (!guild.tier.isUseBuffs) {
            throw InvalidTierException()
        }

        if (!role.isActivateBuff) {
            throw InvalidPermissionException()
        }

        guilds.guiHandler.buffGUI.buffGUI.show(player)
    }

    @Subcommand("info")
    @Description("{@@descriptions.info}")
    @Syntax("")
    @CommandPermission(Constants.BASE_PERM + "info")
    fun info(player: Player, guild: Guild) {
        guilds.guiHandler.infoGUI.getInfoGUI(guild, player).show(player)
    }

    @Subcommand("list")
    @Description("{@@descriptions.list}")
    @Syntax("")
    @CommandPermission(Constants.BASE_PERM + "list")
    fun list(player: Player) {
        guilds.guiHandler.listGUI.listGUI.show(player)
    }

    @Subcommand("members")
    @Description("{@@descriptions.members}")
    @Syntax("")
    @CommandPermission(Constants.BASE_PERM + "members")
    fun members(player: Player, guild: Guild) {
        guilds.guiHandler.infoMembersGUI.getInfoMembersGUI(guild).show(player)
    }

    @Subcommand("vault")
    @Description("{@@descriptions.vault}")
    @Syntax("")
    @CommandPermission(Constants.BASE_PERM + "vault")
    fun vault(player: Player, guild: Guild, role: GuildRole) {
        if (!role.isOpenVault) {
            throw InvalidPermissionException()
        }

        guilds.guiHandler.vaultGUI.getVaultGUI(guild, player, guilds.commandManager).show(player)
    }
}