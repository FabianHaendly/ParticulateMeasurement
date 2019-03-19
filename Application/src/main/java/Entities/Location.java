package Entities;

public class Location {
    private String Latitude;
    private String Longitude;
    private String Altitude;

    public Location(String latitude, String longitude, String altitude){
        Latitude = String.valueOf(latitude);
        Longitude = String.valueOf(longitude);
        Altitude = String.valueOf(altitude);
    }

    public String getLatitude() {
        return Latitude;
    }
    public String getAltitude() {
        return Altitude;
    }

    public String getLongitude() {
        return Longitude;
    }
}
