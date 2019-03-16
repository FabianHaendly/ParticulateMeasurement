package Helper;

import java.util.Comparator;

import Entities.MeasurementObject;

public class DataObjectComparer implements Comparator<MeasurementObject> {
    @Override
    public int compare(MeasurementObject o1, MeasurementObject o2) {

        return o1.getMeasurementDate().compareTo(o2.getMeasurementDate());
    }
}
