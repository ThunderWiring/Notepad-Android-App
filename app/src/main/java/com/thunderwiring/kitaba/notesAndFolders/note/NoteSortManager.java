package com.thunderwiring.kitaba.notesAndFolders.note;

import com.google.common.collect.ImmutableList;
import com.thunderwiring.kitaba.data.NotePresenterEntity;

import java.util.Set;

/**
 * Sorts a collection of {@link NotePresenterEntity} according to a selected criteria.
 */
public class NoteSortManager {
    /**
     * Sorts according to the creation time of the item
     * (e.g. last time the note has been edited).
     */
    public static final int BY_CREATION_TIME = 0;
    /**
     * Sorts by the size of the item (e.g. number of words in a note).
     */
    public static final int BY_SIZE = 2;

    /**
     * Basically preserve the original random order.
     */
    public static final int NONE = 3;

    /**
     * Sorts a collection of {@link NotePresenterEntity} according to the selected sorting
     * parameter. (see {@link #BY_CREATION_TIME}, {@link #BY_SIZE})
     */
    public static ImmutableList<NotePresenterEntity> sort(int sortSelector,
                                                          Set<NotePresenterEntity> notes) {
        if (sortSelector == NONE) {
            return ImmutableList.copyOf(notes);
        } else if (sortSelector == BY_CREATION_TIME) {
            return sortByCreationTime(notes);
        }
        return sortBySize(notes);
    }

    private static ImmutableList<NotePresenterEntity> sortByCreationTime(
            Set<NotePresenterEntity> notes) {
        return ImmutableList.sortedCopyOf(
                (o1, o2) -> o2.getEditDate().compareTo(o1.getEditDate()), notes);
    }

    private static ImmutableList<NotePresenterEntity> sortBySize(Set<NotePresenterEntity> notes) {
        return ImmutableList.sortedCopyOf(
                (o1, o2) -> Integer.compare(o2.getWordsCount(), o1.getWordsCount()), notes);
    }
}
