package com.thunderwiring.kitaba.views.noteEditor;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thunderwiring.kitaba.R;
import com.thunderwiring.kitaba.textEditor.RichStylesTypes;
import com.thunderwiring.kitaba.textEditor.toolbar.ToolBarController;
import com.thunderwiring.kitaba.textEditor.toolbar.MarkdownStyleObserverBase;

import java.util.HashMap;
import java.util.Map;

public class ToolBarFragment extends Fragment {
    private static final String TAG = ToolBarFragment.class.getSimpleName();

    private ToolBarController mToolBarController;
    private Map<String, MarkdownStyleObserverBase> mStyleObservers;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View fragmentView =
                inflater.inflate(R.layout.style_tool_bar_layout, container, false);
        if (mStyleObservers == null) {
            mStyleObservers = new HashMap<>();
            initObservers(fragmentView);
        }
        return fragmentView;
    }

    public void attachController(ToolBarController toolBarController) {
        if (toolBarController != null) {
            toolBarController.attachNoteBridge(new ToolBarButtonsController());
        }
        mToolBarController = toolBarController;
    }

    private void initObservers(View fragmentView) {
        if (mToolBarController == null) {
            Log.e(TAG, "Cannot initialize buttons - null controller");
            return;
        }
        Context context = getActivity();
        mStyleObservers.put(RichStylesTypes.BOLD, new MarkdownStyleObserverBase(
                context, fragmentView.findViewById(R.id.bold), mToolBarController,
                RichStylesTypes.BOLD));

        mStyleObservers.put(RichStylesTypes.ITALIC, new MarkdownStyleObserverBase(
                context, fragmentView.findViewById(R.id.italic), mToolBarController,
                RichStylesTypes.ITALIC));

        mStyleObservers.put(RichStylesTypes.UNDER_LINE, new MarkdownStyleObserverBase(
                context, fragmentView.findViewById(R.id.underline), mToolBarController,
                RichStylesTypes.UNDER_LINE));

        mStyleObservers.put(RichStylesTypes.BULLET_LIST, new MarkdownStyleObserverBase(
                context, fragmentView.findViewById(R.id.bullet_list), mToolBarController,
                RichStylesTypes.BULLET_LIST));

        mStyleObservers.put(RichStylesTypes.NUMBER_LIST, new MarkdownStyleObserverBase(
                context, fragmentView.findViewById(R.id.number_list), mToolBarController,
                RichStylesTypes.NUMBER_LIST));

        mStyleObservers.put(RichStylesTypes.STRIKE_THROUGH, new MarkdownStyleObserverBase(
                context, fragmentView.findViewById(R.id.strike_through), mToolBarController,
                RichStylesTypes.STRIKE_THROUGH));

        mStyleObservers.put(RichStylesTypes.LTR, new MarkdownStyleObserverBase(
                context, fragmentView.findViewById(R.id.ltr), mToolBarController,
                RichStylesTypes.LTR));

        mStyleObservers.put(RichStylesTypes.RTL, new MarkdownStyleObserverBase(
                context, fragmentView.findViewById(R.id.rtl), mToolBarController,
                RichStylesTypes.RTL));
    }

    private class ToolBarButtonsController implements IToolBarButtonsController {
        private void runTaskOnUiThread(Runnable action) {
            try {
                ToolBarFragment.this.getActivity().runOnUiThread(action);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void deselectAllStyles() {
            runTaskOnUiThread(() -> {
                for (MarkdownStyleObserverBase observer : mStyleObservers.values()) {
                    observer.markAsNotSelected();
                }
            });
        }

        @Override
        public void selectStyle(String styleType) {
            MarkdownStyleObserverBase observer = mStyleObservers.get(styleType);
            if (observer == null) {
                Log.e(TAG, "Cannot set style as selected because style type is unknown.");
                return;
            }
            runTaskOnUiThread(observer::markAsSelected);
        }

        @Override
        public void deselectStyle(String styleType) {
            MarkdownStyleObserverBase observer = mStyleObservers.get(styleType);
            if (observer == null) {
                Log.e(TAG, "Cannot set style as not selected because style type is unknown.");
                return;
            }
            runTaskOnUiThread(observer::markAsNotSelected);
        }

        @Override
        public boolean isStyleSet(String styleType) {
            MarkdownStyleObserverBase observer = mStyleObservers.get(styleType);
            if (observer == null) {
                Log.e(TAG, "Cannot check if style is set for an unknown style type");
                return false;
            }
            return observer.isStyleSet();
        }
    }
}
