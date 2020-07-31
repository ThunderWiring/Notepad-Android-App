package com.thunderwiring.kitaba.textEditor.toolbar;

import android.util.Log;

import com.thunderwiring.kitaba.notesAndFolders.note.IToolBarBridge;
import com.thunderwiring.kitaba.textEditor.RichStylesTypes;
import com.thunderwiring.kitaba.views.noteEditor.IToolBarButtonsController;

public class ToolBarController {
    private static final String TAG = ToolBarController.class.getSimpleName();

    private IToolBarBridge mNoteBridge;
    private IToolBarButtonsController mToolBarButtonsController;

    public void attachNoteBridge(IToolBarBridge noteBridge) {
        mNoteBridge = noteBridge;
    }

    public void attachNoteBridge(IToolBarButtonsController toolBarButtonsController) {
        mToolBarButtonsController = toolBarButtonsController;
    }

    /**
     * Handles the selection/deselection of a style from the toolbar.
     */
    public void toggleStyleSelection(String styleType, boolean isSelected) {
        if (mNoteBridge == null) {
            Log.e(TAG, "Note bridge to Toolbar is null");
            return;
        }
        if (isSelected) {
            deselectEquivalentStyle(styleType);
            mNoteBridge.onStyleSelected(styleType);
        } else {
            mNoteBridge.onStyleDeselected(styleType);
        }
    }

    private void deselectEquivalentStyle(String styleType) {
        String equivalentStyle = getEquivalentStyle(styleType);
        if (equivalentStyle == null || !mToolBarButtonsController.isStyleSet(equivalentStyle)) {
            return;
        }
        mToolBarButtonsController.deselectStyle(equivalentStyle);
        mNoteBridge.onStyleDeselected(equivalentStyle);
    }

    private String getEquivalentStyle(String styleType) {
        if (RichStylesTypes.BULLET_LIST.equals(styleType)) {
            return RichStylesTypes.NUMBER_LIST;
        } else if (RichStylesTypes.NUMBER_LIST.equals(styleType)) {
            return RichStylesTypes.BULLET_LIST;
        } else if (RichStylesTypes.LTR.equals(styleType)) {
            return RichStylesTypes.RTL;
        } else if (RichStylesTypes.RTL.equals(styleType)) {
            return RichStylesTypes.LTR;
        }
        return null;
    }


    public IToolBarToTextEditorBridge getToolBarToTextEditorBridge() {
        return new ToolBarToTextEditorBridge();
    }

    private final class ToolBarToTextEditorBridge implements IToolBarToTextEditorBridge {
        @Override
        public void activateStyle(String styleType) {
            mToolBarButtonsController.selectStyle(styleType);

        }

        @Override
        public void deactivateStyle(String styleType) {
            mToolBarButtonsController.deselectStyle(styleType);
        }

        @Override
        public void deactivateAllStyles() {
            mToolBarButtonsController.deselectAllStyles();
        }
    }
}
