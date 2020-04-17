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

package me.glaremasters.guilds.acf

import ch.jalu.configme.SettingsManager
import co.aikar.commands.BaseCommand
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.actions.ActionHandler
import me.glaremasters.guilds.arena.Arena
import me.glaremasters.guilds.arena.ArenaHandler
import me.glaremasters.guilds.challenges.ChallengeHandler
import me.glaremasters.guilds.configuration.sections.PluginSettings
import me.glaremasters.guilds.cooldowns.CooldownHandler
import me.glaremasters.guilds.database.DatabaseAdapter
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.guild.GuildRole
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.scanner.ZISScanner
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import java.util.Locale

class ACFHandler(private val plugin: Guilds, private val commandManager: PaperCommandManager) {
    val languages = mutableListOf<String>()

    fun load() {
        commandManager.usePerIssuerLocale(true, false)
        commandManager.enableUnstableAPI("help")

        loadLang()
        loadContexts(plugin.guildHandler, plugin.arenaHandler)
        loadCompletions(plugin.guildHandler, plugin.arenaHandler)
        loadDI()

        commandManager.commandReplacements.addReplacement("guilds", plugin.settingsHandler.mainConf.getProperty(PluginSettings.PLUGIN_ALIASES))
        commandManager.commandReplacements.addReplacement("syntax", plugin.settingsHandler.mainConf.getProperty(PluginSettings.SYNTAX_NAME))

        loadCommands()
    }

    private fun loadLang() {
        plugin.dataFolder.resolve("languages").listFiles()?.filter()
        {
            it.extension.equals("yml", true)
        }?.forEach()
        {
            val locale = Locale.forLanguageTag(it.nameWithoutExtension)

            commandManager.addSupportedLanguage(locale)
            commandManager.locales.loadYamlLanguageFile(it, locale)
            languages.add(it.nameWithoutExtension)
        }
        commandManager.locales.defaultLocale = Locale.forLanguageTag(plugin.settingsHandler.mainConf.getProperty(PluginSettings.MESSAGES_LANGUAGE))
    }

    private fun loadContexts(guildHandler: GuildHandler, arenaHandler: ArenaHandler) {
        commandManager.commandContexts.registerIssuerAwareContext(Guild::class.java) { c ->
            val guild: Guild = (if (c.hasFlag("admin")) {
                guildHandler.getGuild(c.popFirstArg())
            } else {
                guildHandler.getGuild(c.player)
            })
                    ?: throw InvalidCommandArgument(Messages.ERROR__NO_GUILD)
            guild
        }
        commandManager.commandContexts.registerIssuerOnlyContext(GuildRole::class.java) { c ->
            val guild = guildHandler.getGuild(c.player) ?: return@registerIssuerOnlyContext null
            guildHandler.getGuildRole(guild.getMember(c.player.uniqueId).role.level)
        }
        commandManager.commandContexts.registerContext(Arena::class.java) { c -> arenaHandler.getArena(c.popFirstArg()).get() }
    }

    private fun loadCompletions(guildHandler: GuildHandler, arenaHandler: ArenaHandler) {
        commandManager.commandCompletions.registerCompletion("online") { Bukkit.getOnlinePlayers().map { it.name } }
        commandManager.commandCompletions.registerCompletion("invitedTo") { c -> guildHandler.getInvitedGuilds(c.player) }
        commandManager.commandCompletions.registerCompletion("joinableGuilds") { c -> guildHandler.getJoinableGuild(c.player) }
        commandManager.commandCompletions.registerCompletion("guilds") { guildHandler.guildNames }
        commandManager.commandCompletions.registerCompletion("arenas") { arenaHandler.getArenas().map { it.name } }
        commandManager.commandCompletions.registerAsyncCompletion("locations") { listOf("challenger", "defender") }
        commandManager.commandCompletions.registerCompletion("languages") { languages.sorted() }
        commandManager.commandCompletions.registerAsyncCompletion("sources") { listOf("JSON", "MYSQL", "SQLITE", "MARIADB") }

        commandManager.commandCompletions.registerCompletion("members") { c ->
            val guild = guildHandler.getGuild(c.player) ?: return@registerCompletion null
            guild.members.map { it.asOfflinePlayer.name }
        }
        commandManager.commandCompletions.registerCompletion("members-admin") { c ->
            val guild = c.getContextValue(Guild::class.java, 1) ?: return@registerCompletion null
            guild.members.map { it.asOfflinePlayer.name }
        }
        commandManager.commandCompletions.registerAsyncCompletion("allyInvites") { c ->
            val guild = guildHandler.getGuild(c.player) ?: return@registerAsyncCompletion null
            if (!guild.hasPendingAllies()) {
                return@registerAsyncCompletion null
            }
            guild.pendingAllies.map { guildHandler.getNameById(it) }
        }
        commandManager.commandCompletions.registerAsyncCompletion("allies") { c ->
            val guild = guildHandler.getGuild(c.player) ?: return@registerAsyncCompletion null
            if (!guild.hasAllies()) {
                return@registerAsyncCompletion null
            }
            guild.allies.map { guildHandler.getNameById(it) }
        }
        commandManager.commandCompletions.registerAsyncCompletion("activeCodes") { c ->
            val guild = guildHandler.getGuild(c.player) ?: return@registerAsyncCompletion null
            if (guild.codes == null) {
                return@registerAsyncCompletion null
            }
            guild.codes.map { it.id }
        }
        commandManager.commandCompletions.registerAsyncCompletion("vaultAmount") { c ->
            val guild = guildHandler.getGuild(c.player) ?: return@registerAsyncCompletion null
            if (guild.vaults == null) {
                return@registerAsyncCompletion null
            }
            val list = guildHandler.cachedVaults[guild] ?: return@registerAsyncCompletion null
            (1 until list.size).map(Any::toString)
        }
    }


    private fun loadCommands() {
        ZISScanner().getClasses(Guilds::class.java, "me.glaremasters.guilds.commands").asSequence()
                .filter { BaseCommand::class.java.isAssignableFrom(it) }
                .forEach { commandManager.registerCommand(it.newInstance() as BaseCommand) }
    }

    private fun loadDI() {
        commandManager.registerDependency(GuildHandler::class.java, plugin.guildHandler)
        commandManager.registerDependency(SettingsManager::class.java, plugin.settingsHandler.mainConf)
        commandManager.registerDependency(ActionHandler::class.java, plugin.actionHandler)
        commandManager.registerDependency(Economy::class.java, plugin.economy)
        commandManager.registerDependency(Permission::class.java, plugin.permissions)
        commandManager.registerDependency(CooldownHandler::class.java, plugin.cooldownHandler)
        commandManager.registerDependency(ArenaHandler::class.java, plugin.arenaHandler)
        commandManager.registerDependency(ChallengeHandler::class.java, plugin.challengeHandler)
        commandManager.registerDependency(DatabaseAdapter::class.java, plugin.database)
    }
}
