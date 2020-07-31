package com.thunderwiring.kitaba.files;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Transfers an image from the gallery directory to the app's assets dir.
 */
public class ImageChannel {
    private final static String TAG = "ImageChannel";

    private Context mContext;
    private Uri mSourceImageUri;
    private Uri mImageUri;

    public ImageChannel(Context context, Uri sourceFile) {
        mContext = context;
        mSourceImageUri = sourceFile;
    }

    /**
     * Returns URI of the image to desplay in the image span.
     */
    public Uri getImageUri() {
        return mImageUri;
    }

    /**
     * Transfers the image from the source to the apps dir.
     */
    public void transfer() {
        File sourceFile = new File(mSourceImageUri.getPath());
        if ("content".equals(mSourceImageUri.getScheme())) {
            sourceFile = new File(getRealPathFromURI(mSourceImageUri));
        }
        File destFile = new File(getDestFilePath(sourceFile.getName()));
        try {
            if (!destFile.exists()) {
                destFile.createNewFile();
                destFile.setWritable(true);
            } else {
                Log.i(TAG, "image already transferred");
                mImageUri = Uri.fromFile(destFile);
                return;
            }
            FileChannel source = new FileInputStream(sourceFile).getChannel();
            FileChannel dest = new FileOutputStream(destFile).getChannel();
            dest.transferFrom(source, 0, source.size());
            source.close();
            dest.close();
            mImageUri = Uri.fromFile(destFile);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Failed to transfer image", e);

        } catch (IOException e) {
            Log.e(TAG, "error while finishing transferring the image", e);
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(mContext, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        if (cursor == null) {
            Log.e(TAG, "error while extracting file path from image content URI");
            return "";
        }
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    /**
     * Returns the full path of the dest file according to its name.
     */
    private String getDestFilePath(String fileName) {
        return NoteFilesEnvironment.getAssetsFilePath(fileName);
    }
}
