package com.thunderwiring.kitaba.textEditor.webview;

import com.google.common.collect.ImmutableSet;

import javax.annotation.Nullable;

/**
 * Interface implemented by the text editor.
 * Supplied an API to interact with the rich-text
 */
public interface ITextEditor {
    public static final int H1 = 1;
    public static final int H2 = 2;
    public static final int H3 = 3;

    /**
     * Destroys the text editor when the parent activity has called onDestroy;
     */
    void destroy();

    /**
     * Toggles the span on the selected text or to the text being typed.
     * If the style was enabled, then calling this method would disable it, and if the style was
     * disabled, calling the method would enable it.
     */
    void toggleBoldSpan();

    void toggleUnderlineSpan();

    void toggleItalicSpan();

    void toggleBulletListSpan();

    void toggleNumberListSpan();

    void toggleStrikeThroughSpan();

    void toggleLtr();

    void toggleRtl();

    /**
     * Applies heading format to the div.
     *
     * @param heading level of the heading is one of {@link ITextEditor#H1},
     *                {@link ITextEditor#H2}, {@link ITextEditor#H3}
     */
    void setHeading(int heading);

    /**
     * Reverts the recent change in the editor
     */
    void undo();

    /**
     * Applies the recent reverted change in the editor.
     */
    void redo();

    /**
     * Inserts an image at the current cursor position with an optional caption.
     * if the provided caption string is null then no caption is to be shown.
     */
    void insertImage(String imagePath, @Nullable String caption, int width, int height);

    /**
     * Returns the text with its styles encoded.
     */
    String getRichText();

    int getWordCount();

    /**
     * Returns substring of the rich text (without the rich styles)
     */
    String getSubText(int start, int length);

    ITextSearch getTextSearchInterface();

    void addSpanListener(ITextWatcher spanListener);

    interface ITextSearch {
        /**
         * Performs a search for the specified query inside the text editor content.
         *
         * @param query the string to seach for its occurrences.
         */
        void search(String query);

        void dismissSearchMatches();
    }

    /**
     * Implemented by modules that need to listen to changes in the text editor.
     */
    interface ITextWatcher {
        /**
         * Notifies when a text with <code>spanType</code> span is selected.
         */
        void onActiveStyles(ImmutableSet<String> activeStylesTypes);

        void onTextChanged(String text);

        void onFeaturedImageChanged(String imagePath);
    }

    /**
     * Determines the direction of the paragraph content.
     */
    enum ParagraphDirection {
        LTR {
            @Override
            String getDirection() {
                return "ltr";
            }
        }, RTL {
            @Override
            String getDirection() {
                return "rtl";
            }
        };

        abstract String getDirection();
    }
}