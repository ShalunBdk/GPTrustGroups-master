package com.neoclar.gptrustgroups.storage;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Group {

    private final UUID id;
    private final String name;
    private final UUID owner;
    private final Set<UUID> members = new HashSet<>();
    private final Set<String> nestedGroups = new HashSet<>();
    private final Set<UUID> excludedPlayers = new HashSet<>();
    private final Set<String> excludedGroups = new HashSet<>();

    public Group(UUID id, String name, UUID owner) {
        this.id = id;
        this.name = name.toLowerCase();
        this.owner = owner;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public UUID getOwner() { return owner; }

    public Set<UUID> getMembers() { return Collections.unmodifiableSet(members); }
    public Set<String> getNestedGroups() { return Collections.unmodifiableSet(nestedGroups); }
    public Set<UUID> getExcludedPlayers() { return Collections.unmodifiableSet(excludedPlayers); }
    public Set<String> getExcludedGroups() { return Collections.unmodifiableSet(excludedGroups); }

    public boolean isPublic() {
        return GroupManager.PUBLIC_OWNER.equals(owner);
    }

    boolean addMember(UUID uuid) { return members.add(uuid); }
    boolean removeMember(UUID uuid) { return members.remove(uuid); }
    boolean addNested(String groupName) { return nestedGroups.add(groupName.toLowerCase()); }
    boolean removeNested(String groupName) { return nestedGroups.remove(groupName.toLowerCase()); }
    boolean addExcludedPlayer(UUID uuid) { return excludedPlayers.add(uuid); }
    boolean removeExcludedPlayer(UUID uuid) { return excludedPlayers.remove(uuid); }
    boolean addExcludedGroup(String groupName) { return excludedGroups.add(groupName.toLowerCase()); }
    boolean removeExcludedGroup(String groupName) { return excludedGroups.remove(groupName.toLowerCase()); }
}