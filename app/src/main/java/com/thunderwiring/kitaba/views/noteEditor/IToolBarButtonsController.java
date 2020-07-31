package com.thunderwiring.kitaba.views.noteEditor;

/**
 * Implemented by {@link ToolBarFragment} to provide a way for other controllers to control the
 * appearance of the buttons in the toolbar.
 */
public interface IToolBarButtonsController {

    /**
     * Mark all the style buttons as not-selected
     */
    void deselectAllStyles();

    /**
     * Marks the specified style as selected
     */
    void selectStyle(String styleType);

    /**
     * Marks the specified style as selected
     */
    void deselectStyle(String styleType);

    /**
     * Returns true if the style is set, or false if it is not or if the passed {@code styleType}
     * is not a valid style.
     */
    boolean isStyleSet(String styleType);
}
