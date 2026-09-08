package me.leoko.advancedban;

import me.leoko.advancedban.manager.AltManager;
import me.leoko.advancedban.manager.CommandManager;
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
import java.util.UUID;

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

    @AfterAll
    public static void shutdownUniversal() {
        Universal.get().shutdown();
    }
}
