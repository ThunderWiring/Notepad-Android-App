package com.thunderwiring.kitaba.files;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

import javax.annotation.Nonnull;

/**
 * This class is responsible for monitoring, syncing and creating the files for the notes.
 */
public class NotesFileLibrary {
    private static final String TAG = NotesFileLibrary.class.getSimpleName();

    /**
     * Creates a new file for a note with id <code>noteId</code> if it doesn't exist yet, and if
     * it does, then replace its content with <code>text</code>
     *
     * @param noteId ID of the note
     * @param text   the content (rich text) of the note
     */
    public boolean syncNote(@Nonnull UUID noteId, String text) {
        File noteFile = new File(getNoteFileName(noteId));
        try {
            if (!noteFile.exists() && !noteFile.createNewFile()) {
                Log.e(TAG, "failed to create note file");
            }
            FileWriter fileWriter = new FileWriter(noteFile, false);
            fileWriter.write(text);
            fileWriter.close();
        } catch (IOException e) {
            Log.e(TAG, "failed to save note content", e);
            return false;
        }
        return true;
    }

    public static void deleteNoteFile(String noteId) {
        String fileName = getNoteFileName(UUID.fromString(noteId));
        File noteFile = new File(fileName);
        if (!noteFile.delete()) {
            Log.e(TAG, "unable to delete note file with id " + noteId);
        }
    }

    /**
     * Returns the content in the note with the specified ID. For any errors occurring in the
     * process, this method will return null.
     */
    public static String getNoteFileContent(String noteId) {
        if (noteId == null) {
            return null;
        }
        StringBuilder contentBuilder = new StringBuilder();
        String filePath = getNoteFileName(UUID.fromString(noteId));
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String sCurrentLine = br.readLine();
            while (sCurrentLine != null) {
                contentBuilder.append(sCurrentLine).append("\n");
                sCurrentLine = br.readLine();
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to read note content from file", e);
            return null;
        }
        return contentBuilder.toString();
    }

    /**
     * Returns the file-path of the note with the specified id.
     */
    private static String getNoteFileName(@NonNull UUID noteId) {
        return NoteFilesEnvironment.getNoteFileName(noteId);
    }
}
