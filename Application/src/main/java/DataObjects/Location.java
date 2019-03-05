package DataObjects;

public class Location {
    private float Longitude;
    private float Latitude;
    private float Altidude;
    private String CountryCode;

    public Location(float longitude, float latitude, float altidude, String countryCode){
        Longitude = longitude;
        Latitude = latitude;
        Altidude = altidude;
        CountryCode = countryCode;
    }
}
