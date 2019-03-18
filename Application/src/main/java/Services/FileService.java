package Services;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class FileService {
    private static final String FILE_NAME_OSM = "LastOpenSenseMapSynchronization.txt";
    private static final String FILE_NAME_PSERVER = "LastPrivateServerSynchronization.txt";
    private static final int SAVE_OSM_SYNC = 1;
    private static final int SAVE_PRIVATE_SERVER_SYNC = 2;
    private static int FILE_SELECTION;
    private static String DATE_TO_SAVE;

    private Context context;

    public FileService(Context context, int fileSelection){
        this.context = context;
        FILE_SELECTION = fileSelection;
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DATE_TO_SAVE = sdf.format(cal.getTime());
        System.out.println(sdf.format(cal.getTime()));
    }

    public void saveLatestSyncDate(){
        FileOutputStream fos = null;
        String selectedFile;

        if (FILE_SELECTION == SAVE_OSM_SYNC){
            selectedFile = FILE_NAME_OSM;
        }
        else{
            selectedFile = FILE_NAME_PSERVER;
        }

        try {
            fos = context.openFileOutput(selectedFile, context.MODE_PRIVATE);
            fos.write(DATE_TO_SAVE.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if (fos != null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getLastSyncDate(){
        FileInputStream fis = null;
        String selectedFile;
        String lastSyncDate ="";

        if(FILE_SELECTION == SAVE_OSM_SYNC){
            selectedFile = FILE_NAME_OSM;
        }
        else{
            selectedFile = FILE_NAME_PSERVER;
        }

        try {
            fis = context.openFileInput(selectedFile);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

            String temp;

            while((temp = br.readLine()) != null){
                lastSyncDate += temp;
                Log.d("SAVE SYNC", "FILE: " + selectedFile + " Date: " + lastSyncDate);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lastSyncDate;
    }
}
