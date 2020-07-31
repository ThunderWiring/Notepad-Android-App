package com.thunderwiring.kitaba.views.notesLibrary.presenter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.google.common.collect.ImmutableList;
import com.thunderwiring.kitaba.data.IPresenterEntity;
import com.thunderwiring.kitaba.notesAndFolders.PresenterControllerBase;
import com.thunderwiring.kitaba.notesAndFolders.note.NoteSortManager;
import com.thunderwiring.kitaba.views.notesLibrary.INotesActivityToActionBar;
import com.thunderwiring.kitaba.views.notesLibrary.NotesActivity2;

public interface IEntriesPresenter {

    /**
     * Updates the entries content.
     */
    void refreshEntries();

    /**
     * Returns an immutable collection of the entries views.
     */
    ImmutableList<View> getEntriesViews();

    /**
     * This interface is implemented by {@link NotesActivity2} to give {@link IEntriesPresenter} the
     * ability to inflate new note items and manage their presentation and behavior.
     */
    interface INoteActivityToEntryView {
        /**
         * Returns the view for the note entry item.
         */
        View inflateEntryItemView();

        /**
         * Returns the intent used for starting
         * {@link com.thunderwiring.kitaba.views.noteEditor.NoteEditorActivity}
         */
        Intent getStartNoteEditorActivityIntent();

        void navigateToContentFragment(Fragment fragment);

        PresenterControllerBase getController();

        void setDeleteButtonVisibility(boolean isVisible);

        void deleteEntry(IPresenterEntity entity,
                         INotesActivityToActionBar.IDialogResponseCallback dialogResponseCallback
                , Bundle bundle);

        void updateContentFragments();

        void updateForegroundFragment(int layoutId);

        /**
         * Returns the criteria regarding how to sort the entries. e.g.
         * {@link NoteSortManager#BY_SIZE}
         */
        int getSortSelector();
    }
}
