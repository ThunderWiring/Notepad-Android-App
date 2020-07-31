package com.thunderwiring.kitaba.files;

import com.google.common.collect.ImmutableSet;
import com.thunderwiring.kitaba.data.NotePresenterEntity;
import com.thunderwiring.kitaba.files.presenterFile.NotesPresenterCache;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.google.common.truth.Truth.assertThat;


public class NotesPresenterCacheTest {

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

    private Set<NotePresenterEntity> notes;

    @Before
    public void setup() {
        notes = new HashSet<>(Arrays.asList(noteEntity1, noteEntity2));
    }

    @Test
    public void getEntities_noChanges() {
        NotesPresenterCache cache = new NotesPresenterCache(ImmutableSet.copyOf(notes));
        assertThat(cache.getNoteEntities()).containsExactly(noteEntity1, noteEntity2);
    }

    @Test
    public void getEntities_addNewNote() {
        NotesPresenterCache cache = new NotesPresenterCache(ImmutableSet.copyOf(notes));
        NotePresenterEntity newNoteEntity =
                new NotePresenterEntity.Builder().setId(UUID.randomUUID().toString()).build();
        cache.addNote(newNoteEntity);
        assertThat(cache.getNoteEntities()).containsExactly(noteEntity2, noteEntity1, newNoteEntity);
    }

    @Test
    public void getEntities_updateExistingNote() {
        NotesPresenterCache cache = new NotesPresenterCache(ImmutableSet.copyOf(notes));
        NotePresenterEntity updatedNote = noteEntity1.toBuilder().setSummary("new summary").build();
        cache.addNote(updatedNote);
        assertThat(cache.getNoteEntities()).containsExactly(noteEntity2, updatedNote);
    }
}