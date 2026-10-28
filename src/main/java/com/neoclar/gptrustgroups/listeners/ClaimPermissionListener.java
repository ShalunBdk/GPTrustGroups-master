package com.neoclar.gptrustgroups.listeners;

import com.neoclar.gptrustgroups.GPTrustGroups;
import com.neoclar.gptrustgroups.storage.Group;
import com.neoclar.gptrustgroups.util.GroupIdMapper;
import com.neoclar.gptrustgroups.storage.GroupManager;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.events.ClaimPermissionCheckEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClaimPermissionListener implements Listener {

    private final GroupManager groupManager;

    public ClaimPermissionListener(GPTrustGroups plugin) {
        this.groupManager = plugin.getGroupManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClaimPermissionCheck(ClaimPermissionCheckEvent event) {
        if (event.getDenialReason() == null) return;

        Claim claim = event.getClaim();
        UUID playerId = event.getCheckedUUID();
        if (claim == null) return;

        ClaimPermission required = event.getRequiredPermission();
        if (required == null) return;

        // GP запрещает строить в PvP-бою даже доверенным — не перебиваем этот запрет
        if (required == ClaimPermission.Build
                && GriefPrevention.instance.dataStore.getPlayerData(playerId).inPvpCombat()) return;

        // GP наследует права сабклейма от родителя (если не включено ограничение) — делаем так же
        boolean anyDenied = false;
        boolean anyAllowed = false;
        for (Claim c = claim; c != null; c = c.getSubclaimRestrictions() ? null : c.parent) {
            for (String entry : entriesGranting(c, required)) {
                Group g = GroupIdMapper.groupFor(tryParseUuid(entry));
                if (g == null) continue;

                GroupManager.AccessResult r = groupManager.getAccessStatus(g, playerId);
                if (r == GroupManager.AccessResult.DENIED) anyDenied = true;
                else if (r == GroupManager.AccessResult.ALLOWED) anyAllowed = true;
            }
        }

        // Явное исключение перебивает доступ через другие группы — оставляем запрет GP
        if (!anyDenied && anyAllowed) event.setDenialReason(null);
    }

    /** Записи клейма, дающие требуемый уровень (как ClaimPermission#isGrantedBy в GP). */
    private static List<String> entriesGranting(Claim claim, ClaimPermission required) {
        ArrayList<String> builders = new ArrayList<>();
        ArrayList<String> containers = new ArrayList<>();
        ArrayList<String> accessors = new ArrayList<>();
        ArrayList<String> managers = new ArrayList<>();
        claim.getPermissions(builders, containers, accessors, managers);

        List<String> result = new ArrayList<>();
        switch (required) {
            case Access -> {
                result.addAll(accessors);
                result.addAll(containers);
                result.addAll(builders);
            }
            case Inventory -> {
                result.addAll(containers);
                result.addAll(builders);
            }
            case Build -> result.addAll(builders);
            case Manage -> result.addAll(managers);
            default -> { }
        }
        return result;
    }

    private static UUID tryParseUuid(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return UUID.fromString(s); }
        catch (IllegalArgumentException e) { return null; }
    }
}
