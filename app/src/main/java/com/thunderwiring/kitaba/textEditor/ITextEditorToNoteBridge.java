package com.thunderwiring.kitaba.textEditor;

import com.thunderwiring.kitaba.data.NotePresenterEntity;
import com.thunderwiring.kitaba.notesAndFolders.note.NoteController;

/**
 * This interface is implemented by {@link TextEditorController} and used to send commands to
 * {@link NoteController}
 */
public interface ITextEditorToNoteBridge {
    /**
     * Applies the specified style to the current selection.
     */
    void applyStyle(String styleType);

    /**
     * Removes the specified style from the current selection.
     */
    void removeStyle(String styleType);

    void insertImage(String imagePath, String caption);

    void applyAction(String actionType);

    /**
     * Returns the rich text of the note.
     */
    String getNoteRichText();

    String getNoteTitle();

    /**
     * Returns an instance of {@link NotePresenterEntity} for the current note.
     */
    NotePresenterEntity getNotePresenterEntity();

}
