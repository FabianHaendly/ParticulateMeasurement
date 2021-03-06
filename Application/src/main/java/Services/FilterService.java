package Services;

import android.util.Log;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.regex.Pattern;

import Entities.MeasurementObject;
import Helper.MeasurementObjectComparer;

public class FilterService {
    private static final String TAG ="MATCHES SIZE: ";
    public static final int PERIOD_CURRENT_DAY = 1;
    public static final int PERIOD_CURRENT_WEEK= 2;
    public static final int PERIOD_CURRENT_MONTH = 3;
    public static final int PERIOD_CURRENT_YEAR = 4;
    /**
     *
     * @param list
     * @param period
     * Defines the time time period which the list is filtered by
     * 1 = today
     * 2 = current week
     * 3 = current month
     * 4 = current year
     * @return
     */
    public static ArrayList<MeasurementObject> returnFilteredList(ArrayList<MeasurementObject> list, int period){

        ArrayList<MeasurementObject> filteredList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        String currentDate = sdf.format(cal.getTime());
        String pattern;
        ArrayList<MeasurementObject> allMatches;

        switch(period){
            case PERIOD_CURRENT_DAY:
                pattern = currentDate;
                allMatches = returnMatches(list, pattern,0,10);
                Log.d(TAG, "" + allMatches.size());
                return allMatches;
            case PERIOD_CURRENT_WEEK:
                int week = cal.get(Calendar.WEEK_OF_YEAR);
                pattern = "(";
                cal.set(Calendar.WEEK_OF_YEAR, week);
                for(int i = 1; i<=7; i++) {
                    cal.set(Calendar.DAY_OF_WEEK, i);
                    pattern += sdf.format(cal.getTime()) + "|";
                }
                pattern = pattern.substring(0, pattern.length()-1);
                pattern += ")";

                //relevant substring: index 0-10
                allMatches = returnMatches(list, pattern, 0,10);
                Log.d(TAG, "" + allMatches.size());
                return allMatches;
            case PERIOD_CURRENT_MONTH:
                int month = Calendar.MONTH + 1;
                if (month < 10){
                    pattern = "-0" + month + "-";
                }
                else
                {
                    pattern = "-" + month + "-";
                }

                allMatches = returnMatches(list, pattern,4,8);
                Log.d(TAG, "" + allMatches.size());
                return allMatches;
            case PERIOD_CURRENT_YEAR:
                int year = cal.get(Calendar.YEAR);
                pattern = String.valueOf(year);
                allMatches = returnMatches(list, pattern, 0, 4);
                Log.d(TAG, "" + allMatches.size());
                return allMatches;
            default:
                break;
        }

        return filteredList;
    }

    private static ArrayList<MeasurementObject> returnMatches(ArrayList<MeasurementObject> list, String pattern, int startOfSubstring, int endOfSubstring){
        Pattern DATE_PATTERN = Pattern.compile(pattern);
        ArrayList<MeasurementObject> allMatches = new ArrayList<>();

        for(MeasurementObject obj : list) {
            String timeStamp = obj.getMeasurementDate();
            String substring = timeStamp.substring(startOfSubstring, endOfSubstring);
            if(DATE_PATTERN.matcher(substring).matches()) {
                allMatches.add(obj);
            }
        }

        Collections.sort(allMatches, new MeasurementObjectComparer());

        return allMatches;
    }

}
