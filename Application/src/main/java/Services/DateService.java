package Services;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateService {
    public static String getRFCFormattedDate(String dbDate){
        //remove whitespaces
        dbDate = dbDate.replaceAll("\\s+", "");

        int indexEndOfDate = 9;
        int indexEndOfTime = dbDate.length();

        dbDate = insertString(dbDate, "T", indexEndOfDate);
        dbDate = insertString(dbDate, "Z", indexEndOfTime);

        return dbDate;
    }

    public static String getCurrentDateAndTime(){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return sdf.format(cal.getTime());
    }

    private static String insertString(
            String originalString,
            String stringToBeInserted,
            int index) {

        String newString = new String();

        for (int i = 0; i < originalString.length(); i++) {

            // Insert the original string character
            // into the new string
            newString += originalString.charAt(i);

            if (i == index) {

                // Insert the string to be inserted
                // into the new string
                newString += stringToBeInserted;
            }
        }

        return newString;
    }
}
