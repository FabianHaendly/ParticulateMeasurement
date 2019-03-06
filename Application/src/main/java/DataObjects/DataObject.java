package DataObjects;

import java.util.Date;

public class DataObject {
    private float PmTen;
    private float PmTwentyFive;
    private String MeasurementDate;
    private Location Location;

    public DataObject(float pmTen, float pmTwentyFive, String measurementDate, Location location){
        PmTen = pmTen;
        PmTwentyFive = pmTwentyFive;
        MeasurementDate = measurementDate;
        Location = location;
    }
}
