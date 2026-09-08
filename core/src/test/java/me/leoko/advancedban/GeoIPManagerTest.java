package me.leoko.advancedban;

import me.leoko.advancedban.manager.GeoIPManager;
import me.leoko.advancedban.utils.GeoIPResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GeoIPManagerTest {

    @Test
    public void testLocalAddressDetection() {
        Assertions.assertTrue(GeoIPManager.isLocalAddress("127.0.0.1"));
        Assertions.assertTrue(GeoIPManager.isLocalAddress("localhost"));
        Assertions.assertTrue(GeoIPManager.isLocalAddress("::1"));
        Assertions.assertTrue(GeoIPManager.isLocalAddress("192.168.1.1"));
        Assertions.assertTrue(GeoIPManager.isLocalAddress("10.0.0.5"));
        Assertions.assertTrue(GeoIPManager.isLocalAddress("172.16.0.1"));
        Assertions.assertTrue(GeoIPManager.isLocalAddress("172.31.255.255"));
        Assertions.assertFalse(GeoIPManager.isLocalAddress("8.8.8.8"));
        Assertions.assertFalse(GeoIPManager.isLocalAddress("180.252.160.1"));
    }

    @Test
    public void testLocalGeoIPResult() {
        GeoIPResult result = GeoIPManager.get().analyze("127.0.0.1");
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isLocal());
        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals("Localhost", result.getCity());
        Assertions.assertEquals(100, result.getAccuracy());
    }

    @Test
    public void testAccuracyCalculation() {
        int acc1 = GeoIPManager.calculateAccuracy("8.8.8.8", "Ashburn", "20149", "Virginia", "Google LLC");
        Assertions.assertTrue(acc1 >= 75 && acc1 <= 96, "Accuracy should be in realistic percentage range: " + acc1);

        int accNoPostal = GeoIPManager.calculateAccuracy("8.8.8.8", "Ashburn", null, "Virginia", "Google LLC");
        Assertions.assertTrue(accNoPostal < acc1, "Missing postal code should have slightly lower accuracy");

        int accUnknown = GeoIPManager.calculateAccuracy("8.8.8.8", "Unknown", null, null, null);
        Assertions.assertEquals(50, accUnknown);
    }
}
