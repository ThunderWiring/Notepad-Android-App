package com.thunderwiring.kitaba.data;

import android.os.Bundle;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Immutable class for representing a folder of notes.
 */
public class FolderPresenterEntity implements IPresenterEntity {
    private static final String FOLDER_ENTITY_KEY = "folder_entity_key";

    private Builder mBuilder;

    private FolderPresenterEntity() {
        mBuilder = new Builder();
    }

    private FolderPresenterEntity(Builder builder) {
        mBuilder = builder;
    }

    public Builder toBuilder() {
        return new Builder(mBuilder);
    }

    public String getName() {
        return mBuilder.mName;
    }

    public String getId() {
        return mBuilder.mId;
    }

    public ImmutableSet<NotePresenterEntity> getNotes() {
        return ImmutableSet.copyOf(mBuilder.mNotes);
    }

    @Override
    public int hashCode() {
        return mBuilder.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FolderPresenterEntity)) {
            return false;
        }
        FolderPresenterEntity folder = (FolderPresenterEntity) obj;
        return mBuilder.equals(folder.mBuilder);
    }

    public static Bundle toBundle(FolderPresenterEntity entity) {
        Bundle bundle = new Bundle();
        bundle.putString(FOLDER_ENTITY_KEY, new Gson().toJson(entity));
        return bundle;
    }

    public static FolderPresenterEntity fromBundle(Bundle bundle) {
        if (bundle == null || !bundle.keySet().contains(FOLDER_ENTITY_KEY)) {
            return defaultInstance();
        }
        return new Gson().fromJson(bundle.getString(FOLDER_ENTITY_KEY),
                FolderPresenterEntity.class);
    }

    public static FolderPresenterEntity defaultInstance() {
        return new FolderPresenterEntity();
    }

    public static final class Builder {
        private String mId;
        private String mName;
        private Set<NotePresenterEntity> mNotes;

        Builder(String id, String name, Set<NotePresenterEntity> notes) {
            mId = id;
            mName = name;
            mNotes = new HashSet<>(notes);
        }

        public Builder() {
            this("", "", new HashSet<>());
        }

        Builder(Builder builder) {
            this(builder.mId, builder.mName, builder.mNotes);
        }

        public FolderPresenterEntity build() {
            return new FolderPresenterEntity(this);
        }

        public Builder setId(String id) {
            mId = id;
            return this;
        }

        public Builder setName(String name) {
            mName = name;
            return this;
        }

        public Builder setNotes(Set<NotePresenterEntity> notes) {
            mNotes.addAll(notes);
            return this;
        }

        public Builder addNote(NotePresenterEntity note) {
            mNotes.add(note);
            return this;
        }

        public Builder removeNote(NotePresenterEntity note) {
            mNotes.remove(note);
            return this;
        }

        @Override
        public int hashCode() {
            try {
                return UUID.fromString(mId).hashCode();
            } catch (IllegalArgumentException e) {
                return mId.hashCode();
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Builder)) {
                return false;
            }
            Builder builder = (Builder) obj;

            return mId.equals(builder.mId)
                    && mName.equals(builder.mName)
                    && mNotes.size() == builder.mNotes.size()
                    && notesAreEqual(builder.mNotes);
        }

        private boolean notesAreEqual(Set<NotePresenterEntity> notes) {
            for (NotePresenterEntity note : mNotes) {
                if (!notes.contains(note)) {
                    return false;
                }
            }
            return true;
        }
    }
}
