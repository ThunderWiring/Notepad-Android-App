package com.thunderwiring.kitaba.files.presenterFile;

import android.support.annotation.NonNull;

import com.thunderwiring.kitaba.data.FolderPresenterEntity;
import com.thunderwiring.kitaba.data.NotePresenterEntity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import static com.google.common.truth.Truth.assertThat;

@RunWith(PowerMockRunner.class)
public class NotesFileStateCacheTest {
    private static NotePresenterEntity noteEntity1 =
            new NotePresenterEntity.Builder()
                    .setId(UUID.randomUUID().toString())
                    .setTitle("title text to search for")
                    .build();

    private static NotePresenterEntity noteEntity2 =
            new NotePresenterEntity.Builder()
                    .setId(UUID.randomUUID().toString())
                    .setTitle("second test note")
                    .setWordsCount(3)
                    .setFeatureImagePath("image/path/for/testing.png")
                    .build();

    private NotesFileStateCache notesFileStateCache;

    @Before
    public void setup() {
        notesFileStateCache = new NotesFileStateCache();
    }

    private FolderPresenterEntity getFolderEntity(@NonNull String name,
                                                  NotePresenterEntity... notes) {
        return new FolderPresenterEntity.Builder()
                .setId(UUID.randomUUID().toString())
                .setNotes(new HashSet<>(Arrays.asList(notes)))
                .setName(name)
                .build();
    }

    @Test
    public void addNoteToFolder_moveNoteBetweenFolders() {
        FolderPresenterEntity folder1 = getFolderEntity("folder1");
        FolderPresenterEntity folder2 = getFolderEntity("folder2");
        notesFileStateCache.addNote(noteEntity1);
        notesFileStateCache.addNote(noteEntity2);
        notesFileStateCache.addFolder(folder1);
        notesFileStateCache.addFolder(folder2);

        notesFileStateCache.moveNoteToFolder(noteEntity1, folder1.getId());

        assertThat(notesFileStateCache.getFolderEntity(folder1.getId()).getNotes()).containsExactly(noteEntity1);

        notesFileStateCache.moveNoteToFolder(noteEntity1, folder2.getId());

        assertThat(notesFileStateCache.getFolderEntity(folder1.getId()).getNotes()).isEmpty();
        assertThat(notesFileStateCache.getFolderEntity(folder2.getId()).getNotes()).containsExactly(noteEntity1);

    }
}