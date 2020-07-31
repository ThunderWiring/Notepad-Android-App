package com.thunderwiring.kitaba.views;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.thunderwiring.kitaba.files.NoteFilesEnvironment;
import com.thunderwiring.kitaba.views.notesLibrary.NotesActivity2;

import java.util.HashMap;
import java.util.Map;
import com.facebook.FacebookSdk;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 1;
    private static final int DELAY_AFTER_PERMISSION_MILLI_SEC = 500;

    private Runnable mActivityRunnable;
    private Handler mPermissionsHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPermissionsHandler = new Handler();
        mActivityRunnable = () -> {
            initAssetsFileTree();
            Intent notesActivity = new Intent(
                    MainActivity.this, NotesActivity2.class);
            notesActivity.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(notesActivity);
        };

        if (!isStoragePermissionGranted()) {
            requestStoragePermission();
        } else {
            mPermissionsHandler.postDelayed(mActivityRunnable, DELAY_AFTER_PERMISSION_MILLI_SEC);
        }

    }

    private void initAssetsFileTree() {
        NoteFilesEnvironment.createAppDir();
        NoteFilesEnvironment.createAssetsDir();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        int index = 0;
        Map<String, Integer> PermissionsMap = new HashMap<>();
        for (String permission : permissions) {
            PermissionsMap.put(permission, grantResults[index]);
            index++;
        }

        try {
            if ((PermissionsMap.get(WRITE_EXTERNAL_STORAGE) != 0)) {
                handlePermissionGrantRejection();
                finish();
            } else {
                mPermissionsHandler.postDelayed(mActivityRunnable,
                        DELAY_AFTER_PERMISSION_MILLI_SEC);
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "Failed to check for storage permission status", e);
            handlePermissionGrantRejection();
        }
    }

    /**
     * Handles when user rejects to grant the storage permission to the app. Since this
     * permission is essential to the key functionality of the app, the app should re-engage the
     * user to try and grant it.
     */
    private void handlePermissionGrantRejection() {
    }

    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        } else { //permission is automatically granted on sdk>23 upon installation
            return true;
        }
    }

    /**
     * Prompts system dialog to request storage permission from the user. The dialog is created
     * asynchronously, therefor the rendering of the activity should wait for the dialog and
     * handle the result on {@link FragmentActivity#onRequestPermissionsResult}
     */
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{WRITE_EXTERNAL_STORAGE},
                STORAGE_PERMISSION_REQUEST_CODE);
    }
}
