package com.thunderwiring.kitaba.textEditor.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.common.collect.ImmutableSet;
import com.thunderwiring.kitaba.files.NotesFileLibrary;

import javax.annotation.Nullable;

public class WebViewEditor implements ITextEditor {
    private static final String TAG = WebViewEditor.class.getSimpleName();
    private static final String INDEX_HTML = "file:///android_asset/index.html";
    private static final String JS_INTERFACE = "JSInterface";
    private static final int EXEC_MILLI_SEC = 10;

    private static final String IMAGE_ID_PREFIX = "noteImage";

    private WebView mWebViewEditor;
    private ITextWatcher mTextWatcher;
    private JSInterface mJsInterface;
    private boolean mIsReady;
    private String mHtmlContent;
    private String mPlainText;
    private Context mContext;

    public WebViewEditor(Context context, WebView webview, String noteId) {
        mContext = context;
        mIsReady = false;
        mJsInterface = new JSInterface();
        mWebViewEditor = webview;
        mPlainText = "";
        mHtmlContent = "";
        initEditor(noteId);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initEditor(String noteId) {
        mWebViewEditor.loadUrl(INDEX_HTML);
        mWebViewEditor.setVerticalScrollBarEnabled(false);
        mWebViewEditor.setHorizontalScrollBarEnabled(false);
        mWebViewEditor.getSettings().setJavaScriptEnabled(true);
        mWebViewEditor.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mWebViewEditor.addJavascriptInterface(mJsInterface, JS_INTERFACE);
        mWebViewEditor.setWebViewClient(new EditorWebViewClient());
        mWebViewEditor.setWebChromeClient(new WebChromeClient());
        mWebViewEditor.requestFocus();
        initEditorContent(noteId);
    }

    /**
     * Injects into the editor the text in the note file.
     */
    private void initEditorContent(String noteId) {
        String content = NotesFileLibrary.getNoteFileContent(noteId);
        if (content == null) {
            Log.e(TAG, "Failed to read note content from file path");
            return;
        }
        updateContent(content);
        exec(JSEditorCallback.initEditorWithContent(content), this::updateFeaturedImage);
    }

    @Override
    public void destroy() {
        if (mWebViewEditor != null) {
            mWebViewEditor.destroy();
        }
    }

    @Override
    public void toggleBoldSpan() {
        exec(JSEditorCallback.applyBoldStyle(), null);
    }

    @Override
    public void toggleUnderlineSpan() {
        exec(JSEditorCallback.applyUnderlineStyle(), null);
    }

    @Override
    public void toggleItalicSpan() {
        exec(JSEditorCallback.applyItalicStyle(), null);
    }

    @Override
    public void toggleBulletListSpan() {
        exec(JSEditorCallback.applyBulletListStyle(), null);
    }

    @Override
    public void toggleNumberListSpan() {
        exec(JSEditorCallback.applyNumberListStyle(), null);
    }

    @Override
    public void toggleStrikeThroughSpan() {
        exec(JSEditorCallback.applyStrikeThroughStyle(), null);
    }

    @Override
    public void toggleLtr() {
        exec(JSEditorCallback.setDirection(ParagraphDirection.LTR), null);
    }

    @Override
    public void toggleRtl() {
        exec(JSEditorCallback.setDirection(ParagraphDirection.RTL), null);
    }

    @Override
    public void setHeading(int heading) {
        exec(JSEditorCallback.setHeading(heading), null);
    }

    @Override
    public void undo() {
        exec(JSEditorCallback.undo(), this::updateContent);
    }

    @Override
    public void redo() {
        exec(JSEditorCallback.redo(), this::updateContent);
    }

    @Override
    public void insertImage(String imagePath, @Nullable String caption, int width, int height) {
        exec(JSEditorCallback.insertImage(
                "file://" + imagePath,
                IMAGE_ID_PREFIX,
                "alt", width, height),
                this::updateFeaturedImage);
    }

    @Override
    public String getRichText() {
        return mHtmlContent;
    }

    @Override
    public int getWordCount() {
        return WebViewTextUtils.getWordsCount(mPlainText);
    }

    @Override
    public String getSubText(int start, int length) {
        return WebViewTextUtils.getSubText(mPlainText, start, length);
    }

    @Override
    public ITextSearch getTextSearchInterface() {
        return new ITextSearch() {
            @Override
            public void search(String query) {
                mWebViewEditor.findAllAsync(query);
            }

            @Override
            public void dismissSearchMatches() {
                mWebViewEditor.clearMatches();
            }
        };
    }

    @Override
    public void addSpanListener(ITextWatcher spanListener) {
        mTextWatcher = spanListener;
    }

    private void updateFeaturedImage() {
        updateFeaturedImage(mHtmlContent);
    }

    private void updateFeaturedImage(String innerHtml) {
        updateContent(innerHtml);
        String path = WebViewTextUtils.getFirstImagePath(mHtmlContent);
        mTextWatcher.onFeaturedImageChanged(path);
    }

    private void updateContent(String htmlContent) {
        mHtmlContent = WebViewTextUtils.unescapeHtml(htmlContent);
        mPlainText = WebViewTextUtils.getPlainTextFromHtml(mHtmlContent);
    }

    private void exec(final String cmd, final ValueCallback<String> callback) {
        if (mIsReady) {
            load(cmd, callback);
        } else {
            mWebViewEditor.postDelayed(() -> exec(cmd, callback), EXEC_MILLI_SEC);
        }
    }

    private void load(String cmd, ValueCallback<String> callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebViewEditor.evaluateJavascript(cmd, callback);
        } else {
            mWebViewEditor.loadUrl(cmd);
        }
    }

    private class EditorWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            mIsReady = url.equalsIgnoreCase(INDEX_HTML);
            InputMethodManager imm =
                    (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return super.shouldOverrideUrlLoading(view, request);
        }
    }

    private void onActiveStylesChanged(ImmutableSet<String> activeStyles) {
        if (mTextWatcher == null || activeStyles == null) {
            return;
        }
        mTextWatcher.onActiveStyles(activeStyles);
    }

    /**
     * This class contains methods to be called from the javascript
     */
    public class JSInterface {

        @JavascriptInterface
        public void onSelectionChanged(String innerHtml, String[] activeStyles) {
            updateContent(innerHtml);
            updateFeaturedImage();
            onActiveStylesChanged(ImmutableSet.copyOf(activeStyles));
        }
    }


}
