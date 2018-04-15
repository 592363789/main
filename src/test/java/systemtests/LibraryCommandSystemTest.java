package systemtests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import guitests.GuiRobot;
import javafx.collections.ObservableList;
import seedu.address.commons.core.Messages;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.LibraryCommand;
import seedu.address.logic.commands.RecentCommand;
import seedu.address.logic.commands.SearchCommand;
import seedu.address.logic.commands.SelectCommand;
import seedu.address.model.Model;
import seedu.address.model.book.Book;

//@@author qiu-siqi
public class LibraryCommandSystemTest extends BibliotekSystemTest {

    private static final String NLB_CATALOGUE_URL = "catalogue.nlb.gov.sg";

    @Test
    public void library() {

        /* ---------- Test on book shelf ------------- */

        ObservableList<Book> bookList = getModel().getDisplayBookList();

        assertCommandSuccess(LibraryCommand.COMMAND_WORD + " 1", bookList.get(0));
        assertValidUrlLoaded();

        assertCommandSuccess(LibraryCommand.COMMAND_WORD + " " + bookList.size(),
                bookList.get(bookList.size() - 1));
        assertValidUrlLoaded();

        assertCommandFailure(LibraryCommand.COMMAND_WORD + " " + (bookList.size() + 1),
                Messages.MESSAGE_INVALID_BOOK_DISPLAYED_INDEX);

        /* ---------- Test on search results list ------------- */

        executeBackgroundCommand(SearchCommand.COMMAND_WORD + " hello", SearchCommand.MESSAGE_SEARCHING);

        bookList = getModel().getSearchResultsList();

        assertCommandSuccess(LibraryCommand.COMMAND_WORD + " 1", bookList.get(0));
        assertCommandFailure(LibraryCommand.COMMAND_WORD + " " + (bookList.size() + 1),
                Messages.MESSAGE_INVALID_BOOK_DISPLAYED_INDEX);

        /* ---------- Test on recent books list ------------- */

        executeCommand(SelectCommand.COMMAND_WORD + " 1");
        executeCommand(RecentCommand.COMMAND_WORD);

        bookList = getModel().getRecentBooksList();

        assertCommandSuccess(LibraryCommand.COMMAND_WORD + " 1", bookList.get(0));
        assertCommandFailure(LibraryCommand.COMMAND_WORD + " " + (bookList.size() + 1),
                Messages.MESSAGE_INVALID_BOOK_DISPLAYED_INDEX);

        /* ---------- Negative test cases ------------- */

        assertCommandFailure(LibraryCommand.COMMAND_WORD + " 0",
                String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, LibraryCommand.MESSAGE_USAGE));
    }

    /**
     * Executes {@code command} and verifies that, after the web API has returned a result,<br>
     * 1. Command box displays an empty string.<br>
     * 2. Command box has the default style class.<br>
     * 3. Result display box displays the success message.<br>
     * 4. {@code Model} and {@code Storage} remain unchanged.<br>
     * 5. Selected book list card remain unchanged.<br>
     * 6. {@code BookInLibraryPanel} is visible.
     * 7. Status bar's sync status remains unchanged.<br>
     * Verifications 1, 3 and 4 are performed by
     * {@code BibliotekSystemTest#assertApplicationDisplaysExpected(String, String, Model)}.<br>
     * @see BibliotekSystemTest#assertApplicationDisplaysExpected(String, String, Model)
     * @see BibliotekSystemTest#assertSelectedBookListCardChanged(Index)
     */
    private void assertCommandSuccess(String command, Book bookToSearch) {
        Model expectedModel = getModel();

        executeCommand(command);
        assertCommandBoxShowsDefaultStyle();

        new GuiRobot().waitForEvent(() -> !getResultDisplay().getText().equals(LibraryCommand.MESSAGE_SEARCHING));

        String expectedResultMessage = String.format(LibraryCommand.MESSAGE_SUCCESS, bookToSearch);

        assertApplicationDisplaysExpected("", expectedResultMessage, expectedModel);
        assertSelectedBookListCardUnchanged();
        assertCommandBoxShowsDefaultStyle();
        assertBookInLibraryPanelVisible();
        assertStatusBarUnchanged();
    }

    /**
     * Verifies that some NLB catalogue page is being loaded.
     */
    private void assertValidUrlLoaded() {
        assertTrue(getBookInLibraryPanel().getLoadedUrl().toExternalForm().contains(NLB_CATALOGUE_URL));
    }
}
