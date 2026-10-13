package me.leoko.advancedban;

import me.leoko.advancedban.manager.DisguiseManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DisguiseManagerTest {

    private DisguiseManager disguiseManager;

    @BeforeEach
    public void setup() {
        disguiseManager = DisguiseManager.get();
        disguiseManager.setDisguised("AdminUser", false);
    }

    @Test
    public void testToggleDisguise() {
        assertFalse(disguiseManager.isDisguised("AdminUser"));
        assertTrue(disguiseManager.toggle("AdminUser"));
        assertTrue(disguiseManager.isDisguised("AdminUser"));
        assertFalse(disguiseManager.toggle("AdminUser"));
        assertFalse(disguiseManager.isDisguised("AdminUser"));
    }

    @Test
    public void testCaseInsensitive() {
        disguiseManager.setDisguised("darkignite", true);
        assertTrue(disguiseManager.isDisguised("DarkIgnite"));
        assertTrue(disguiseManager.isDisguised("DARKIGNITE"));
        disguiseManager.setDisguised("darkignite", false);
    }

    @Test
    public void testGetDisplayOperator() {
        disguiseManager.setDisguised("DarkIgnite", true);
        assertEquals("CONSOLE", disguiseManager.getDisplayOperator("DarkIgnite"));

        disguiseManager.setDisguised("DarkIgnite", false);
        assertEquals("DarkIgnite", disguiseManager.getDisplayOperator("DarkIgnite"));
    }

    @Test
    public void testNullSafety() {
        assertFalse(disguiseManager.isDisguised(null));
        assertNull(disguiseManager.getDisplayOperator(null));
        assertFalse(disguiseManager.toggle(null));
    }
}
