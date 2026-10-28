package com.neoclar.gptrustgroups.listeners;

import me.ryanhamshire.GriefPrevention.ClaimPermission;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrustCommandInterceptorTest {

    @Test
    void stripsNamespaceAndCase() {
        assertEquals("trust", TrustCommandInterceptor.normalizeCommand("Trust"));
        assertEquals("trust", TrustCommandInterceptor.normalizeCommand("GriefPrevention:trust"));
        assertEquals("otherplugin:trust", TrustCommandInterceptor.normalizeCommand("otherplugin:trust"));
    }

    @Test
    void trustCommandsAndAliases() {
        assertEquals(ClaimPermission.Build, TrustCommandInterceptor.findTrustCommand("tr").permission());
        assertEquals(ClaimPermission.Inventory, TrustCommandInterceptor.findTrustCommand("ct").permission());
        assertEquals(ClaimPermission.Access, TrustCommandInterceptor.findTrustCommand("accesstrust").permission());
        assertEquals(ClaimPermission.Manage, TrustCommandInterceptor.findTrustCommand("managetrust").permission());
        assertNull(TrustCommandInterceptor.findTrustCommand("untrust"));
        assertNull(TrustCommandInterceptor.findTrustCommand("trustlist"));
    }

    @Test
    void untrustAliases() {
        assertTrue(TrustCommandInterceptor.isUntrust("untrust"));
        assertTrue(TrustCommandInterceptor.isUntrust("ut"));
        assertFalse(TrustCommandInterceptor.isUntrust("trust"));
    }
}
