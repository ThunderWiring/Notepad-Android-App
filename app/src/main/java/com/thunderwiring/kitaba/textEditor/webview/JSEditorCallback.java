package com.thunderwiring.kitaba.textEditor.webview;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Defines the callbacks that can be sent to the javascript module for the editor.
 */
class JSEditorCallback {

    private static final String JS_MODULE = "javascript:Editor.";
    private static final String UTF8 = "UTF-8";


    private JSEditorCallback() {
    }

    private static String getCommand(Callback callback) {
        return getCommandWithParams(callback);
    }

    private static String getCommandWithParams(Callback cmd, String... args) {
        StringBuilder params = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            String param = "'" + args[i] + "'";
            params.append(param);
            if (i < args.length - 1) {
                params.append(',');
            }

        }
        return JS_MODULE + cmd.name() + "(" + params + ");";
    }

    static String applyBoldStyle() {
        return getCommand(Callback.applyBoldStyle);
    }

    static String applyUnderlineStyle() {
        return getCommand(Callback.applyUnderlineStyle);
    }

    static String applyItalicStyle() {
        return getCommand(Callback.applyItalicStyle);
    }

    static String applyBulletListStyle() {
        return getCommand(Callback.applyBulletListStyle);
    }

    static String applyStrikeThroughStyle() {
        return getCommand(Callback.applyStrikeThroughStyle);
    }

    static String applyNumberListStyle() {
        return getCommand(Callback.applyNumberListStyle);
    }

    static String undo() {
        return getCommand(Callback.undo);
    }

    static String redo() {
        return getCommand(Callback.redo);
    }

    static String initEditorWithContent(String html) {
        return getCommandWithParams(Callback.initEditorWithContent, getEncodedParam(html));
    }

    static String setDirection(ITextEditor.ParagraphDirection direction) {
        return getCommandWithParams(Callback.setDirection,
                getEncodedParam(direction.getDirection()));
    }

    static String setHeading(int heading) {
        return getCommandWithParams(Callback.setHeading, getEncodedParam(String.valueOf(heading)));
    }

    static String insertImage(String imagePath, String imageIdPrefix, String alt, int width,
                              int height) {
        return getCommandWithParams(Callback.insertImage,
                imagePath,
                getEncodedParam(imageIdPrefix),
                String.valueOf(width),
                String.valueOf(height));
    }

    private static String getEncodedParam(String param) {
        try {
            return URLEncoder.encode(param, UTF8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    enum Callback {
        applyBoldStyle(),
        applyUnderlineStyle(),
        applyItalicStyle(),
        applyBulletListStyle(),
        applyNumberListStyle(),
        applyStrikeThroughStyle(),
        setHeading(),
        setDirection(),
        insertImage(),
        initEditorWithContent(),
        undo(),
        redo();

        @Override
        public String toString() {
            return super.toString() + "()";
        }
    }
}
