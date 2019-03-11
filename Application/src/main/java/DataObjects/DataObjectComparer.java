package DataObjects;

import java.util.Comparator;

public class DataObjectComparer implements Comparator<DataObject> {
    @Override
    public int compare(DataObject o1, DataObject o2) {

        return o1.getMeasurementDate().compareTo(o2.getMeasurementDate());
    }
}
