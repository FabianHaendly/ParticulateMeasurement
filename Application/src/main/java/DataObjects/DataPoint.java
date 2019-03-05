package DataObjects;

import java.util.Date;

public class DataPoint {
    private float PmTen;
    private float PmTwentyFive;
    private Date MeasurementDate;
    private Location Location;

    public DataPoint(float pmTen, float pmTwentyFive, Date measurementDate, Location location){
        PmTen = pmTen;
        PmTwentyFive = pmTwentyFive;
        MeasurementDate = measurementDate;
        Location = location;
    }
}
