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
import co.aikar.commands.BukkitCommandCompletionContext
import co.aikar.commands.BukkitCommandExecutionContext
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import com.google.common.reflect.ClassPath
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
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import java.util.Locale

class ACFHandler(private val plugin: Guilds, private val commandManager: PaperCommandManager) {

    fun load() {
        commandManager.usePerIssuerLocale(true, false)
        commandManager.enableUnstableAPI("help")

        loadLang()
        loadContexts(plugin.guildHandler, plugin.arenaHandler)
        loadCompletions(plugin.guildHandler, plugin.arenaHandler)
        loadDI()

        commandManager.commandReplacements.addReplacement("guilds", plugin.settingsHandler.settingsManager.getProperty(PluginSettings.PLUGIN_ALIASES))
        commandManager.commandReplacements.addReplacement("syntax", plugin.settingsHandler.settingsManager.getProperty(PluginSettings.SYNTAX_NAME))

        loadCommands()
    }

    fun loadLang() {
        plugin.dataFolder.resolve("languages").listFiles()?.filter()
        {
            it.extension.equals("yml", true)
        }?.forEach()
        {
            val locale = Locale.forLanguageTag(it.nameWithoutExtension)

            commandManager.addSupportedLanguage(locale)
            commandManager.locales.loadYamlLanguageFile(it, locale)
        }
        commandManager.locales.defaultLocale = Locale.forLanguageTag(plugin.settingsHandler.settingsManager.getProperty(PluginSettings.MESSAGES_LANGUAGE))
    }

    private fun loadContexts(guildHandler: GuildHandler, arenaHandler: ArenaHandler) {
        commandManager.commandContexts.registerIssuerAwareContext(Guild::class.java) { c: BukkitCommandExecutionContext ->
            val guild: Guild = (if (c.hasFlag("admin")) {
                guildHandler.getGuild(c.popFirstArg())
            } else {
                guildHandler.getGuild(c.player)
            })
                    ?: throw InvalidCommandArgument(Messages.ERROR__NO_GUILD)
            guild
        }
        commandManager.commandContexts.registerIssuerOnlyContext(GuildRole::class.java) { c: BukkitCommandExecutionContext ->
            val guild = guildHandler.getGuild(c.player) ?: return@registerIssuerOnlyContext null
            guildHandler.getGuildRole(guild.getMember(c.player.uniqueId).role.level)
        }
        commandManager.commandContexts.registerContext(Arena::class.java) { c: BukkitCommandExecutionContext -> arenaHandler.getArena(c.popFirstArg()).get() }
    }

    private fun loadCompletions(guildHandler: GuildHandler, arenaHandler: ArenaHandler) {
        commandManager.commandCompletions.registerCompletion("online") { Bukkit.getOnlinePlayers().map { it.name } }
        commandManager.commandCompletions.registerCompletion("invitedTo") { c: BukkitCommandCompletionContext -> guildHandler.getInvitedGuilds(c.player) }
        commandManager.commandCompletions.registerCompletion("joinableGuilds") { c: BukkitCommandCompletionContext -> guildHandler.getJoinableGuild(c.player) }
        commandManager.commandCompletions.registerCompletion("guilds") { guildHandler.guildNames }
        commandManager.commandCompletions.registerCompletion("arenas") { arenaHandler.getArenas().map { it.name } }
        commandManager.commandCompletions.registerCompletion("locations") { listOf("challenger", "defender") }
        commandManager.commandCompletions.registerCompletion("languages") { plugin.loadedLanguages.sorted() }
        commandManager.commandCompletions.registerCompletion("sources") { listOf("JSON", "MYSQL", "SQLITE", "MARIADB") }

        commandManager.commandCompletions.registerCompletion("members") { c: BukkitCommandCompletionContext ->
            val guild = guildHandler.getGuild(c.player) ?: return@registerCompletion null
            guild.members.map { it.asOfflinePlayer.name }
        }
        commandManager.commandCompletions.registerCompletion("members-admin") { c: BukkitCommandCompletionContext ->
            val guild = c.getContextValue(Guild::class.java, 1) ?: return@registerCompletion null
            guild.members.map { it.asOfflinePlayer.name }
        }
        commandManager.commandCompletions.registerAsyncCompletion("allyInvites") { c: BukkitCommandCompletionContext ->
            val guild = guildHandler.getGuild(c.player) ?: return@registerAsyncCompletion null
            if (!guild.hasPendingAllies()) {
                return@registerAsyncCompletion null
            }
            guild.pendingAllies.map { guildHandler.getNameById(it) }
        }
        commandManager.commandCompletions.registerAsyncCompletion("allies") { c: BukkitCommandCompletionContext ->
            val guild = guildHandler.getGuild(c.player) ?: return@registerAsyncCompletion null
            if (!guild.hasAllies()) {
                return@registerAsyncCompletion null
            }
            guild.allies.map { guildHandler.getNameById(it) }
        }
        commandManager.commandCompletions.registerAsyncCompletion("activeCodes") { c: BukkitCommandCompletionContext ->
            val guild = guildHandler.getGuild(c.player) ?: return@registerAsyncCompletion null
            if (guild.codes == null) {
                return@registerAsyncCompletion null
            }
            guild.codes.map { it.id }
        }
        commandManager.commandCompletions.registerAsyncCompletion("vaultAmount") { c: BukkitCommandCompletionContext ->
            val guild = guildHandler.getGuild(c.player) ?: return@registerAsyncCompletion null
            if (guild.vaults == null) {
                return@registerAsyncCompletion null
            }
            val list = guildHandler.cachedVaults[guild] ?: return@registerAsyncCompletion null
            (1 until list.size).map(Any::toString)
        }
    }


    private fun loadCommands() {
        val classes = ClassPath.from(this.javaClass.classLoader).getTopLevelClassesRecursive("me.glaremasters.guilds.commands")
        classes.map(ClassPath.ClassInfo::load)
                .filter(BaseCommand::class.java::isAssignableFrom)
                .map(Class<*>::newInstance)
                .filterIsInstance<BaseCommand>()
                .forEach(commandManager::registerCommand)
    }

    private fun loadDI() {
        commandManager.registerDependency(GuildHandler::class.java, plugin.guildHandler)
        commandManager.registerDependency(SettingsManager::class.java, plugin.settingsHandler.settingsManager)
        commandManager.registerDependency(ActionHandler::class.java, plugin.actionHandler)
        commandManager.registerDependency(Economy::class.java, plugin.economy)
        commandManager.registerDependency(Permission::class.java, plugin.permissions)
        commandManager.registerDependency(CooldownHandler::class.java, plugin.cooldownHandler)
        commandManager.registerDependency(ArenaHandler::class.java, plugin.arenaHandler)
        commandManager.registerDependency(ChallengeHandler::class.java, plugin.challengeHandler)
        commandManager.registerDependency(DatabaseAdapter::class.java, plugin.database)
    }
}
