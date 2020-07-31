package com.thunderwiring.kitaba.views.notesLibrary.presenter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.thunderwiring.kitaba.R;
import com.thunderwiring.kitaba.data.FolderPresenterEntity;
import com.thunderwiring.kitaba.data.NotePresenterEntity;
import com.thunderwiring.kitaba.files.presenterFile.NotesPresenterFile;
import com.thunderwiring.kitaba.notesAndFolders.note.NotePresenterController;
import com.thunderwiring.kitaba.views.notesLibrary.INotesActivityToActionBar;
import com.thunderwiring.kitaba.views.notesLibrary.contetnt.FolderContentFragment;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class FolderPresenter implements IEntriesPresenter {
    private static final String TAG = FolderPresenter.class.getSimpleName();
    private static final String DELETION_META_BUNDLE_KEY = "deletion_meta_bundle";

    private Context mContext;
    private List<View> mFoldersEntriesViews;
    private INoteActivityToEntryView mNoteActivityToEntryView;

    public FolderPresenter(@Nonnull Context context,
                           INoteActivityToEntryView noteActivityToEntryView) {
        mContext = context;
        mNoteActivityToEntryView = noteActivityToEntryView;
        mFoldersEntriesViews = new ArrayList<>();
    }

    public static boolean isDeepDeletion(Bundle meta) {
        if (meta == null || !meta.containsKey(DELETION_META_BUNDLE_KEY)) {
            return false;
        }
        return meta.getBoolean(DELETION_META_BUNDLE_KEY);
    }

    @Override
    public void refreshEntries() {
        ImmutableSet<FolderPresenterEntity> entities =
                NotesPresenterFile.get().getFolderEntities();
        constructViews(entities);
    }

    @Override
    public ImmutableList<View> getEntriesViews() {
        refreshEntries();
        return ImmutableList.copyOf(mFoldersEntriesViews);
    }

    private void constructViews(ImmutableSet<FolderPresenterEntity> folderEntities) {
        mFoldersEntriesViews.clear();
        for (FolderPresenterEntity folderEntity : folderEntities) {
            mFoldersEntriesViews.add(getFolderView(folderEntity));
        }
    }

    private View getFolderView(FolderPresenterEntity folderEntity) {
        View folderItemView = mNoteActivityToEntryView.inflateEntryItemView();
        folderItemView.setLayoutParams(getViewLayoutParams());
        setFolderTitle(folderItemView, folderEntity);
        setNumberOfItemsText(folderItemView, folderEntity);
        setMenu(folderItemView, folderEntity);

        folderItemView.setOnClickListener(getFolderOnClick(folderEntity));
        return folderItemView;
    }

    private LinearLayout.LayoutParams getViewLayoutParams() {
        Resources resources = mContext.getResources();
        float containerSidePadding = 2 * resources.getDimension(R.dimen.container_padding);
        int itemSidePadding = (int) resources.getDimension(R.dimen.item_side_padding);
        float width =
                ((resources.getDisplayMetrics().widthPixels - containerSidePadding) / 2) - itemSidePadding;
        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams((int) width, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.setMargins(itemSidePadding, itemSidePadding, 0, 0);
        return lp;
    }

    private View.OnClickListener getFolderOnClick(FolderPresenterEntity folderEntity) {
        return v -> {
            mNoteActivityToEntryView.updateForegroundFragment(R.layout.notes_activity_content_layout);
            FolderContentFragment folderContentFragment = new FolderContentFragment();
            folderContentFragment.setArguments(FolderPresenterEntity.toBundle(folderEntity));
            mNoteActivityToEntryView.navigateToContentFragment(folderContentFragment);
        };
    }

    private void setMenu(View folderItemView, FolderPresenterEntity folderEntity) {
        ImageButton menuButton = folderItemView.findViewById(R.id.folder_entry_menu);
        FolderEntryPopupMenu menu = new FolderEntryPopupMenu(menuButton, folderEntity);
        menuButton.setOnClickListener(v -> menu.show());
    }

    private void setFolderTitle(View folderItemView, FolderPresenterEntity folderEntity) {
        TextView titleTextView = folderItemView.findViewById(R.id.folder_name);
        if (titleTextView == null) {
            Log.e(TAG,
                    "Couldn't find the text view of the title, check that the specified id is " +
                            "correct.");
            return;
        }
        titleTextView.setText(folderEntity.getName());
    }

    private void setNumberOfItemsText(View folderItemView, FolderPresenterEntity folderEntity) {
        TextView numberOfItems = folderItemView.findViewById(R.id.folder_description);
        if (numberOfItems == null) {
            Log.e(TAG,
                    "Couldn't find the text view for the text view of the folder description. " +
                            "Make sure the specified ID is correct.");
            return;
        }
        String description = mContext.getResources().getString(R.string.folder_item_view_desc,
                folderEntity.getNotes().size());
        numberOfItems.setText(description);
    }

    private class FolderEntryPopupMenu {
        private static final int RENAME = 1;
        private static final int DELETE_SHALLOW = 2;
        private static final int DELETE_DEEP = 3;

        private FolderPresenterEntity mFolderEntity;
        private PopupMenu mPopupMenu;
        private boolean mShouldDeleteFolderNotes;

        FolderEntryPopupMenu(View menuAnchor, FolderPresenterEntity folderEntity) {
            mShouldDeleteFolderNotes = false;
            mFolderEntity = folderEntity;
            mPopupMenu = new PopupMenu(mContext, menuAnchor);
            setupMenu();
        }

        private void setupMenu() {
            Menu menu = mPopupMenu.getMenu();
            menu.add(Menu.NONE, RENAME, 1, R.string.note_entry_menu_rename);
            menu.add(Menu.NONE, DELETE_SHALLOW, 2, R.string.note_entry_menu_shallow_delete);
            menu.add(Menu.NONE, DELETE_DEEP, 3, R.string.note_entry_menu_deep_delete);
            setOnItemClick();
        }

        void show() {
            mPopupMenu.show();
        }

        private void setOnItemClick() {
            mPopupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case RENAME:
                        RenameFolderDialog renameDialog = new RenameFolderDialog(mFolderEntity);
                        renameDialog.show();
                        break;
                    case DELETE_SHALLOW:
                        onShallowDelete();
                        break;
                    case DELETE_DEEP:
                        onDeepDelete();
                        break;
                    default:
                        Log.e(TAG, "unknown menu item clicked.");
                        return false;
                }
                return true;
            });
        }

        private void onShallowDelete() {
            mShouldDeleteFolderNotes = false;
            mNoteActivityToEntryView.deleteEntry(mFolderEntity, getDialogResponses(),
                    getDialogMetaBundle());
        }

        private void onDeepDelete() {
            mShouldDeleteFolderNotes = true;
            mNoteActivityToEntryView.deleteEntry(mFolderEntity, getDialogResponses(),
                    getDialogMetaBundle());
        }

        private Bundle getDialogMetaBundle() {
            Bundle meta = new Bundle();
            meta.putBoolean(DELETION_META_BUNDLE_KEY, mShouldDeleteFolderNotes);
            return meta;
        }

        private INotesActivityToActionBar.IDialogResponseCallback getDialogResponses() {
            return new INotesActivityToActionBar.IDialogResponseCallback() {
                @Override
                public void onPositiveResponse() {
                    if (mShouldDeleteFolderNotes) {
                        NotePresenterController notePresenterController =
                                new NotePresenterController();
                        for (NotePresenterEntity note : mFolderEntity.getNotes()) {
                            notePresenterController.deleteEntry(note);
                        }
                    }
                }

                @Override
                public void onNegativeResponse() {
                    /* nothing to handle after dialog response. */
                }
            };
        }
    }

    private final class RenameFolderDialog {
        private AlertDialog mDialog;
        private FolderPresenterEntity mFolderEntity;
        private EditText mFolderNameTextInput;

        RenameFolderDialog(FolderPresenterEntity folderEntity) {
            mFolderEntity = folderEntity;
            mFolderNameTextInput = getNewFolderTextInput();
            createDialog();
        }

        void show() {
            mDialog.show();
        }

        private void createDialog() {
            mDialog = new AlertDialog.Builder(mContext)
                    .setView(mFolderNameTextInput)
                    .setTitle(R.string.rename_folder_alert_title)
                    .setPositiveButton(R.string.rename_folder_alert_positive_button, null)
                    .setNegativeButton(R.string.alert_dialog_negative_button, null)
                    .create();
            mDialog.setOnShowListener(getDialogOnShowListener());
        }

        private DialogInterface.OnShowListener getDialogOnShowListener() {
            return dialog -> {
                mDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(onPositiveButtonOnClick());
                mDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setOnClickListener(v -> mDialog.dismiss());
            };
        }

        private View.OnClickListener onPositiveButtonOnClick() {
            return v -> {
                String newName = mFolderNameTextInput.getText().toString();
                if (newName.isEmpty()) {
                    mFolderNameTextInput.setError(
                            mContext.getResources()
                                    .getString(R.string.add_folder_dialog_empty_name_error));
                } else {
                    NotesPresenterFile.get().renameFolder(mFolderEntity, newName);
                    refreshEntries();
                    mNoteActivityToEntryView.updateContentFragments();
                    mDialog.dismiss();
                }
            };
        }

        private EditText getNewFolderTextInput() {
            EditText newFolderNameTextInput = new EditText(mContext);
            newFolderNameTextInput.setId(R.id.rename_folder_alert_text_input);
            newFolderNameTextInput.setHint(mFolderEntity.getName());
            newFolderNameTextInput.setSingleLine(true);
            newFolderNameTextInput.setFocusable(true);
            newFolderNameTextInput.setOnFocusChangeListener((v, hasFocus) -> newFolderNameTextInput.post(() -> {
                InputMethodManager inputMethodManager =
                        (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(newFolderNameTextInput,
                        InputMethodManager.SHOW_IMPLICIT);
            }));
            newFolderNameTextInput.requestFocus();
            newFolderNameTextInput.setOnClickListener(
                    v -> newFolderNameTextInput.setHint(mFolderEntity.getName()));

            return newFolderNameTextInput;
        }
    }
}
