package Entities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MeasurementObject {
    private int ID;
    private String PmTen;
    private String PmTwentyFive;
    private String MeasurementDate;
    private Location Location;
    private String SensorId;

    public MeasurementObject() {}

    public MeasurementObject(String pmTen, String pmTwentyFive, String measurementDate, Location location, String sensorId){
        PmTen = pmTen;
        PmTwentyFive = pmTwentyFive;
        MeasurementDate = measurementDate;
        Location = location;
        SensorId = sensorId;
    }

    public static String returnTimeStamp() {
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = dateFormat.format(date);

        return formattedDate;
    }

    public String getPmTen() {
        return PmTen;
    }

    public String getPmTwentyFive() {
        return PmTwentyFive;
    }

    public String getMeasurementDate() {
        return MeasurementDate;
    }

    public Location getLocation() {
        return Location;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getID() {
        return ID;
    }

    public String getSensorId() {
        return SensorId;
    }
}
