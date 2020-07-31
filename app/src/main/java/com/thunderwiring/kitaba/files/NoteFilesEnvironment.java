package com.thunderwiring.kitaba.files;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.UUID;

public class NoteFilesEnvironment {
    private static final String TAG = "NoteFilesEnvironment";

    private static final String DIR_NAME = "MemoNotes";
    private static final String ROOT_DIR =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath();
    private static final String ASSETS_DIR_NAME = "assets";

    private static final String PARENT_DIR_PATH = ROOT_DIR + "/" + DIR_NAME + "/";
    private static final String ASSETS_DIR_PATH = PARENT_DIR_PATH + ASSETS_DIR_NAME + "/";

    static String getAssetsFilePath(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        return ASSETS_DIR_PATH + fileName;
    }

    private static void createDocumentsFile() {
        File docDir = new File(ROOT_DIR);
        if (!docDir.mkdir()) {
            Log.e(TAG, "Failed to create documents dir");
        }
    }

    /**
     * Creates a new directory for storing the apps files.
     */
    public static void createAppDir() {
        createDocumentsFile();
        File appDir = new File(ROOT_DIR, DIR_NAME);
        if (!appDir.mkdir()) {
            Log.d(TAG, "failed to create app directory");
        } else {
            Log.d(TAG, "createAppDir: appDir: " + appDir.getAbsolutePath());
        }
    }

    /**
     * Returns the name of the note's file according to its id.
     */
    public static String getNoteFileName(UUID noteId) {
        return getFilePath(noteId.toString(), "html");
    }

    public static String getFilePath(String filename, String extension) {
        return PARENT_DIR_PATH + filename + "." + extension;
    }

    /**
     * Creates a new directory for storing the notes assets like images.
     */
    public static void createAssetsDir() {
        File assetsDir = new File(NoteFilesEnvironment.PARENT_DIR_PATH,
                NoteFilesEnvironment.ASSETS_DIR_NAME);
        if (!assetsDir.mkdir()) {
            Log.d(TAG, "failed to create assets directory");
        } else {
            Log.d(TAG, "createAppDir: assetsDir: " + assetsDir.getAbsolutePath());
        }
    }
}
