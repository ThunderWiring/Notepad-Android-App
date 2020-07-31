package com.thunderwiring.kitaba.views.notesLibrary;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.thunderwiring.kitaba.R;
import com.thunderwiring.kitaba.notesAndFolders.note.NoteSortManager;
import com.thunderwiring.kitaba.views.INotesActivity;
import com.thunderwiring.kitaba.views.SettingsMenuCommand;
import com.thunderwiring.kitaba.views.SettingsMenuCommands;
import com.thunderwiring.kitaba.views.notesLibrary.contetnt.IContentFragment;
import com.thunderwiring.kitaba.views.notesLibrary.contetnt.NotesActivityContentFragment;
import com.thunderwiring.kitaba.views.notesLibrary.contetnt.TabLibraryFragment;

public class NotesActivity2 extends AppCompatActivity {
    private NotesActivityActionBarFragment mActionBar;
    private NotesActivityToActionBar mNotesActivityToActionBar;
    private FloatingActionButton mFloatingActionButton;
    private int mCurrentContentFragmentLayout;
    private IContentFragment mContentFragment;

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof IContentFragment) {
            IContentFragment contentFragment = (IContentFragment) fragment;
            contentFragment.setNotesActivityInterface(new NotesActivityBridge());
        }
        if (fragment instanceof TabLibraryFragment) {
            mContentFragment = (IContentFragment) fragment;
            mContentFragment.setNotesActivityInterface(new NotesActivityBridge());
            mNotesActivityToActionBar = new NotesActivityToActionBar();
            mActionBar.setActionBarBridge(mNotesActivityToActionBar);
            setActionButton(mContentFragment.getActionButtonResource(),
                    mContentFragment.getActionButtonClickListener());
        }
        if (fragment instanceof NotesActivityActionBarFragment) {
            mActionBar = (NotesActivityActionBarFragment) fragment;
        }
    }

    private void setActionButton(int iconRes, View.OnClickListener onClick) {
        if (mFloatingActionButton == null) {
            mFloatingActionButton = findViewById(R.id.new_note_button);
        }
        mFloatingActionButton.setImageDrawable(ContextCompat.getDrawable(this, iconRes));
        mFloatingActionButton.setOnClickListener(onClick);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.notes_activity);
        mFloatingActionButton = findViewById(R.id.new_note_button);
        mCurrentContentFragmentLayout = R.layout.note_library_tab_layout;

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.notes_action_bar, new NotesActivityActionBarFragment())
                .replace(R.id.container_fragment, new TabLibraryFragment())
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (mCurrentContentFragmentLayout != R.layout.note_library_tab_layout) {
            mCurrentContentFragmentLayout = R.layout.note_library_tab_layout;
            ViewGroup viewGroup = findViewById(R.id.container_fragment);
            viewGroup.removeAllViews();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container_fragment, new TabLibraryFragment())
                    .commit();
        } else if (mContentFragment.hasSelection()) {
            mNotesActivityToActionBar.clearNotesSelection();
        } else {
            super.onBackPressed();
        }
    }

    final class NotesActivityToActionBar implements INotesActivityToActionBar {
        private IContentFragment.IActivityListener getActivityListener() {
            return mContentFragment.getActivityListener();
        }

        @Override
        public void deleteSelectedNotes(IDialogResponseCallback dialogResponseCallback) {
            getActivityListener().onDeleteNotes(dialogResponseCallback);
        }

        @Override
        public void clearNotesSelection() {
            getActivityListener().clearNotesSelection();
        }

        @Override
        public boolean isSelectionTriggered() {
            return mContentFragment.hasSelection();
        }

        @Override
        public void onBackPressed() {
            NotesActivity2.this.onBackPressed();
        }

        @Override
        public void onSettingsItemClicked(@SettingsMenuCommand int command) {
            if (command == SettingsMenuCommands.SORT_BY_CREATION_TIME) {
                mContentFragment.orderContentBy(NoteSortManager.BY_CREATION_TIME);
            } else if (command == SettingsMenuCommands.SORT_BY_WORDS_COUNT) {
                mContentFragment.orderContentBy(NoteSortManager.BY_SIZE);
            }
        }

        @Override
        public boolean onClose() {
            return getActivityListener().onClose();
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            return getActivityListener().onQueryTextChange(query);
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return getActivityListener().onQueryTextChange(newText);
        }
    }

    final class NotesActivityBridge implements INotesActivity {
        @Override
        public void setActionBarDeleteButtonVisibility(boolean isVisible) {
            mActionBar.setDeleteButtonVisibility(isVisible);
        }

        @Override
        public ViewGroup getContentViewGroup() {
            return findViewById(R.id.container_fragment);
        }

        @Override
        public void setActionButton(int iconRes, View.OnClickListener onClickListener) {
            mFloatingActionButton.setImageDrawable(
                    ContextCompat.getDrawable(NotesActivity2.this, iconRes));
            mFloatingActionButton.setOnClickListener(onClickListener);
        }

        @Override
        public void replaceContentContainerFragment(Fragment fragment) {
            ViewGroup viewGroup = findViewById(R.id.container_fragment);
            viewGroup.removeAllViews();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container_fragment, fragment)
                    .commit();
            if (fragment instanceof IContentFragment) {
                updateState(((IContentFragment) fragment));
            }
        }

        @Override
        public void updateForegroundFragment(int layoutId) {
            mCurrentContentFragmentLayout = layoutId;
        }

        @Override
        public void updateState(IContentFragment currentContentFragment) {
            mContentFragment = currentContentFragment;
            NotesActivity2.this.setActionButton(currentContentFragment.getActionButtonResource(),
                    currentContentFragment.getActionButtonClickListener());
        }

        @Override
        public AlertDialog.Builder getAlertDialog(
                NotesActivityContentFragment.IDeletionDialogConfig dialogConfig,
                INotesActivityToActionBar.IDialogResponseCallback dialogResponseCallback) {
            AlertDialog.Builder alertBuilder =
                    new android.app.AlertDialog.Builder(NotesActivity2.this);
            alertBuilder
                    .setMessage(dialogConfig.getMessage())
                    .setPositiveButton(
                            dialogConfig.getPositiveButtonText(),
                            dialogConfig.onPositiveButtonClick(dialogResponseCallback))
                    .setNegativeButton(
                            dialogConfig.getNegativeButton(),
                            dialogConfig.onNegativeButtonClick(dialogResponseCallback));
            return alertBuilder;
        }
    }
}
