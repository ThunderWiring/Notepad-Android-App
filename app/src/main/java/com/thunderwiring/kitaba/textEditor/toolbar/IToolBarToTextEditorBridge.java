package com.thunderwiring.kitaba.textEditor.toolbar;

/**
 * This interface is implemented by {@link ToolBarController} and used to communicate with
 * {@link com.thunderwiring.kitaba.textEditor.TextEditorController}
 */
public interface IToolBarToTextEditorBridge {
    /**
     * Sets the style button associated with the specified style to be set as selected.
     */
    void activateStyle(String styleType);

    /**
     * Sets the style button associated with the specified style to be set as deselected.
     */
    void deactivateStyle(String styleType);

    void deactivateAllStyles();
}
