package seedu.address.logic.commands;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalBooks.getTypicalBookShelf;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_BOOK;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_BOOK;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import seedu.address.commons.core.Messages;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.events.ui.ShowBookReviewsRequestEvent;
import seedu.address.logic.CommandHistory;
import seedu.address.logic.UndoStack;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.book.Book;
import seedu.address.network.NetworkManager;
import seedu.address.testutil.TestUtil;
import seedu.address.ui.testutil.EventsCollectorRule;

//@@author qiu-siqi
/**
 * Contains integration tests (interaction with the Model) for {@code ReviewsCommand}.
 */
public class ReviewsCommandTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Rule
    public final EventsCollectorRule eventsCollectorRule = new EventsCollectorRule();

    private Model model;

    @Before
    public void setUp() {
        model = new ModelManager(getTypicalBookShelf(), new UserPrefs());
    }

    @Test
    public void constructor_nullIndex_throwsNullPointerException() {
        thrown.expect(NullPointerException.class);
        new ReviewsCommand(null);
    }

    @Test
    public void execute_validIndexBookShelf_success() {
        ReviewsCommand reviewsCommand = prepareCommand(INDEX_FIRST_BOOK);
        ModelManager expectedModel = new ModelManager(model.getBookShelf(), new UserPrefs());

        assertCommandSuccess(reviewsCommand, model,
                prepareExpectedMessage(model.getDisplayBookList(), INDEX_FIRST_BOOK), expectedModel);
        assertTrue(eventsCollectorRule.eventsCollector.getMostRecent() instanceof ShowBookReviewsRequestEvent);
    }

    @Test
    public void execute_invalidIndexBookShelf_failure() {
        ReviewsCommand reviewsCommand = prepareCommand(Index.fromOneBased(model.getDisplayBookList().size() + 1));

        assertCommandFailure(reviewsCommand, model, Messages.MESSAGE_INVALID_BOOK_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexSearchResults_success() {
        TestUtil.prepareSearchResultListInModel(model);

        ReviewsCommand reviewsCommand = prepareCommand(INDEX_FIRST_BOOK);
        ModelManager expectedModel = new ModelManager(model.getBookShelf(), new UserPrefs());
        TestUtil.prepareSearchResultListInModel(expectedModel);

        assertCommandSuccess(reviewsCommand, model,
                prepareExpectedMessage(model.getSearchResultsList(), INDEX_FIRST_BOOK), expectedModel);
        assertTrue(eventsCollectorRule.eventsCollector.getMostRecent() instanceof ShowBookReviewsRequestEvent);
    }

    @Test
    public void execute_invalidIndexSearchResults_failure() {
        TestUtil.prepareSearchResultListInModel(model);

        ReviewsCommand reviewsCommand = prepareCommand(Index.fromOneBased(model.getSearchResultsList().size() + 1));

        assertCommandFailure(reviewsCommand, model, Messages.MESSAGE_INVALID_BOOK_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexRecentBooks_success() {
        TestUtil.prepareRecentBooksListInModel(model);

        ReviewsCommand reviewsCommand = prepareCommand(INDEX_FIRST_BOOK);
        ModelManager expectedModel = new ModelManager(model.getBookShelf(), new UserPrefs());
        TestUtil.prepareRecentBooksListInModel(expectedModel);

        assertCommandSuccess(reviewsCommand, model,
                prepareExpectedMessage(model.getRecentBooksList(), INDEX_FIRST_BOOK), expectedModel);
        assertTrue(eventsCollectorRule.eventsCollector.getMostRecent() instanceof ShowBookReviewsRequestEvent);
    }

    @Test
    public void execute_invalidIndexRecentBooks_failure() {
        TestUtil.prepareRecentBooksListInModel(model);

        ReviewsCommand reviewsCommand = prepareCommand(Index.fromOneBased(model.getRecentBooksList().size() + 1));

        assertCommandFailure(reviewsCommand, model, Messages.MESSAGE_INVALID_BOOK_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        ReviewsCommand reviewsFirstCommand = prepareCommand(INDEX_FIRST_BOOK);
        ReviewsCommand reviewsSecondCommand = prepareCommand(INDEX_SECOND_BOOK);

        // same object -> returns true
        assertTrue(reviewsFirstCommand.equals(reviewsFirstCommand));

        // same values -> returns true
        ReviewsCommand reviewsFirstCommandCopy = prepareCommand(INDEX_FIRST_BOOK);
        assertTrue(reviewsFirstCommand.equals(reviewsFirstCommandCopy));

        // different types -> returns false
        assertFalse(reviewsFirstCommand.equals(1));

        // null -> returns false
        assertFalse(reviewsFirstCommand.equals(null));

        // different book -> returns false
        assertFalse(reviewsFirstCommand.equals(reviewsSecondCommand));
    }

    /**
     * Returns a {@code ReviewsCommand} with parameters {@code index}.
     */
    private ReviewsCommand prepareCommand(Index index) {
        ReviewsCommand reviewsCommand = new ReviewsCommand(index);
        reviewsCommand.setData(model, mock(NetworkManager.class), new CommandHistory(), new UndoStack());
        return reviewsCommand;
    }

    /**
     * Returns the expected loading message of the book at {@code index} of {@code list}.
     */
    private String prepareExpectedMessage(List<Book> list, Index index) {
        return String.format(ReviewsCommand.MESSAGE_SUCCESS, list.get(index.getZeroBased()));
    }
}
