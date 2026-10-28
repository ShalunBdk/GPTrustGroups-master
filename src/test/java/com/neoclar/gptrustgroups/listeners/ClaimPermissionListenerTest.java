package com.neoclar.gptrustgroups.listeners;

import me.ryanhamshire.GriefPrevention.ClaimPermission;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClaimPermissionListenerTest {
    private static final List<String> BUILDERS = List.of("b");
    private static final List<String> CONTAINERS = List.of("c");
    private static final List<String> ACCESSORS = List.of("a");
    private static final List<String> MANAGERS = List.of("m");

    private static List<String> granting(ClaimPermission p) {
        return ClaimPermissionListener.entriesGranting(p, BUILDERS, CONTAINERS, ACCESSORS, MANAGERS);
    }

    @Test
    void higherTrustImpliesLower() {
        assertEquals(List.of("a", "c", "b"), granting(ClaimPermission.Access));
        assertEquals(List.of("c", "b"), granting(ClaimPermission.Inventory));
        assertEquals(List.of("b"), granting(ClaimPermission.Build));
    }

    @Test
    void manageIsSeparateFromBuild() {
        // Как в GP: менеджер не получает права строить, и наоборот
        assertEquals(List.of("m"), granting(ClaimPermission.Manage));
        assertFalse(granting(ClaimPermission.Build).contains("m"));
    }

    @Test
    void editIsNeverGrantedByGroups() {
        assertTrue(granting(ClaimPermission.Edit).isEmpty());
    }
}
