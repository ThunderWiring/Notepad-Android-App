package com.thunderwiring.kitaba.views.noteEditor;

import com.thunderwiring.kitaba.views.IActionBarActivity;

public interface INoteEditorActivityToActionBar extends IActionBarActivity {
    void onQueryTextSubmit(String query);

    void onQueryTextChange(String newText);

    void onSearchDismissed();

    void onBackPressed();
}
