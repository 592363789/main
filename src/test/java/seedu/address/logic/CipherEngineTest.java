package seedu.address.logic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import seedu.address.commons.util.FileUtil;
import seedu.address.commons.util.StringUtil;

//@@author qiu-siqi
public class CipherEngineTest {

    private static final String TEST_DATA_FOLDER = FileUtil.getPath("./src/test/data/XmlUtilTest/");
    private static final String VALID_BOOK_SHELF = TEST_DATA_FOLDER + "validBookShelf.xml";

    private static final String TEST_KEY_1 = "";
    private static final String TEST_KEY_2 = "Qg5gk20g%1~";

    private static final String PASSWORD_1 = "";
    private static final String PASSWORD_2 = "thisismypassword";
    private static final String PASSWORD_3 = "1RS#(`D #Q HT%";

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void encryptDecryptFile() throws Exception {
        File validBookShelf = new File(VALID_BOOK_SHELF);
        String tempFile = testFolder.getRoot().getPath() + File.separator
                + StringUtil.generateRandomPrefix() + "temp.xml";
        File copy = new File(tempFile);

        try {
            FileUtil.copyFile(validBookShelf, copy);

            CipherEngine.encryptFile(tempFile, TEST_KEY_1);
            assertDifferentContent(validBookShelf, copy);
            CipherEngine.decryptFile(tempFile, TEST_KEY_1);
            assertSameContent(validBookShelf, copy);

            CipherEngine.encryptFile(tempFile, TEST_KEY_2);
            assertDifferentContent(validBookShelf, copy);
            CipherEngine.decryptFile(tempFile, TEST_KEY_2);
            assertSameContent(validBookShelf, copy);
        } finally {
            copy.delete();
        }
    }

    @Test
    public void encryptDecryptKey() throws Exception {
        String hashed = CipherEngine.hashPassword(PASSWORD_1);
        assertNotEquals(hashed, PASSWORD_1);
        assertTrue(CipherEngine.isValidPasswordHash(hashed));
        assertTrue(CipherEngine.checkPassword(PASSWORD_1, hashed));

        hashed = CipherEngine.hashPassword(PASSWORD_2);
        assertNotEquals(hashed, PASSWORD_2);
        assertTrue(CipherEngine.isValidPasswordHash(hashed));
        assertTrue(CipherEngine.checkPassword(PASSWORD_2, hashed));

        hashed = CipherEngine.hashPassword(PASSWORD_3);
        assertNotEquals(hashed, PASSWORD_3);
        assertTrue(CipherEngine.isValidPasswordHash(hashed));
        assertTrue(CipherEngine.checkPassword(PASSWORD_3, hashed));
    }

    //@@author
    @Test
    public void checkPassword_invalidHash_throwsIllegalArgumentException() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        CipherEngine.checkPassword("12345", "PBKDF2WithHmacSHA256$22000$256$hash");
    }

    @Test
    public void isValidPasswordHash_invalidHash_false() {
        assertFalse(CipherEngine.isValidPasswordHash("PBKDF2WithHmacSHA256$22000$256$hash"));
        assertFalse(CipherEngine.isValidPasswordHash("PBKDF2WithHmacSHA256$xxxx$xxxx$hash$salt"));
    }

    @Test
    public void isValidPasswordHash_validHash_true() {
        assertTrue(CipherEngine.isValidPasswordHash("PBKDF2WithHmacSHA256$22000$256$hash$salt"));
    }
    //@@author qiu-siqi

    private boolean isSameInContent(File one, File two) throws IOException {
        return Arrays.equals(Files.readAllBytes(one.toPath()), (Files.readAllBytes(two.toPath())));
    }

    private void assertSameContent(File one, File two) throws IOException {
        assertTrue(isSameInContent(one, two));
    }

    private void assertDifferentContent(File one, File two) throws IOException {
        assertFalse(isSameInContent(one, two));
    }
}
