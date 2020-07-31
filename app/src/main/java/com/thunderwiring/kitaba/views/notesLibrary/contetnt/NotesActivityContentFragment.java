package com.thunderwiring.kitaba.views.notesLibrary.contetnt;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.thunderwiring.kitaba.R;
import com.thunderwiring.kitaba.data.IPresenterEntity;
import com.thunderwiring.kitaba.notesAndFolders.PresenterControllerBase;
import com.thunderwiring.kitaba.notesAndFolders.note.NoteLibrarySearch;
import com.thunderwiring.kitaba.notesAndFolders.note.NoteSortManager;
import com.thunderwiring.kitaba.views.INotesActivity;
import com.thunderwiring.kitaba.views.noteEditor.NoteEditorActivity;
import com.thunderwiring.kitaba.views.notesLibrary.INotesActivityToActionBar;
import com.thunderwiring.kitaba.views.notesLibrary.presenter.FolderPresenter;
import com.thunderwiring.kitaba.views.notesLibrary.presenter.IEntriesPresenter;
import com.thunderwiring.kitaba.views.notesLibrary.presenter.NotesPresenter;

import java.util.Set;

abstract public class NotesActivityContentFragment extends Fragment implements IContentFragment {
    private static final int COLUMN_COUNT = 1;

    private GridLayout mEntriesGridLayout;
    private ViewGroup mLibraryContentContainer;
    private INotesActivity mNotesActivity;
    private IEntriesPresenter mEntryPresenter;
    private NoteLibrarySearch mNotesLibrarySearch;
    private int mCurrentInflatedLayout;
    private int mContentItemLayoutRes;
    private int mSortingCriteria;

    public NotesActivityContentFragment() {
        super();
        mNotesLibrarySearch = new NoteLibrarySearch();
        mContentItemLayoutRes = R.layout.note_library_item_layout;
        mSortingCriteria = NoteSortManager.NONE;
    }

    /**
     * Returns the id for the layout of the empty state.
     */
    abstract protected int getEmptyStateLayoutId();

    /**
     * Returns the layout id of the content grid-view entries item
     */
    abstract protected int getContentItemLayoutRes();

    abstract protected PresenterControllerBase getPresenterController();

    /**
     * Returns the configuration for displaying the deletion alert dialog.
     *
     * @param meta a bundle with extra info regarding the deletion process. The presenter class
     *             (e.g. {@link FolderPresenter}) is the only module that builds this bundle and
     *             un-bundle it.
     */
    abstract protected IDeletionDialogConfig getDeletionDialogConfig(
            ImmutableSet<IPresenterEntity> entitiesToDelete, Bundle meta);

    protected int getGridColumnsCount() {
        return COLUMN_COUNT;
    }

    @Override
    public void orderContentBy(int orderSelector) {
        mSortingCriteria = orderSelector;
        refreshEntries();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mContentItemLayoutRes = getContentItemLayoutRes();
        mCurrentInflatedLayout = R.layout.notes_activity_content_layout;
        View root = inflater.inflate(mCurrentInflatedLayout, container, false);
        initGridLayout(root);
        mLibraryContentContainer = root.findViewById(R.id.notes_activity_view);

        return root;
    }

    private void initGridLayout(View root) {
        mEntriesGridLayout = root.findViewById(R.id.notes_grid_layout);
        if (mEntriesGridLayout == null) {
            /* The currently inflated layout is the empty state and not the content layout. */
            return;
        }
        mEntriesGridLayout.setColumnCount(getGridColumnsCount());
    }

    @Override
    public void onResume() {
        super.onResume();
        mEntryPresenter = getEntriesPresenter();
        if (mEntryPresenter.getEntriesViews().isEmpty()) {
            replaceContentWith(getEmptyStateLayoutId());
            return;
        }
        replaceContentWith(R.layout.notes_activity_content_layout);
        refreshEntries();
    }

    @Override
    public void onFragmentSelected() {
        mNotesActivity.updateState(this);
        if (getPresenterController().isUpToDate()) {
            return;
        }
        mEntryPresenter = getEntriesPresenter();
        if (mEntryPresenter.getEntriesViews().isEmpty()) {
            replaceContentWith(getEmptyStateLayoutId());
            return;
        }
        replaceContentWith(R.layout.notes_activity_content_layout);
        refreshEntries();
        getPresenterController().markUpToDate();
    }

    @Override
    public void setNotesActivityInterface(@Nullable INotesActivity bridge) {
        mNotesActivity = bridge;
    }

    @Override
    public IActivityListener getActivityListener() {
        return new ActivityListenerImpl();
    }

    protected GridLayout getEntriesGrid() {
        return mEntriesGridLayout;
    }

    @Override
    public boolean hasSelection() {
        GridLayout notesGridLayout = getEntriesGrid();
        if (notesGridLayout == null || notesGridLayout.getChildCount() == 0) {
            return false;
        }
        View noteItem = notesGridLayout.getChildAt(0);
        if (noteItem == null || noteItem.findViewById(R.id.item_selected_checkbox) == null) {
            return false;
        }
        return noteItem.findViewById(R.id.item_selected_checkbox).getVisibility() == View.VISIBLE;
    }

    /**
     * Returns alert dialog used when deleting a selected item/s.
     */
    private AlertDialog.Builder getDeletionDialog(
            ImmutableSet<IPresenterEntity> entitiesToDelete,
            INotesActivityToActionBar.IDialogResponseCallback dialogResponseCallback,
            Bundle bundle) {
        IDeletionDialogConfig dialogConfig = getDeletionDialogConfig(entitiesToDelete, bundle);
        return mNotesActivity.getAlertDialog(dialogConfig, dialogResponseCallback);
    }

    /**
     * Creates the library of the previous notes which user has created.
     */
    private void createNotesList(ImmutableList<View> notesEntries) {
        if (mEntriesGridLayout == null && !notesEntries.isEmpty()) {
            replaceContentWith(R.layout.notes_activity_content_layout);
        } else if (mEntriesGridLayout == null) {
            return;
        }
        mEntriesGridLayout.removeAllViews();
        for (View noteEntry : notesEntries) {
            mEntriesGridLayout.addView(noteEntry, mEntriesGridLayout.getChildCount());
        }
    }

    protected void updateContentFragments() {
        mEntryPresenter.refreshEntries();
        if (mEntryPresenter.getEntriesViews().isEmpty()) {
            replaceContentWith(getEmptyStateLayoutId());
        } else {
            createNotesList(mEntryPresenter.getEntriesViews());
        }
    }

    protected void refreshEntries() {
        if (mEntriesGridLayout == null) {
            Log.e(getClass().getName(), "grid layout is null, cannot refresh entries");
            return;
        }
        mEntryPresenter.refreshEntries();
        createNotesList(mEntryPresenter.getEntriesViews());
    }

    private void replaceContentWith(int layout) {
        if (mLibraryContentContainer == null || layout == mCurrentInflatedLayout) {
            return;
        }
        mLibraryContentContainer.removeAllViews();
        View root = getLayoutInflater().inflate(
                layout, mLibraryContentContainer, false);
        mCurrentInflatedLayout = layout;
        mLibraryContentContainer.addView(root);
        initGridLayout(root);
    }

    private void deleteNotes(ImmutableSet<IPresenterEntity> entriesToDelete,
                             INotesActivityToActionBar.IDialogResponseCallback dialogResponseCallback, Bundle bundle) {

        getDeletionDialog(entriesToDelete, dialogResponseCallback, bundle).show();
    }

    protected IEntriesPresenter.INoteActivityToEntryView getNoteEntryViewFactory() {
        return new IEntriesPresenter.INoteActivityToEntryView() {
            @Override
            public View inflateEntryItemView() {
                return getLayoutInflater().inflate(
                        mContentItemLayoutRes, mEntriesGridLayout, false);
            }

            @Override
            public Intent getStartNoteEditorActivityIntent() {
                return new Intent(getActivity(), NoteEditorActivity.class);
            }

            @Override
            public void navigateToContentFragment(Fragment fragment) {
                mNotesActivity.replaceContentContainerFragment(fragment);
            }

            @Override
            public PresenterControllerBase getController() {
                return getPresenterController();
            }

            @Override
            public void setDeleteButtonVisibility(boolean isVisible) {
                mNotesActivity.setActionBarDeleteButtonVisibility(isVisible);
            }

            @Override
            public void deleteEntry(IPresenterEntity entity,
                                    INotesActivityToActionBar.IDialogResponseCallback dialogResponseCallback, Bundle bundle) {
                deleteNotes(ImmutableSet.of(entity), dialogResponseCallback, bundle);
            }

            @Override
            public void updateContentFragments() {
                NotesActivityContentFragment.this.updateContentFragments();
            }

            @Override
            public void updateForegroundFragment(int layoutId) {
                mNotesActivity.updateForegroundFragment(layoutId);
            }

            @Override
            public int getSortSelector() {
                return mSortingCriteria;
            }
        };
    }

    private class ActivityListenerImpl implements IActivityListener {
        @Override
        public void onDeleteNotes(
                INotesActivityToActionBar.IDialogResponseCallback dialogResponseCallback) {
            deleteNotes(
                    getPresenterController().getSelectedEntities(), dialogResponseCallback, null);
        }

        @Override
        public void clearNotesSelection() {
            if (mEntriesGridLayout == null) {
                Log.e(getClass().getName(),
                        "cannot clear notes entries because grid layout is null");
                return;
            }
            for (int i = 0; i < mEntriesGridLayout.getChildCount(); i++) {
                View noteEntry = mEntriesGridLayout.getChildAt(i);
                CheckBox checkbox = noteEntry.findViewById(R.id.item_selected_checkbox);
                if (checkbox == null) {
                    continue;
                }
                checkbox.setChecked(false);
                checkbox.setVisibility(View.GONE);
            }
            getPresenterController().clearSelection();
        }

        @Override
        public boolean onClose() {
            mContentItemLayoutRes = getContentItemLayoutRes();
            updateContentFragments();
            return false;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            mContentItemLayoutRes = R.layout.note_library_item_layout;
            NotesPresenter searchNotesPresenter = new NotesPresenter(
                    getActivity(),
                    getNoteEntryViewFactory(), ImmutableSet.of());
            ImmutableList<View> views = searchNotesPresenter.getEntriesViews(
                    mNotesLibrarySearch.getNotesMatches(newText));
            if (views.isEmpty()) {
                replaceContentWith(R.layout.search_empty_state);
            } else {
                createNotesList(views);
            }
            return false;
        }
    }

    public interface IDeletionDialogConfig {
        /**
         * Returns the string resource ID for message on the dialog.
         */
        int getMessage();

        int getPositiveButtonText();

        DialogInterface.OnClickListener onPositiveButtonClick(INotesActivityToActionBar.IDialogResponseCallback dialogResponseCallback);

        int getNegativeButton();

        DialogInterface.OnClickListener onNegativeButtonClick(INotesActivityToActionBar.IDialogResponseCallback dialogResponseCallback);
    }

    class DeletionDialogConfig implements IDeletionDialogConfig {
        private Set<IPresenterEntity> mEntriesToDelete;
        private PresenterControllerBase mPresenterController;

        DeletionDialogConfig(PresenterControllerBase presenterController,
                             ImmutableSet<IPresenterEntity> entriesToDelete) {
            mPresenterController = presenterController;
            mEntriesToDelete = ImmutableSet.copyOf(entriesToDelete);
        }

        @Override
        public int getMessage() {
            return R.string.delete_notes_alert_message;
        }

        @Override
        public int getPositiveButtonText() {
            return R.string.delete_notes_alert_positive_button;
        }

        @Override
        public DialogInterface.OnClickListener onPositiveButtonClick(
                INotesActivityToActionBar.IDialogResponseCallback dialogResponseCallback) {
            return (dialog, which) -> {
                for (IPresenterEntity entity : mEntriesToDelete) {
                    mPresenterController.deleteEntry(entity);
                }
                refreshEntries();
                updateContentFragments();
                dialogResponseCallback.onPositiveResponse();
            };
        }

        @Override
        public int getNegativeButton() {
            return R.string.delete_notes_alert_negative_button;
        }

        @Override
        public DialogInterface.OnClickListener onNegativeButtonClick(
                INotesActivityToActionBar.IDialogResponseCallback dialogResponseCallback) {
            return (dialog, which) -> dialogResponseCallback.onNegativeResponse();
        }
    }
}
