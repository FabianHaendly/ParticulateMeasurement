package Services;

import android.content.Context;
import android.text.LoginFilter;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileService {
    private static final String FILE_NAME_OSM = "LastOpenSenseMapSynchronization.txt";
    private static final String FILE_NAME_PSERVER = "LastPrivateServerSynchronization.txt";
    private static final int SAVE_OSM_SYNC = 1;
    private static final int SAVE_PRIVATE_SERVER_SYNC = 2;
    private static int FILE_SELECTION;

    private Context context;

    public FileService(Context context, int fileSelection){
        this.context = context;
        FILE_SELECTION = fileSelection;
    }

    public void saveLatestSyncDate(String date){
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
            fos.write(date.getBytes());
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

            if (!fileExists(context, selectedFile)) {
                Log.d("FileService", "Created file " + selectedFile);
                saveLatestSyncDate("2000-01-01 00:00:00");
            }

            fis = context.openFileInput(selectedFile);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

            String temp;

            while((temp = br.readLine()) != null){
                lastSyncDate += temp;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lastSyncDate;
    }

    public boolean fileExists(Context context, String fileName) {
        File file = new File(context.getFilesDir() + "/" + fileName);

        if(file == null || !file.exists()) {
            return false;
        }
        return true;
    }
}
