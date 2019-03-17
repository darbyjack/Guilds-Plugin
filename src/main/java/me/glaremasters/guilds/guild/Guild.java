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

package me.glaremasters.guilds.guild;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class Guild {

    public enum Status {
        Public("Public"),
        Private("Private");

        Status(String s) {
        }
    }

    private final UUID id;
    private String name, prefix;
    private GuildMember guildMaster;

    private GuildHome home = null;
    // Guild Vault
    private GuildSkull guildSkull;
    private Status status;
    private GuildTier tier;
    private double balance = 0;

    private List<GuildMember> members = new ArrayList<>();
    private List<UUID> invitedMembers = new ArrayList<>();

    private List<UUID> allies = new ArrayList<>();
    private List<UUID> pendingAllies = new ArrayList<>();
    private List<GuildCode> codes = new ArrayList<>();

    /**
     * Get a member in the guild
     * @param uuid the uuid of the member
     * @return the member which was found
     */
    public GuildMember getMember(UUID uuid) {
        return getMembers().stream().filter(m -> m.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    /**
     * Get the itemstack of the guild skull
     * @return itemstack of skull
     */
    public ItemStack getSkull() {
        return guildSkull.getSkull(guildSkull.getSerialized());
    }

    /**
     * Add a member using their GuildMember object
     *
     * @param guildMember the member to add
     */
    public void addMember(GuildMember guildMember){
        if (getMembers().contains(guildMember)) return;
        removeInvitedMember(guildMember.getUuid());
        getMembers().add(guildMember);
    }

    /**
     * Remove a member by their GuildMember object
     * @param guildMember the guildmember to remove
     */
    public void removeMember(GuildMember guildMember){
        getMembers().remove(guildMember);
    }

    /**
     * Remove a member using it's OfflinePlayer object
     * @param player the OfflinePlayer to remove
     */
    public void removeMember(OfflinePlayer player){
        removeMember(getMember(player.getUniqueId()));
        //todo remove guild perms
    }

    /**
     * Removes an ally's id from the list
     * @param guild the guild to remove
     */
    public void removeAlly(Guild guild) {
        getAllies().remove(guild.getId());
    }

    /**
     * Add an ally's id to the list
     * @param guild the guild to add
     */
    public void addAlly(Guild guild) {
        getAllies().add(guild.getId());
    }

    /**
     * Adds a pending ally's id to the list
     * @param guild the guild to add
     */
    public void addPendingAlly(Guild guild) {
        getPendingAllies().add(guild.getId());
    }

    /**
     * Removes a pending ally's id from the list
     * @param guild the guild to remove
     */
    public void removePendingAlly(Guild guild) {
        getPendingAllies().remove(guild.getId());
    }

    /**
     * Invites a member to this guild.
     *
     * @param uuid the UUID of the player.
     */
    public void inviteMember(UUID uuid) {
        if (getInvitedMembers().contains(uuid)) return;
        getInvitedMembers().add(uuid);
    }

    /**
     * Removes an invited member
     *
     * @param uuid the member to remove from the invites.
     */
    public void removeInvitedMember(UUID uuid) {
        getInvitedMembers().remove(uuid);
    }

    /**
     * Get the amount of members
     * @return size of members list.
     */
    public int getSize() {
        return getMembers().size();
    }

    /**
     * Returns amount of online members
     *
     * @return a List of online members
     */
    public List<GuildMember> getOnlineMembers() {
        return getMembers().stream().filter(GuildMember::isOnline).collect(Collectors.toList());
    }
}

