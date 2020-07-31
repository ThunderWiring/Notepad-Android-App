package com.thunderwiring.kitaba.files.presenterFile;

import android.support.annotation.NonNull;

import com.thunderwiring.kitaba.data.FolderPresenterEntity;
import com.thunderwiring.kitaba.data.NotePresenterEntity;
import com.thunderwiring.kitaba.files.NoteFilesEnvironment;
import com.thunderwiring.kitaba.files.NotesFileLibrary;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import static com.google.common.truth.Truth.assertThat;
import static org.powermock.api.mockito.PowerMockito.whenNew;


@RunWith(PowerMockRunner.class)
@PrepareForTest({File.class, NotesFileLibrary.class, NoteFilesEnvironment.class})
public class NotesPresenterFileTest {
    private static final String TEST_FILE_PATH = "testFile.xml";
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

    private static NotePresenterEntity noteEntity3 =
            new NotePresenterEntity.Builder()
                    .setId(UUID.randomUUID().toString())
                    .setTitle("3rd test note")
                    .setWordsCount(3)
                    .setFeatureImagePath("image/path/for/testing.png")
                    .build();

    private NotesPresenterFile notesPresenterFile;

    @Before
    public void setUp() {
        File presenterFile = new File(TEST_FILE_PATH);
        if (presenterFile.exists()) {
            presenterFile.delete();
        }
        notesPresenterFile = NotesPresenterFile.getForTest(TEST_FILE_PATH);
    }

    private FolderPresenterEntity getFolderEntity(@NonNull String name,
                                                  NotePresenterEntity... notes) {
        return new FolderPresenterEntity.Builder()
                .setId(UUID.randomUUID().toString())
                .setNotes(new HashSet<>(Arrays.asList(notes)))
                .setName(name)
                .build();
    }

    private FolderPresenterEntity[] getFoldersArray() {
        return notesPresenterFile.getFolderEntities().toArray(new FolderPresenterEntity[0]);
    }

    @Test
    public void addNote_noDuplications() {
        notesPresenterFile.addNoteEntity(noteEntity1);
        assertThat(notesPresenterFile.getNotesEntities()).containsExactly(noteEntity1);

        notesPresenterFile.addNoteEntity(noteEntity1);
        assertThat(notesPresenterFile.getNotesEntities()).containsExactly(noteEntity1);
    }

    @Test
    public void addEmptyFolder() {
        FolderPresenterEntity folderEntity = getFolderEntity("folder1", noteEntity1);
        notesPresenterFile.addFolder(folderEntity);
        assertThat(notesPresenterFile.getFolderEntities()).containsExactly(folderEntity);
    }

    @Test
    public void addFolderWithNotes() {
        FolderPresenterEntity folderEntity = getFolderEntity("folder1");
        notesPresenterFile.addFolder(folderEntity);
        notesPresenterFile.addNoteEntity(noteEntity1, folderEntity.getId());

        folderEntity = folderEntity.toBuilder().addNote(noteEntity1).build();
        assertThat(notesPresenterFile.getFolderEntities()).containsExactly(folderEntity);

        FolderPresenterEntity expected = getFoldersArray()[0];
        assertThat(expected.getNotes()).containsExactly(noteEntity1);
    }

    @Test
    public void addFolderNotesAndFreeNotes() {
        FolderPresenterEntity folderEntity = getFolderEntity("folder1", noteEntity1);
        notesPresenterFile.addFolder(folderEntity);
        notesPresenterFile.addNoteEntity(noteEntity1, folderEntity.getId());
        notesPresenterFile.addNoteEntity(noteEntity2);

        assertThat(
                notesPresenterFile.getNotesEntities()).containsExactly(noteEntity1, noteEntity2);
        assertThat(notesPresenterFile.getFolderEntities()).containsExactly(folderEntity);
        FolderPresenterEntity expected = notesPresenterFile.getFolderEntities().iterator().next();
        assertThat(expected.getNotes()).containsExactly(noteEntity1);
    }

    @Test
    public void addNoteEntity_nonExistingFolder_fail() {
        notesPresenterFile.addNoteEntity(noteEntity1, "non-existing-folder-id");
        notesPresenterFile.addNoteEntity(noteEntity2);
        notesPresenterFile.addNoteEntity(noteEntity2, "non-existing-folder-id");

        assertThat(notesPresenterFile.getFolderEntities()).isEmpty();
        assertThat(notesPresenterFile.getNotesEntities()).containsExactly(noteEntity2);
    }

    @Test
    public void addNoteEntity_moveNoteToOneFolder() {
        FolderPresenterEntity folderEntity1 = getFolderEntity("folder1");
        FolderPresenterEntity folderEntity2 = getFolderEntity("folder2");

        notesPresenterFile.addNoteEntity(noteEntity1);
        notesPresenterFile.addNoteEntity(noteEntity2);
        notesPresenterFile.addFolder(folderEntity1);
        notesPresenterFile.addFolder(folderEntity2);
        notesPresenterFile.addNoteEntity(noteEntity1, folderEntity1.getId());

        assertThat(notesPresenterFile.getNotesEntities())
                .containsExactly(noteEntity2, noteEntity1);
        assertThat(notesPresenterFile.getFolderEntities().size()).isEqualTo(2);

        FolderPresenterEntity[] folders = getFoldersArray();
        FolderPresenterEntity folder1 = folders[0];
        FolderPresenterEntity folder2 = folders[1];
        if (folder1.getId().equals(folderEntity1.getId())) {
            assertThat(folder1.getNotes()).containsExactly(noteEntity1);
            assertThat(folder2.getNotes()).isEmpty();
        } else {
            assertThat(folder2.getNotes()).containsExactly(noteEntity1);
            assertThat(folder1.getNotes()).isEmpty();
        }
    }

    @Test
    public void addNoteEntity_moveNotesBetweenFolders() {
        FolderPresenterEntity folderEntity1 = getFolderEntity("folder1");
        FolderPresenterEntity folderEntity2 = getFolderEntity("folder2");

        notesPresenterFile.addNoteEntity(noteEntity1);
        notesPresenterFile.addNoteEntity(noteEntity2);
        notesPresenterFile.addFolder(folderEntity1);
        notesPresenterFile.addFolder(folderEntity2);
        notesPresenterFile.addNoteEntity(noteEntity1, folderEntity1.getId());

        assertThat(notesPresenterFile.getNotesEntities())
                .containsExactly(noteEntity2, noteEntity1);
        assertThat(notesPresenterFile.getFolderEntities().size()).isEqualTo(2);

        FolderPresenterEntity[] folders = getFoldersArray();
        FolderPresenterEntity folder1 = folders[0];
        FolderPresenterEntity folder2 = folders[1];
        if (folder1.getId().equals(folderEntity1.getId())) {
            assertThat(folder1.getNotes()).containsExactly(noteEntity1);
            assertThat(folder2.getNotes()).isEmpty();
        } else {
            assertThat(folder2.getNotes()).containsExactly(noteEntity1);
            assertThat(folder1.getNotes()).isEmpty();
        }

        notesPresenterFile.addNoteEntity(noteEntity1, folderEntity2.getId());
        folders = getFoldersArray();
        folder1 = folders[0];
        folder2 = folders[1];
        NotePresenterEntity noteWithParent =
                noteEntity1.toBuilder().setParentFolderId(folderEntity2.getId()).build();
        if (folder1.getId().equals(folderEntity1.getId())) {
            assertThat(folder1.getNotes()).isEmpty();
            assertThat(folder2.getNotes()).containsExactly(noteWithParent);
        } else {
            assertThat(folder1.getNotes()).containsExactly(noteWithParent);
            assertThat(folder2.getNotes()).isEmpty();
        }
    }

    @Test
    public void removeNote_fromExistingFolder() throws Exception {
        FolderPresenterEntity folderEntity1 = getFolderEntity("folder1");
        File testFile = new File(TEST_FILE_PATH);

        whenNew(File.class)
                .withAnyArguments()
                .thenReturn(testFile);

        notesPresenterFile.addNoteEntity(noteEntity2);
        notesPresenterFile.addFolder(folderEntity1);
        notesPresenterFile.addNoteEntity(noteEntity1, folderEntity1.getId());

        assertThat(notesPresenterFile.getNotesEntities())
                .containsExactly(noteEntity2, noteEntity1);
        assertThat(notesPresenterFile.getFolderEntities()).hasSize(1);
        assertThat(getFoldersArray()[0].getNotes()).containsExactly(noteEntity1);

//        notesPresenterFile.deleteNotes(ImmutableSet.of(noteEntity1));

//        assertThat(notesPresenterFile.getNotesEntities())
//                .containsExactly(noteEntity2);
//        assertThat(notesPresenterFile.getFolderEntities().size()).isEqualTo(1);
//        assertThat(getFoldersArray()[0].getNotes()).isEmpty();
    }

    @Test
    public void renameFolder_existingFolder() {
        FolderPresenterEntity folderEntity1 = getFolderEntity("folder1");
        notesPresenterFile.addFolder(folderEntity1);
        notesPresenterFile.renameFolder(folderEntity1, "anotherName");

        assertThat(notesPresenterFile.getFolderEntities()).hasSize(1);
        assertThat(getFoldersArray()[0].getName()).isEqualTo("anotherName");
    }

    @Test
    public void renameFolder_nonExistingFolder() {
        FolderPresenterEntity folderEntity1 = getFolderEntity("folder1");
        FolderPresenterEntity folderEntity2 = getFolderEntity("folder2");
        FolderPresenterEntity folderEntity3 = getFolderEntity("folder3");

        notesPresenterFile.addFolder(folderEntity2);
        notesPresenterFile.renameFolder(folderEntity1, "anotherName1");
        notesPresenterFile.renameFolder(folderEntity3, "anotherName3");

        assertThat(notesPresenterFile.getFolderEntities()).containsExactly(folderEntity2);
    }

    @Test
    public void renameFolder_invalidName() {
        String folderName = "folder1";
        FolderPresenterEntity folderEntity1 = getFolderEntity(folderName);

        notesPresenterFile.addFolder(folderEntity1);
        notesPresenterFile.renameFolder(folderEntity1, null);
        assertThat(notesPresenterFile.getFolderEntities()).containsExactly(folderEntity1);
        assertThat(getFoldersArray()[0].getName()).isEqualTo(folderName);

        notesPresenterFile.renameFolder(folderEntity1, "");
        assertThat(notesPresenterFile.getFolderEntities()).containsExactly(folderEntity1);
        assertThat(getFoldersArray()[0].getName()).isEqualTo(folderName);
    }

    @Test
    public void deleteFolderShallow_folderWithNotes() {
        FolderPresenterEntity folderEntity1 = getFolderEntity("f1", noteEntity1);
        notesPresenterFile.addFolder(folderEntity1);
        notesPresenterFile.addNoteEntity(noteEntity1, folderEntity1.getId());

        assertThat(notesPresenterFile.getNotesEntities()).containsExactly(noteEntity1);
        assertThat(notesPresenterFile.getFolderEntities()).containsExactly(folderEntity1);

        notesPresenterFile.deleteFolderShallow(folderEntity1);
        assertThat(notesPresenterFile.getNotesEntities()).containsExactly(noteEntity1);
        assertThat(notesPresenterFile.getFolderEntities()).isEmpty();
    }

    @Test
    public void deleteFolderShallow_folderWithoutNotes() {
        FolderPresenterEntity folder1 = getFolderEntity("f1");
        notesPresenterFile.addNoteEntity(noteEntity1);
        notesPresenterFile.addFolder(folder1);

        assertThat(notesPresenterFile.getFolderEntities()).containsExactly(folder1);
        assertThat(notesPresenterFile.getNotesEntities()).containsExactly(noteEntity1);

        notesPresenterFile.deleteFolderShallow(folder1);

        assertThat(notesPresenterFile.getFolderEntities()).isEmpty();
        assertThat(notesPresenterFile.getNotesEntities()).containsExactly(noteEntity1);
    }

    /*****************************************************************************************
     * Cannot test remove not due to: java.lang.NoClassDefFoundError: android/os/Environment *
     *****************************************************************************************/

//    @Test
//    public void deleteFolderShallow_folderAfterNotesDeletion() {
//        FolderPresenterEntity f1 = getFolderEntity("f1", noteEntity1);
//        notesPresenterFile.addFolder(f1);
//        notesPresenterFile.addNoteEntity(noteEntity1, f1.getId());
//        notesPresenterFile.addNoteEntity(noteEntity2);
//
//        assertThat(notesPresenterFile.getNotesEntities())
//                .containsExactly(noteEntity1, noteEntity2);
//        assertThat(notesPresenterFile.getFolderEntities()).containsExactly(f1);
//
//        notesPresenterFile.deleteEntry(noteEntity1);
//        assertThat(notesPresenterFile.getNotesEntities()).containsExactly(noteEntity2);
//        assertThat(notesPresenterFile.getFolderEntities())
//                .containsExactly(f1.toBuilder().removeNote(noteEntity1).build());
//
//        notesPresenterFile.deleteFolderShallow(f1);
//        assertThat(notesPresenterFile.getFolderEntities()).isEmpty();
//        assertThat(notesPresenterFile.getNotesEntities()).containsExactly(noteEntity2);
//    }

    @Test
    public void deleteFolderDeep_folderWithNotes() {
        FolderPresenterEntity f1 = getFolderEntity("f1", noteEntity1, noteEntity2);

        notesPresenterFile.addFolder(f1);
        notesPresenterFile.addNoteEntity(noteEntity1, f1.getId());
        notesPresenterFile.addNoteEntity(noteEntity2, f1.getId());
        notesPresenterFile.addNoteEntity(noteEntity3);

        assertThat(notesPresenterFile.getNotesEntities()).containsExactly(noteEntity3,
                noteEntity2, noteEntity1);
        assertThat(notesPresenterFile.getFolderEntities()).containsExactly(f1);

        notesPresenterFile.deleteFolderDeep(f1);
        assertThat(notesPresenterFile.getNotesEntities()).containsExactly(noteEntity3);
        assertThat(notesPresenterFile.getFolderEntities()).isEmpty();

    }
}
