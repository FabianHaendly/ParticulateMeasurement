package SQLLocalDatabse;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;

import DataObjects.DataObject;
import DataObjects.Location;

public class SQLiteDBHelper extends SQLiteOpenHelper {
    private static String TAG = "---- SQLHelper ----";
    private static final int DATABASE_VERSION = 1;
    SQLiteDatabase db;

    public SQLiteDBHelper(Context context) {
        super(context, FeedEntry.DATABASE_NAME, null, DATABASE_VERSION);
        db = getReadableDatabase();

        populateDB();
    }

    private void populateDB(){
        ArrayList<String> dates = returnDates();
        ArrayList<DataObject> list = returnDataObjects(dates);

        for(DataObject obj: list){
            this.addItem(obj);
        }

        Log.d(TAG, "populateDB: Size " + list.size());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create = "CREATE TABLE " + FeedEntry.MEASUREMENT_TABLE + " (" +
                FeedEntry.MEASUREMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                FeedEntry.PM_TEN + " TEXT, " +
                FeedEntry.PM_TWENTY_FIVE + " TEXT, " +
                FeedEntry.MEASUREMENT_DATE + " TEXT, " +
                FeedEntry.LONGITUDE + " TEXT, " +
                FeedEntry.LATITUDE + " TEXT, " +
                FeedEntry.ALTITUDE + " TEXT" + " )";
        Log.d(TAG, "onCreate: " + create);
        db.execSQL(create);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FeedEntry.MEASUREMENT_TABLE);
        onCreate(db);
    }

    public void addItem(DataObject dataObject) {
        ContentValues values = new ContentValues();
        values.put(FeedEntry.PM_TEN, dataObject.getPmTen());
        values.put(FeedEntry.PM_TWENTY_FIVE, dataObject.getPmTwentyFive());
        values.put(FeedEntry.MEASUREMENT_DATE, dataObject.getMeasurementDate());
        values.put(FeedEntry.LONGITUDE, dataObject.getLocation().getLongitude());
        values.put(FeedEntry.LATITUDE, dataObject.getLocation().getLatitude());
        values.put(FeedEntry.ALTITUDE, dataObject.getLocation().getAltitude());

        db.insert(FeedEntry.MEASUREMENT_TABLE, null, values);
    }

    public ArrayList<DataObject> getItems() {
        Cursor cursor = db.rawQuery(Querys.GET_ALL_ITEMS, null);

        ArrayList<DataObject> items = new ArrayList<>();
        while (cursor.moveToNext()) {
            int measurementId = cursor.getInt(cursor.getColumnIndexOrThrow(FeedEntry.MEASUREMENT_ID));
            String pmTen = cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.PM_TEN));
            String pmTwentyFive = cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.PM_TWENTY_FIVE));
            String measurementDate = cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.MEASUREMENT_DATE));
            String longitude = cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.LONGITUDE));
            String latitude = cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.LATITUDE));
            String altitude = cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.ALTITUDE));

            Location loc = new Location(longitude, latitude, altitude);
            DataObject item = new DataObject(pmTen, pmTwentyFive, measurementDate, loc);
            item.setID(measurementId);
            items.add(item);
        }
        cursor.close();

        return items;
    }

    public static class FeedEntry implements BaseColumns {
        public static final String DATABASE_NAME = "Measurement.db";
        public static final String MEASUREMENT_TABLE = "measurement_table";
        public static final String MEASUREMENT_ID = "ID";
        public static final String PM_TEN = "PM10";
        public static final String PM_TWENTY_FIVE = "PM25";
        public static final String MEASUREMENT_DATE = "DATE";
        public static final String LONGITUDE = "LONGITUDE";
        public static final String LATITUDE = "LATITUDE";
        public static final String ALTITUDE = "ALTITUDE";
    }

    public static class Querys{
        public static String GET_ALL_ITEMS = "select * from " + SQLiteDBHelper.FeedEntry.MEASUREMENT_TABLE;
    }

    private static ArrayList<DataObject> returnDataObjects(ArrayList<String> dates){
        ArrayList<DataObject> objs = new ArrayList<>();
//        Location loc = new Location("20.1312", "54.123", "234.645");

//        for(int i = 0; i<dates.size(); i++){
//            Random r = new Random();
//            float randPmTen = (float)(3.00 + r.nextFloat() * (3.00 - 1.50));
//            float randPmTwen = (float)(2.00 + r.nextFloat() * (2.00 - 0.00));
//
//            DataObject obj = new DataObject(String.valueOf(randPmTen).substring(0,4), String.valueOf(randPmTwen).substring(0,4), returnDates().get(i), loc);
//            objs.add(obj);
//        }

        Location loc1 = new Location("12.43077537", "51.37791981", "");
        Location loc2 = new Location("12.43076853", "51.3778346", "");
        Location loc3 = new Location("12.43076853", "50.45165111", "");
        Location loc4 = new Location("12.43077537", "51.37791981", "");

        DataObject o1 = new DataObject("5.201", "0.801", "2019-03-12 22:26:00",loc1);
        DataObject o2 = new DataObject("5.201","1.801","2019-02-25 10:53:41", loc2);
        DataObject o3 = new DataObject("5.201","5.401","2019-03-25 10:53:41", loc3);
        DataObject o4 = new DataObject("10.0", "10.0", "2019-03-31 22:59:36",loc4);

        objs.add(o1);
//        objs.add(o2);
//        objs.add(o3);
//        objs.add(o4);

        return objs;
    }

    private static ArrayList<String> returnDates(){
        ArrayList<String> dates = new ArrayList<>();

        // KW 9

        dates.add("2019-02-25 10:53:41");   //KW
        dates.add("2019-02-26 14:53:12");   //KW
        dates.add("2019-02-27 15:53:41");   //KW
        dates.add("2019-02-28 21:47:47");   //KW
        dates.add("2019-03-01 15:53:41");   //KW    //M
        dates.add("2019-03-02 21:47:47");   //KW    //M
        dates.add("2019-03-03 15:53:41");   //KW    //M
        // aktueller Monat
        dates.add("2019-03-01 22:36:41");   //M
        dates.add("2019-03-01 22:59:36");   //M
        dates.add("2019-03-03 22:36:41");   //M
        dates.add("2019-03-03 22:59:36");   //M
        dates.add("2019-03-06 22:36:41");   //M     //H
        dates.add("2019-03-07 22:59:36");   //M     //H
        dates.add("2019-03-07 22:59:36");   //M     //H

        //aktuelles Jahr
        dates.add("2019-01-14 10:53:41");   //J
        dates.add("2019-01-23 14:53:12");   //J
        dates.add("2019-02-07 15:53:41");   //J
        dates.add("2019-02-18 21:47:47");   //J

        dates.add("2018-03-03 22:59:36");

        return dates;
    }
}
