package DataObjects;

public class DataObject {
    private int ID;
    private String PmTen;
    private String PmTwentyFive;
    private String MeasurementDate;
    private Location Location;

    public DataObject() {}

    public DataObject(String pmTen, String pmTwentyFive, String measurementDate, Location location){
        PmTen = pmTen;
        PmTwentyFive = pmTwentyFive;
        MeasurementDate = measurementDate;
        Location = location;
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
}
