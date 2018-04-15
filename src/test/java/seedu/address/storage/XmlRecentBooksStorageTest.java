package seedu.address.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static seedu.address.testutil.TypicalBooks.ARTEMIS;
import static seedu.address.testutil.TypicalBooks.BABYLON_ASHES;
import static seedu.address.testutil.TypicalBooks.getTypicalBookShelf;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import seedu.address.commons.exceptions.DataConversionException;
import seedu.address.commons.util.FileUtil;
import seedu.address.model.BookShelf;
import seedu.address.model.ReadOnlyBookShelf;

//@@author qiu-siqi
public class XmlRecentBooksStorageTest {
    private static final String TEST_DATA_FOLDER =
            FileUtil.getPath("./src/test/data/XmlBookShelfStorageTest/");

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void readRecentBooksList_nullFilePath_throwsNullPointerException() throws Exception {
        thrown.expect(NullPointerException.class);
        readRecentBooksList(null);
    }

    private java.util.Optional<ReadOnlyBookShelf> readRecentBooksList(String filePath) throws Exception {
        return new XmlRecentBooksStorage(filePath).readRecentBooksList(addToTestDataPathIfNotNull(filePath));
    }

    private String addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null ? TEST_DATA_FOLDER + prefsFileInTestDataFolder : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readRecentBooksList("NonExistentFile.xml").isPresent());
    }

    @Test
    public void read_notXmlFormat_exceptionThrown() throws Exception {
        thrown.expect(DataConversionException.class);
        readRecentBooksList("NotXmlFormatBookShelf.xml");
    }

    @Test
    public void readRecentBooksList_invalidBookBookShelf_throwDataConversionException() throws Exception {
        thrown.expect(DataConversionException.class);
        readRecentBooksList("invalidBookBookShelf.xml");
    }

    @Test
    public void readRecentBooksList_invalidAndValidBookBookShelf_throwDataConversionException() throws Exception {
        thrown.expect(DataConversionException.class);
        readRecentBooksList("invalidAndValidBookBookShelf.xml");
    }

    @Test
    public void readAndSaveRecentBooksList_allInOrder_success() throws Exception {
        String filePath = testFolder.getRoot().getPath() + File.separator + "TempRecentBooksData.xml";
        BookShelf original = getTypicalBookShelf();
        XmlRecentBooksStorage xmlRecentBooksStorage = new XmlRecentBooksStorage(filePath);

        //Save in new file and read back
        xmlRecentBooksStorage.saveRecentBooksList(original, filePath);
        ReadOnlyBookShelf readBack = xmlRecentBooksStorage.readRecentBooksList(filePath).get();
        assertEquals(original, new BookShelf(readBack));

        //Modify data, overwrite exiting file, and read back
        original.removeBook(ARTEMIS);
        original.addBook(ARTEMIS);
        original.removeBook(BABYLON_ASHES);
        xmlRecentBooksStorage.saveRecentBooksList(original, filePath);
        readBack = xmlRecentBooksStorage.readRecentBooksList(filePath).get();
        assertEquals(original, new BookShelf(readBack));

        //Save and read without specifying file path
        original.addBook(BABYLON_ASHES);
        xmlRecentBooksStorage.saveRecentBooksList(original); //file path not specified
        readBack = xmlRecentBooksStorage.readRecentBooksList().get(); //file path not specified
        assertEquals(original, new BookShelf(readBack));

    }

    @Test
    public void saveRecentBooksList_nullBookShelf_throwsNullPointerException() {
        thrown.expect(NullPointerException.class);
        saveRecentBooksList(null, "SomeFile.xml");
    }

    /**
     * Saves {@code recentBooksData} at the specified {@code filePath}.
     */
    private void saveRecentBooksList(ReadOnlyBookShelf recentBooksData, String filePath) {
        try {
            new XmlRecentBooksStorage(filePath).saveRecentBooksList(recentBooksData,
                    addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveRecentBooksList_nullFilePath_throwsNullPointerException() {
        thrown.expect(NullPointerException.class);
        saveRecentBooksList(new BookShelf(), null);
    }

}
