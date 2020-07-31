package com.thunderwiring.kitaba.textEditor.webview;

import android.util.Log;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;

import javax.annotation.Nullable;

/**
 * Provides text manipulation methods to be used in this text editor implementation.
 * This class is made public in order to be testable.
 */
public class WebViewTextUtils {
    private static final String TAG = WebViewTextUtils.class.getSimpleName();
    private static final String HTML_TAG_REGEX = "<(.|\\n)*?>";
    private static final String IMAGE_ID_PREFIX = "noteImage";
    private static final String SRC = "src=\"";

    private static final char SPACE = ' ';
    private static final char LINE_BREAK = '\n';
    private static final int INDEX_NOT_FOUND = -1;

    static int getWordsCount(String text) {
        if (text == null) {
            Log.e(TAG, "Cannot get word count of a null text string");
            return 0;
        }
        int wordCount = 0;
        boolean word = false;
        int endOfLine = text.length() - 1;

        for (int i = 0; i < text.length(); i++) {
            boolean isChar = text.charAt(i) != SPACE
                    && text.charAt(i) != LINE_BREAK;
            if (isChar && i != endOfLine) {
                word = true;
            } else if (!isChar && word) {
                wordCount++;
                word = false;
            } else if (isChar) {
                wordCount++;
            }
        }
        return wordCount;
    }

    static String getSubText(@Nullable String text, int start, int length) {
        if (text == null) {
            Log.e(TAG, "illegal arguments for getting substring");
            return "";
        }
        int end = Math.min(text.length(), length) - start;
        if (start < 0 || end < 0 || start > end) {
            Log.e(TAG, "illegal arguments for getting substring");
            return "";
        }
        return text.substring(start, end);
    }

    public static String getPlainTextFromHtml(String html) {
        String parsedHtml = Jsoup.parse(html).text();
        parsedHtml = removeSurroundingQuotes(parsedHtml);
        return parsedHtml;
    }

    private static String removeSurroundingQuotes(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        while (str.length() > 1 && str.startsWith("\"") && str.endsWith("\"")) {
            str = str.substring(1, str.length() - 1);
        }
        return str;
    }

    static String unescapeHtml(String htmlContent) {
        htmlContent = removeSurroundingQuotes(htmlContent);
        return StringEscapeUtils.unescapeJava(htmlContent);
    }

    static String getFirstImagePath(String html) {
        try {
            int firstImageIndex = html.indexOf(IMAGE_ID_PREFIX);
            int imageSrcStart = html.indexOf(SRC, firstImageIndex) + SRC.length();
            int imageSrcEnd = html.indexOf("\"", imageSrcStart);
            if (firstImageIndex == INDEX_NOT_FOUND || imageSrcEnd == INDEX_NOT_FOUND) {
                return "";
            }
            return html.substring(imageSrcStart, imageSrcEnd);
        } catch (Exception e) {
            return "";
        }
    }
}