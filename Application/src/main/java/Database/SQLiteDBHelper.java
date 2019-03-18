package Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Random;

import Activities.SyncActivity;
import Entities.MeasurementObject;
import Entities.Location;

public class SQLiteDBHelper extends SQLiteOpenHelper {
    private static String TAG = "---- SQLHelper ----";
    private static final int DATABASE_VERSION = 1;
    SQLiteDatabase db;

    public SQLiteDBHelper(Context context) {
        super(context, DBSchema.DATABASE_NAME, null, DATABASE_VERSION);
        db = getReadableDatabase();

        //populateDB();
    }

    private void populateDB(){
        ArrayList<String> dates = returnDates();
        ArrayList<MeasurementObject> list = returnDataObjects(dates);

        for(MeasurementObject obj: list){
            this.addItem(obj);
        }

        Log.d(TAG, "populateDB: Size " + list.size());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create = "CREATE TABLE " + DBSchema.MEASUREMENT_TABLE + " (" +
                DBSchema.MEASUREMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DBSchema.PM_TEN + " TEXT, " +
                DBSchema.PM_TWENTY_FIVE + " TEXT, " +
                DBSchema.MEASUREMENT_DATE + " TEXT, " +
                DBSchema.LONGITUDE + " TEXT, " +
                DBSchema.LATITUDE + " TEXT, " +
                DBSchema.ALTITUDE + " TEXT," +
                DBSchema.SENSOR_ID + " INTEGER" + " )";
        Log.d(TAG, "onCreate: " + create);
        db.execSQL(create);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DBSchema.MEASUREMENT_TABLE);
        onCreate(db);
    }

    public void addItem(MeasurementObject measurementObject) {
        ContentValues values = new ContentValues();
        values.put(DBSchema.PM_TEN, measurementObject.getPmTen());
        values.put(DBSchema.PM_TWENTY_FIVE, measurementObject.getPmTwentyFive());
        values.put(DBSchema.MEASUREMENT_DATE, measurementObject.getMeasurementDate());
        values.put(DBSchema.LONGITUDE, measurementObject.getLocation().getLongitude());
        values.put(DBSchema.LATITUDE, measurementObject.getLocation().getLatitude());
        values.put(DBSchema.ALTITUDE, measurementObject.getLocation().getAltitude());
        values.put(DBSchema.SENSOR_ID, Integer.valueOf(measurementObject.getSensorId()));

        db.insert(DBSchema.MEASUREMENT_TABLE, null, values);
    }

    public ArrayList<MeasurementObject> getItems() {
        Cursor cursor = db.rawQuery(Querys.GET_ALL_ITEMS, null);

        ArrayList<MeasurementObject> items = new ArrayList<>();
        while (cursor.moveToNext()) {
            int measurementId = cursor.getInt(cursor.getColumnIndexOrThrow(DBSchema.MEASUREMENT_ID));
            String pmTen = cursor.getString(cursor.getColumnIndexOrThrow(DBSchema.PM_TEN));
            String pmTwentyFive = cursor.getString(cursor.getColumnIndexOrThrow(DBSchema.PM_TWENTY_FIVE));
            String measurementDate = cursor.getString(cursor.getColumnIndexOrThrow(DBSchema.MEASUREMENT_DATE));
            String longitude = cursor.getString(cursor.getColumnIndexOrThrow(DBSchema.LONGITUDE));
            String latitude = cursor.getString(cursor.getColumnIndexOrThrow(DBSchema.LATITUDE));
            String altitude = cursor.getString(cursor.getColumnIndexOrThrow(DBSchema.ALTITUDE));
            int sensorId = cursor.getInt(cursor.getColumnIndexOrThrow(DBSchema.SENSOR_ID));

            Location loc = new Location(longitude, latitude, altitude);
            MeasurementObject item = new MeasurementObject(pmTen, pmTwentyFive, measurementDate, loc, String.valueOf(sensorId));
            item.setID(measurementId);
            items.add(item);
        }
        cursor.close();

        return items;
    }

    public static class DBSchema implements BaseColumns {
        public static final String DATABASE_NAME = "Measurement.db";
        public static final String MEASUREMENT_TABLE = "measurement_table";
        public static final String MEASUREMENT_ID = "ID";
        public static final String PM_TEN = "PM10";
        public static final String PM_TWENTY_FIVE = "PM25";
        public static final String MEASUREMENT_DATE = "DATE";
        public static final String LONGITUDE = "LONGITUDE";
        public static final String LATITUDE = "LATITUDE";
        public static final String ALTITUDE = "ALTITUDE";
        public static final String SENSOR_ID = "SENSORID";
    }

    public static class Querys{
        public static String GET_ALL_ITEMS = "select * from " + DBSchema.MEASUREMENT_TABLE;
    }

    //Methods for testing purposes

    private static ArrayList<MeasurementObject> returnDataObjects(ArrayList<String> dates){
        ArrayList<MeasurementObject> objs = new ArrayList<>();

        int minTen = 3;
        int maxTen = 5;
        int minTwen = 1;
        int maxTwen = 3;

        Location loc1 = new Location("51.37791981", "12.43077537", "110");

        for(int i = 0; i<returnDates().size(); i++){
            double randomTen = minTen + Math.random() * (maxTen - minTen);
            double randomTwen = minTwen + Math.random() * (maxTwen - minTwen);
            String randTen = String.valueOf(round(randomTen,2)).substring(0,3) + "0";
            String randTwen = String.valueOf(round(randomTwen,2)).substring(0,3) + "0";

            MeasurementObject object = new MeasurementObject(randTen, randTwen, dates.get(i), loc1, "12345");
            objs.add(object);
        }

        Location loc2 = new Location("12.43076853", "51.3778346", "");
        Location loc3 = new Location("12.43076853", "50.45165111", "");
        Location loc4 = new Location("12.43077537", "51.37791981", "");

        return objs;
    }

    private static ArrayList<String> returnDates(){
        ArrayList<String> dates = new ArrayList<>();

        // KW 9

        dates.add("2019-03-19 15:15:00");   //KW
//        dates.add("2019-03-17 10:30:00");   //KW
//        dates.add("2019-03-17 11:30:00");   //KW
//        dates.add("2019-03-17 11:00:00");   //KW
//        dates.add("2019-03-17 12:30:00");   //KW
//        dates.add("2019-02-28 21:47:47");   //KW
//        dates.add("2019-03-01 15:53:41");   //KW    //M
//        dates.add("2019-03-02 21:47:47");   //KW    //M
//        dates.add("2019-03-03 15:53:41");   //KW    //M
//        // aktueller Monat
//        dates.add("2019-03-01 22:36:41");   //M
//        dates.add("2019-03-01 22:59:36");   //M
//        dates.add("2019-03-03 22:36:41");   //M
//        dates.add("2019-03-03 22:59:36");   //M
//        dates.add("2019-03-06 22:36:41");   //M     //H
//        dates.add("2019-03-07 22:59:36");   //M     //H
//        dates.add("2019-03-07 22:59:36");   //M     //H
//
//        //aktuelles Jahr
//        dates.add("2019-01-14 10:53:41");   //J
//        dates.add("2019-01-23 14:53:12");   //J
//        dates.add("2019-02-07 15:53:41");   //J
//        dates.add("2019-02-18 21:47:47");   //J
//
//        dates.add("2019-03-17 22:59:36");   //M     //H
//        dates.add("2019-03-17 22:59:36");   //M     //H
//        dates.add("2019-03-17 22:59:36");   //M     //H
//        dates.add("2019-03-17 22:59:36");   //M     //H
//        dates.add("2019-03-17 22:59:36");   //M     //H
//        dates.add("2019-03-17 22:59:36");   //M     //H
//        dates.add("2019-03-17 22:59:36");   //M     //H
//        dates.add("2019-03-17 22:59:36");   //M     //H
//        dates.add("2019-03-11 22:59:36");   //M     //H
//        dates.add("2019-03-11 22:59:36");   //M     //H
//
//        dates.add("2018-03-03 22:59:36");

        return dates;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
