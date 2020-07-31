package com.thunderwiring.kitaba.views.notesLibrary.contetnt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.common.collect.ImmutableSet;
import com.thunderwiring.kitaba.R;
import com.thunderwiring.kitaba.data.IPresenterEntity;
import com.thunderwiring.kitaba.notesAndFolders.PresenterControllerBase;
import com.thunderwiring.kitaba.notesAndFolders.note.NotePresenterController;
import com.thunderwiring.kitaba.views.noteEditor.NoteEditorActivity;
import com.thunderwiring.kitaba.views.notesLibrary.presenter.IEntriesPresenter;
import com.thunderwiring.kitaba.views.notesLibrary.presenter.NotesPresenter;

public class AllNotesFragment extends NotesActivityContentFragment {

    private NotePresenterController mNotePresenterController;

    public AllNotesFragment() {
        super();
        mNotePresenterController = new NotePresenterController();
    }

    @Override
    public int getActionButtonResource() {
        return R.drawable.write_new_note;
    }

    @Override
    protected int getContentItemLayoutRes() {
        return R.layout.note_library_item_layout;
    }

    @Override
    protected PresenterControllerBase getPresenterController() {
        return mNotePresenterController;
    }

    @Override
    public View.OnClickListener getActionButtonClickListener() {
        return v -> {
            Intent startNewNote = new Intent(
                    getActivity().getApplicationContext(), NoteEditorActivity.class);
            startActivity(startNewNote);
        };
    }

    @Override
    public int getEmptyStateLayoutId() {
        return R.layout.notes_library_empty_state_layout;
    }

    @Override
    public IEntriesPresenter getEntriesPresenter() {
        return new NotesPresenter(getActivity(), getNoteEntryViewFactory());
    }

    @Override
    public IDeletionDialogConfig getDeletionDialogConfig(
            ImmutableSet<IPresenterEntity> entitiesToDelete, Bundle bundle) {
        return new DeletionDialogConfig(mNotePresenterController, entitiesToDelete);
    }
}
