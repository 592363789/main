package seedu.address.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static seedu.address.testutil.TypicalBooks.ARTEMIS;
import static seedu.address.testutil.TypicalBooks.BABYLON_ASHES;
import static seedu.address.testutil.TypicalBooks.getTypicalBookShelf;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import seedu.address.commons.exceptions.DataConversionException;
import seedu.address.commons.util.FileUtil;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.LockManager;
import seedu.address.model.BookShelf;
import seedu.address.model.ReadOnlyBookShelf;

public class XmlBookShelfStorageTest {
    private static final String TEST_DATA_FOLDER = FileUtil.getPath("./src/test/data/XmlBookShelfStorageTest/");

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @After
    public void tearDown() {
        LockManager.getInstance().initialize(LockManager.NO_PASSWORD);
    }

    @Test
    public void readBookShelf_nullFilePath_throwsNullPointerException() throws Exception {
        thrown.expect(NullPointerException.class);
        readBookShelf(null);
    }

    private java.util.Optional<ReadOnlyBookShelf> readBookShelf(String filePath) throws Exception {
        return new XmlBookShelfStorage(filePath).readBookShelf(addToTestDataPathIfNotNull(filePath));
    }

    private String addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER + prefsFileInTestDataFolder
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readBookShelf("NonExistentFile.xml").isPresent());
    }

    @Test
    public void read_notXmlFormat_exceptionThrown() throws Exception {

        thrown.expect(DataConversionException.class);
        readBookShelf("NotXmlFormatBookShelf.xml");

        /* IMPORTANT: Any code below an exception-throwing line (like the one above) will be ignored.
         * That means you should not have more than one exception test in one method
         */
    }

    @Test
    public void readBookShelf_invalidBookBookShelf_throwDataConversionException() throws Exception {
        thrown.expect(DataConversionException.class);
        readBookShelf("invalidBookBookShelf.xml");
    }

    @Test
    public void readBookShelf_invalidAndValidBookBookShelf_throwDataConversionException() throws Exception {
        thrown.expect(DataConversionException.class);
        readBookShelf("invalidAndValidBookBookShelf.xml");
    }

    @Test
    public void readAndSaveBookShelf_allInOrder_success() throws Exception {
        String filePath = testFolder.getRoot().getPath() + File.separator
                + StringUtil.generateRandomPrefix() + "temp.xml";
        BookShelf original = getTypicalBookShelf();
        XmlBookShelfStorage xmlBookShelfStorage = new XmlBookShelfStorage(filePath);

        //Save in new file and read back
        xmlBookShelfStorage.saveBookShelf(original, filePath);
        ReadOnlyBookShelf readBack = xmlBookShelfStorage.readBookShelf(filePath).get();
        assertEquals(original, new BookShelf(readBack));

        //Modify data, overwrite exiting file, and read back
        original.removeBook(ARTEMIS);
        original.addBook(ARTEMIS);
        original.removeBook(BABYLON_ASHES);
        xmlBookShelfStorage.saveBookShelf(original, filePath);
        readBack = xmlBookShelfStorage.readBookShelf(filePath).get();
        assertEquals(original, new BookShelf(readBack));

        //Save and read without specifying file path
        original.addBook(BABYLON_ASHES);
        xmlBookShelfStorage.saveBookShelf(original); //file path not specified
        readBack = xmlBookShelfStorage.readBookShelf().get(); //file path not specified
        assertEquals(original, new BookShelf(readBack));

    }

    @Test
    public void readAndSaveBookShelf_withPassword_success() throws Exception {
        String filePath = testFolder.getRoot().getPath() + File.separator
                + StringUtil.generateRandomPrefix() + "temp.xml";
        BookShelf original = getTypicalBookShelf();
        XmlBookShelfStorage xmlBookShelfStorage = new XmlBookShelfStorage(filePath);

        LockManager.getInstance().initialize(LockManager.NO_PASSWORD);
        LockManager.getInstance().setPassword(LockManager.NO_PASSWORD, "newpw");

        //Save in new file and read back
        xmlBookShelfStorage.saveBookShelf(original, filePath);
        ReadOnlyBookShelf readBack = xmlBookShelfStorage.readBookShelf(filePath).get();
        assertEquals(original, new BookShelf(readBack));
    }

    @Test
    public void readBookShelf_differentPassword_throwDataConversionException() throws Exception {
        LockManager.getInstance().initialize(LockManager.NO_PASSWORD);
        LockManager.getInstance().setPassword(LockManager.NO_PASSWORD, "newpw");

        String filePath = testFolder.getRoot().getPath() + File.separator
                + StringUtil.generateRandomPrefix() + "temp.xml";
        BookShelf original = getTypicalBookShelf();
        XmlBookShelfStorage xmlBookShelfStorage = new XmlBookShelfStorage(filePath);
        xmlBookShelfStorage.saveBookShelf(original, filePath);

        LockManager.getInstance().setPassword("newpw", "hunter2");

        File tempFile = new File(filePath);
        try {
            thrown.expect(DataConversionException.class);
            new XmlBookShelfStorage("TempBookShelf.xml").readBookShelf(filePath);
        } finally {
            tempFile.delete();
        }
    }

    @Test
    public void saveBookShelf_nullBookShelf_throwsNullPointerException() {
        thrown.expect(NullPointerException.class);
        saveBookShelf(null, "SomeFile.xml");
    }

    /**
     * Saves {@code bookShelf} at the specified {@code filePath}.
     */
    private void saveBookShelf(ReadOnlyBookShelf bookShelf, String filePath) {
        try {
            new XmlBookShelfStorage(filePath).saveBookShelf(bookShelf, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveBookShelf_nullFilePath_throwsNullPointerException() {
        thrown.expect(NullPointerException.class);
        saveBookShelf(new BookShelf(), null);
    }

}
