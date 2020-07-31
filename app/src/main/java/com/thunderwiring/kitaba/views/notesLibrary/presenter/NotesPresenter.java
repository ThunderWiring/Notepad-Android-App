package com.thunderwiring.kitaba.views.notesLibrary.presenter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.thunderwiring.kitaba.R;
import com.thunderwiring.kitaba.data.FolderPresenterEntity;
import com.thunderwiring.kitaba.data.NotePresenterEntity;
import com.thunderwiring.kitaba.files.presenterFile.NotesPresenterFile;
import com.thunderwiring.kitaba.notesAndFolders.PresenterControllerBase;
import com.thunderwiring.kitaba.files.presenterFile.INotesDataProvider;
import com.thunderwiring.kitaba.notesAndFolders.note.NoteSortManager;
import com.thunderwiring.kitaba.views.notesLibrary.INotesActivityToActionBar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * This class reads the raw data of the notes from the data base and create a view which
 * represent the notes which to be added to the notes library view.
 */
public class NotesPresenter implements IEntriesPresenter {
    private static final String TAG = NotesPresenter.class.getSimpleName();
    private static final int THUMBNAIL_DIMENSION = 275;

    private Context mContext;
    private List<View> mLibraryNotesEntryViews;
    private INoteActivityToEntryView mNoteActivityToEntryView;
    private PresenterControllerBase mNotePresenterController;
    private ImmutableSet<NotePresenterEntity> mNotesEntities;
    private INotesDataProvider mNotesDataProvider;

    public NotesPresenter(@Nonnull Context context,
                          INoteActivityToEntryView noteActivityToEntryView,
                          ImmutableSet<NotePresenterEntity> notesEntities,
                          INotesDataProvider notesDataProvider) {
        mContext = context;
        mNoteActivityToEntryView = noteActivityToEntryView;
        mLibraryNotesEntryViews = new ArrayList<>();
        mNotePresenterController = noteActivityToEntryView.getController();
        mNotesEntities = notesEntities;
        mNotesDataProvider = notesDataProvider;
    }

    public NotesPresenter(@Nonnull Context context,
                          INoteActivityToEntryView noteActivityToEntryView,
                          ImmutableSet<NotePresenterEntity> notesEntities) {
        this(context, noteActivityToEntryView, notesEntities, NotesPresenterFile.get());
    }

    public NotesPresenter(@Nonnull Context context,
                          INoteActivityToEntryView noteActivityToEntryView) {
        this(context, noteActivityToEntryView, NotesPresenterFile.get().getNotesEntities(),
                NotesPresenterFile.get());
    }

    @Override
    public void refreshEntries() {
        mNotesEntities = mNotesDataProvider.getNotesEntities();
        mLibraryNotesEntryViews = constructViews(mNotesEntities);
    }

    @Override
    public ImmutableList<View> getEntriesViews() {
        refreshEntries();
        return ImmutableList.copyOf(mLibraryNotesEntryViews);
    }

    public ImmutableList<View> getEntriesViews(ImmutableSet<NotePresenterEntity> notes) {
        return constructViews(notes);
    }

    private ImmutableList<View> constructViews(ImmutableSet<NotePresenterEntity> notes) {
        ImmutableList.Builder<View> views = new ImmutableList.Builder<>();
        for (NotePresenterEntity noteEntity : getSortedNotes(notes)) {
            if (noteEntity == null
                    || NotePresenterEntity.getDefaultInstance().equals(noteEntity)) {
                continue;
            }
            views.add(getNoteView(noteEntity));
        }
        return views.build();
    }

    private ImmutableList<NotePresenterEntity> getSortedNotes(Set<NotePresenterEntity> notes) {
        return NoteSortManager.sort(mNoteActivityToEntryView.getSortSelector(), notes);
    }

    /**
     * Creates a single note entry view for the notes library activity.
     */
    private View getNoteView(@Nonnull final NotePresenterEntity noteEntity) {
        View noteLibraryItemView = mNoteActivityToEntryView.inflateEntryItemView();
        noteLibraryItemView.setLayoutParams(getViewLayoutParams());
        CheckBox checkBox = noteLibraryItemView.findViewById(R.id.item_selected_checkbox);

        setTitle(noteLibraryItemView, noteEntity);
        setMenu(noteLibraryItemView, noteEntity);
        setSummary(noteLibraryItemView, noteEntity);
        setFooter(noteLibraryItemView, noteEntity);

        boolean hasNotesSelection =
                mNotePresenterController != null && mNotePresenterController.hasEntrySelection();
        checkBox.setOnCheckedChangeListener(
                (buttonView, isChecked) -> onPresenterCheckedChanged(isChecked, noteEntity));
        setCheckboxesVisibility(hasNotesSelection);
        setOnLongClickListener(noteLibraryItemView);
        setOnClickListener(noteLibraryItemView, noteEntity);
        return noteLibraryItemView;
    }

    private LinearLayout.LayoutParams getViewLayoutParams() {

        int itemSidePadding = (int) mContext.getResources().getDimension(R.dimen.item_side_padding);

        float containerSidePadding =
                2 * mContext.getResources().getDimension(R.dimen.container_padding);
        float width =
                mContext.getResources().getDisplayMetrics().widthPixels - containerSidePadding;

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams((int) width, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.setMargins(0, itemSidePadding, 0, 0);
        return lp;
    }

    private void setMenu(View noteLibraryItemView, NotePresenterEntity noteEntity) {
        ImageButton menuButton = noteLibraryItemView.findViewById(R.id.note_entry_menu);
        NoteEntryPopupMenu menu = new NoteEntryPopupMenu(menuButton, noteEntity);
        menuButton.setOnClickListener(v -> menu.show());
    }

    private void setOnClickListener(final View noteLibraryItemView,
                                    final NotePresenterEntity noteEntity) {
        noteLibraryItemView.setOnClickListener(
                getNoteEntryOnClickListener(noteEntity));
    }

    private View.OnClickListener getNoteEntryOnClickListener(
            final NotePresenterEntity noteEntity) {
        return v -> {
            Intent noteEditorIntent = mNoteActivityToEntryView
                    .getStartNoteEditorActivityIntent();
            noteEditorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            noteEditorIntent.putExtras(NotePresenterEntity.toBundle(noteEntity));
            mContext.startActivity(noteEditorIntent);
        };
    }

    private View.OnLongClickListener getNoteEntryOnLongClickListener() {
        return v -> {
            setCheckboxesVisibility(true);
            return true;
        };
    }

    private void setOnLongClickListener(final View noteLibraryItemView) {
        noteLibraryItemView.setOnLongClickListener(getNoteEntryOnLongClickListener());
    }

    private void setImageClickListeners(ImageView summaryImageView,
                                        NotePresenterEntity noteEntity) {
        summaryImageView.setOnClickListener(getNoteEntryOnClickListener(noteEntity));
        summaryImageView.setOnLongClickListener(getNoteEntryOnLongClickListener());
    }

    private void onPresenterCheckedChanged(boolean isChecked, NotePresenterEntity noteEntity) {
        mNotePresenterController
                .onPresenterEntityChecked(noteEntity, isChecked);
        mNoteActivityToEntryView.setDeleteButtonVisibility(
                mNotePresenterController.hasEntrySelection());
    }

    private void setCheckboxesVisibility(boolean isVisible) {
        int visibility = isVisible ? View.VISIBLE : View.GONE;
        for (View listItem : mLibraryNotesEntryViews) {
            CheckBox checkBox = listItem.findViewById(R.id.item_selected_checkbox);
            checkBox.setVisibility(visibility);
        }
    }


    private void setTitle(View noteLibraryItemView, NotePresenterEntity noteEntity) {
        TextView titleTextView =
                noteLibraryItemView.findViewById(R.id.title_text_view);

        String title = noteEntity.getTitle() == null || noteEntity.getTitle().isEmpty()
                ? mContext.getString(R.string.note_title_placeholder)
                : noteEntity.getTitle();
        titleTextView.setText(title);
    }

    private void setSummary(View noteLibraryItemView, NotePresenterEntity noteEntity) {
        LinearLayout summaryLayout =
                noteLibraryItemView.findViewById(R.id.summary_layout);
        summaryLayout.addView(getSummaryTextView(noteEntity), summaryLayout.getChildCount());
        summaryLayout.addView(getSummaryImageView(noteEntity), summaryLayout.getChildCount());
    }

    private void setFooter(View noteLibraryItemView, NotePresenterEntity noteEntity) {
        LinearLayout footerLayout =
                noteLibraryItemView.findViewById(R.id.details_footer_layout);
        footerLayout.addView(getLastEditedTextView(noteEntity), footerLayout.getChildCount());
        footerLayout.addView(getFooterSpacer(), footerLayout.getChildCount());
        footerLayout.addView(getWordCountTextView(noteEntity), footerLayout.getChildCount());
    }

    /**
     * Returns {@link TextView} view for the note's summary text.
     */
    private TextView getSummaryTextView(@Nonnull NotePresenterEntity noteEntity) {
        TextView summaryTextView = new TextView(mContext);

        LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        textLayoutParams.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

        summaryTextView.setLayoutParams(textLayoutParams);
        summaryTextView.setText(noteEntity.getSummary());

        return summaryTextView;
    }

    private ImageView getSummaryImageView(@Nonnull NotePresenterEntity noteEntity) {
        ImageView summaryImageView = new ImageButton(mContext);
        summaryImageView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        String imageFilePath = noteEntity.getFeatureImagePath();
        if (imageFilePath == null || imageFilePath.isEmpty()) {
            summaryImageView.setVisibility(View.GONE);
            return summaryImageView;
        }
        setImageClickListeners(summaryImageView, noteEntity);
        String path = noteEntity.getFeatureImagePath()
                .replace("file://", "")
                .replace("\"", "");
        summaryImageView.setImageBitmap(getBitmap(mContext, Uri.fromFile(new File(path))));
        return summaryImageView;
    }

    private TextView getLastEditedTextView(@Nonnull NotePresenterEntity noteEntity) {
        TextView detailsTextView = new TextView(mContext);
        String lastEditText = noteEntity.getLastEditDate();
        String lastEditAt = mContext.getResources().getString(R.string.last_edit, lastEditText);
        detailsTextView.setText(lastEditAt);
        return detailsTextView;
    }

    private TextView getWordCountTextView(@Nonnull NotePresenterEntity noteEntity) {
        TextView detailsTextView = new TextView(mContext);
        String wordsCount = mContext.getResources().getString(R.string.word_count,
                noteEntity.getWordsCount());
        detailsTextView.setText(wordsCount);
        return detailsTextView;
    }

    private TextView getFooterSpacer() {
        TextView spacer = new TextView(mContext);
        spacer.setText("|");
        spacer.setPaddingRelative(15, 0, 15, 0);
        return spacer;
    }

    private static Bitmap getBitmap(Context context, Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(),
                    imageUri);
            return ThumbnailUtils.extractThumbnail(
                    bitmap,
                    THUMBNAIL_DIMENSION,
                    THUMBNAIL_DIMENSION);
        } catch (IOException e) {
            Log.e(TAG, "failed to build image bitmap", e);
            return null;
        }
    }

    private class NoteEntryPopupMenu {
        private static final int MOVE_TO_FOLDER = 1;
        private static final int DELETE_ID = 2;

        private NotePresenterEntity mNoteEntity;
        private PopupMenu mPopupMenu;

        NoteEntryPopupMenu(View menuAnchor, NotePresenterEntity noteEntity) {
            mNoteEntity = noteEntity;
            mPopupMenu = new PopupMenu(mContext, menuAnchor);
            setupMenu();
        }

        private void setupMenu() {
            Menu menu = mPopupMenu.getMenu();
            if (!NotesPresenterFile.get().getFolderEntities().isEmpty()) {
                menu.add(Menu.NONE, MOVE_TO_FOLDER, MOVE_TO_FOLDER,
                        mContext.getResources().getString(R.string.note_entry_menu_move_to_folder));
            }

            menu.add(Menu.NONE, DELETE_ID, DELETE_ID,
                    mContext.getResources().getString(R.string.note_entry_menu_delete));
            setOnItemClick();
        }

        void show() {
            mPopupMenu.show();
        }

        private void setOnItemClick() {
            mPopupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case DELETE_ID:
                        onDelete();
                        break;
                    case MOVE_TO_FOLDER:
                        new MoveNoteToFolderDialog(mNoteEntity).show();
                        break;
                    default:
                        Log.e(TAG, "unknown menu item clicked.");
                        return false;
                }
                return true;
            });
        }

        private void onDelete() {
            mNoteActivityToEntryView.deleteEntry(mNoteEntity, getDialogResponses(), null);
        }

        private INotesActivityToActionBar.IDialogResponseCallback getDialogResponses() {
            return new INotesActivityToActionBar.IDialogResponseCallback() {
                @Override
                public void onPositiveResponse() {
                    /* nothing to handle after dialog response. */
                }

                @Override
                public void onNegativeResponse() {
                    /* nothing to handle after dialog response. */
                }
            };
        }
    }

    private class MoveNoteToFolderDialog {
        private NotePresenterEntity mNoteEntity;
        private AlertDialog mDialog;
        private Map<String, Integer> mIdsMap;
        private Set<FolderPresenterEntity> mFolderEntities;
        private int mSelectedFolder;

        MoveNoteToFolderDialog(NotePresenterEntity noteEntity) {
            mSelectedFolder = 0;
            mNoteEntity = noteEntity;
            mFolderEntities = NotesPresenterFile.get().getFolderEntities();
            initIdsMap();
            createDialog();
        }

        private void initIdsMap() {
            mIdsMap = new HashMap<>();
            int id = 1;
            for (FolderPresenterEntity folder : mFolderEntities) {
                mIdsMap.put(folder.getId(), id++);
            }
        }

        void show() {
            mDialog.show();
        }

        private void createDialog() {
            mDialog = new AlertDialog.Builder(mContext)
                    .setView(getDialogView())
                    .setTitle(R.string.move_note_dialog_title)
                    .setPositiveButton(R.string.move_note_dialog_positive_button, null)
                    .setNegativeButton(R.string.alert_dialog_negative_button, null)
                    .create();
            mDialog.setOnShowListener(dialog -> {
                mDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(getPositiveButtonOnClick());
                mDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setOnClickListener(v -> mDialog.dismiss());
            });
        }

        private View.OnClickListener getPositiveButtonOnClick() {
            return v -> {
                for (Map.Entry<String, Integer> entry : mIdsMap.entrySet()) {
                    if (entry.getValue() == mSelectedFolder) {
                        NotesPresenterFile.get().addNoteEntity(mNoteEntity, entry.getKey());
                        break;
                    }
                }
                mDialog.dismiss();
                mNoteActivityToEntryView.updateContentFragments();
            };

        }

        /**
         * Builds a radio group for all the folder names
         */
        private RadioGroup getDialogView() {
            RadioGroup namesRadioGroup = new RadioGroup(mContext);
            namesRadioGroup.setOrientation(RadioGroup.VERTICAL);
            for (FolderPresenterEntity folder : mFolderEntities) {
                RadioButton folderButton = new RadioButton(mContext);
                folderButton.setOnClickListener(v -> mSelectedFolder = folderButton.getId());
                folderButton.setText(folder.getName());

                int id = mIdsMap.containsKey(folder.getId()) ? mIdsMap.get(folder.getId()) : 1;
                folderButton.setId(id);
                if (folder.getId().equals(mNoteEntity.getParentFolderId())) {
                    folderButton.setChecked(true);
                }

                namesRadioGroup.addView(folderButton);
            }
            return namesRadioGroup;
        }
    }
}

