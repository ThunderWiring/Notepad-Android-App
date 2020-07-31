package com.thunderwiring.kitaba.notesAndFolders.note;

/**
 * This interface is used by {@link NoteController} to send commands to the tool bar.
 */
public interface IToolBarBridge {
    /**
     * Defines the action when a style is selected from the toolbar.
     *
     * @param styleType id of the style.
     */
    void onStyleSelected(String styleType);

    /**
     * Defined the action when a style is deselected from the toolbar
     */
    void onStyleDeselected(String styleType);
}
