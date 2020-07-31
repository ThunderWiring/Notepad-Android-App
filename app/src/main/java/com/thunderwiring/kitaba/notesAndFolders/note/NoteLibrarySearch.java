package com.thunderwiring.kitaba.notesAndFolders.note;

import com.google.common.collect.ImmutableSet;
import com.thunderwiring.kitaba.data.NotePresenterEntity;
import com.thunderwiring.kitaba.files.NoteFilesEnvironment;
import com.thunderwiring.kitaba.files.presenterFile.NotesPresenterFile;
import com.thunderwiring.kitaba.textEditor.webview.WebViewTextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

public class NoteLibrarySearch {
    private static final String WORD_SPLIT_DELIMITER = " ";
    private static final float MATCH_CONFIDENCE = 0.3f;
    private static final float DELTA = 0.00001f;

    public ImmutableSet<NotePresenterEntity> getNotesMatches(String query) {
        if (query == null || query.isEmpty()) {
            return ImmutableSet.of();
        }
        query = query.toLowerCase();
        Set<NotePresenterEntity> notesEntities = NotesPresenterFile.get().getNotesEntities();
        ImmutableSet.Builder<NotePresenterEntity> matchesBuilder = new ImmutableSet.Builder<>();
        for (NotePresenterEntity noteEntity : notesEntities) {
            if (doesNoteMatchQuery(noteEntity, query)) {
                matchesBuilder.add(noteEntity);
            }
        }
        return matchesBuilder.build();
    }

    private boolean doesNoteMatchQuery(NotePresenterEntity noteEntity, String searchQuery) {
        String noteTitle = noteEntity.getTitle();
        String noteSummary = noteEntity.getSummary();
        return isMatch(getMatchConfidence(searchQuery, noteTitle))
                || isMatch(getMatchConfidence(searchQuery, noteSummary))
                || doesQueryAppearInNote(searchQuery, noteEntity);
    }

    private boolean isMatch(float confidence) {
        return confidence - MATCH_CONFIDENCE >= DELTA;
    }

    private float getMatchConfidence(String query, String text) {
        String[] queryWords = query.split(WORD_SPLIT_DELIMITER);
        int intersectionSize = 0;
        for (String word : queryWords) {
            intersectionSize += getOccurrencesInText(word, text).size();
        }
        int numberOfWordsInText = text.split(WORD_SPLIT_DELIMITER).length;
        return (2.0f * intersectionSize) / (numberOfWordsInText + queryWords.length);
    }

    private boolean doesQueryAppearInNote(String searchQuery, NotePresenterEntity noteEntity) {
        UUID noteId = UUID.fromString(noteEntity.getId());
        File noteFile = new File(NoteFilesEnvironment.getNoteFileName(noteId));
        String[] searchWords = searchQuery.split(WORD_SPLIT_DELIMITER);
        try {
            Scanner noteScanner = new Scanner(noteFile);
            while (noteScanner.hasNextLine()) {
                String line = escapeHtml(noteScanner.nextLine().toLowerCase());
                Map<String, Set<Integer>> occurrences = getOccurrencesInText(searchWords, line);
                if (occurrences.isEmpty()) {
                    continue;
                }
                return true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Extracts only the text from an html string - removes the html tags.
     */
    private String escapeHtml(String html) {
        return WebViewTextUtils.getPlainTextFromHtml(html);
    }

    /**
     * Finds the occurrences of a search word inside a text, and returns a set of the indexes of
     * those occurrences.
     */
    private Set<Integer> getOccurrencesInText(String word, String text) {
        Set<Integer> occurrences = new HashSet<>();
        text = text.toLowerCase();
        int startIndex = 0;
        while (startIndex < text.length()) {
            int match = text.indexOf(word, startIndex);
            if (match >= 0) {
                occurrences.add(match);
            }
            startIndex++;
        }
        return occurrences;
    }

    /**
     * Returns a map of all the occurrences
     */
    private Map<String, Set<Integer>> getOccurrencesInText(String[] words, String text) {
        Map<String, Set<Integer>> occurrences = new HashMap<>();
        for (String word : words) {
            Set<Integer> wordOccurrences = getOccurrencesInText(word, text);
            if (wordOccurrences.isEmpty()) {
                continue;
            }
            occurrences.put(word, wordOccurrences);
        }
        return occurrences;
    }
}
