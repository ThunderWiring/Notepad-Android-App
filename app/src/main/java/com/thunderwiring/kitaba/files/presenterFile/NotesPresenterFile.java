package com.thunderwiring.kitaba.files.presenterFile;


import android.util.Log;

import com.google.common.collect.ImmutableSet;
import com.thunderwiring.kitaba.data.FolderPresenterEntity;
import com.thunderwiring.kitaba.data.NotePresenterEntity;
import com.thunderwiring.kitaba.files.NoteFilesEnvironment;
import com.thunderwiring.kitaba.files.NotesFileLibrary;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Manages the file that's responsible for the presentation data for each note.
 * The presentational data for each note includes the title, summary and a feature image if
 * relevant.
 * The file is xml-style file with an entry per note.
 * There is only one instance of this class.
 */
public class NotesPresenterFile implements INotesDataProvider {
    private static final String TAG = NotesPresenterFile.class.getSimpleName();
    private static final String ROOT_TAG = "NoteLibrary";
    private static final String NOTE_TAG = "Note";
    private static final String TITLE_TAG = "Title";
    private static final String SUMMARY_TAG = "Summary";
    private static final String IMAGE_TAG = "Image";
    private static final String WORDS_TAG = "WordCount";
    private static final String DATE_TAG = "EditDate";
    private final static String FOLDER_TAG = "Folder";
    private static final String NAME_ATTR = "name";
    private static final String ID_ATTR = "id";

    private static final String DEFAULT_NOTE_TITLE = "No Title";
    private static final String FILE_NAME = "notes_presenter";
    private static NotesPresenterFile instance = null;

    private File mPresenterFile;
    private Document mDocument;
    private NotesPresenterCache mNoteEntitiesCache;

    private NotesPresenterFile() {
        this(NoteFilesEnvironment.getFilePath(FILE_NAME, "xml"));
    }

    private NotesPresenterFile(String filePath) {
        mPresenterFile = new File(filePath);
        initDocument();
        mNoteEntitiesCache = new NotesPresenterCache(
                ImmutableSet.copyOf(extractAllNoteEntities()),
                ImmutableSet.copyOf(extractFolderEntities()));
    }

    /**
     * This method is used only in tests and should not be used anywhere else in the code.
     */
    static NotesPresenterFile getForTest(String path) {
        return new NotesPresenterFile(path);
    }

    public static NotesPresenterFile get() {
        if (instance == null) {
            instance = new NotesPresenterFile();
        }
        return instance;
    }

    /**
     * Returns a collection of all the {@link NotePresenterEntity} representing all the
     * existing notes.
     */
    @Override
    public ImmutableSet<NotePresenterEntity> getNotesEntities() {
        return mNoteEntitiesCache.getNoteEntities();
    }

    public ImmutableSet<FolderPresenterEntity> getFolderEntities() {
        return mNoteEntitiesCache.getFolderEntities();
    }

    public FolderPresenterEntity getFolder(String folderId) {
        return mNoteEntitiesCache.getFolder(folderId);
    }

    /**
     * Creates the presenter file if it doesn't exist yet. Also it adds the root element to the
     * file.
     */
    private void initPresenterFile() {
        try {
            if (!mPresenterFile.exists() && !mPresenterFile.createNewFile()) {
                Log.e(TAG, "failed to create presenter file");
            }
            if (mDocument == null) {
                initDocument();
            }
        } catch (IOException e) {
            Log.e(TAG, "failed to create presenter file", e);
        }
    }

    private void initDocument() {
        DocumentBuilder documentBuilder;
        try {
            boolean doesFileHasEntries = mNoteEntitiesCache == null ||
                    mNoteEntitiesCache.getNoteEntities().size() > 0;
            documentBuilder =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder();
            mDocument = mPresenterFile.exists() && doesFileHasEntries
                    ? documentBuilder.parse(mPresenterFile)
                    : documentBuilder.newDocument();

            if (mDocument.getFirstChild() == null) {
                mDocument.appendChild(createNode(ROOT_TAG));
            }
        } catch (ParserConfigurationException e) {
            Log.e(TAG, "Failed to create document builder for the presenter file.", e);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Set<NotePresenterEntity> extractAllNoteEntities() {
        final String noFolderPath = "/" + ROOT_TAG + "/" + NOTE_TAG + "/@id";
        final String foldersPath = "/" + ROOT_TAG + "/" + FOLDER_TAG + "/" + NOTE_TAG +
                "/@id";
        Set<NotePresenterEntity> noFolderNoteEntitySet = extractNotes(noFolderPath);
        Set<NotePresenterEntity> notesFromFolders = extractNotes(foldersPath);
        notesFromFolders.addAll(noFolderNoteEntitySet);
        return notesFromFolders;
    }

    private Set<NotePresenterEntity> extractNotes(String pathExpression) {
        Set<NotePresenterEntity> noteEntitiesSet = new HashSet<>();
        XPath xpath = XPathFactory.newInstance().newXPath();
        try {
            XPathExpression expression = xpath.compile(pathExpression);
            NodeList nodes = (NodeList) expression.evaluate(mDocument, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                String noteId = nodes.item(i).getNodeValue();
                noteEntitiesSet.add(getPresenterEntityForNoteId(noteId));
            }
        } catch (XPathExpressionException e) {
            Log.e(TAG, "failed to compile expression: " + pathExpression, e);
        }
        return noteEntitiesSet;
    }

    private Set<FolderPresenterEntity> extractFolderEntities() {
        String foldersPath = "/" + ROOT_TAG + "/" + FOLDER_TAG + "/@id";
        Set<FolderPresenterEntity> folderEntitiesSet = new HashSet<>();
        XPath xpath = XPathFactory.newInstance().newXPath();
        try {
            XPathExpression expression = xpath.compile(foldersPath);
            NodeList nodes = (NodeList) expression.evaluate(mDocument, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                String folderId = nodes.item(i).getNodeValue();
                folderEntitiesSet.add(getPresenterEntityForFolderId(folderId));
            }
        } catch (XPathExpressionException e) {
            Log.e(TAG, "failed to compile expression: " + foldersPath, e);
        }
        return folderEntitiesSet;
    }

    /**
     * Creates a new element with the specified name.
     */
    private Element createNode(String tagName) {
        if (mDocument == null) {
            Log.e(TAG, "Failed to create presenter file document element");
            return null;
        }
        return mDocument.createElement(tagName);
    }

    /**
     * Creates a new node with text content
     *
     * @param tagName name for the node.
     * @param content content inside the node.
     */
    private Element createTextElement(String tagName, String content) {
        Element element = createNode(tagName);
        if (element == null) {
            Log.e(TAG, "failed to create text element");
            return null;
        }
        String nonNullContent = content == null ? "" : content;
        element.appendChild(mDocument.createTextNode(nonNullContent));
        return element;
    }

    private Element createNoteElement(NotePresenterEntity noteEntity) {
        Element noteElement = createNode(NOTE_TAG);
        if (noteElement == null) {
            Log.e(TAG, "Failed to create a note element");
            return null;
        }
        noteElement.setAttribute(ID_ATTR, noteEntity.getId());
        noteElement.appendChild(createTextElement(TITLE_TAG, noteEntity.getTitle()));
        noteElement.appendChild(createTextElement(SUMMARY_TAG, noteEntity.getSummary()));
        noteElement.appendChild(createTextElement(IMAGE_TAG, noteEntity.getFeatureImagePath()));
        noteElement.appendChild(createTextElement(DATE_TAG, noteEntity.getLastEditDate()));
        noteElement.appendChild(createTextElement(WORDS_TAG,
                String.valueOf(noteEntity.getWordsCount())));
        return noteElement;
    }

    private Element createFolderElement(FolderPresenterEntity folderEntity) {
        Element folderElement = createNode(FOLDER_TAG);
        if (folderElement == null) {
            Log.e(TAG, "Failed to create folder element");
            return null;
        }
        folderElement.setAttribute(ID_ATTR, folderEntity.getId());
        folderElement.setAttribute(NAME_ATTR, folderEntity.getName());
        for (NotePresenterEntity noteEntity : folderEntity.getNotes()) {
            folderElement.appendChild(createNoteElement(noteEntity));
        }
        return folderElement;
    }

    private void updatePresenterFile() {
        if (!mNoteEntitiesCache.isDirty()) {
            return;
        }
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(mDocument);
            StreamResult streamResult = new StreamResult(mPresenterFile);
            transformer.transform(domSource, streamResult);
        } catch (TransformerException e) {
            Log.e(TAG, "Failed to update content for presenter file", e);
        }
    }

    public boolean isUpToDate() {
        return !mNoteEntitiesCache.isDirty();
    }

    public void markClean() {
        mNoteEntitiesCache.markClean();
    }

    public boolean isNoteEntityValid(NotePresenterEntity noteEntity) {
        if (noteEntity == null || NotePresenterEntity.getDefaultInstance().equals(noteEntity)) {
            return false;
        }
        boolean hasImage = !noteEntity.getFeatureImagePath().isEmpty();
        boolean hasText = !noteEntity.getSummary().isEmpty()
                || !DEFAULT_NOTE_TITLE.equals(noteEntity.getTitle());
        return hasImage || hasText;
    }

    /**
     * Adds a new entry record for the note. If there is already an entry for the specified note
     * entry, then it's data will get updated.
     */
    public void addNoteEntity(NotePresenterEntity noteEntity) {
        if (!isNoteEntityValid(noteEntity)) {
            return;
        }
        initPresenterFile();
        if (getPresenterEntityForNoteId(noteEntity.getId()) != null) {
            removeNoteEntity(noteEntity);
        }
        mDocument.getFirstChild().appendChild(createNoteElement(noteEntity));
        mNoteEntitiesCache.addNote(noteEntity);
        updatePresenterFile();
    }

    /**
     * Adds a note to a folder.
     * If the folder does not exist, then the node won't be moved anywhere.
     * If the note exists in some other folder, then it will be moved from the old folder to the
     * new one.
     */
    public void addNoteEntity(NotePresenterEntity noteEntity, String folderId) {
        if (!mNoteEntitiesCache.containsFolder(folderId) || !isNoteEntityValid(noteEntity)) {
            return;
        }
        initPresenterFile();
        Node folderNode = getFolderEntryNode(folderId);
        if (folderNode == null) {
            return;
        }

        String oldFolderId =
                mNoteEntitiesCache.getNoteParentFolderId(noteEntity);
        FolderPresenterEntity oldFolder =
                getPresenterEntityForFolderId(oldFolderId);
        if (oldFolder != null) {
            mNoteEntitiesCache.removeFolder(oldFolder);
            mNoteEntitiesCache.addFolder(oldFolder.toBuilder().removeNote(noteEntity).build());
        }

        removeNoteEntity(noteEntity);
        folderNode.appendChild(createNoteElement(noteEntity));

        NotePresenterEntity updatedNote =
                noteEntity.toBuilder().setParentFolderId(folderId).build();
        mNoteEntitiesCache.addNote(updatedNote);
        mNoteEntitiesCache.addNoteToFolder(updatedNote, folderId);

        updatePresenterFile();
    }

    public void addFolder(FolderPresenterEntity folderEntity) {
        if (folderEntity == null || getFolderEntryNode(folderEntity.getId()) != null) {
            return;
        }
        initPresenterFile();
        mDocument.getFirstChild().appendChild(createFolderElement(folderEntity));
        mNoteEntitiesCache.addFolder(folderEntity);
        updatePresenterFile();
    }

    /**
     * @param folderEntity existing folder to change its name.
     * @param newName      new name for the folder.
     */
    public void renameFolder(FolderPresenterEntity folderEntity, String newName) {
        boolean isNameInvalid = newName == null || newName.isEmpty();
        boolean unableToRenameFolder =
                folderEntity == null || getFolderEntryNode(folderEntity.getId()) == null;
        if (isNameInvalid || unableToRenameFolder) {
            return;
        }
        FolderPresenterEntity renamedFolderEntity =
                folderEntity.toBuilder().setName(newName).build();
        mNoteEntitiesCache.renameFolder(folderEntity, newName);

        initPresenterFile();
        Element folderNode = (Element) getFolderEntryNode(renamedFolderEntity.getId());
        folderNode.setAttribute(NAME_ATTR, newName);
        updatePresenterFile();
    }

    /**
     * Deletes the folder but keeps the notes, if it had any.
     */
    public void deleteFolderShallow(FolderPresenterEntity folderEntity) {
        if (folderEntity == null || getFolderEntryNode(folderEntity.getId()) == null) {
            return;
        }
        mNoteEntitiesCache.removeFolder(folderEntity);
        initPresenterFile();
        // remove the tag
        Node folderNode = getFolderEntryNode(folderEntity.getId());
        Node parent = folderNode.getParentNode();
        NodeList childNodes = folderNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            parent.appendChild(child);
        }
        parent.removeChild(folderNode);
        updatePresenterFile();
    }

    /**
     * Deletes the folder and the notes inside it.
     */
    public void deleteFolderDeep(FolderPresenterEntity folderEntity) {
        if (folderEntity == null || getFolderEntryNode(folderEntity.getId()) == null) {
            return;
        }
        mNoteEntitiesCache.removeFolder(folderEntity);
        for (NotePresenterEntity note : folderEntity.getNotes()) {
            mNoteEntitiesCache.removeNote(note);
        }
        initPresenterFile();
        Node folderNode = getFolderEntryNode(folderEntity.getId());
        Node parent = folderNode.getParentNode();
        parent.removeChild(folderNode);
        updatePresenterFile();
    }

    /**
     * Deletes the notes entries from the presenter file as well as deletes the files associated
     * with the notes from the file system.
     */
    public void deleteNotes(ImmutableSet<NotePresenterEntity> entities) {
        if (entities == null || !mPresenterFile.exists()) {
            return;
        }
        for (NotePresenterEntity note : entities) {
            deleteNoteEntity(note);
        }
    }

    public void deleteNote(NotePresenterEntity noteEntity) {
        deleteNoteEntity(noteEntity);
    }

    private void deleteNoteEntity(NotePresenterEntity noteEntity) {
        removeNoteEntity(noteEntity);
        NotesFileLibrary.deleteNoteFile(noteEntity.getId());
    }

    /**
     * Remove the entry for the specified entity.
     */
    private void removeNoteEntity(NotePresenterEntity noteEntity) {
        Node nodeToRemove = getNoteEntryNode(noteEntity.getId());
        if (nodeToRemove != null) {
            nodeToRemove.getParentNode().removeChild(nodeToRemove);
            mNoteEntitiesCache.removeNote(noteEntity);
            updatePresenterFile();
        }
    }

    private Node getNoteEntryNode(String noteId) {
        final String pathExpression = "/" + ROOT_TAG + "/" + NOTE_TAG + "[@id='" + noteId + "']";
        final String noteFolderExpression =
                "/" + ROOT_TAG + "/" + FOLDER_TAG + "/" + NOTE_TAG + "[@id='" + noteId + "']";
        Node noteNode = getNodeForPath(pathExpression);
        return noteNode != null ? noteNode : getNodeForPath(noteFolderExpression);
    }

    private Node getFolderEntryNode(String folderId) {
        final String pathExpression =
                "/" + ROOT_TAG + "/" + FOLDER_TAG + "[@id='" + folderId + "']";
        return getNodeForPath(pathExpression);
    }

    private FolderPresenterEntity getPresenterEntityForFolderId(String folderId) {
        if (mDocument == null) {
            initPresenterFile();
        }
        return getFolderEntityFromNode(getFolderEntryNode(folderId));
    }

    private FolderPresenterEntity getFolderEntityFromNode(Node folderEntryNode) {
        if (folderEntryNode == null) {
            return null;
        }
        NodeList entityItems = folderEntryNode.getChildNodes();
        FolderPresenterEntity.Builder builder = new FolderPresenterEntity.Builder();
        builder.setId(folderEntryNode.getAttributes().getNamedItem(ID_ATTR).getNodeValue());
        builder.setName(folderEntryNode.getAttributes().getNamedItem(NAME_ATTR).getNodeValue());

        for (int i = 0; i < entityItems.getLength(); i++) {
            Node child = entityItems.item(i);
            if (NOTE_TAG.equals(child.getNodeName())) {
                builder.addNote(getNoteEntityFromNode(child));
            }
        }
        return builder.build();
    }

    private NotePresenterEntity getPresenterEntityForNoteId(String noteId) {
        if (mDocument == null) {
            initPresenterFile();
        }
        return getNoteEntityFromNode(getNoteEntryNode(noteId));
    }

    /**
     * Extracts the fields for constructing a {@link NotePresenterEntity} instance from the
     * children of the specified node.
     */
    private NotePresenterEntity getNoteEntityFromNode(Node note) {
        if (note == null) {
            return null;
        }
        NodeList entityItems = note.getChildNodes();
        NotePresenterEntity.Builder builder = new NotePresenterEntity.Builder();
        builder.setId(note.getAttributes().getNamedItem(ID_ATTR).getNodeValue());

        Node noteParent = note.getParentNode();
        if (FOLDER_TAG.equals(noteParent.getNodeName())) {
            builder.setParentFolderId(
                    noteParent.getAttributes().getNamedItem(ID_ATTR).getNodeValue());
        }

        for (int i = 0; i < entityItems.getLength(); i++) {
            Node child = entityItems.item(i);
            String nodeName = child.getNodeName();
            String value = child.getTextContent();
            if (TITLE_TAG.equals(nodeName)) {
                builder.setTitle(value);
            } else if (SUMMARY_TAG.equals(nodeName)) {
                builder.setSummary(value);
            } else if (IMAGE_TAG.equals(nodeName)) {
                builder.setFeatureImagePath(value);
            } else if (WORDS_TAG.equals(nodeName)) {
                builder.setWordsCount(Integer.valueOf(value));
            } else if (DATE_TAG.equals(nodeName)) {
                builder.setLastEditDate(value);
            }
        }
        return builder.build();
    }

    private Node getNodeForPath(String path) {
        XPath xpath = XPathFactory.newInstance().newXPath();
        try {
            XPathExpression expression = xpath.compile(path);
            return (Node) expression.evaluate(mDocument, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            Log.e(TAG, "failed to compile path: " + path, e);
        }
        return null;
    }
}
