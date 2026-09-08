package me.leoko.advancedban.utils;

/**
 * Represents the resolved geolocation data for an IP address.
 */
public class GeoIPResult {
    private final String ip;
    private final String country;
    private final String countryCode;
    private final String region;
    private final String city;
    private final String postal;
    private final String isp;
    private final String org;
    private final String timezone;
    private final int accuracy;
    private final boolean success;
    private final boolean local;

    public GeoIPResult(String ip, String country, String countryCode, String region, String city,
                       String postal, String isp, String org, String timezone, int accuracy,
                       boolean success, boolean local) {
        this.ip = ip;
        this.country = country != null && !country.isEmpty() ? country : "Unknown";
        this.countryCode = countryCode != null && !countryCode.isEmpty() ? countryCode : "N/A";
        this.region = region != null && !region.isEmpty() ? region : "Unknown";
        this.city = city != null && !city.isEmpty() ? city : "Unknown";
        this.postal = postal != null ? postal : "";
        this.isp = isp != null && !isp.isEmpty() ? isp : "Unknown";
        this.org = org != null && !org.isEmpty() ? org : "Unknown";
        this.timezone = timezone != null && !timezone.isEmpty() ? timezone : "Unknown";
        this.accuracy = accuracy;
        this.success = success;
        this.local = local;
    }

    public static GeoIPResult createLocal(String ip) {
        return new GeoIPResult(ip, "Local Network", "LAN", "Local Area", "Localhost",
                "00000", "Internal Loopback", "Localhost", "Local Time", 100, true, true);
    }

    public static GeoIPResult createFailed(String ip) {
        return new GeoIPResult(ip, "Unknown", "N/A", "Unknown", "Unknown",
                "", "Unknown", "Unknown", "Unknown", 0, false, false);
    }

    public String getIp() {
        return ip;
    }

    public String getCountry() {
        return country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getRegion() {
        return region;
    }

    public String getCity() {
        return city;
    }

    public String getPostal() {
        return postal;
    }

    public String getIsp() {
        return isp;
    }

    public String getOrg() {
        return org;
    }

    public String getTimezone() {
        return timezone;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isLocal() {
        return local;
    }
}
