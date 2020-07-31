package com.thunderwiring.kitaba.textEditor;

import android.graphics.BitmapFactory;
import android.support.v4.util.Pair;
import android.util.Log;

import com.google.common.collect.ImmutableSet;
import com.thunderwiring.kitaba.data.NotePresenterEntity;
import com.thunderwiring.kitaba.notesAndFolders.note.ITextEditorBridge;
import com.thunderwiring.kitaba.textEditor.toolbar.IToolBarToTextEditorBridge;
import com.thunderwiring.kitaba.textEditor.webview.ITextEditor;
import com.thunderwiring.kitaba.textEditor.webview.ITextEditor.ITextWatcher;

import java.io.File;

public class TextEditorController {
    private static final String TAG = TextEditorController.class.getSimpleName();
    private static final int SUMMARY_TEXT_LENGTH = 40;
    private static final String ELLIPSIS = "...";

    private ITextEditorBridge mTextEditorBridge;
    private ITextEditor mTextEditor;
    private IToolBarToTextEditorBridge mToolBarBridge;
    private NotePresenterEntity.Builder mNoteEntityBuilder;

    public TextEditorController(NotePresenterEntity noteEntity) {
        mNoteEntityBuilder = noteEntity == null
                ? NotePresenterEntity.getDefaultInstance().toBuilder()
                : noteEntity.toBuilder();
    }

    public void attachTextEditor(ITextEditor textEditor) {
        if (textEditor != null) {
            textEditor.addSpanListener(new TextWatcher());
        }
        mTextEditor = textEditor;
    }

    public void attachTextEditorBridge(ITextEditorBridge textEditorBridge) {
        if (textEditorBridge != null) {
            mNoteEntityBuilder.setId(textEditorBridge.getNoteId().toString());
        }
        mTextEditorBridge = textEditorBridge;
    }

    public void attachToolBarBridge(IToolBarToTextEditorBridge toolBarBridge) {
        mToolBarBridge = toolBarBridge;
    }

    public void onDestroy() {
        if (mTextEditor != null) {
            mTextEditorBridge.syncNoteWithFileSystem();
        }
    }

    public void dismissSearchMatches() {
        mTextEditor.getTextSearchInterface().dismissSearchMatches();
    }

    public void performSearchQuery(String query) {
        mTextEditor.getTextSearchInterface().search(query);
    }

    public ITextEditorToNoteBridge getTextEditorToNoteBridge() {
        return new TextEditorToNoteBridge();
    }

    public void setNoteTitle(String title) {
        if (mTextEditorBridge == null) {
            Log.e(TAG, "cannot set note title - text editor bridge is null");
            return;
        }
        mNoteEntityBuilder.setTitle(title);
    }

    private String getSummaryText() {
        String summaryText = mTextEditor.getSubText(0, SUMMARY_TEXT_LENGTH);
        return summaryText.isEmpty() ? summaryText : summaryText + ELLIPSIS;
    }

    private final class TextWatcher implements ITextWatcher {
        @Override
        public void onActiveStyles(ImmutableSet<String> activeStylesTypes) {
            mToolBarBridge.deactivateAllStyles();
            for (String style : activeStylesTypes) {
                mToolBarBridge.activateStyle(style);
            }
        }

        @Override
        public void onTextChanged(String text) {

        }

        @Override
        public void onFeaturedImageChanged(String imagePath) {
            mNoteEntityBuilder.setFeatureImagePath(imagePath);
        }
    }

    /**
     * provides implementation for {@link ITextEditorToNoteBridge}
     */
    private final class TextEditorToNoteBridge implements ITextEditorToNoteBridge {

        @Override
        public void applyStyle(String styleType) {
            Styles.valueOf(styleType).toggleStyle(mTextEditor);
        }

        @Override
        public void removeStyle(String styleType) {
            Styles.valueOf(styleType).toggleStyle(mTextEditor);
        }

        @Override
        public void insertImage(String imagePath, String caption) {
            Pair<Integer, Integer> imageDimensions = getImageDimensions(imagePath);
            mTextEditor.insertImage(imagePath, caption, imageDimensions.first,
                    imageDimensions.second);
            mNoteEntityBuilder.setFeatureImagePath(imagePath);
        }

        @Override
        public void applyAction(String actionType) {
            Actions.valueOf(actionType).execCommand(mTextEditor);
        }

        @Override
        public String getNoteRichText() {
            return mTextEditor.getRichText();
        }

        @Override
        public String getNoteTitle() {
            return mNoteEntityBuilder.build().getTitle();
        }

        @Override
        public NotePresenterEntity getNotePresenterEntity() {
            String editDate = mNoteEntityBuilder.build().getLastEditDate();
            if (editDate == null || editDate.isEmpty()) {
                mNoteEntityBuilder.setLastEditDate(null);
            }
            return mNoteEntityBuilder
                    .setSummary(getSummaryText())
                    .setWordsCount(mTextEditor.getWordCount())
                    .build();
        }

        /**
         * Returns the width and height for the image to be displayed according to the
         * device screen dimensions.
         * The first value of the returned pair is the width and the second value is the height.
         */
        private Pair<Integer, Integer> getImageDimensions(String imagePath) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(new File(imagePath).getAbsolutePath(), options);
            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;
            return new Pair<>(imageWidth, imageHeight);
        }
    }

    private enum Actions {
        /**
         * Important Note:
         * Names of the fields must match with the values in {@link RichStylesTypes}
         */
        UNDO {
            @Override
            void execCommand(ITextEditor textEditor) {
                textEditor.undo();
            }
        },
        REDO {
            @Override
            void execCommand(ITextEditor textEditor) {
                textEditor.redo();
            }
        };

        abstract void execCommand(ITextEditor textEditor);
    }

    private enum Styles {
        /**
         * Important Note:
         * Names of the fields must match with the values in {@link RichStylesTypes}
         */
        UNDER_LINE {
            @Override
            void toggleStyle(ITextEditor textEditor) {
                textEditor.toggleUnderlineSpan();
            }
        },
        ITALIC {
            @Override
            void toggleStyle(ITextEditor textEditor) {
                textEditor.toggleItalicSpan();
            }
        },
        BOLD {
            @Override
            void toggleStyle(ITextEditor textEditor) {
                textEditor.toggleBoldSpan();
            }
        },
        NUMBER_LIST {
            @Override
            void toggleStyle(ITextEditor textEditor) {
                textEditor.toggleNumberListSpan();
            }
        },
        STRIKE_THROUGH {
            @Override
            void toggleStyle(ITextEditor textEditor) {
                textEditor.toggleStrikeThroughSpan();
            }
        },
        RTL {
            @Override
            void toggleStyle(ITextEditor textEditor) {
                textEditor.toggleRtl();
            }
        },
        LTR {
            @Override
            void toggleStyle(ITextEditor textEditor) {
                textEditor.toggleLtr();
            }
        },
        BULLET_LIST {
            @Override
            void toggleStyle(ITextEditor textEditor) {
                textEditor.toggleBulletListSpan();
            }
        };

        abstract void toggleStyle(ITextEditor textEditor);
    }
}
