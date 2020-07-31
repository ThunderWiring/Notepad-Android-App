package com.thunderwiring.kitaba.notesAndFolders.note;

import android.net.Uri;

import com.thunderwiring.kitaba.data.NotePresenterEntity;
import com.thunderwiring.kitaba.files.NotesFileLibrary;
import com.thunderwiring.kitaba.files.presenterFile.NotesPresenterFile;
import com.thunderwiring.kitaba.textEditor.ITextEditorToNoteBridge;

import java.util.Set;
import java.util.UUID;

public class NoteController {

    private ITextEditorToNoteBridge mTextEditorBridge;
    private UUID mId;

    public NoteController(String id) {
        mId = id == null ? UUID.randomUUID() : UUID.fromString(id);
    }

    /**
     * Returns new implementation instance of {@link IToolBarBridge}
     */
    public IToolBarBridge getToolBarBridge() {
        return new ToolBarBridge();
    }

    /**
     * Returns new implementation instance of {@link ITextEditorBridge}
     */
    public ITextEditorBridge getTextEditorBridge() {
        return new TextEditorBridge();
    }

    public void attachTextEditorBridge(ITextEditorToNoteBridge textEditorBridge) {
        mTextEditorBridge = textEditorBridge;
    }

    public void notifyOnImageSelected(Uri imageUri) {
        mTextEditorBridge.insertImage(imageUri.getPath(), "");
    }

    /**
     * Saves the rich text from the note in the filesystem
     */
    public void syncNoteWithFileSystem() {
        NotesFileLibrary fileLibrary = new NotesFileLibrary();
        String richText = mTextEditorBridge.getNoteRichText();
        String noteTitle = mTextEditorBridge.getNoteTitle();

        NotePresenterEntity noteEntity = mTextEditorBridge.getNotePresenterEntity();
        NotesPresenterFile notesPresenterFile = NotesPresenterFile.get();

        boolean isNoteValid = notesPresenterFile.isNoteEntityValid(noteEntity);

        if (!isNoteValid) {
            notesPresenterFile.deleteNote(noteEntity);
            NotesFileLibrary.deleteNoteFile(noteEntity.getId());
            return;
        }

        boolean shouldSync = noteTitle != null && !noteTitle.isEmpty();
        if (shouldSync && fileLibrary.syncNote(mId, richText)) {
            notesPresenterFile.addNoteEntity(noteEntity);
        }
    }

    private final class ToolBarBridge implements IToolBarBridge {
        @Override
        public void onStyleSelected(String styleType) {
            mTextEditorBridge.applyStyle(styleType);
        }

        @Override
        public void onStyleDeselected(String styleType) {
            mTextEditorBridge.removeStyle(styleType);
        }
    }

    private final class TextEditorBridge implements ITextEditorBridge {
        @Override
        public Set<Integer> getActiveStylesAtPosition() {
            return null;
        }

        @Override
        public UUID getNoteId() {
            return mId;
        }

        @Override
        public void syncNoteWithFileSystem() {
            NoteController.this.syncNoteWithFileSystem();
        }
    }
}
