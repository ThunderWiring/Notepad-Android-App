package com.thunderwiring.kitaba.notesAndFolders.folder;

import com.thunderwiring.kitaba.data.FolderPresenterEntity;
import com.thunderwiring.kitaba.data.IPresenterEntity;
import com.thunderwiring.kitaba.files.presenterFile.NotesPresenterFile;
import com.thunderwiring.kitaba.notesAndFolders.PresenterControllerBase;

public class FolderPresenterController extends PresenterControllerBase {
    @Override
    public void deleteEntry(IPresenterEntity entry) {
        if (!(entry instanceof FolderPresenterEntity)) {
            return;
        }
        FolderPresenterEntity folderEntity = (FolderPresenterEntity) entry;
        NotesPresenterFile.get().deleteFolderShallow(folderEntity);
    }

    /**
     * Returns number of folders with the given name.
     */
    public int getFoldersWithName(String name) {
        int count = 0;
        for (FolderPresenterEntity folder : NotesPresenterFile.get().getFolderEntities()) {
            if (folder.getName().equals(name)) {
                count++;
            }
        }

        return count;
    }
}
