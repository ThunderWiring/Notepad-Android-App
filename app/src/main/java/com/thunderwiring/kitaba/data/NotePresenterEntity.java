package com.thunderwiring.kitaba.data;

import android.os.Bundle;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Immutable class that holds the data needed for representing the note.
 * This data includes
 * <ul>
 * <li>id</li>
 * <li>title</li>
 * <li>summary text</li>
 * <li>last edit date</li>
 * <li>path to the feature image</li>
 * <li>...ect.</li>
 * </ul>
 */
public class NotePresenterEntity implements IPresenterEntity {
    private static final String DATE_PATTERN = "dd.MM.yyyy  HH:mm";
    private static final String ID_KEY = "id";
    private static final String TITLE_KEY = "title";
    private static final String SUMMARY_KEY = "summary";
    private static final String IMAGE_KEY = "image";
    private static final String WORDS_KEY = "words_count";
    private static final String EDIT_DATE_KEY = "edit_date";
    private static final String PARENT_FOLDER_ID_KEY = "parent_folder";


    private Builder mBuilder;

    private NotePresenterEntity(Builder builder) {
        mBuilder = new Builder(builder);
    }

    private NotePresenterEntity() {
        mBuilder = new Builder();
    }

    public int getWordsCount() {
        return mBuilder.mWordsCount;
    }

    public String getId() {
        return mBuilder.mId;
    }

    public String getTitle() {
        return mBuilder.mTitle;
    }

    public String getSummary() {
        return mBuilder.mSummary;
    }

    public String getLastEditDate() {
        return mBuilder.mLastEditDate;
    }

    public Date getEditDate() {
        return mBuilder.mEditDate;
    }

    public String getFeatureImagePath() {
        return mBuilder.mFeatureImagePath;
    }

    public String getParentFolderId() {
        return mBuilder.mParentFolderId;
    }

    public Builder toBuilder() {
        return new Builder(mBuilder);
    }

    @Override
    public int hashCode() {
        return mBuilder.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NotePresenterEntity)) {
            return false;
        }
        NotePresenterEntity entity = (NotePresenterEntity) obj;
        return mBuilder.equals(entity.toBuilder());
    }

    public static NotePresenterEntity getDefaultInstance() {
        return new NotePresenterEntity();
    }


    public static Bundle toBundle(NotePresenterEntity entity) {
        Bundle bundle = new Bundle();
        NotePresenterEntity noteEntity = entity == null ? getDefaultInstance() : entity;
        bundle.putString(ID_KEY, noteEntity.getId());
        bundle.putString(TITLE_KEY, noteEntity.getTitle());
        bundle.putString(SUMMARY_KEY, noteEntity.getSummary());
        bundle.putString(IMAGE_KEY, noteEntity.getFeatureImagePath());
        bundle.putInt(WORDS_KEY, noteEntity.getWordsCount());
        bundle.putString(EDIT_DATE_KEY, noteEntity.getLastEditDate());
        bundle.putString(PARENT_FOLDER_ID_KEY, noteEntity.getParentFolderId());
        return bundle;
    }

    public static NotePresenterEntity fromBundle(Bundle bundle) {
        if (bundle == null) {
            return getDefaultInstance();
        }
        return new Builder(
                bundle.getString(ID_KEY),
                bundle.getString(TITLE_KEY),
                bundle.getString(SUMMARY_KEY),
                bundle.getString(IMAGE_KEY),
                bundle.getInt(WORDS_KEY),
                bundle.getString(EDIT_DATE_KEY),
                bundle.getString(PARENT_FOLDER_ID_KEY)
        ).build();
    }

    public static final class Builder {
        private String mId;
        private String mTitle;
        private String mSummary;
        private String mFeatureImagePath;
        private String mLastEditDate;
        private int mWordsCount;
        private String mParentFolderId;
        private Date mEditDate;

        public Builder() {
            this("",
                    "",
                    "",
                    "",
                    0);
        }

        public Builder(Builder builder) {
            this(builder.mId, builder.mTitle, builder.mSummary, builder.mFeatureImagePath,
                    builder.mWordsCount, builder.mLastEditDate, builder.mParentFolderId);
        }

        public Builder(String id, String title, String summary, String imagePath,
                       int wordsCount) {
            this(id, title, summary, imagePath, wordsCount, new SimpleDateFormat(DATE_PATTERN,
                    Locale.US).format(new Date()), "");
        }

        public Builder(String id, String title, String summary, String imagePath,
                       int wordsCount, String date, String parentFolderId) {
            mId = id;
            mTitle = title == null ? "" : title;
            mSummary = summary == null ? "" : summary;
            mFeatureImagePath = imagePath == null ? "" : imagePath;
            mWordsCount = wordsCount;
            mLastEditDate = date == null ? "" : date;
            mParentFolderId = parentFolderId == null ?  "" : parentFolderId;
            mEditDate = new Date();
        }

        public NotePresenterEntity build() {
            return new NotePresenterEntity(this);
        }

        public Builder setParentFolderId(String folderId) {
            mParentFolderId = folderId;
            return this;
        }

        public Builder setId(String id) {
            mId = id;
            return this;
        }

        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public Builder setSummary(String summary) {
            mSummary = summary;
            return this;
        }

        public Builder setFeatureImagePath(String featureImagePath) {
            mFeatureImagePath = featureImagePath;
            return this;
        }

        public Builder setWordsCount(int wordsCount) {
            mWordsCount = wordsCount;
            return this;
        }

        public Builder setLastEditDate(String lastEditDate) {
            if (lastEditDate == null) {
                mEditDate = new Date();
            }
            mLastEditDate = lastEditDate == null
                    ? new SimpleDateFormat(DATE_PATTERN, Locale.US).format(mEditDate)
                    : lastEditDate;
            return this;
        }

        @Override
        public int hashCode() {
            return UUID.fromString(mId).hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Builder)) {
                return false;
            }
            Builder builder = (Builder) obj;
            return mId.equals(builder.mId)
                    && mTitle.equals(builder.mTitle)
                    && mSummary.equals(builder.mSummary)
                    && mFeatureImagePath.equals(builder.mFeatureImagePath)
                    && mWordsCount == builder.mWordsCount
                    && mLastEditDate.equals(builder.mLastEditDate);
        }
    }
}