package com.thunderwiring.kitaba.views;

import android.app.AlertDialog;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;

import com.thunderwiring.kitaba.views.notesLibrary.INotesActivityToActionBar;
import com.thunderwiring.kitaba.views.notesLibrary.contetnt.IContentFragment;
import com.thunderwiring.kitaba.views.notesLibrary.contetnt.NotesActivityContentFragment;

/**
 * Interface between the notes activity and its child fragments.
 */
public interface INotesActivity {
    void setActionBarDeleteButtonVisibility(boolean isVisible);

    ViewGroup getContentViewGroup();

    void setActionButton(int iconRes, View.OnClickListener onClickListener);

    /**
     * Replaces the current content fragment with another fragment.
     */
    void replaceContentContainerFragment(Fragment fragment);

    void updateForegroundFragment(int layoutId);

    void updateState(IContentFragment currentContentFragment);

    AlertDialog.Builder getAlertDialog(
            NotesActivityContentFragment.IDeletionDialogConfig dialogConfig,
            INotesActivityToActionBar.IDialogResponseCallback dialogResponseCallback);
}
