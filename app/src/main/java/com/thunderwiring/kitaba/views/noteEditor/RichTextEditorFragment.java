package com.thunderwiring.kitaba.views.noteEditor;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;

import com.thunderwiring.kitaba.R;
import com.thunderwiring.kitaba.data.NotePresenterEntity;
import com.thunderwiring.kitaba.textEditor.TextEditorController;
import com.thunderwiring.kitaba.textEditor.webview.WebViewEditor;


public class RichTextEditorFragment extends Fragment {
    private EditText mTitleEditText;
    private ImageView mTitleDismissContent;

    private TextEditorController mTextEditorController;
    private NotePresenterEntity mNotePresenterEntity = NotePresenterEntity.getDefaultInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View richTextEditorView =
                inflater.inflate(R.layout.rich_text_editor_layout, container, false);
        WebView webView = richTextEditorView.findViewById(R.id.web_view_editor);
        if (mTextEditorController == null) {
            Log.e(getClass().getName(), "Note controller is null.");
        } else {
            mNotePresenterEntity = getNoteDataObjectFromIntent();
            WebViewEditor webViewEditor = new WebViewEditor(
                    getActivity(),
                    webView, mNotePresenterEntity.getId());
            mTextEditorController.attachTextEditor(webViewEditor);
        }

        mTitleEditText = richTextEditorView.findViewById(R.id.note_title);
        mTitleEditText.setSingleLine(true);
        mTitleEditText.addTextChangedListener(getTitleTextWatcher());
        mTitleEditText.setOnFocusChangeListener(getTitleFocusListener());
        mTitleEditText.setText(mNotePresenterEntity.getTitle());
        mTitleDismissContent =
                richTextEditorView.findViewById(R.id.title_dismiss_content);
        mTitleDismissContent.setOnClickListener(onTitleContentDismiss());

        return richTextEditorView;
    }

    public void attachController(TextEditorController textEditorController) {
        mTextEditorController = textEditorController;
    }

    /**
     * Returns a {@link TextWatcher} implementation for the title text editor which updates the
     * note's title.
     */
    private TextWatcher getTitleTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                /* Nothing to be done. */
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                /* Nothing to be done. */
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mTextEditorController == null) {
                    return;
                }
                String title = s.length() == 0
                        ? getString(R.string.note_title_placeholder)
                        : s.toString();
                mTextEditorController.setNoteTitle(title);
            }
        };
    }

    private View.OnClickListener onTitleContentDismiss() {
        return v -> mTitleEditText.setText("");
    }

    /**
     * Returns {@link View.OnFocusChangeListener} for the title text editor.
     */
    private View.OnFocusChangeListener getTitleFocusListener() {
        return (v, hasFocus) -> {
            if (hasFocus) {
                mTitleEditText.setHint("");
                mTitleDismissContent.setVisibility(View.VISIBLE);
            } else {
                mTitleDismissContent.setVisibility(View.GONE);
                if (mTitleEditText.length() == 0) {
                    mTitleEditText.setHint(R.string.note_title_placeholder);
                }
            }
        };
    }

    /**
     * Returns {@link NotePresenterEntity} object that is passed to the activity or null if no
     * such object is passed.
     */
    private NotePresenterEntity getNoteDataObjectFromIntent() {
        Bundle noteDataBundle = getArguments() == null
                ? new Bundle()
                : getArguments();
        return NotePresenterEntity.fromBundle(noteDataBundle);
    }
}
