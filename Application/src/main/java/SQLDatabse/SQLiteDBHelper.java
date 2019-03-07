package SQLDatabse;

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

    public ArrayList getItems(String query) {
        Cursor cursor = db.rawQuery(query, null);

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
        public static String GET_ALL_ITEMS = "select * from " + SQLiteDBHelper.FeedEntry.MEASUREMENT_TABLE + " where " + SQLiteDBHelper.FeedEntry.MEASUREMENT_ID + " > 0";
    }
}
