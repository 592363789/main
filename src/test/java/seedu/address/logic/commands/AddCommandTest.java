package seedu.address.logic.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.prepareUndoCommand;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_BOOK;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_BOOK;

import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import seedu.address.commons.core.Messages;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.events.ui.NewResultAvailableEvent;
import seedu.address.logic.CommandHistory;
import seedu.address.logic.UndoStack;
import seedu.address.model.ActiveListType;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.book.Book;
import seedu.address.network.NetworkManager;
import seedu.address.testutil.TestUtil;
import seedu.address.ui.testutil.EventsCollectorRule;

//@@author qiu-siqi
/**
 * Contains integration tests (interaction with the Model and UndoCommand) and unit tests for
 * {@code AddCommand}.
 */
public class AddCommandTest {

    @Rule
    public final EventsCollectorRule eventsCollectorRule = new EventsCollectorRule();
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Model model;

    /**
     * By default, set up search results list as active list.
     */
    @Before
    public void setUp() {
        model = new ModelManager();
        TestUtil.prepareSearchResultListInModel(model);
    }

    @Test
    public void constructor_nullBook_throwsNullPointerException() {
        thrown.expect(NullPointerException.class);
        new AddCommand(null);
    }

    @Test
    public void execute_invalidActiveListType_failure() {
        model.setActiveListType(ActiveListType.BOOK_SHELF);
        AddCommand addCommandSmallIndex = prepareCommand(INDEX_FIRST_BOOK);
        AddCommand addCommandLargeIndex = prepareCommand(Index.fromOneBased(100));

        assertCommandFailure(addCommandSmallIndex, model, AddCommand.MESSAGE_WRONG_ACTIVE_LIST);

        // Wrong active list message should take precedence over invalid index
        assertCommandFailure(addCommandLargeIndex, model, AddCommand.MESSAGE_WRONG_ACTIVE_LIST);
    }

    @Test
    public void execute_validIndexSearchResults_success() throws Exception {
        ModelManager expectedModel = new ModelManager();
        TestUtil.prepareSearchResultListInModel(expectedModel);
        expectedModel.addBook(model.getSearchResultsList().get(0));

        assertExecutionSuccess(INDEX_FIRST_BOOK, model.getSearchResultsList().get(0), expectedModel);
    }

    @Test
    public void execute_invalidIndexSearchResults_failure() {
        AddCommand addCommand = prepareCommand(Index.fromOneBased(model.getSearchResultsList().size() + 1));

        assertCommandFailure(addCommand, model, Messages.MESSAGE_INVALID_BOOK_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexRecentBooks_success() throws Exception {
        TestUtil.prepareRecentBooksListInModel(model);

        ModelManager expectedModel = new ModelManager();
        TestUtil.prepareSearchResultListInModel(expectedModel);
        TestUtil.prepareRecentBooksListInModel(expectedModel);
        expectedModel.addBook(model.getRecentBooksList().get(0));

        assertExecutionSuccess(INDEX_FIRST_BOOK, model.getRecentBooksList().get(0), expectedModel);
    }

    @Test
    public void execute_invalidIndexRecentBooks_failure() {
        TestUtil.prepareRecentBooksListInModel(model);

        AddCommand addCommand = prepareCommand(Index.fromOneBased(model.getRecentBooksList().size() + 1));

        assertCommandFailure(addCommand, model, Messages.MESSAGE_INVALID_BOOK_DISPLAYED_INDEX);
    }

    @Test
    public void executeUndo_validIndex_success() throws Exception {
        ModelManager expectedModel = new ModelManager(model.getBookShelf(), new UserPrefs());
        TestUtil.prepareSearchResultListInModel(expectedModel);
        UndoStack undoStack = new UndoStack();
        UndoCommand undoCommand = prepareUndoCommand(model, undoStack);

        Book toAdd = model.getSearchResultsList().get(0);

        NetworkManager networkManagerMock = mock(NetworkManager.class);
        when(networkManagerMock.getBookDetails(toAdd.getGid().gid))
                .thenReturn(CompletableFuture.completedFuture(model.getSearchResultsList().get(0)));

        AddCommand addCommand = new AddCommand(INDEX_FIRST_BOOK, false);
        addCommand.setData(model, networkManagerMock, new CommandHistory(), new UndoStack());

        // add -> first book added
        addCommand.execute();
        undoStack.push(addCommand);

        // undo -> reverts bookshelf back to previous state
        assertCommandSuccess(undoCommand, model, String.format(AddCommand.UNDO_SUCCESS, toAdd), expectedModel);
    }

    @Test
    public void executeUndo_invalidIndex_failure() throws Exception {
        UndoStack undoStack = new UndoStack();
        UndoCommand undoCommand = prepareUndoCommand(model, undoStack);
        Index outOfBoundIndex = Index.fromOneBased(model.getSearchResultsList().size() + 1);
        AddCommand addCommand = prepareCommand(outOfBoundIndex);

        // execution failed -> addCommand not pushed into undoStack
        assertCommandFailure(addCommand, model, Messages.MESSAGE_INVALID_BOOK_DISPLAYED_INDEX);

        // no commands in undoStack -> undoCommand fail
        assertCommandFailure(undoCommand, model, UndoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void execute_networkError_raisesExpectedEvent() throws Exception {
        AddCommand addCommand = new AddCommand(INDEX_FIRST_BOOK, false);

        NetworkManager networkManagerMock = mock(NetworkManager.class);
        Book toAdd = model.getSearchResultsList().get(INDEX_FIRST_BOOK.getZeroBased());
        when(networkManagerMock.getBookDetails(toAdd.getGid().gid))
                .thenReturn(TestUtil.getFailedFuture());

        UndoStack undoStack = new UndoStack();
        addCommand.setData(model, networkManagerMock, new CommandHistory(), undoStack);
        addCommand.execute();

        NewResultAvailableEvent resultEvent = (NewResultAvailableEvent)
                eventsCollectorRule.eventsCollector.getMostRecent(NewResultAvailableEvent.class);
        assertEquals(AddCommand.MESSAGE_ADD_FAIL, resultEvent.message);

        // undo -> nothing to undo
        UndoCommand undoCommand = prepareUndoCommand(model, undoStack);
        assertCommandFailure(undoCommand, model, UndoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void equals() throws Exception {
        AddCommand addFirstCommand = prepareCommand(INDEX_FIRST_BOOK);
        AddCommand addSecondCommand = prepareCommand(INDEX_SECOND_BOOK);

        // same object -> returns true
        assertTrue(addFirstCommand.equals(addFirstCommand));

        // same values -> returns true
        AddCommand addFirstCommandCopy = prepareCommand(INDEX_FIRST_BOOK);
        assertTrue(addFirstCommand.equals(addFirstCommandCopy));

        // one command preprocessed when previously equal -> returns false
        addFirstCommandCopy.preprocessUndoableCommand();
        assertFalse(addFirstCommand.equals(addFirstCommandCopy));

        // different types -> returns false
        assertFalse(addFirstCommand.equals(1));

        // null -> returns false
        assertFalse(addFirstCommand.equals(null));

        // different book -> returns false
        assertFalse(addFirstCommand.equals(addSecondCommand));
    }

    /**
     * Executes an {@code AddCommand} with the given {@code index}, and checks that
     * {@code network.getBookDetails(bookId)} is being called with the correct book ID.
     */
    private void assertExecutionSuccess(Index index, Book expectedBook, Model expectedModel) {
        AddCommand addCommand = new AddCommand(index, false);

        NetworkManager networkManagerMock = mock(NetworkManager.class);
        when(networkManagerMock.getBookDetails(expectedBook.getGid().gid))
                .thenReturn(CompletableFuture.completedFuture(expectedBook));

        addCommand.setData(model, networkManagerMock, new CommandHistory(), new UndoStack());

        assertCommandSuccess(addCommand, model, AddCommand.MESSAGE_ADDING, expectedModel);
        verify(networkManagerMock).getBookDetails(expectedBook.getGid().gid);
    }

    /**
     * Returns an {@code AddCommand} with the parameter {@code index}.
     */
    private AddCommand prepareCommand(Index index) {
        AddCommand addCommand = new AddCommand(index, false);
        addCommand.setData(model, mock(NetworkManager.class), new CommandHistory(), new UndoStack());
        return addCommand;
    }

}
