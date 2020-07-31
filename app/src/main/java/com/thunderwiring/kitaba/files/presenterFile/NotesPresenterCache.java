package com.thunderwiring.kitaba.files.presenterFile;

import com.google.common.collect.ImmutableSet;
import com.thunderwiring.kitaba.data.FolderPresenterEntity;
import com.thunderwiring.kitaba.data.NotePresenterEntity;

import java.util.HashSet;
import java.util.Set;

/**
 * Caches the <code>NotePresenterEntity</code> objects that are loaded from the file system.
 */
public class NotesPresenterCache {

    private boolean mIsDirty;
    private Set<NotePresenterEntity> mNoteEntitiesCache;
    private Set<FolderPresenterEntity> mFolderEntitiesCache;
    private Set<String> mCachedNotesIds;
    private Set<String> mCachedFolderIds;
    private NotesFileStateCache mStateCache;

    public NotesPresenterCache(ImmutableSet<NotePresenterEntity> noteEntities,
                               ImmutableSet<FolderPresenterEntity> folderEntities) {
        mIsDirty = false;
        mNoteEntitiesCache = new HashSet<>(noteEntities);
        mFolderEntitiesCache = new HashSet<>(folderEntities);
        mCachedNotesIds = new HashSet<>();
        mCachedFolderIds = new HashSet<>();
        mStateCache = new NotesFileStateCache(noteEntities, folderEntities);

        for (NotePresenterEntity noteEntity : noteEntities) {
            mCachedNotesIds.add(noteEntity.getId());
        }
        for (FolderPresenterEntity folderEntity : folderEntities) {
            mCachedFolderIds.add(folderEntity.getId());
        }
    }

    public NotesPresenterCache(ImmutableSet<NotePresenterEntity> noteEntities) {
        this(noteEntities, ImmutableSet.of());
    }

    public boolean isDirty() {
        return mIsDirty;
    }

    public void markClean() {
        mIsDirty = false;
    }

    public FolderPresenterEntity getFolder(String folderId) {
        return mStateCache.getFolderEntity(folderId);
    }

    public ImmutableSet<NotePresenterEntity> getNoteEntities() {
        return mStateCache.getNoteEntities();
    }

    public ImmutableSet<FolderPresenterEntity> getFolderEntities() {
        return mStateCache.getFolderEntities();
    }

    public String getNoteParentFolderId(NotePresenterEntity noteEntity) {
        return mStateCache.getNoteParentFolderId(noteEntity.getId());
    }

    public void addFolder(FolderPresenterEntity folderEntity) {
        if (folderEntity == null) {
            return;
        } else if (mCachedFolderIds.contains(folderEntity.getId())) {
            mFolderEntitiesCache.remove(folderEntity);
        }
        mCachedFolderIds.add(folderEntity.getId());
        mFolderEntitiesCache.add(folderEntity);
        mStateCache.addFolder(folderEntity);
        mIsDirty = true;
    }

    public void removeFolder(FolderPresenterEntity folderEntity) {
        if (folderEntity == null) {
            return;
        }
        mFolderEntitiesCache.remove(folderEntity);
        mCachedFolderIds.remove(folderEntity.getId());
        mStateCache.removeFolder(folderEntity.getId());
        mIsDirty = true;
    }

    public void renameFolder(FolderPresenterEntity folder, String newName) {
        if (folder == null || newName == null || newName.isEmpty()
                || !mCachedFolderIds.contains(folder.getId())) {
            return;
        }
        mFolderEntitiesCache.remove(folder);
        mFolderEntitiesCache.add(folder.toBuilder().setName(newName).build());
        mStateCache.renameFolder(folder, newName);
        mIsDirty = true;
    }

    public void addNoteToFolder(NotePresenterEntity noteEntity, String folderId) {
        if (folderId == null || folderId.isEmpty() || !isValidNote(noteEntity)
                || !mCachedNotesIds.contains(noteEntity.getId())
                || !mCachedFolderIds.contains(folderId)) {
            return;
        }
        for (FolderPresenterEntity folderEntity : mFolderEntitiesCache) {
            if (folderEntity.getId().equals(folderId)) {
                FolderPresenterEntity target =
                        folderEntity.toBuilder().addNote(noteEntity).build();
                mFolderEntitiesCache.remove(folderEntity);
                mFolderEntitiesCache.add(target);
                break;
            }
        }
        mStateCache.moveNoteToFolder(noteEntity, folderId);
        mIsDirty = true;
    }

    public void addNote(NotePresenterEntity noteEntity) {
        if (!isValidNote(noteEntity)) {
            return;
        } else if (mCachedNotesIds.contains(noteEntity.getId())) {
            removeNoteEntityWithId(noteEntity.getId());
        }
        addNoteEntity(noteEntity);
        mStateCache.addNote(noteEntity);
        mIsDirty = true;
    }

    public void removeNote(NotePresenterEntity noteEntity) {
        if (!isValidNote(noteEntity) || !mCachedNotesIds.contains(noteEntity.getId())) {
            return;
        }
        removeNoteEntityWithId(noteEntity.getId());
        mStateCache.removeNote(noteEntity.getId());
        mIsDirty = true;
    }

    public boolean containsFolder(String folderId) {
        return mStateCache.containsFolder(folderId);
    }

    private void removeNoteEntityWithId(String id) {
        for (NotePresenterEntity noteEntity : mNoteEntitiesCache) {
            if (noteEntity.getId().equals(id)) {
                removeNoteEntity(noteEntity);
                break;
            }
        }
    }

    private void removeNoteEntity(NotePresenterEntity noteEntity) {
        mCachedNotesIds.remove(noteEntity.getId());
        mNoteEntitiesCache.remove(noteEntity);
    }

    private void addNoteEntity(NotePresenterEntity noteEntity) {
        mCachedNotesIds.add(noteEntity.getId());
        mNoteEntitiesCache.add(noteEntity);
    }

    private boolean isValidNote(NotePresenterEntity noteEntity) {
        return noteEntity != null
                && !NotePresenterEntity.getDefaultInstance().equals(noteEntity);
    }
}
