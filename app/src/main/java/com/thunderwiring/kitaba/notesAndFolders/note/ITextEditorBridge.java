package com.thunderwiring.kitaba.notesAndFolders.note;

import java.util.Set;
import java.util.UUID;

/**
 * This interface is used by {@link com.thunderwiring.kitaba.textEditor.TextEditorController} to
 * communicate with {@link NoteController}
 */
public interface ITextEditorBridge {
    /**
     * Returns a collection of the IDs for the activated styles at the current position.
     */
    Set<Integer> getActiveStylesAtPosition();

    UUID getNoteId();

    void syncNoteWithFileSystem();
}
