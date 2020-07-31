package com.thunderwiring.kitaba.views.noteEditor;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.thunderwiring.kitaba.R;
import com.thunderwiring.kitaba.data.NotePresenterEntity;
import com.thunderwiring.kitaba.files.ImageChannel;
import com.thunderwiring.kitaba.notesAndFolders.note.NoteController;
import com.thunderwiring.kitaba.textEditor.TextEditorController;
import com.thunderwiring.kitaba.textEditor.toolbar.ToolBarController;

public class NoteEditorActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 1;
    private static final String TAG = NoteEditorActivity.class.getSimpleName();
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 1;

    private NoteController mNoteController;
    private TextEditorController mTextEditorController;

    @Override
    protected void onPause() {
        super.onPause();
        if (!isStoragePermissionGranted()) {
            requestStoragePermission();
        } else {
            syncNote();
        }
    }

    @Override
    protected void onDestroy() {
        if (mTextEditorController != null) {
            mTextEditorController.onDestroy();
        }
        super.onDestroy();
    }

    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
        /* permission is automatically granted on sdk>23 upon installation. */
        return true;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                STORAGE_PERMISSION_REQUEST_CODE);
    }

    /**
     * Updates the shared preferences with the current content of the note.
     */
    private void syncNote() {
        if (mNoteController == null) {
            return;
        }
        mNoteController.syncNoteWithFileSystem();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.note_editor_activity);
        NotePresenterEntity noteEntity =
                NotePresenterEntity.fromBundle(getNoteDataObjectBundle());

        mNoteController = new NoteController(noteEntity.getId());
        ToolBarController toolBarController = new ToolBarController();
        mTextEditorController = new TextEditorController(noteEntity);

        toolBarController.attachNoteBridge(mNoteController.getToolBarBridge());
        mTextEditorController.attachTextEditorBridge(mNoteController.getTextEditorBridge());
        mTextEditorController.attachToolBarBridge(toolBarController.getToolBarToTextEditorBridge());
        mNoteController.attachTextEditorBridge(mTextEditorController.getTextEditorToNoteBridge());

        NoteEditorActionBarFragment actionBarFragment = new NoteEditorActionBarFragment();
        ToolBarFragment toolBarFragment = new ToolBarFragment();
        RichTextEditorFragment richEditorFragment = new RichTextEditorFragment();

        toolBarFragment.attachController(toolBarController);
        richEditorFragment.setArguments(getNoteDataObjectBundle());
        richEditorFragment.attachController(mTextEditorController);
        actionBarFragment.attachNoteEditorActivityToActionBar(new NoteEditorActivityToActionBar());
        actionBarFragment.attachTextEditorBridge(mTextEditorController.getTextEditorToNoteBridge());

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.actionbar, actionBarFragment)
                .replace(R.id.toolbar, toolBarFragment)
                .replace(R.id.rich_editor, richEditorFragment)
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        boolean hasActivitySucceeded =
                resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE;
        boolean isDataCorrupted = data == null;
        if (!hasActivitySucceeded || isDataCorrupted) {
            Log.e(TAG, "Failed to choose image");
        } else if (data.getData() != null) {
            ImageChannel imageTransfer = new ImageChannel(this, data.getData());
            imageTransfer.transfer();
            mNoteController.notifyOnImageSelected(imageTransfer.getImageUri());
        } else if (data.getClipData() != null) {
            Log.e(TAG, "Unsupported multi image selection");
        }
    }

    /**
     * Returns a {@link Bundle} object with the note data object if user is opening a previously
     * saved note, or an empty bundle otherwise.
     */
    private Bundle getNoteDataObjectBundle() {
        Intent intent = getIntent();
        if (intent == null || intent.getExtras() == null
                || intent.getExtras().keySet().isEmpty()) {
            return new Bundle();
        }
        return intent.getExtras();
    }

    private final class NoteEditorActivityToActionBar implements INoteEditorActivityToActionBar {
        @Override
        public void onQueryTextSubmit(String query) {
            mTextEditorController.performSearchQuery(query);
        }

        @Override
        public void onQueryTextChange(String newText) {

        }

        @Override
        public void onSearchDismissed() {
            mTextEditorController.dismissSearchMatches();
        }

        @Override
        public void onBackPressed() {
            NoteEditorActivity.this.onBackPressed();
        }

        @Override
        public void startActivityForResult(Intent intent) {
            syncNote();
            NoteEditorActivity.this.startActivityForResult(intent, PICK_IMAGE);
        }
    }
}
