package seedu.address.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static seedu.address.testutil.TypicalAliases.SEARCH;
import static seedu.address.testutil.TypicalAliases.UNREAD;
import static seedu.address.testutil.TypicalAliases.getTypicalAliasList;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import seedu.address.commons.exceptions.DataConversionException;
import seedu.address.commons.util.FileUtil;
import seedu.address.model.alias.ReadOnlyAliasList;
import seedu.address.model.alias.UniqueAliasList;

public class XmlAliasListStorageTest {
    private static final String TEST_DATA_FOLDER = FileUtil.getPath("./src/test/data/XmlAliasListStorageTest/");

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void readAliasList_nullFilePath_throwsNullPointerException() throws Exception {
        thrown.expect(NullPointerException.class);
        readAliasList(null);
    }

    private java.util.Optional<ReadOnlyAliasList> readAliasList(String filePath) throws Exception {
        return new XmlAliasListStorage(filePath).readAliasList(addToTestDataPathIfNotNull(filePath));
    }

    private String addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER + prefsFileInTestDataFolder
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAliasList("NonExistentFile.xml").isPresent());
    }

    @Test
    public void read_notXmlFormat_exceptionThrown() throws Exception {
        thrown.expect(DataConversionException.class);
        readAliasList("NotXmlFormatAliasList.xml");
    }

    @Test
    public void readAliasList_invalidAliasAliasList_throwDataConversionException() throws Exception {
        thrown.expect(DataConversionException.class);
        readAliasList("invalidAliasAliasList.xml");
    }

    @Test
    public void readAliasList_invalidAndValidAliasAliasList_throwDataConversionException() throws Exception {
        thrown.expect(DataConversionException.class);
        readAliasList("invalidAndValidAliasAliasList.xml");
    }

    @Test
    public void readAndSaveAliasList_allInOrder_success() throws Exception {
        String filePath = testFolder.getRoot().getPath() + File.separator + "TempAliasList.xml";
        UniqueAliasList original = getTypicalAliasList();
        XmlAliasListStorage xmlAliasListStorage = new XmlAliasListStorage(filePath);

        // save in new file and read back
        xmlAliasListStorage.saveAliasList(original, filePath);
        ReadOnlyAliasList readBack = xmlAliasListStorage.readAliasList(filePath).get();
        assertEquals(original, new UniqueAliasList(readBack));

        // modify data, overwrite exiting file, and read back
        original.remove(UNREAD.getName());
        original.add(UNREAD);
        original.remove(SEARCH.getName());
        xmlAliasListStorage.saveAliasList(original, filePath);
        readBack = xmlAliasListStorage.readAliasList(filePath).get();
        assertEquals(original, new UniqueAliasList(readBack));

        // save and read without specifying file path
        original.add(SEARCH);
        xmlAliasListStorage.saveAliasList(original); // file path not specified
        readBack = xmlAliasListStorage.readAliasList().get(); // file path not specified
        assertEquals(original, new UniqueAliasList(readBack));
    }

    @Test
    public void saveAliasList_nullAliasList_throwsNullPointerException() {
        thrown.expect(NullPointerException.class);
        saveAliasList(null, "SomeFile.xml");
    }

    /**
     * Saves {@code aliasList} at the specified {@code filePath}.
     */
    private void saveAliasList(ReadOnlyAliasList aliasList, String filePath) {
        try {
            new XmlAliasListStorage(filePath).saveAliasList(aliasList, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAliasList_nullFilePath_throwsNullPointerException() {
        thrown.expect(NullPointerException.class);
        saveAliasList(new UniqueAliasList(), null);
    }
}
