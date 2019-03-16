package Entities;

public class Location {
    private String Longitude;
    private String Latitude;
    private String Altitude;

    public Location(String longitude, String latitude, String altitude){
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
