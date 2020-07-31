package com.thunderwiring.kitaba.views;

import android.content.Intent;

/**
 * Interface definitions for callbacks to be used by the note editor activity action bar
 * call-to-action views when clicked.
 */
public interface IActionBarActivity {

    /**
     * Launch an activity for which you would like a result when it finished.
     *
     * @param intent the intent to start
     */
    void startActivityForResult(Intent intent);
}
