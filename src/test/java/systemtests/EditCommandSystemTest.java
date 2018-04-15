package systemtests;

import static org.junit.Assert.assertTrue;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PRIORITY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RATING;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_BOOK;

import org.junit.Test;

import seedu.address.commons.core.Messages;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.SelectCommand;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.book.Book;
import seedu.address.model.book.Priority;
import seedu.address.model.book.Status;
import seedu.address.testutil.BookBuilder;

public class EditCommandSystemTest extends BibliotekSystemTest {

    @Test
    public void edit() throws Exception {

        /* ----------------- Performing edit operation while an unfiltered list is being shown ---------------------- */

        /* Case: edit all fields -> edited */
        Index index = INDEX_FIRST_BOOK;
        String command = " " + EditCommand.COMMAND_WORD + "  " + index.getOneBased() + "  " + PREFIX_STATUS + "u  "
                + PREFIX_PRIORITY + "high " + PREFIX_RATING + "4  ";
        Book editedBook = new BookBuilder(getModel().getDisplayBookList().get(0)).withStatus(Status.UNREAD)
                .withPriority(Priority.HIGH).withRating(4).build();
        assertCommandSuccess(command, index, editedBook);

        /* Case: edit a book with new values same as existing values -> edited */
        Book bookToEdit = getModel().getDisplayBookList().get(0);
        command = EditCommand.COMMAND_WORD + " " + index.getOneBased() + " " + PREFIX_STATUS
                + bookToEdit.getStatus().toString() + " " + PREFIX_PRIORITY + bookToEdit.getPriority().toString()
                + " " + PREFIX_RATING + bookToEdit.getRating().toString();
        assertCommandSuccess(command, index, new BookBuilder(bookToEdit).build());

        /* Case: edit some fields -> edited */
        command = EditCommand.COMMAND_WORD + " " + index.getOneBased() + " " + PREFIX_STATUS + "r";
        bookToEdit = getModel().getDisplayBookList().get(index.getZeroBased());
        editedBook = new BookBuilder(bookToEdit).withStatus(Status.READ).build();
        assertCommandSuccess(command, index, editedBook);

        /* Case: un-rate book -> unrated */
        index = Index.fromOneBased(getModel().getDisplayBookList().size());
        bookToEdit = getModel().getDisplayBookList().get(index.getZeroBased());
        command = EditCommand.COMMAND_WORD + " " + index.getOneBased() + " " + PREFIX_RATING + "-1";
        editedBook = new BookBuilder(bookToEdit).withRating(-1).build();
        assertCommandSuccess(command, index, editedBook);
        //@@author 592363789
        /* Case: non-priority book -> non-priority */
        index = Index.fromOneBased(getModel().getDisplayBookList().size());
        bookToEdit = getModel().getDisplayBookList().get(index.getZeroBased());
        command = EditCommand.COMMAND_WORD + " " + index.getOneBased() + " " + PREFIX_PRIORITY + "n";
        editedBook = new BookBuilder(bookToEdit).withPriority(Priority.NONE).build();
        assertCommandSuccess(command, index, editedBook);

        /* Case: unread book -> unread */
        index = Index.fromOneBased(getModel().getDisplayBookList().size());
        bookToEdit = getModel().getDisplayBookList().get(index.getZeroBased());
        command = EditCommand.COMMAND_WORD + " " + index.getOneBased() + " " + PREFIX_STATUS + "u";
        editedBook = new BookBuilder(bookToEdit).withStatus(Status.UNREAD).build();
        assertCommandSuccess(command, index, editedBook);

        /* Case: edit two fields -> rating and status */
        index = Index.fromOneBased(getModel().getDisplayBookList().size());
        bookToEdit = getModel().getDisplayBookList().get(index.getZeroBased());
        command = EditCommand.COMMAND_WORD + " " + index.getOneBased() + " " + PREFIX_STATUS + "u" + " "
                + PREFIX_RATING + "4";
        editedBook = new BookBuilder(bookToEdit).withStatus(Status.UNREAD).withRating(4).build();
        assertCommandSuccess(command, index, editedBook);

        /* Case: edit two fields -> rating and priority */
        index = Index.fromOneBased(getModel().getDisplayBookList().size());
        bookToEdit = getModel().getDisplayBookList().get(index.getZeroBased());
        command = EditCommand.COMMAND_WORD + " " + index.getOneBased() + " " + PREFIX_PRIORITY + "h"
                + " " + PREFIX_RATING + "0";
        editedBook = new BookBuilder(bookToEdit).withPriority(Priority.HIGH).withRating(0).build();
        assertCommandSuccess(command, index, editedBook);

        /* Case: edit two fields -> status and priority */
        index = Index.fromOneBased(getModel().getDisplayBookList().size());
        bookToEdit = getModel().getDisplayBookList().get(index.getZeroBased());
        command = EditCommand.COMMAND_WORD + " " + index.getOneBased() + " " + PREFIX_STATUS + "r"
                + " " + PREFIX_PRIORITY + "l";
        editedBook = new BookBuilder(bookToEdit).withStatus(Status.READ).withPriority(Priority.LOW).build();
        assertCommandSuccess(command, index, editedBook);
        //@@author
        /* ------------------ Performing edit operation while a filtered list is being shown ------------------------ */

        /* Case: filtered book list, edit index within bounds of book shelf and book list -> edited */
        executeCommand(ListCommand.COMMAND_WORD + " " + PREFIX_STATUS + "u");
        index = INDEX_FIRST_BOOK;
        assertTrue(index.getZeroBased() < getModel().getDisplayBookList().size());
        command = EditCommand.COMMAND_WORD + " " + index.getOneBased() + " " + PREFIX_STATUS + "r";
        bookToEdit = getModel().getDisplayBookList().get(index.getZeroBased());
        editedBook = new BookBuilder(bookToEdit).withStatus(Status.READ).build();
        assertCommandSuccess(command, index, editedBook);

        /* Case: edit index within bounds of book shelf but out of bounds of filtered book list -> rejected */
        executeCommand(ListCommand.COMMAND_WORD + " " + PREFIX_STATUS + "u");
        int invalidIndex = getModel().getDisplayBookList().size() + 1;
        assertCommandFailure(EditCommand.COMMAND_WORD + " " + invalidIndex + " " + PREFIX_RATING + "2",
                Messages.MESSAGE_INVALID_BOOK_DISPLAYED_INDEX);

        /* --------------------- Performing edit operation while a book card is selected -------------------------- */

        /* Case: selects first card in the book list, edit a book -> edited, card selection remains unchanged */
        executeCommand(ListCommand.COMMAND_WORD);
        index = INDEX_FIRST_BOOK;
        executeCommand(SelectCommand.COMMAND_WORD + " " + index.getOneBased());
        command = EditCommand.COMMAND_WORD + " " + index.getOneBased() + " " + PREFIX_RATING + "5";
        bookToEdit = getModel().getDisplayBookList().get(index.getZeroBased());
        editedBook = new BookBuilder(bookToEdit).withRating(5).build();
        Model expectedModel = getModel();
        expectedModel.updateBook(bookToEdit, editedBook);
        assertCommandSuccess(command, editedBook, expectedModel);

        /* --------------------------------- Performing invalid edit operation -------------------------------------- */


        /* Case: invalid index (0) -> rejected */
        assertCommandFailure(EditCommand.COMMAND_WORD + " 0 " + PREFIX_RATING + "5",
                String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));

        /* Case: invalid index (-1) -> rejected */
        assertCommandFailure(EditCommand.COMMAND_WORD + " -1 " + PREFIX_RATING + "5",
                String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));

        /* Case: invalid index (size + 1) -> rejected */
        invalidIndex = getModel().getDisplayBookList().size() + 1;
        assertCommandFailure(EditCommand.COMMAND_WORD + " " + invalidIndex + " " + PREFIX_RATING + "5",
                Messages.MESSAGE_INVALID_BOOK_DISPLAYED_INDEX);

        /* Case: missing index -> rejected */
        assertCommandFailure(EditCommand.COMMAND_WORD + " " + PREFIX_RATING + "5",
                String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));

        /* Case: missing all fields -> rejected */
        assertCommandFailure(EditCommand.COMMAND_WORD + " " + INDEX_FIRST_BOOK.getOneBased(),
                EditCommand.MESSAGE_NO_PARAMETERS);

        /* Case: invalid status -> rejected */
        assertCommandFailure(EditCommand.COMMAND_WORD + " " + INDEX_FIRST_BOOK.getOneBased() + " "
                + PREFIX_STATUS + "status", Messages.MESSAGE_INVALID_STATUS);

        /* Case: invalid priority -> rejected */
        assertCommandFailure(EditCommand.COMMAND_WORD + " " + INDEX_FIRST_BOOK.getOneBased() + " "
                + PREFIX_PRIORITY + "priority", Messages.MESSAGE_INVALID_PRIORITY);

        /* Case: invalid rating -> rejected */
        assertCommandFailure(EditCommand.COMMAND_WORD + " " + INDEX_FIRST_BOOK.getOneBased() + " "
                + PREFIX_RATING + "100", Messages.MESSAGE_INVALID_RATING);
        //@@author 592363789
        /* Case: valid rating + invalid status -> rejected */
        assertCommandFailure(EditCommand.COMMAND_WORD + " " + INDEX_FIRST_BOOK.getOneBased() + " "
                + PREFIX_RATING + "3" + " " + PREFIX_STATUS + "STATUS", Messages.MESSAGE_INVALID_STATUS);

        /* Case: valid rating + invalid priority -> rejected */
        assertCommandFailure(EditCommand.COMMAND_WORD + " " + INDEX_FIRST_BOOK.getOneBased() + " "
                + PREFIX_RATING + "3" + " " + PREFIX_PRIORITY + "priority", Messages.MESSAGE_INVALID_PRIORITY);

        /* Case: valid priority + invalid status -> rejected */
        assertCommandFailure(EditCommand.COMMAND_WORD + " " + INDEX_FIRST_BOOK.getOneBased() + " "
                + PREFIX_PRIORITY + "h" + " " + PREFIX_STATUS + "STATUS", Messages.MESSAGE_INVALID_STATUS);

        /* Case: valid priority + invalid rating -> rejected */
        assertCommandFailure(EditCommand.COMMAND_WORD + " " + INDEX_FIRST_BOOK.getOneBased() + " "
                + PREFIX_RATING + "-100" + " " + PREFIX_STATUS + "r", Messages.MESSAGE_INVALID_RATING);

        /* Case: valid status + invalid rating -> rejected */
        assertCommandFailure(EditCommand.COMMAND_WORD + " " + INDEX_FIRST_BOOK.getOneBased() + " "
                + PREFIX_RATING + "-3" + " " + PREFIX_STATUS + "r", Messages.MESSAGE_INVALID_RATING);

        /* Case: valid status + invalid priority -> rejected */
        assertCommandFailure(EditCommand.COMMAND_WORD + " " + INDEX_FIRST_BOOK.getOneBased() + " "
                + PREFIX_PRIORITY + "highpriority" + " " + PREFIX_STATUS
                + "r", Messages.MESSAGE_INVALID_PRIORITY);
        //@@author
    }

    /**
     * Performs the same verification as {@code assertCommandSuccess(String, Book, Model)} with the
     * expected model having an empty search result and recent book list, and the only difference in
     * book shelf is the edited book.
     * @see EditCommandSystemTest#assertCommandSuccess(String, Book, Model)
     */
    private void assertCommandSuccess(String command, Index index, Book expectedBook) throws Exception {
        Model model = getModel();
        Model expectedModel = new ModelManager(model.getBookShelf(), new UserPrefs(),
                model.getRecentBooksListAsBookShelf(), model.getAliasList());
        expectedModel.updateBookListFilter(getModel().getBookListFilter());
        expectedModel.updateBook(expectedModel.getDisplayBookList().get(index.getZeroBased()), expectedBook);
        assertCommandSuccess(command, expectedBook, expectedModel);
    }

    /**
     * Executes {@code command} and verifies that,<br>
     * 1. Command box displays an empty string.<br>
     * 2. Command box has the default style class.<br>
     * 3. Result display box displays the success message.<br>
     * 4. {@code Model}, {@code Storage} and {@code BookListPanel} equal to the corresponding components
     * after editing.<br>
     * 5. Selected book card remain unchanged.<br>
     * 6. Status bar's sync status changes.<br>
     * Verifications 1, 3 and 4 are performed by
     * {@code BibliotekSystemTest#assertApplicationDisplaysExpected(String, String, Model)}.<br>
     * @see BibliotekSystemTest#assertApplicationDisplaysExpected(String, String, Model)
     * @see BibliotekSystemTest#assertSelectedBookListCardChanged(Index)
     */
    private void assertCommandSuccess(String command, Book expectedBook, Model expectedModel) throws Exception {
        executeCommand(command);
        assertApplicationDisplaysExpected("",
                String.format(EditCommand.MESSAGE_SUCCESS, expectedBook), expectedModel);
        assertCommandBoxShowsDefaultStyle();
        assertSelectedBookListCardUnchanged();
        assertStatusBarUnchangedExceptSyncStatus();
    }
}
