package com.thunderwiring.kitaba.views.notesLibrary.contetnt;

import android.view.View;
import android.widget.SearchView;

import com.thunderwiring.kitaba.views.INotesActivity;
import com.thunderwiring.kitaba.views.notesLibrary.INotesActivityToActionBar;
import com.thunderwiring.kitaba.views.notesLibrary.NotesActivity2;
import com.thunderwiring.kitaba.views.notesLibrary.presenter.IEntriesPresenter;

/**
 * Defines method to be implemented by the fragments that displays the content in the
 * {@link NotesActivity2}
 */
public interface IContentFragment {
    int SORT_BY_TIME = 0;
    int SORT_BY_SIZE = 1;

    /**
     * Returns the resource id of the icon of the action button (i.e. floating action button)
     */
    int getActionButtonResource();

    /**
     * Defines the click handler for the action button.
     */
    View.OnClickListener getActionButtonClickListener();

    /**
     * Returns implementation for {@link IEntriesPresenter}.
     */
    IEntriesPresenter getEntriesPresenter();

    /**
     * Returns true if any of the content entries has a selection.
     */
    boolean hasSelection();

    void orderContentBy(int orderSelector);

    /**
     * Attaches an interface between the Notes A
     */
    void setNotesActivityInterface(INotesActivity bridge);

    IActivityListener getActivityListener();

    void onFragmentSelected();

    /**
     * Interface between the content fragment and the notes activity.
     * This is to provide handles actions from the notes activity.
     */
    interface IActivityListener extends SearchView.OnQueryTextListener,
            SearchView.OnCloseListener {
        void onDeleteNotes(INotesActivityToActionBar.IDialogResponseCallback dialogResponseCallback);

        void clearNotesSelection();
    }
}
