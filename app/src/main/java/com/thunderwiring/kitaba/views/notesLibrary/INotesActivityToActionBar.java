package com.thunderwiring.kitaba.views.notesLibrary;

import android.widget.SearchView;

import com.thunderwiring.kitaba.views.SettingsMenuCommand;

/**
 * This interface is implemented by {@link NotesActivity2} to provide the action bar with the
 * ability to manage the presentation and control the behavior of its controls (i.e. the delete
 * notes button)
 */
public interface INotesActivityToActionBar extends SearchView.OnQueryTextListener,
        SearchView.OnCloseListener {

    void deleteSelectedNotes(IDialogResponseCallback dialogResponseCallback);

    void clearNotesSelection();

    boolean isSelectionTriggered();

    void onBackPressed();

    /**
     * Notifies the activity about the clicked settings item from the settings popup menu.
     */
    void onSettingsItemClicked(@SettingsMenuCommand int command);

    interface IDialogResponseCallback {
        void onPositiveResponse();

        void onNegativeResponse();
    }
}

