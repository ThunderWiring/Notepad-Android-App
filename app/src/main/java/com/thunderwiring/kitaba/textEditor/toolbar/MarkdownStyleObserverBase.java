package com.thunderwiring.kitaba.textEditor.toolbar;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.thunderwiring.kitaba.R;

public class MarkdownStyleObserverBase implements View.OnClickListener {
    private boolean mIsStyleSet;
    private Context mContext;
    private View mStyleControllerView;
    private ToolBarController mToolbarController;
    private String mSpanType;

    public MarkdownStyleObserverBase(Context context, View button,
                                     ToolBarController toolBarController,
                                     String spanType) {
        button.setOnClickListener(this);
        mSpanType = spanType;
        mIsStyleSet = false;
        mContext = context;
        mStyleControllerView = button;
        mToolbarController = toolBarController;
        updateView(false);
    }

    @Override
    public void onClick(View v) {
        mIsStyleSet = !mIsStyleSet;
        updateView(mIsStyleSet);
        mToolbarController.toggleStyleSelection(mSpanType, mIsStyleSet);
    }

    private void updateView(boolean shouldSetStyle) {
        mIsStyleSet = shouldSetStyle;
        int buttonColor = shouldSetStyle ? R.color.toolbarButtonEnabled :
                R.color.toolbarButtonDisabled;
        mStyleControllerView.setBackgroundColor(ContextCompat.getColor(mContext,
                buttonColor));
    }

    public void markAsSelected() {
        updateView(true);
    }

    public void markAsNotSelected() {
        updateView(false);
    }

    public boolean isStyleSet() {
        return mIsStyleSet;
    }
}
