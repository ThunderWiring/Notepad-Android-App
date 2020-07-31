package com.thunderwiring.kitaba.textEditor.webview;

import org.junit.Test;

public class WebViewEditorTest {

    @Test
    public void getWordCount_emptyString() {
        String emptyString = "";
        assertEquals(
                WebViewEditor.WebViewTextUtils.getWordsCount(emptyString),
                0);
    }

    @Test
    public void getWordCount_whiteSpaces() {
        String spaces = "   ";
        assertEquals(
                WebViewEditor.WebViewTextUtils.getWordsCount(spaces),
                0);

        String text2 = "    asd asd";
        assertEquals(
                WebViewEditor.WebViewTextUtils.getWordsCount(text2),
                2);

        String text3 = "a     a  asd  a  ";
        assertEquals(
                WebViewEditor.WebViewTextUtils.getWordsCount(text3),
                4);
    }

    @Test
    public void getWordCount_lineBreaks() {
        String s1 = " \n  ";
        assertEquals(
                WebViewEditor.WebViewTextUtils.getWordsCount(s1),
                0);

        String s2 = "a";
        assertEquals(
                WebViewEditor.WebViewTextUtils.getWordsCount(s2),
                1);

        String s3 = "\na\nb\nc";
        assertEquals(
                WebViewEditor.WebViewTextUtils.getWordsCount(s3),
                3);
    }

    @Test
    public void getWordCount_specialChars() {
        String s1= "!@#$";
        assertEquals(
                WebViewEditor.WebViewTextUtils.getWordsCount(s1),
                1);

        String s2 = "don't 'qoute' `another quote` ";
        assertEquals(
                WebViewEditor.WebViewTextUtils.getWordsCount(s2),
                4);
    }

    @Test
    public void getSubText() {
    }
}