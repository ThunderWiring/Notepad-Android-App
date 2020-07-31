package com.thunderwiring.kitaba.views.notesLibrary.contetnt;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.collect.ImmutableSet;
import com.thunderwiring.kitaba.R;
import com.thunderwiring.kitaba.data.FolderPresenterEntity;
import com.thunderwiring.kitaba.data.IPresenterEntity;
import com.thunderwiring.kitaba.data.NotePresenterEntity;
import com.thunderwiring.kitaba.files.presenterFile.INotesDataProvider;
import com.thunderwiring.kitaba.files.presenterFile.NotesPresenterFile;
import com.thunderwiring.kitaba.notesAndFolders.PresenterControllerBase;
import com.thunderwiring.kitaba.notesAndFolders.note.NotePresenterController;
import com.thunderwiring.kitaba.views.noteEditor.NoteEditorActivity;
import com.thunderwiring.kitaba.views.notesLibrary.presenter.IEntriesPresenter;
import com.thunderwiring.kitaba.views.notesLibrary.presenter.NotesPresenter;

import java.util.UUID;

/**
 * Displays the notes in a certain folder.
 */
public class FolderContentFragment extends NotesActivityContentFragment {
    private FolderPresenterEntity mFolderEntity;
    private INotesDataProvider mNotesDataProvider;
    private NotePresenterController mNotePresenterController;

    public FolderContentFragment() {
        super();
        mFolderEntity = FolderPresenterEntity.defaultInstance();
        mNotePresenterController = new NotePresenterController();
        mNotesDataProvider = new NotesDataProvider();
    }


    @Override
    protected int getEmptyStateLayoutId() {
        return R.layout.folder_content_empty_state_layout;
    }

    @Override
    protected int getContentItemLayoutRes() {
        return R.layout.note_library_item_layout;
    }

    @Override
    protected PresenterControllerBase getPresenterController() {
        return mNotePresenterController;
    }

    @Override
    protected IDeletionDialogConfig getDeletionDialogConfig(
            ImmutableSet<IPresenterEntity> entitiesToDelete, Bundle bundle) {
        return new DeletionDialogConfig(mNotePresenterController, entitiesToDelete);
    }

    @Override
    public int getActionButtonResource() {
        return R.drawable.write_new_note;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mFolderEntity = FolderPresenterEntity.fromBundle(getArguments());
        View root = super.onCreateView(inflater, container, savedInstanceState);
        TextView folderNameView = root.findViewById(R.id.folder_name);
        folderNameView.setVisibility(View.VISIBLE);
        folderNameView.setText(mFolderEntity.getName());
        return root;
    }

    @Override
    public View.OnClickListener getActionButtonClickListener() {
        return v -> {
            Intent intent = new Intent(getActivity(), NoteEditorActivity.class);

            NotePresenterEntity newNoteEntity =
                    new NotePresenterEntity.Builder()
                            .setId(UUID.randomUUID().toString())
                            .setParentFolderId(mFolderEntity.getId()).build();
            intent.putExtras(NotePresenterEntity.toBundle(newNoteEntity));
            startActivity(intent);
        };
    }

    @Override
    public IEntriesPresenter getEntriesPresenter() {
        return new NotesPresenter(getActivity(), getNoteEntryViewFactory(),
                mNotesDataProvider.getNotesEntities(), mNotesDataProvider);
    }

    private class NotesDataProvider implements INotesDataProvider {

        @Override
        public ImmutableSet<NotePresenterEntity> getNotesEntities() {
            mFolderEntity = NotesPresenterFile.get().getFolder(mFolderEntity.getId());
            return mFolderEntity.getNotes();
        }

        @Override
        public void addNoteEntity(NotePresenterEntity noteEntity) {
            NotesPresenterFile.get().addNoteEntity(noteEntity, mFolderEntity.getId());
        }
    }
}
