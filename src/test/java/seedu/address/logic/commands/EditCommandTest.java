package seedu.address.logic.commands;
//@@author 592363789
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.prepareUndoCommand;
import static seedu.address.logic.commands.CommandTestUtil.showBookAtIndex;
import static seedu.address.testutil.TypicalBooks.getTypicalBookShelf;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_BOOK;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_BOOK;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import seedu.address.commons.core.Messages;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.CommandHistory;
import seedu.address.logic.UndoStack;
import seedu.address.logic.commands.EditCommand.EditDescriptor;
import seedu.address.model.ActiveListType;
import seedu.address.model.BookShelf;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.book.Book;
import seedu.address.model.book.Priority;
import seedu.address.model.book.Rating;
import seedu.address.model.book.Status;
import seedu.address.network.NetworkManager;
import seedu.address.testutil.BookBuilder;
import seedu.address.testutil.EditDescriptorBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for EditCommand.
 */
//@@author
public class EditCommandTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Model model;

    @Before
    public void setUp() {
        model = new ModelManager(getTypicalBookShelf(), new UserPrefs());
    }

    @Test
    public void constructor_nullBook_throwsNullPointerException() {
        thrown.expect(NullPointerException.class);
        new EditCommand(null, null);
    }

    @Test
    public void execute_invalidActiveListType_failure() {
        model.setActiveListType(ActiveListType.SEARCH_RESULTS);
        EditDescriptor descriptor = new EditDescriptorBuilder().withPriority(Priority.DEFAULT_PRIORITY).build();
        EditCommand editCommandSmallIndex = prepareCommand(INDEX_FIRST_BOOK, descriptor);
        EditCommand editCommandLargeIndex = prepareCommand(Index.fromOneBased(100), descriptor);

        assertCommandFailure(editCommandSmallIndex, model, EditCommand.MESSAGE_WRONG_ACTIVE_LIST);

        // Wrong active list message should take precedence over invalid index
        assertCommandFailure(editCommandLargeIndex, model, EditCommand.MESSAGE_WRONG_ACTIVE_LIST);
    }

    @Test
    public void execute_allFieldsSpecifiedUnfilteredList_success() throws Exception {
        Book editedBook = new BookBuilder(model.getDisplayBookList().get(0)).withStatus(Status.UNREAD).build();
        EditDescriptor descriptor = new EditDescriptorBuilder().withStatus(Status.UNREAD).build();
        EditCommand editCommand = prepareCommand(INDEX_FIRST_BOOK, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_SUCCESS, editedBook);

        Model expectedModel = new ModelManager(new BookShelf(model.getBookShelf()), new UserPrefs());
        expectedModel.updateBook(model.getDisplayBookList().get(0), editedBook);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_someFieldsSpecifiedUnfilteredList_success() throws Exception {
        model.updateBookListFilter(x -> x.getStatus().equals(Status.UNREAD));
        Index indexLastBook = Index.fromOneBased(model.getDisplayBookList().size());
        Book lastBook = model.getDisplayBookList().get(indexLastBook.getZeroBased());

        BookBuilder bookInList = new BookBuilder(lastBook);
        Book editedBook = bookInList.withStatus(Status.READ).build();

        EditDescriptor descriptor = new EditDescriptorBuilder().withStatus(Status.READ).build();
        EditCommand editCommand = prepareCommand(indexLastBook, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_SUCCESS, editedBook);

        Model expectedModel = new ModelManager(new BookShelf(model.getBookShelf()), new UserPrefs());
        expectedModel.updateBook(lastBook, editedBook);
        expectedModel.updateBookListFilter(x -> x.getStatus().equals(Status.UNREAD));

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void executeUndo_validIndexUnfilteredList_success() throws Exception {
        UndoStack undoStack = new UndoStack();
        UndoCommand undoCommand = prepareUndoCommand(model, undoStack);
        Book editedBook = model.getDisplayBookList().get(INDEX_FIRST_BOOK.getZeroBased());
        EditDescriptor descriptor = new EditDescriptorBuilder(editedBook).build();
        EditCommand editCommand = prepareCommand(INDEX_FIRST_BOOK, descriptor);
        Model expectedModel = new ModelManager(new BookShelf(model.getBookShelf()), new UserPrefs());

        // edit -> first book edited
        editCommand.execute();
        undoStack.push(editCommand);

        // undo -> reverts book shelf back to previous state
        assertCommandSuccess(undoCommand, model, String.format(EditCommand.UNDO_SUCCESS, editedBook), expectedModel);
    }

    @Test
    public void executeUndo_invalidIndexUnfilteredList_failure() {
        UndoStack undoStack = new UndoStack();
        UndoCommand undoCommand = prepareUndoCommand(model, undoStack);
        Index outOfBoundIndex = Index.fromOneBased(model.getDisplayBookList().size() + 1);
        EditDescriptor descriptor = new EditDescriptorBuilder().withStatus(Status.UNREAD).build();
        EditCommand editCommand = prepareCommand(outOfBoundIndex, descriptor);

        // execution failed -> editCommand not pushed into undoStack
        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_BOOK_DISPLAYED_INDEX);

        // no commands in undoStack -> undoCommand fail
        assertCommandFailure(undoCommand, model, UndoCommand.MESSAGE_FAILURE);
    }
    //@@author 592363789
    @Test
    public void execute_invalidBookIndexUnfilteredList_failure() {
        Index outOfBoundIndex = Index.fromOneBased(model.getDisplayBookList().size() + 1);
        EditDescriptor descriptor = new EditDescriptorBuilder().withStatus(Status.DEFAULT_STATUS).build();
        EditCommand editCommand = prepareCommand(outOfBoundIndex, descriptor);

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_BOOK_DISPLAYED_INDEX);
    }

    /**
     * Test with index larger than size of filtered list
     * but smaller than size of book shelf.
     */
    @Test
    public void execute_invalidBookIndexFilteredList_failure() {
        showBookAtIndex(model, INDEX_FIRST_BOOK);
        Index outOfBoundIndex = INDEX_SECOND_BOOK;
        // ensures that outOfBoundIndex is still in bounds of book shelf
        assertTrue(outOfBoundIndex.getZeroBased() < model.getBookShelf().getBookList().size());

        EditDescriptor descriptor = new EditDescriptorBuilder().withStatus(Status.DEFAULT_STATUS).build();
        EditCommand editCommand = prepareCommand(outOfBoundIndex, descriptor);

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_BOOK_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        EditDescriptor descriptor = new EditDescriptorBuilder().withStatus(Status.DEFAULT_STATUS).build();

        EditCommand editCommand = prepareCommand(INDEX_FIRST_BOOK, descriptor);

        // same values -> returns true
        EditCommand commandWithSameValues = prepareCommand(INDEX_FIRST_BOOK, new EditDescriptor(descriptor));
        assertTrue(editCommand.equals(commandWithSameValues));

        // same object -> returns true
        assertTrue(editCommand.equals(editCommand));

        // null -> returns false
        assertFalse(editCommand.equals(null));

        // different types -> returns false
        assertFalse(editCommand.equals(new ClearCommand()));

        // different index -> returns false
        assertFalse(editCommand.equals(new EditCommand(INDEX_SECOND_BOOK, descriptor)));

        // different descriptor -> returns false
        assertFalse(editCommand.equals(new EditCommand(INDEX_FIRST_BOOK,
                new EditDescriptorBuilder().withRating(new Rating(5)).build())));
    }

    /**
     * Returns an {@code EditCommand} with parameters {@code index} and {@code descriptor}.
     */
    private EditCommand prepareCommand(Index index, EditDescriptor descriptor) {
        EditCommand editCommand = new EditCommand(index, descriptor);
        editCommand.setData(model, mock(NetworkManager.class), new CommandHistory(), new UndoStack());
        return editCommand;
    }
}
