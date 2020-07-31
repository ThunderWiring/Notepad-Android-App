package com.thunderwiring.kitaba.note;

import com.google.common.collect.ImmutableSet;
import com.thunderwiring.kitaba.data.NotePresenterEntity;
import com.thunderwiring.kitaba.files.presenterFile.NotesPresenterFile;
import com.thunderwiring.kitaba.notesAndFolders.note.NoteLibrarySearch;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.UUID;

import static com.google.common.truth.Truth.assertThat;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NotesPresenterFile.class})
public class NoteLibrarySearchTest {

    private static NotePresenterEntity noteEntity1 =
            new NotePresenterEntity.Builder()
                    .setId(UUID.randomUUID().toString())
                    .setTitle("title text to search for")
                    .build();

    private static NotePresenterEntity noteEntity2 =
            new NotePresenterEntity.Builder()
                    .setId(UUID.randomUUID().toString())
                    .setTitle("another title for another note")
                    .build();

    @Mock
    private NotesPresenterFile mockNotesPresenterFile;

    @Before
    public void setUp() {
        try {
            when(mockNotesPresenterFile.getNotesEntities())
                    .thenReturn(ImmutableSet.of(noteEntity1, noteEntity2));
            whenNew(NotesPresenterFile.class)
                    .withNoArguments()
                    .thenReturn(mockNotesPresenterFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getNotesMatches_matchTitle() {
        NoteLibrarySearch search = new NoteLibrarySearch();
        String searchQuery = "text";
        assertThat(search.getNotesMatches(searchQuery)).containsExactly(noteEntity1);
    }
}