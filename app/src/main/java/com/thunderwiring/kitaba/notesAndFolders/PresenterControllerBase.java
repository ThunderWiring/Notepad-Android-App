package com.thunderwiring.kitaba.notesAndFolders;

import com.google.common.collect.ImmutableSet;
import com.thunderwiring.kitaba.data.IPresenterEntity;
import com.thunderwiring.kitaba.files.presenterFile.NotesPresenterFile;

import java.util.HashSet;
import java.util.Set;

public abstract class PresenterControllerBase {
    private Set<IPresenterEntity> mSelectedEntities;

    public PresenterControllerBase() {
        mSelectedEntities = new HashSet<>();
    }

    public void onPresenterEntityChecked(IPresenterEntity entity, boolean isSelected) {
        if (isSelected) {
            mSelectedEntities.add(entity);
        } else {
            mSelectedEntities.remove(entity);
        }
    }

    /**
     * Return true if there are selected notes.
     */
    public boolean hasEntrySelection() {
        return mSelectedEntities.size() > 0;
    }

    public ImmutableSet<IPresenterEntity> getSelectedEntities() {
        return ImmutableSet.copyOf(mSelectedEntities);
    }

    public void clearSelection() {
        mSelectedEntities.clear();
    }

    public abstract void deleteEntry(IPresenterEntity entry);

    public boolean isUpToDate() {
        return NotesPresenterFile.get().isUpToDate();
    }

    public void markUpToDate() {
        NotesPresenterFile.get().markClean();
    }

}
