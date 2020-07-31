package com.thunderwiring.kitaba.views.noteEditor;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SearchView;

import com.thunderwiring.kitaba.R;
import com.thunderwiring.kitaba.textEditor.ITextEditorToNoteBridge;
import com.thunderwiring.kitaba.textEditor.RichStylesTypes;

import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

public class NoteEditorActionBarFragment extends Fragment {
    private static final String TAG = NoteEditorActionBarFragment.class.getSimpleName();

    private INoteEditorActivityToActionBar mActionBarActivity;
    private ITextEditorToNoteBridge mTextEditorBridge;

    private View mNoteEditorActionBarView;
    private SearchView mSearchView;
    private ImageButton mAddImage;
    private ImageButton mUndoButton;
    private ImageButton mRedoButton;
    private ImageButton mBackButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mNoteEditorActionBarView =
                inflater.inflate(R.layout.editor_action_bar_layout, container, false);

        initActionBarControls();
        setControlsActions();
        return mNoteEditorActionBarView;
    }

    private void initActionBarControls() {
        if (mNoteEditorActionBarView == null) {
            Log.e(TAG, "Cannot init action bar controls of a null action bar.");
            return;
        }
        mAddImage = mNoteEditorActionBarView.findViewById(R.id.add_image);
        mUndoButton = mNoteEditorActionBarView.findViewById(R.id.undo);
        mRedoButton = mNoteEditorActionBarView.findViewById(R.id.redo);
        mSearchView = mNoteEditorActionBarView.findViewById(R.id.note_editor_search);
        mBackButton = mNoteEditorActionBarView.findViewById(R.id.back_button);

        for (View view : mNoteEditorActionBarView.getTouchables()) {
            view.setBackgroundColor(ContextCompat.getColor(getContext().getApplicationContext(),
                    R.color.colorRipple));
        }
    }

    private void setControlsActions() {
        mBackButton.setOnClickListener(v -> {
            mActionBarActivity.onBackPressed();
        });

        NoteSearchViewListener searchListener = new NoteSearchViewListener();
        mSearchView.setOnQueryTextListener(searchListener);
        mSearchView.setOnCloseListener(searchListener);

        mAddImage.setOnClickListener(getAddImageOnClick());
        mUndoButton.setOnClickListener(
                v -> mTextEditorBridge.applyAction(RichStylesTypes.UNDO));
        mRedoButton.setOnClickListener(
                v -> mTextEditorBridge.applyAction(RichStylesTypes.REDO));
    }

    public void attachNoteEditorActivityToActionBar(INoteEditorActivityToActionBar actionBarActivity) {
        mActionBarActivity = actionBarActivity;
    }

    public void attachTextEditorBridge(ITextEditorToNoteBridge textEditorToNoteBridge) {
        mTextEditorBridge = textEditorToNoteBridge;
    }

    /**
     * Returns implementation of {@link View.OnClickListener} for the addNote-image action-bar
     * button.
     */
    private View.OnClickListener getAddImageOnClick() {
        return v -> {
            Intent pickIntent = new Intent(Intent.ACTION_PICK);
            pickIntent.setDataAndType(EXTERNAL_CONTENT_URI, "image/*");
//                pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
            mActionBarActivity.startActivityForResult(pickIntent);
        };
    }

    private class NoteSearchViewListener implements SearchView.OnQueryTextListener,
            SearchView.OnCloseListener {
        @Override
        public boolean onQueryTextSubmit(String query) {
            if (mActionBarActivity == null) {
                return false;
            }
            mActionBarActivity.onQueryTextSubmit(query);
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            if (mActionBarActivity == null) {
                return false;
            }
            mActionBarActivity.onQueryTextChange(newText);
            return true;
        }

        @Override
        public boolean onClose() {
            if (mActionBarActivity == null) {
                return false;
            }
            mActionBarActivity.onSearchDismissed();
            return false;
        }
    }
}
