package me.leoko.advancedban;

import me.leoko.advancedban.manager.AltManager;
import me.leoko.advancedban.manager.MessageManager;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.TimeManager;
import me.leoko.advancedban.manager.UUIDManager;
import me.leoko.advancedban.utils.DetectedAlt;
import me.leoko.advancedban.utils.DetectionKind;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.PunishmentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AltManagerTest {

    @TempDir
    public static File dataFolder;

    @BeforeAll
    public static void setupUniversal() {
        Universal.get().setup(new TestMethods(dataFolder));
    }

    @Test
    public void shouldRecordAndDetectAltsAccurately() {
        String uuid1 = UUIDManager.get().getUUID("Player1");
        String uuid2 = UUIDManager.get().getUUID("Player2");
        String uuid3 = UUIDManager.get().getUUID("Player3");

        // Player 1 connects with IP 192.168.1.100
        Universal.get().callConnection("Player1", "192.168.1.100");
        // Player 2 connects with the same IP (192.168.1.100) -> Direct match (NORMAL)
        Universal.get().callConnection("Player2", "192.168.1.100");
        // Player 3 connects with IP 192.168.1.200
        Universal.get().callConnection("Player3", "192.168.1.200");
        // Player 1 connects from IP 192.168.1.200 later -> links Player3 to Player1
        Universal.get().callConnection("Player1", "192.168.1.200");

        // Ban Player2
        Punishment ban = new Punishment("player2", uuid2, "Hacking", "Admin", PunishmentType.BAN, TimeManager.getTime(), -1, null, -1);
        ban.create();
        PunishmentManager.get().discard("player2");

        // Detect alts for Player1
        List<DetectedAlt> alts = AltManager.get().detectAlts(uuid1, "Player1", "192.168.1.200");
        assertFalse(alts.isEmpty(), "Should detect alts for Player1");

        // Player3 was seen on 192.168.1.200 (Player1's current IP) -> NORMAL
        // Player2 was seen on 192.168.1.100 (Player1's historical IP) -> STRICT
        DetectedAlt altPlayer2 = alts.stream().filter(a -> a.getName().equalsIgnoreCase("Player2")).findFirst().orElse(null);
        assertNotNull(altPlayer2, "Player2 should be detected as an alt");
        assertTrue(altPlayer2.isBanned(), "Player2 should be recognized as banned");
        assertEquals(DetectionKind.STRICT, altPlayer2.getDetectionKind(), "Player2 should be STRICT possibility");

        DetectedAlt altPlayer3 = alts.stream().filter(a -> a.getName().equalsIgnoreCase("Player3")).findFirst().orElse(null);
        assertNotNull(altPlayer3, "Player3 should be detected as an alt");
        assertFalse(altPlayer3.isBanned(), "Player3 should not be banned");
        assertEquals(DetectionKind.NORMAL, altPlayer3.getDetectionKind(), "Player3 should be NORMAL possibility");

        // Test formatUserName
        String p2Formatted = AltManager.formatUserName(altPlayer2);
        assertNotNull(p2Formatted);

        // Test formatDetectionKind
        String normalDesc = AltManager.formatDetectionKind(DetectionKind.NORMAL);
        String strictDesc = AltManager.formatDetectionKind(DetectionKind.STRICT);
        assertNotNull(normalDesc);
        assertNotNull(strictDesc);
    }

    @Test
    public void shouldDistinguishBannedAndMutedAlts() {
        String uuid4 = UUIDManager.get().getUUID("Player4");
        String uuid5 = UUIDManager.get().getUUID("Player5");
        String uuid6 = UUIDManager.get().getUUID("Player6");

        // Connect Player4, Player5, and Player6 on same IP
        Universal.get().callConnection("Player4", "192.168.1.300");
        Universal.get().callConnection("Player5", "192.168.1.300");
        Universal.get().callConnection("Player6", "192.168.1.300");

        // Mute Player5 for 30 minutes (TEMP_MUTE)
        long endMute = TimeManager.getTime() + 1800000L;
        Punishment mute = new Punishment("player5", uuid5, "Spam", "Admin", PunishmentType.TEMP_MUTE, TimeManager.getTime(), endMute, null, -1);
        mute.create();
        PunishmentManager.get().discard("player5");

        // Ban Player6 for 30 minutes (TEMP_BAN)
        long endBan = TimeManager.getTime() + 1800000L;
        Punishment tempBan = new Punishment("player6", uuid6, "Hacking", "Admin", PunishmentType.TEMP_BAN, TimeManager.getTime(), endBan, null, -1);
        tempBan.create();
        PunishmentManager.get().discard("player6");

        // Detect alts for Player4
        List<DetectedAlt> alts = AltManager.get().detectAlts(uuid4, "Player4", "192.168.1.300");
        assertFalse(alts.isEmpty(), "Should detect alts for Player4");

        // Verify Player5 (tempmuted for 30 mins)
        DetectedAlt altPlayer5 = alts.stream().filter(a -> a.getName().equalsIgnoreCase("Player5")).findFirst().orElse(null);
        assertNotNull(altPlayer5, "Player5 should be detected as an alt");
        assertTrue(altPlayer5.hasActivePunishment(), "Player5 should have an active punishment");
        assertFalse(altPlayer5.isBanned(), "Player5 is muted for 30m, should NOT be marked as banned");
        assertTrue(altPlayer5.isMuted(), "Player5 is muted for 30m, should be marked as muted");

        String p5Formatted = AltManager.formatUserName(altPlayer5);
        String expectedMutedDisplay = MessageManager.getMessage("Alts.Formatting.NameDisplay.Muted", "USERNAME", altPlayer5.getName());
        assertEquals(expectedMutedDisplay, p5Formatted, "Player5 username should be formatted as Muted, not Banned");

        // Verify Player6 (tempbanned for 30 mins)
        DetectedAlt altPlayer6 = alts.stream().filter(a -> a.getName().equalsIgnoreCase("Player6")).findFirst().orElse(null);
        assertNotNull(altPlayer6, "Player6 should be detected as an alt");
        assertTrue(altPlayer6.hasActivePunishment(), "Player6 should have an active punishment");
        assertTrue(altPlayer6.isBanned(), "Player6 is banned for 30m, should be marked as banned");
        assertFalse(altPlayer6.isMuted(), "Player6 is banned for 30m, should NOT be marked as muted");

        String p6Formatted = AltManager.formatUserName(altPlayer6);
        String expectedBannedDisplay = MessageManager.getMessage("Alts.Formatting.NameDisplay.Banned", "USERNAME", altPlayer6.getName());
        assertEquals(expectedBannedDisplay, p6Formatted, "Player6 username should be formatted as Banned");
    }

    @AfterAll
    public static void shutdownUniversal() {
        Universal.get().shutdown();
    }
}
