package DataObjects;

public class Location {
    private String Longitude;
    private String Latitude;
    private String Altitude;

    public Location(float longitude, float latitude, float altitude){
        Longitude = String.valueOf(longitude);
        Latitude = String.valueOf(latitude);
        Altitude = String.valueOf(altitude);
    }

    public String getAltitude() {
        return Altitude;
    }

    public String getLatitude() {
        return Latitude;
    }

    public String getLongitude() {
        return Longitude;
    }
}
