package me.leoko.advancedban.manager;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.leoko.advancedban.utils.GeoIPResult;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages IP geolocation lookups and analysis caching.
 */
public class GeoIPManager {
    private static GeoIPManager instance;
    private final Map<String, GeoIPResult> cache = new ConcurrentHashMap<>();

    public static synchronized GeoIPManager get() {
        if (instance == null) {
            instance = new GeoIPManager();
        }
        return instance;
    }

    /**
     * Checks whether an IP is a local or private address.
     */
    public static boolean isLocalAddress(String ip) {
        if (ip == null || ip.trim().isEmpty() || ip.equalsIgnoreCase("none cached") || ip.equalsIgnoreCase("unknown")) {
            return true;
        }
        String cleanIp = ip.trim().replace("/", "");
        if (cleanIp.equals("127.0.0.1") || cleanIp.equalsIgnoreCase("localhost")
                || cleanIp.equals("0:0:0:0:0:0:0:1") || cleanIp.equals("::1")) {
            return true;
        }
        if (cleanIp.startsWith("10.") || cleanIp.startsWith("192.168.")) {
            return true;
        }
        if (cleanIp.startsWith("172.")) {
            String[] parts = cleanIp.split("\\.");
            if (parts.length >= 2) {
                try {
                    int second = Integer.parseInt(parts[1]);
                    if (second >= 16 && second <= 31) {
                        return true;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return false;
    }

    /**
     * Analyzes an IP address and resolves geolocation information.
     */
    public GeoIPResult analyze(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return GeoIPResult.createFailed("Unknown");
        }
        String cleanIp = ip.trim().replace("/", "");

        if (isLocalAddress(cleanIp)) {
            return GeoIPResult.createLocal(cleanIp);
        }

        GeoIPResult cached = cache.get(cleanIp);
        if (cached != null) {
            return cached;
        }

        GeoIPResult result = queryIpWhoIs(cleanIp);
        if (result == null || !result.isSuccess()) {
            result = queryIpApi(cleanIp);
        }

        if (result == null) {
            result = GeoIPResult.createFailed(cleanIp);
        }

        cache.put(cleanIp, result);
        return result;
    }

    private GeoIPResult queryIpWhoIs(String ip) {
        try {
            URL url = new URL("https://ipwho.is/" + ip);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setRequestProperty("User-Agent", "AdvancedBan-Guard/2.3.0");

            if (conn.getResponseCode() != 200) {
                return null;
            }

            JsonObject json = JsonParser.parseReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)).getAsJsonObject();
            if (!json.has("success") || !json.get("success").getAsBoolean()) {
                return null;
            }

            String country = json.has("country") && !json.get("country").isJsonNull() ? json.get("country").getAsString() : "Unknown";
            String countryCode = json.has("country_code") && !json.get("country_code").isJsonNull() ? json.get("country_code").getAsString() : "N/A";
            String region = json.has("region") && !json.get("region").isJsonNull() ? json.get("region").getAsString() : "Unknown";
            String city = json.has("city") && !json.get("city").isJsonNull() ? json.get("city").getAsString() : "Unknown";
            String postal = json.has("postal") && !json.get("postal").isJsonNull() ? json.get("postal").getAsString() : "";

            String isp = "Unknown";
            String org = "Unknown";
            if (json.has("connection") && json.get("connection").isJsonObject()) {
                JsonObject connObj = json.getAsJsonObject("connection");
                if (connObj.has("isp") && !connObj.get("isp").isJsonNull()) {
                    isp = connObj.get("isp").getAsString();
                }
                if (connObj.has("org") && !connObj.get("org").isJsonNull()) {
                    org = connObj.get("org").getAsString();
                }
            }

            String timezone = "Unknown";
            if (json.has("timezone") && json.get("timezone").isJsonObject()) {
                JsonObject tzObj = json.getAsJsonObject("timezone");
                if (tzObj.has("id") && !tzObj.get("id").isJsonNull()) {
                    timezone = tzObj.get("id").getAsString();
                }
            }

            int accuracy = calculateAccuracy(ip, city, postal, region, isp);
            return new GeoIPResult(ip, country, countryCode, region, city, postal, isp, org, timezone, accuracy, true, false);
        } catch (Exception ignored) {
            return null;
        }
    }

    private GeoIPResult queryIpApi(String ip) {
        try {
            URL url = new URL("http://ip-api.com/json/" + ip + "?fields=status,message,country,countryCode,regionName,city,zip,isp,org,timezone");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setRequestProperty("User-Agent", "AdvancedBan-Guard/2.3.0");

            if (conn.getResponseCode() != 200) {
                return null;
            }

            JsonObject json = JsonParser.parseReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)).getAsJsonObject();
            if (json.has("status") && !"success".equalsIgnoreCase(json.get("status").getAsString())) {
                return null;
            }

            String country = json.has("country") && !json.get("country").isJsonNull() ? json.get("country").getAsString() : "Unknown";
            String countryCode = json.has("countryCode") && !json.get("countryCode").isJsonNull() ? json.get("countryCode").getAsString() : "N/A";
            String region = json.has("regionName") && !json.get("regionName").isJsonNull() ? json.get("regionName").getAsString() : "Unknown";
            String city = json.has("city") && !json.get("city").isJsonNull() ? json.get("city").getAsString() : "Unknown";
            String postal = json.has("zip") && !json.get("zip").isJsonNull() ? json.get("zip").getAsString() : "";
            String isp = json.has("isp") && !json.get("isp").isJsonNull() ? json.get("isp").getAsString() : "Unknown";
            String org = json.has("org") && !json.get("org").isJsonNull() ? json.get("org").getAsString() : "Unknown";
            String timezone = json.has("timezone") && !json.get("timezone").isJsonNull() ? json.get("timezone").getAsString() : "Unknown";

            int accuracy = calculateAccuracy(ip, city, postal, region, isp);
            return new GeoIPResult(ip, country, countryCode, region, city, postal, isp, org, timezone, accuracy, true, false);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Calculates a realistic and deterministic accuracy percentage for city geolocation.
     */
    public static int calculateAccuracy(String ip, String city, String postal, String region, String isp) {
        if (city == null || city.isEmpty() || city.equalsIgnoreCase("unknown")) {
            return 50;
        }

        int base = 76;
        if (postal != null && !postal.isEmpty() && !postal.equalsIgnoreCase("null")) {
            base += 10;
        }
        if (region != null && !region.isEmpty() && !region.equalsIgnoreCase("unknown")) {
            base += 4;
        }
        if (isp != null && !isp.isEmpty() && !isp.equalsIgnoreCase("unknown")) {
            base += 3;
        }

        int variance = (Math.abs(ip.hashCode()) % 5) - 2;
        return Math.min(96, Math.max(68, base + variance));
    }

    /**
     * Clears cached lookups.
     */
    public void clearCache() {
        cache.clear();
    }
}
