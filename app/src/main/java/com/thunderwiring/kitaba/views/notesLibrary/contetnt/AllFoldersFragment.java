package com.thunderwiring.kitaba.views.notesLibrary.contetnt;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.common.collect.ImmutableSet;
import com.thunderwiring.kitaba.R;
import com.thunderwiring.kitaba.data.FolderPresenterEntity;
import com.thunderwiring.kitaba.data.IPresenterEntity;
import com.thunderwiring.kitaba.files.presenterFile.NotesPresenterFile;
import com.thunderwiring.kitaba.notesAndFolders.PresenterControllerBase;
import com.thunderwiring.kitaba.notesAndFolders.folder.FolderPresenterController;
import com.thunderwiring.kitaba.views.notesLibrary.INotesActivityToActionBar;
import com.thunderwiring.kitaba.views.notesLibrary.presenter.FolderPresenter;
import com.thunderwiring.kitaba.views.notesLibrary.presenter.IEntriesPresenter;

import java.util.UUID;

public class AllFoldersFragment extends NotesActivityContentFragment {
    private static final int GRID_COLUMNS = 2;

    private FolderPresenterController mFolderPresenterController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mFolderPresenterController = new FolderPresenterController();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected int getGridColumnsCount() {
        return GRID_COLUMNS;
    }

    @Override
    protected int getEmptyStateLayoutId() {
        return R.layout.notes_library_folders_empty_state_layout;
    }

    @Override
    protected int getContentItemLayoutRes() {
        return R.layout.folder_library_item_layout;
    }

    @Override
    protected PresenterControllerBase getPresenterController() {
        return mFolderPresenterController;
    }

    @Override
    public int getActionButtonResource() {
        return R.drawable.add_dark;
    }

    @Override
    public IEntriesPresenter getEntriesPresenter() {
        return new FolderPresenter(getActivity(), getNoteEntryViewFactory());
    }

    @Override
    public View.OnClickListener getActionButtonClickListener() {
        return v -> new NewFolderDialog().show();
    }

    private void createNewFolder(String folderName) {
        FolderPresenterEntity folderEntity =
                new FolderPresenterEntity.Builder()
                        .setId(UUID.randomUUID().toString())
                        .setName(folderName)
                        .build();
        NotesPresenterFile.get().addFolder(folderEntity);
    }

    @Override
    protected IDeletionDialogConfig getDeletionDialogConfig(
            ImmutableSet<IPresenterEntity> entitiesToDelete, Bundle meta) {
        boolean isDeepDeletion = FolderPresenter.isDeepDeletion(meta);
        return new IDeletionDialogConfig() {
            @Override
            public int getMessage() {
                return isDeepDeletion
                        ? R.string.delete_folder_deep_alert_message
                        : R.string.delete_folder_shallow_alert_message;
            }

            @Override
            public int getPositiveButtonText() {
                return R.string.delete_folder_shallow_alert_positive_button;
            }

            @Override
            public int getNegativeButton() {
                return R.string.delete_folder_shallow_alert_negative_button;
            }

            @Override
            public DialogInterface.OnClickListener onPositiveButtonClick(
                    INotesActivityToActionBar.IDialogResponseCallback dialogResponseCallback) {
                return (dialog, which) -> {
                    for (IPresenterEntity entity : entitiesToDelete) {
                        mFolderPresenterController.deleteEntry(entity);
                    }
                    dialogResponseCallback.onPositiveResponse();
                    refreshEntries();
                    updateContentFragments();
                };
            }

            @Override
            public DialogInterface.OnClickListener onNegativeButtonClick(
                    INotesActivityToActionBar.IDialogResponseCallback dialogResponseCallback) {
                return (dialog, which) -> dialogResponseCallback.onNegativeResponse();
            }
        };
    }

    private final class NewFolderDialog {
        private EditText mFolderNameTextInput;
        private AlertDialog mDialog;

        NewFolderDialog() {
            mFolderNameTextInput = getNewFolderTextInput();
            createDialog();
        }

        void show() {
            mDialog.show();
        }

        private void createDialog() {
            mDialog = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.add_folder_dialog_message)
                    .setView(mFolderNameTextInput)
                    .setPositiveButton(R.string.add_folder_dialog_positive_button, null)
                    .setNegativeButton(R.string.add_folder_dialog_negative_button, null)
                    .create();
            mDialog.setOnShowListener(getOnNewFolderDialogShowListener());
        }

        private DialogInterface.OnShowListener getOnNewFolderDialogShowListener() {
            return dialog -> {
                mDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(getPositiveButtonOnClick());
                mDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setOnClickListener(getNegativeButtonOnClick());
            };
        }

        private View.OnClickListener getPositiveButtonOnClick() {
            return v -> {
                String folderName = mFolderNameTextInput.getText().toString();
                if (folderName.isEmpty()) {
                    mFolderNameTextInput.setError(getResources().getString(R.string.add_folder_dialog_empty_name_error));
                } else {
                    int duplication = mFolderPresenterController.getFoldersWithName(folderName);
                    if (duplication > 0) {
                        folderName = folderName + " (" + String.valueOf(duplication) + ")";
                    }
                    createNewFolder(folderName);
                    updateContentFragments();
                    mDialog.dismiss();
                }
            };
        }

        private View.OnClickListener getNegativeButtonOnClick() {
            return v -> mDialog.dismiss();
        }

        private EditText getNewFolderTextInput() {
            EditText newFolderNameTextInput = new EditText(getContext());
            newFolderNameTextInput.setId(R.id.new_folder_alert_text_input);
            newFolderNameTextInput.setHint(R.string.add_folder_dialog_text_input_placeholder);
            newFolderNameTextInput.setSingleLine(true);
            newFolderNameTextInput.setOnClickListener(v -> newFolderNameTextInput.setHint(""));

            newFolderNameTextInput.setOnFocusChangeListener((v, hasFocus) -> newFolderNameTextInput.post(() -> {
                InputMethodManager inputMethodManager =
                        (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(newFolderNameTextInput,
                        InputMethodManager.SHOW_IMPLICIT);
            }));
            newFolderNameTextInput.requestFocus();

            return newFolderNameTextInput;
        }
    }
}
