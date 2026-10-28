package com.neoclar.gptrustgroups.storage;

import com.neoclar.gptrustgroups.util.GroupIdMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static com.neoclar.gptrustgroups.TestSupport.*;
import static com.neoclar.gptrustgroups.storage.GroupManager.AccessResult.*;
import static org.junit.jupiter.api.Assertions.*;

class GroupManagerTest {
    @TempDir
    Path dir;

    private final UUID owner = player(0);
    private final UUID alice = player(1);
    private final UUID bob = player(2);
    private GroupManager manager;

    @BeforeEach
    void setUp() {
        manager = manager(dir);
    }

    // ---------- create ----------

    @Test
    void createValidatesName() {
        assertEquals(GroupManager.CreateResult.INVALID_NAME, manager.checkCreate("x", owner));
        assertEquals(GroupManager.CreateResult.INVALID_NAME, manager.checkCreate("bad name", owner));
        assertEquals(GroupManager.CreateResult.RESERVED_NAME, manager.checkCreate("public", owner));
        assertEquals(GroupManager.CreateResult.OK, manager.checkCreate("Friends", owner));
    }

    @Test
    void createIsCaseInsensitiveAndPerOwner() {
        assertNotNull(manager.create("Friends", owner));
        assertEquals(GroupManager.CreateResult.NAME_TAKEN, manager.checkCreate("FRIENDS", owner));
        assertNull(manager.create("friends", owner));
        assertNotNull(manager.create("friends", alice));
        assertNotSame(manager.get(owner, "friends"), manager.get(alice, "friends"));
    }

    @Test
    void publicGroupAlwaysExists() {
        Group pub = manager.get(owner, "PUBLIC");
        assertNotNull(pub);
        assertTrue(pub.isPublic());
        assertEquals(GroupManager.PUBLIC_ID, pub.getId());
        assertSame(pub, GroupIdMapper.groupFor(GroupManager.PUBLIC_ID));
    }

    // ---------- access ----------

    @Test
    void memberIsAllowedOthersAreNot() {
        Group g = manager.create("friends", owner);
        manager.addMember(g, alice);
        assertEquals(ALLOWED, manager.getAccessStatus(g, alice));
        assertEquals(NONE, manager.getAccessStatus(g, bob));
    }

    @Test
    void publicAllowsEveryone() {
        assertEquals(ALLOWED, manager.getAccessStatus(manager.getPublicGroup(), bob));
    }

    @Test
    void excludedPlayerDeniedEvenIfMember() {
        Group g = manager.create("friends", owner);
        manager.addMember(g, alice);
        manager.excludePlayer(g, alice);
        assertEquals(DENIED, manager.getAccessStatus(g, alice));
    }

    @Test
    void ownerCannotBeExcluded() {
        Group g = manager.create("friends", owner);
        assertFalse(manager.excludePlayer(g, owner));
    }

    @Test
    void nestedGroupGivesAccess() {
        Group parent = manager.create("builders", owner);
        Group child = manager.create("friends", owner);
        manager.addMember(child, alice);
        assertNull(manager.addNested(parent, "friends"));
        assertEquals(ALLOWED, manager.getAccessStatus(parent, alice));
        assertEquals(NONE, manager.getAccessStatus(parent, bob));
    }

    @Test
    void publicNestedWithExclusion() {
        Group g = manager.create("everyone", owner);
        assertNull(manager.addNested(g, "public"));
        manager.excludePlayer(g, bob);
        assertEquals(ALLOWED, manager.getAccessStatus(g, alice));
        assertEquals(DENIED, manager.getAccessStatus(g, bob));
    }

    @Test
    void exclusionInChildBlocksParentUnlessDirectMember() {
        Group parent = manager.create("builders", owner);
        Group child = manager.create("friends", owner);
        manager.addNested(child, "public");
        manager.excludePlayer(child, alice);
        manager.addNested(parent, "friends");

        assertEquals(DENIED, manager.getAccessStatus(parent, alice));
        manager.addMember(parent, alice);
        assertEquals(ALLOWED, manager.getAccessStatus(parent, alice));
    }

    @Test
    void excludedGroupDeniesItsMembers() {
        Group g = manager.create("everyone", owner);
        Group griefers = manager.create("griefers", owner);
        manager.addNested(g, "public");
        manager.addMember(griefers, bob);
        assertTrue(manager.excludeGroup(g, "griefers"));

        assertEquals(ALLOWED, manager.getAccessStatus(g, alice));
        assertEquals(DENIED, manager.getAccessStatus(g, bob));
    }

    @Test
    void directMemberOverridesExcludedGroup() {
        Group g = manager.create("everyone", owner);
        Group griefers = manager.create("griefers", owner);
        manager.addMember(griefers, bob);
        manager.excludeGroup(g, "griefers");
        manager.addMember(g, bob);
        assertEquals(ALLOWED, manager.getAccessStatus(g, bob));
    }

    @Test
    void mutuallyExcludedGroupsDoNotLoop() {
        Group a = manager.create("aa", owner);
        Group b = manager.create("bb", owner);
        manager.addMember(b, alice);
        manager.excludeGroup(a, "bb");
        manager.excludeGroup(b, "aa");
        assertEquals(DENIED, manager.getAccessStatus(a, alice));
        assertEquals(ALLOWED, manager.getAccessStatus(b, alice));
    }

    @Test
    void excludeUnknownOrSelfGroupFails() {
        Group g = manager.create("friends", owner);
        assertFalse(manager.excludeGroup(g, "nope"));
        assertFalse(manager.excludeGroup(g, "friends"));
    }

    // ---------- nesting ----------

    @Test
    void nestingErrors() {
        Group a = manager.create("aa", owner);
        Group b = manager.create("bb", owner);
        assertEquals("self", manager.addNested(a, "aa"));
        assertEquals("notFound", manager.addNested(a, "nope"));
        assertNull(manager.addNested(a, "bb"));
        assertEquals("already", manager.addNested(a, "BB"));
        assertEquals("cycle", manager.addNested(b, "aa"));
    }

    @Test
    void cannotNestOtherOwnersGroup() {
        Group mine = manager.create("mine", owner);
        manager.create("theirs", alice);
        assertEquals("notFound", manager.addNested(mine, "theirs"));
    }

    @Test
    void nestingDepthIsLimited() {
        for (int i = 0; i <= GroupManager.MAX_NESTING_HEIGHT + 1; i++) manager.create("c" + i, owner);
        for (int i = 1; i <= GroupManager.MAX_NESTING_HEIGHT; i++) {
            assertNull(manager.addNested(manager.get(owner, "c" + i), "c" + (i - 1)), "level " + i);
        }
        int next = GroupManager.MAX_NESTING_HEIGHT + 1;
        assertEquals("tooDeep", manager.addNested(manager.get(owner, "c" + next), "c" + (next - 1)));
    }

    // ---------- delete ----------

    @Test
    void deleteCleansReferences() {
        Group parent = manager.create("builders", owner);
        Group child = manager.create("friends", owner);
        manager.addNested(parent, "friends");
        manager.excludeGroup(parent, "friends");

        assertTrue(manager.delete(owner, "friends"));
        assertNull(manager.get(owner, "friends"));
        assertNull(GroupIdMapper.groupFor(child.getId()));
        assertTrue(parent.getNestedGroups().isEmpty());
        assertTrue(parent.getExcludedGroups().isEmpty());
    }

    @Test
    void publicCannotBeDeleted() {
        assertFalse(manager.delete(GroupManager.PUBLIC_OWNER, "public"));
        assertNotNull(manager.getPublicGroup());
    }

    // ---------- storage ----------

    @Test
    void saveAndLoadRoundTrip() {
        Group parent = manager.create("builders", owner);
        Group child = manager.create("friends", owner);
        manager.addMember(child, alice);
        manager.addNested(parent, "friends");
        manager.excludePlayer(parent, bob);

        GroupManager reloaded = manager(dir);
        Group loaded = reloaded.get(owner, "builders");
        assertEquals(parent.getId(), loaded.getId());
        assertEquals(parent.getNestedGroups(), loaded.getNestedGroups());
        assertEquals(parent.getExcludedPlayers(), loaded.getExcludedPlayers());
        assertEquals(ALLOWED, reloaded.getAccessStatus(loaded, alice));
        assertSame(loaded, GroupIdMapper.groupFor(parent.getId()));
    }

    @Test
    void loadSkipsPublicNameOfRegularOwner() throws Exception {
        Files.writeString(groupsFile(dir).toPath(), """
                groups:
                  "%s":
                    public:
                      id: "%s"
                      members: ["%s"]
                """.formatted(owner, UUID.randomUUID(), alice));

        GroupManager loaded = manager(dir);
        assertTrue(loaded.getAllFor(owner).isEmpty());
        assertTrue(loaded.getPublicGroup().isPublic());
        assertTrue(loaded.getPublicGroup().getMembers().isEmpty());
    }

    @Test
    void missingIdIsGeneratedAndPersisted() throws Exception {
        Files.writeString(groupsFile(dir).toPath(), """
                groups:
                  "%s":
                    friends:
                      members: ["%s", "not-a-uuid"]
                """.formatted(owner, alice));

        Group g = manager(dir).get(owner, "friends");
        assertNotNull(g.getId());
        assertEquals(1, g.getMembers().size());
        assertEquals(g.getId(), manager(dir).get(owner, "friends").getId());
    }
}
