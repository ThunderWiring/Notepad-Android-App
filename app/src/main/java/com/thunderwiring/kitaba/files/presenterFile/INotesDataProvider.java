package com.thunderwiring.kitaba.files.presenterFile;

import com.google.common.collect.ImmutableSet;
import com.thunderwiring.kitaba.data.NotePresenterEntity;

/**
 * Provides data for the notes presenter
 * */
public interface INotesDataProvider {
    ImmutableSet<NotePresenterEntity> getNotesEntities();

    void addNoteEntity(NotePresenterEntity noteEntity);
}
