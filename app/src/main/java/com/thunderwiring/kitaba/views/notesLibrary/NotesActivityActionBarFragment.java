package com.thunderwiring.kitaba.views.notesLibrary;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.SearchView;

import com.thunderwiring.kitaba.R;
import com.thunderwiring.kitaba.views.SettingsMenuCommands;

public class NotesActivityActionBarFragment extends Fragment {

    private static final String TAG = NotesActivityActionBarFragment.class.getSimpleName();

    private ImageButton mDeleteButton;
    private ImageButton mBackButton;
    private ImageButton mSettingsButton;
    private SearchView mNotesLibrarySearchView;
    private INotesActivityToActionBar mNotesActivityToActionBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View actionBar = inflater.inflate(
                R.layout.notes_activity_action_bar, container, false);

        mDeleteButton = actionBar.findViewById(R.id.delete_notes);
        mNotesLibrarySearchView = actionBar.findViewById(R.id.notes_library_search);
        mBackButton = actionBar.findViewById(R.id.back_button);
        mSettingsButton = actionBar.findViewById(R.id.settings);

        for (View view : actionBar.getTouchables()) {
            view.setBackgroundColor(ContextCompat.getColor(
                    getContext().getApplicationContext(), R.color.colorRipple));
        }
        initControls(actionBar.getTouchables().size());

        return actionBar;
    }

    void setActionBarBridge(INotesActivityToActionBar notesActivityToActionBar) {
        mNotesActivityToActionBar = notesActivityToActionBar;
    }

    private void initControls(int numberOfControls) {
        mBackButton.setOnClickListener(v -> {
            if (mNotesActivityToActionBar.isSelectionTriggered()) {
                mNotesActivityToActionBar.clearNotesSelection();
            } else {
                mNotesActivityToActionBar.onBackPressed();
            }
        });
        mSettingsButton.setOnClickListener(getSettingsButtonClick());

        DeletionHandler deletionHandler = new DeletionHandler();
        mDeleteButton.setVisibility(View.GONE);
        mDeleteButton.setOnClickListener(deletionHandler.getDeleteNotesOnClick());
        mNotesLibrarySearchView.setOnQueryTextListener(mNotesActivityToActionBar);
        mNotesLibrarySearchView.setOnCloseListener(mNotesActivityToActionBar);
        mNotesLibrarySearchView.setMaxWidth(getSearchViewMaxWidth(numberOfControls));
    }

    private int getSearchViewMaxWidth(int numberOfControls) {
        Resources resources = getActivity().getResources();
        float containerSidePadding = 2 * resources.getDimension(R.dimen.container_padding);
        int itemSidePadding = (int) resources.getDimension(R.dimen.action_bar_control_padding);

        mDeleteButton.measure(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        float itemWidth =
                mDeleteButton.getMeasuredWidth() + itemSidePadding;

        /*
         * subtracting 2 from numberOfControls to not count the search view item and the
         * LinearLayout used to push the settings icon to the end part of the actionbar.
         */
        float allControlsWidth = itemWidth * (numberOfControls - 2) + itemSidePadding;

        float width =
                (resources.getDisplayMetrics().widthPixels - containerSidePadding) - allControlsWidth;
        return (int) width;
    }

    private View.OnClickListener getSettingsButtonClick() {
        SettingsPopupMenu menu = new SettingsPopupMenu(mSettingsButton);
        return v -> menu.show();
    }

    public void setDeleteButtonVisibility(boolean isVisible) {
        int visibility = isVisible ? View.VISIBLE : ViewGroup.GONE;
        mDeleteButton.setVisibility(visibility);
    }

    private class DeletionHandler {
        View.OnClickListener getDeleteNotesOnClick() {
            return v -> {
                if (v.getVisibility() != View.VISIBLE) {
                    Log.e(TAG, "Cannot delete items, except on user command.");
                    return;
                }
                createDeletionAlert();
            };
        }

        private void handleNotesSelectionCancelation() {
            if (mNotesActivityToActionBar != null) {
                mNotesActivityToActionBar.clearNotesSelection();
                setDeleteButtonVisibility(false);
            }
        }

        private void createDeletionAlert() {
            mNotesActivityToActionBar.deleteSelectedNotes(getDeleteDialogCallbacks());
        }

        private INotesActivityToActionBar.IDialogResponseCallback getDeleteDialogCallbacks() {
            return new INotesActivityToActionBar.IDialogResponseCallback() {
                @Override
                public void onPositiveResponse() {
                    mDeleteButton.setVisibility(View.GONE);
                    handleNotesSelectionCancelation();
                }

                @Override
                public void onNegativeResponse() {
                    handleNotesSelectionCancelation();
                }
            };
        }
    }

    /**
     * Builds the settings popup menu in the action bar.
     */
    private class SettingsPopupMenu {
        private static final int SORT_BY_TIME_ORDER = 0;
        private static final int SORT_BY_WORD_COUNT_ORDER = 1;
        private PopupMenu mPopupMenu;

        SettingsPopupMenu(View anchor) {
            mPopupMenu = new PopupMenu(getActivity(), anchor);
            setupMenu();
        }

        private void setupMenu() {
            Menu menu = mPopupMenu.getMenu();
            menu.add(Menu.NONE, SettingsMenuCommands.SORT_BY_CREATION_TIME,
                    SORT_BY_TIME_ORDER,
                    R.string.actionbar_setting_sort_time);
            menu.add(Menu.NONE, SettingsMenuCommands.SORT_BY_WORDS_COUNT,
                    SORT_BY_WORD_COUNT_ORDER,
                    R.string.actionbar_setting_sort_word_count);
            setOnItemClick();
        }

        private void setOnItemClick() {
            mPopupMenu.setOnMenuItemClickListener(item -> {
                mNotesActivityToActionBar.onSettingsItemClicked(item.getItemId());
                return true;
            });
        }

        void show() {
            mPopupMenu.show();
        }
    }
}
