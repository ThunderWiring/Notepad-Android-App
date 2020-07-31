package com.thunderwiring.kitaba.files.presenterFile;

import com.google.common.collect.ImmutableSet;
import com.thunderwiring.kitaba.data.FolderPresenterEntity;
import com.thunderwiring.kitaba.data.NotePresenterEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class caches the relation between the notes and the folders.
 */
public class NotesFileStateCache {
    private static final String NO_FOLDER = "";

    private Map<String, FolderPresenterEntity> mFolderMapper;
    private Map<String, NotePresenterEntity> mNotesMapper;

    private Map<String, Set<String>> mFolderHierarchy;

    NotesFileStateCache() {
        this(ImmutableSet.of(), ImmutableSet.of());
    }

    NotesFileStateCache(ImmutableSet<NotePresenterEntity> notes,
                        ImmutableSet<FolderPresenterEntity> folders) {
        mFolderMapper = new HashMap<>();
        mNotesMapper = new HashMap<>();
        mFolderHierarchy = new HashMap<>();

        for (NotePresenterEntity note : notes) {
            mNotesMapper.put(note.getId(), note);
        }

        for (FolderPresenterEntity folder : folders) {
            String folderId = folder.getId();
            mFolderMapper.put(folderId, folder);
            mFolderHierarchy.put(folderId, new HashSet<>());
            for (NotePresenterEntity note : folder.getNotes()) {
                mFolderHierarchy.get(folderId).add(note.getId());
            }
        }
    }

    void addNote(NotePresenterEntity note) {
        mNotesMapper.put(note.getId(), note);
        String parentFolderId = note.getParentFolderId();
        if (!parentFolderId.isEmpty()) {
            mFolderHierarchy.get(parentFolderId).add(note.getId());
            FolderPresenterEntity updatedFolder =
                    mFolderMapper.get(parentFolderId).toBuilder().addNote(note).build();
            mFolderMapper.put(parentFolderId, updatedFolder);
        }
    }

    void removeNote(String noteId) {
        if (!mNotesMapper.containsKey(noteId)) {
            return;
        }
        NotePresenterEntity note = mNotesMapper.get(noteId);
        String parentFolderId = note != null
                ? note.getParentFolderId() : NO_FOLDER;
        if (!NO_FOLDER.equals(parentFolderId)
                && mFolderHierarchy.containsKey(parentFolderId)) {
            mFolderHierarchy.get(parentFolderId).remove(noteId);
            FolderPresenterEntity updatedFolder =
                    mFolderMapper.get(parentFolderId).toBuilder().removeNote(note).build();
            mFolderMapper.put(parentFolderId, updatedFolder);
        }
        mNotesMapper.remove(noteId);
    }

    String getNoteParentFolderId(String noteId) {
        if (!mNotesMapper.containsKey(noteId)) {
            return NO_FOLDER;
        }
        return mNotesMapper.get(noteId).getParentFolderId();
    }

    void addFolder(FolderPresenterEntity folder) {
        if (mFolderMapper.containsKey(folder.getId())) {
            return;
        }
        mFolderMapper.put(folder.getId(), folder);
        mFolderHierarchy.put(folder.getId(), new HashSet<>());
        for (NotePresenterEntity note : folder.getNotes()) {
            mFolderHierarchy.get(folder.getId()).add(note.getId());
        }
    }

    boolean containsFolder(String folderId) {
        return mFolderMapper.containsKey(folderId);
    }

    FolderPresenterEntity getFolderEntity(String folderId) {
        if (!mFolderMapper.containsKey(folderId)) {
            return FolderPresenterEntity.defaultInstance();
        }
        return mFolderMapper.get(folderId);
    }

    void renameFolder(FolderPresenterEntity folder, String newName) {
        if (folder == null || newName == null || newName.isEmpty()
                || !mFolderMapper.containsKey(folder.getId())) {
            return;
        }
        mFolderMapper.put(folder.getId(), folder.toBuilder().setName(newName).build());
    }

    /**
     * Removes the folder and if there are notes associated with it, then set the parent folder
     * of those notes to none.
     */
    void removeFolder(String folderId) {
        if (!mFolderMapper.containsKey(folderId)) {
            return;
        }
        mFolderMapper.remove(folderId);
        for (String noteId : mFolderHierarchy.get(folderId)) {
            NotePresenterEntity note = mNotesMapper.get(noteId);
            mNotesMapper.put(noteId, note.toBuilder().setParentFolderId(NO_FOLDER).build());
        }
        mFolderHierarchy.remove(folderId);
    }

    void moveNoteToFolder(NotePresenterEntity note, String folderId) {
        if (!mNotesMapper.containsKey(note.getId())) {
            return;
        }

        NotePresenterEntity oldNote = mNotesMapper.get(note.getId());
        if (oldNote != null) {
            String oldNoteParentFolder = oldNote.getParentFolderId();
            removeNoteFromFolder(oldNote.getId(), oldNoteParentFolder);
            addNoteToFolder(note.getId(), folderId);
        }
    }

    ImmutableSet<FolderPresenterEntity> getFolderEntities() {
        return ImmutableSet.copyOf(mFolderMapper.values());
    }

    ImmutableSet<NotePresenterEntity> getNoteEntities() {
        return ImmutableSet.copyOf(mNotesMapper.values());
    }

    /**
     * Removes the association between a note and a folder.
     */
    private void removeNoteFromFolder(String noteId, String folderId) {
        NotePresenterEntity note = mNotesMapper.get(noteId);
        FolderPresenterEntity folder = mFolderMapper.get(folderId);
        if (folder == null || note == null) {
            return;
        }
        mFolderHierarchy.get(folderId).remove(noteId);
        mFolderMapper.put(folderId, folder.toBuilder().removeNote(note).build());

        mNotesMapper.put(noteId, note.toBuilder().setParentFolderId(NO_FOLDER).build());
    }

    private void addNoteToFolder(String noteId, String folderId) {
        NotePresenterEntity note = mNotesMapper.get(noteId);
        FolderPresenterEntity folder = mFolderMapper.get(folderId);
        if (folder == null || note == null) {
            return;
        }
        note = note.toBuilder().setParentFolderId(folderId).build();
        mFolderMapper.put(folderId, folder.toBuilder().addNote(note).build());
        if (mFolderHierarchy.get(folderId) != null) {
            mFolderHierarchy.get(folderId).add(noteId);
        }
        mNotesMapper.put(noteId, note);
    }
}
