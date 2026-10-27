package com.neoclar.gptrustgroups.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.neoclar.gptrustgroups.storage.Group;

public final class GroupIdMapper {

    private static final Map<UUID, Group> ID_TO_GROUP = new ConcurrentHashMap<>();

    private GroupIdMapper() {}

    public static void register(Group group) { ID_TO_GROUP.put(group.getId(), group); }
    public static void unregister(UUID id) { if (id != null) ID_TO_GROUP.remove(id); }
    public static void clear() { ID_TO_GROUP.clear(); }
    public static Group groupFor(UUID id) { return id == null ? null : ID_TO_GROUP.get(id); }
}