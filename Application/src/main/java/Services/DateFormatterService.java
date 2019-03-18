package Services;

public class DateFormatterService {
    public static String returnRFCFormattedDate(String dbDate){
        //remove whitespaces
        dbDate = dbDate.replaceAll("\\s+", "");

        int indexEndOfDate = 9;
        int indexEndOfTime = dbDate.length();

        dbDate = insertString(dbDate, "T", indexEndOfDate);
        dbDate = insertString(dbDate, "Z", indexEndOfTime);

        return dbDate;
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
