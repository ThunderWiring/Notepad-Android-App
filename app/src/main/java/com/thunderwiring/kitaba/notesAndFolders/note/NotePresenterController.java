package com.thunderwiring.kitaba.notesAndFolders.note;

import com.thunderwiring.kitaba.data.IPresenterEntity;
import com.thunderwiring.kitaba.data.NotePresenterEntity;
import com.thunderwiring.kitaba.files.presenterFile.NotesPresenterFile;
import com.thunderwiring.kitaba.notesAndFolders.PresenterControllerBase;
import com.thunderwiring.kitaba.views.notesLibrary.presenter.NotesPresenter;

/**
 * Controls {@link NotesPresenter}
 */
public class NotePresenterController extends PresenterControllerBase {

    @Override
    public void deleteEntry(IPresenterEntity entry) {
        if (entry instanceof NotePresenterEntity) {
            NotePresenterEntity note = (NotePresenterEntity)entry;
            NotesPresenterFile.get().deleteNote(note);
        }
    }
}
