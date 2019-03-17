package Activities;

import android.app.Activity;
import android.media.MediaSync;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import BLEHelper.bluetoothlegatt.R;

import com.github.mikephil.charting.charts.LineChart;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import Entities.MeasurementObject;
import Database.SQLiteDBHelper;
import Services.FilterService;
import Services.GraphService;

public class StatisticsActivity extends Activity {
    private static String TAG = "DISPLAYLIST: ";

    Button mTodayBtn;
    Button mWeekBtn;
    Button mMonthBtn;
    Button mYearBtn;
    LineChart mLineChart;
    GraphService mGraphService;
    ArrayList<MeasurementObject> mDbData;
    ArrayList<MeasurementObject> mDisplayList;
    FilterService mFilterService;
    TextView mPeriodData;

    //    Statistic values
    TextView mValues;
    TextView mDistance;
    TextView mMaxPm10;
    TextView mMaxPm25;
    TextView mAvgPm10;
    TextView mAvgPm25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        mTodayBtn = findViewById(R.id.today_btn);
        mWeekBtn = findViewById(R.id.week_btn);
        mMonthBtn = findViewById(R.id.month_btn);
        mYearBtn = findViewById(R.id.year_btn);
        mLineChart = findViewById(R.id.lineChart);
        mFilterService = new FilterService();
        mPeriodData = findViewById(R.id.tv_period_data);

        mValues = findViewById(R.id.kv_values_data);
        mDistance = findViewById(R.id.kv_distance_data);
        mMaxPm10 = findViewById(R.id.kv_maxPmTen_data);
        mMaxPm25 = findViewById(R.id.kv_maxPmTwentyFive_data);
        mAvgPm10 = findViewById(R.id.kv_avgPmTen_data);
        mAvgPm25 = findViewById(R.id.kv_avgPmTwentyFive_data);

        SQLiteDBHelper db = new SQLiteDBHelper(this);
        mDbData = db.getItems();

        mGraphService = new GraphService(mLineChart, new ArrayList<MeasurementObject>());
        mGraphService.initializeStaticGraph();
    }

    public void onTodayBtnClick(View view) {
        btnExecute(FilterService.PERIOD_CURRENT_DAY);
    }

    public void onWeekBtnClick(View view) {
        btnExecute(FilterService.PERIOD_CURRENT_WEEK);
    }

    public void onMonthBtnClick(View view) {
        btnExecute(FilterService.PERIOD_CURRENT_MONTH);
    }

    public void onYearBtnClick(View view) {
        btnExecute(FilterService.PERIOD_CURRENT_YEAR);
    }

    private void btnExecute(int period) {

        setPeriodTextView(period);

        mDisplayList = FilterService.returnFilteredList(mDbData, period);
        Log.d(TAG, "" + mDisplayList.size());
        if (mDisplayList.size() > 0) {
            mGraphService = new GraphService(mLineChart, mDisplayList);
            mGraphService.initializeStaticGraph();
            setStatisticValues(mDisplayList);
        } else {
            mGraphService = new GraphService(mLineChart, mDisplayList);
            mGraphService.initializeStaticGraph();
            toastMessage("No values for this selection!");
        }
    }

    private void setPeriodTextView(int period) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();

        switch (period) {
            case FilterService.PERIOD_CURRENT_DAY:
                String currentDate = sdf.format(cal.getTime());
                mPeriodData.setText(currentDate);
                break;
            case FilterService.PERIOD_CURRENT_WEEK:
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                String firstDayWeek = sdf.format(cal.getTime());

                for (int i = 0; i <6; i++) {
                    cal.add(Calendar.DATE, 1);
                }

                String lastDayWeek = sdf.format(cal.getTime());
                String weekPeriod = firstDayWeek + " - " + lastDayWeek;

                mPeriodData.setText(weekPeriod);
                break;
            case FilterService.PERIOD_CURRENT_MONTH:
                Calendar gCal = new GregorianCalendar();
                int month = gCal.get(Calendar.MONTH);
                gCal.set(Calendar.MONTH, month);
                gCal.set(Calendar.DAY_OF_MONTH, 1);
                Date monthStart = gCal.getTime();
                gCal.add(Calendar.MONTH, 1);
                gCal.add(Calendar.DAY_OF_MONTH, -1);
                Date monthEnd = gCal.getTime();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                String monthPeriod = dateFormat.format(monthStart) + " - " + dateFormat.format(monthEnd);
                mPeriodData.setText(monthPeriod);
                break;
            case FilterService.PERIOD_CURRENT_YEAR:
                String cDate = sdf.format(cal.getTime());
                cal.set(Calendar.DAY_OF_YEAR, 1);
                String startDate = sdf.format(cal.getTime());

                mPeriodData.setText(startDate + " - " + cDate);
                break;
            default:
                mPeriodData.setText("No selection yet!");
        }
    }

    private void setStatisticValues(ArrayList<MeasurementObject> list) {
        ArrayList<MeasurementObject> maxes = returnMaxPmObject(list);
        ArrayList<String> avgs = returnAveragePmValues(list);

        mValues.setText(String.valueOf(list.size()));
        mDistance.setText(returnTraveledDistance(list));
        mMaxPm10.setText(maxes.get(0).getPmTen());
        mMaxPm25.setText(maxes.get(1).getPmTwentyFive());
        mAvgPm10.setText(avgs.get(0));
        mAvgPm25.setText(avgs.get(1));
    }

    private String returnTraveledDistance(ArrayList<MeasurementObject> list) {

        double distanceSum = 0.0;
        double lat1, lat2, lon1, lon2, distance;

        for (int i = 0; i < list.size() - 1; i++) {
            lat1 = Double.valueOf(list.get(i).getLocation().getLatitude());
            lat2 = Double.valueOf(list.get(i + 1).getLocation().getLatitude());
            lon1 = Double.valueOf(list.get(i).getLocation().getLongitude());
            lon2 = Double.valueOf(list.get(i + 1).getLocation().getLongitude());

            if (lat1 != 0.0 && lon1 != 0.0 && lat2 != 0.0 && lon2 != 0) {
                distance = distanceHaversine(lon1, lat1, lon2, lat2);
                distanceSum += distance;
            }

        }

//        Log.d(TAG, "returnTraveledDistance: " + distanceSum);

        DecimalFormat df = new DecimalFormat("#.###");
        df.setRoundingMode(RoundingMode.HALF_UP);

//        String formatted = df.format(distanceSum);
//        Log.d(TAG, "---returnTraveledDistance:--- " + df.format(distanceSum));

        return df.format(distanceSum);
    }

    private double distanceHaversine(double lon1, double lat1, double lon2, double lat2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // convert to meters

        distance = Math.pow(distance, 2);

        return Math.sqrt(distance);
    }

    private ArrayList<MeasurementObject> returnMaxPmObject(ArrayList<MeasurementObject> list) {
        MeasurementObject maxPmTenObj = list.get(0);
        MeasurementObject maxPmTwentyFiveObj = list.get(0);
        double maxPmTen = Double.valueOf(maxPmTenObj.getPmTen());
        double maxPmTwentyFive = Double.valueOf(maxPmTwentyFiveObj.getPmTen());

        for (int i = 1; i < list.size(); i++) {
            double currentPmTen = Double.valueOf(list.get(i).getPmTen());
            double currentPmTwentyFive = Double.valueOf(list.get(i).getPmTwentyFive());

//            Log.d(TAG, "returnMaxPmObject: CURRENT PM10: " + currentPmTen);
//            Log.d(TAG, "returnMaxPmObject: CURRENT PM25: " + currentPmTwentyFive);

            if (currentPmTen > maxPmTen) {
                maxPmTen = currentPmTen;
                maxPmTenObj = list.get(i);
            }
            if (currentPmTwentyFive > maxPmTwentyFive) {
                maxPmTwentyFive = currentPmTwentyFive;
                maxPmTwentyFiveObj = list.get(i);
            }
        }

        ArrayList<MeasurementObject> result = new ArrayList<>();
        result.add(maxPmTenObj);
        result.add(maxPmTwentyFiveObj);

        return result;
    }

    private ArrayList<String> returnAveragePmValues(ArrayList<MeasurementObject> list) {
        double sumPmTen = 0.0;
        double sumPmTwentyFive = 0.0;

        for (int i = 0; i < list.size(); i++) {
            sumPmTen += Double.valueOf(list.get(i).getPmTen());
            sumPmTwentyFive += Double.valueOf(list.get(i).getPmTwentyFive());
        }

        String avgPmTen = String.valueOf(sumPmTen / list.size()).substring(0, 3);
        String avgPmTwentyFive = String.valueOf(sumPmTwentyFive / list.size()).substring(0, 3);

//        Log.d(TAG, "returnAveragePmValues: PM10 AVG: " + avgPmTen);
//        Log.d(TAG, "returnAveragePmValues: PM25 AVG: " + mAvgPm25);

        ArrayList<String> avgs = new ArrayList<>();
        avgs.add(avgPmTen);
        avgs.add(avgPmTwentyFive);

        return avgs;
    }

    private void toastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
