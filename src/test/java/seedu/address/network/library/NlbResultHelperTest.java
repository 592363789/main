package seedu.address.network.library;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import seedu.address.commons.util.FileUtil;
import seedu.address.model.book.Book;
import seedu.address.testutil.BookBuilder;
import seedu.address.testutil.TypicalBooks;

//@@author qiu-siqi
public class NlbResultHelperTest {
    private static final String TEST_DATA_FOLDER =
            FileUtil.getPath("src/test/data/NlbResultTest/");
    private static final File INVALID_RESPONSE_TOO_MANY_REQUESTS =
            new File(TEST_DATA_FOLDER + "InvalidResponseTooManyRequests.html");
    private static final File VALID_RESPONSE_BRIEF_DISPLAY =
            new File(TEST_DATA_FOLDER + "ValidResponseBriefDisplay.html");
    private static final File VALID_RESPONSE_FULL_DISPLAY =
            new File(TEST_DATA_FOLDER + "ValidResponseFullDisplay.html");
    private static final File VALID_RESPONSE_NO_RESULTS =
            new File(TEST_DATA_FOLDER + "ValidResponseNoResults.html");

    private static final String INVALID_RESPONSE_TOO_MANY_REQUESTS_URL = NlbResultHelper.NO_RESULTS_FOUND;
    private static final String VALID_RESPONSE_BRIEF_DISPLAY_URL =
            "https://catalogue.nlb.gov.sg/cgi-bin/spydus.exe/FULL/EXPNOS/BIBENQ/6585278/226559740,1";
    private static final String VALID_RESPONSE_FULL_DISPLAY_URL = NlbResultHelper.URL_FULL_DISPLAY
            .replaceAll("%s", "Harry Potter and the Classical World Richard A. Spencer");
    private static final String VALID_RESPONSE_NO_RESULTS_URL = NlbResultHelper.NO_RESULTS_FOUND;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void getUrl_nullString_throwsNullPointerException() {
        thrown.expect(NullPointerException.class);
        NlbResultHelper.getUrl(null, TypicalBooks.ARTEMIS);
    }

    @Test
    public void getUrl_nullBook_throwsNullPointerException() throws Exception {
        String content = FileUtil.readFromFile(VALID_RESPONSE_BRIEF_DISPLAY);
        thrown.expect(NullPointerException.class);
        NlbResultHelper.getUrl(content, null);
    }

    @Test
    public void getUrl_invalidResponseTooManyRequests_success() throws Exception {
        String content = FileUtil.readFromFile(INVALID_RESPONSE_TOO_MANY_REQUESTS);
        assertEquals(INVALID_RESPONSE_TOO_MANY_REQUESTS_URL, NlbResultHelper.getUrl(content, TypicalBooks.ARTEMIS));
    }

    @Test
    public void getUrl_validResponseBriefDisplay_success() throws Exception {
        String content = FileUtil.readFromFile(VALID_RESPONSE_BRIEF_DISPLAY);
        assertEquals(VALID_RESPONSE_BRIEF_DISPLAY_URL, NlbResultHelper.getUrl(content, TypicalBooks.ARTEMIS));
    }

    @Test
    public void getUrl_validResponseFullDisplay_success() throws Exception {
        String content = FileUtil.readFromFile(VALID_RESPONSE_FULL_DISPLAY);
        Book expectedBook = new BookBuilder()
                .withTitle("Harry Potter and the Classical World").withAuthors("Richard A. Spencer").build();
        assertEquals(VALID_RESPONSE_FULL_DISPLAY_URL, NlbResultHelper.getUrl(content, expectedBook));
    }

    @Test
    public void getUrl_validResponseNoResults() throws Exception {
        String content = FileUtil.readFromFile(VALID_RESPONSE_NO_RESULTS);
        Book book = new BookBuilder().withTitle("xxxxxxxxxxxxxxxxxxxxxx").withAuthors("yyyyyyyyy").build();
        assertEquals(VALID_RESPONSE_NO_RESULTS_URL, NlbResultHelper.getUrl(content, book));

    }
}
