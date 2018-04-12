# qiu-siqi
###### \java\guitests\guihandles\BookDescriptionViewHandle.java
``` java
/**
 * Provides a handle for {@code BookDescriptionView}.
 */
public class BookDescriptionViewHandle extends NodeHandle<Node> {
    public static final String BOOK_DESCRIPTION_VIEW_ID = "#bookDescriptionView";

    private static final String DESCRIPTION_FIELD_ID = "#description";

    private final WebView descriptionView;

    public BookDescriptionViewHandle(Node bookDescriptionNode) {
        super(bookDescriptionNode);

        this.descriptionView = getChildNode(DESCRIPTION_FIELD_ID);
    }

    public String getContent() {
        final FutureTask<String> query = new FutureTask<>(() -> (String) descriptionView.getEngine()
                .executeScript("document.getElementById('description').innerHTML"));
        guiRobot.interact(query);
        try {
            return query.get();
        } catch (InterruptedException | ExecutionException e) {
            LogsCenter.getLogger(this.getClass()).warning("Failed to fetch book description.");
            return "";
        }
    }
}
```
###### \java\guitests\guihandles\BookInLibraryPanelHandle.java
``` java
/**
 * Provides a handle for the {@code BookInLibraryPanel} of the UI.
 */
public class BookInLibraryPanelHandle extends NodeHandle<Node> {
    public static final String BOOK_IN_LIBRARY_PANEL_ID = "#bookInLibraryPanel";

    public BookInLibraryPanelHandle(Node bookInLibraryPanelNode) {
        super(bookInLibraryPanelNode);
    }

    public boolean isVisible() {
        return getRootNode().isVisible();
    }

}
```
###### \java\guitests\guihandles\BookReviewsPanelHandle.java
``` java
/**
 * Provides a handle for the {@code BookReviewsPanel} of the UI.
 */
public class BookReviewsPanelHandle extends NodeHandle<Node> {
    public static final String BOOK_REVIEWS_PANE_ID = "#bookReviewsPane";

    public BookReviewsPanelHandle(Node bookReviewsPanelNode) {
        super(bookReviewsPanelNode);
    }

    public boolean isVisible() {
        return getRootNode().isVisible();
    }

    /**
     * Returns the {@code URL} of the currently loaded page.
     */
    public URL getLoadedUrl() {
        return WebViewUtil.getLoadedUrl(WebViewManager.getInstance().getWebView());
    }
}
```
###### \java\guitests\guihandles\RecentBooksPanelHandle.java
``` java
/**
 * Provides a handle for {@code RecentBooksPanel} containing the list of {@code Book}.
 */
public class RecentBooksPanelHandle extends NodeHandle<ListView<Book>> {
    public static final String RECENT_BOOKS_LIST_VIEW_ID = "#recentBooksListView";

    private static final String CARD_PANE_ID = "#cardPane";

    private Optional<Book> lastRememberedSelectedBookCard;

    public RecentBooksPanelHandle(ListView<Book> recentBooksPanelNode) {
        super(recentBooksPanelNode);
    }

    /**
     * Returns a handle to the selected {@code BookCardHandle}.
     * A maximum of 1 item can be selected at any time.
     * @throws AssertionError if no card is selected, or more than 1 card is selected.
     */
    public BookCardHandle getHandleToSelectedCard() {
        List<Book> bookList = getRootNode().getSelectionModel().getSelectedItems();

        if (bookList.size() != 1) {
            throw new AssertionError("Recent books list size expected 1.");
        }

        return getAllCardNodes().stream()
                .map(BookCardHandle::new)
                .filter(handle -> handle.equals(bookList.get(0)))
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }

    /**
     * Returns the index of the selected card.
     */
    public int getSelectedCardIndex() {
        return getRootNode().getSelectionModel().getSelectedIndex();
    }

    /**
     * Returns true if a card is currently selected.
     */
    public boolean isAnyCardSelected() {
        List<Book> selectedCardsList = getRootNode().getSelectionModel().getSelectedItems();

        if (selectedCardsList.size() > 1) {
            throw new AssertionError("Card list size expected 0 or 1.");
        }

        return !selectedCardsList.isEmpty();
    }

    /**
     * Navigates the listview to display and select {@code book}.
     */
    public void navigateToCard(Book book) {
        if (!getRootNode().getItems().contains(book)) {
            throw new IllegalArgumentException("Book does not exist.");
        }

        guiRobot.interact(() -> getRootNode().scrollTo(book));
        guiRobot.pauseForHuman();
    }

    /**
     * Navigates the listview to {@code index}.
     */
    public void navigateToCard(int index) {
        if (index < 0 || index >= getRootNode().getItems().size()) {
            throw new IllegalArgumentException("Index is out of bounds.");
        }

        guiRobot.interact(() -> getRootNode().scrollTo(index));
        guiRobot.pauseForHuman();
    }

    /**
     * Returns the book card handle of a book associated with the {@code index} in the list.
     */
    public Optional<BookCardHandle> getBookCardHandle(int index) {
        return getAllCardNodes().stream()
                .map(BookCardHandle::new)
                .filter(handle -> handle.equals(getBook(index)))
                .findFirst();
    }

    private Book getBook(int index) {
        return getRootNode().getItems().get(index);
    }

    /**
     * Returns all card nodes in the scene graph.
     * Card nodes that are visible in the listview are definitely in the scene graph, while some nodes that are not
     * visible in the listview may also be in the scene graph.
     */
    private Set<Node> getAllCardNodes() {
        return guiRobot.lookup(CARD_PANE_ID).queryAll();
    }

    /**
     * Selects the {@code Book} at {@code index} in the list.
     */
    public void select(int index) {
        getRootNode().getSelectionModel().select(index);
    }

    public boolean isVisible() {
        return getRootNode().getParent().isVisible();
    }

    /**
     * Remembers the selected {@code Book} in the list.
     */
    public void rememberSelectedBookCard() {
        List<Book> selectedItems = getRootNode().getSelectionModel().getSelectedItems();

        if (selectedItems.size() == 0) {
            lastRememberedSelectedBookCard = Optional.empty();
        } else {
            lastRememberedSelectedBookCard = Optional.of(selectedItems.get(0));
        }
    }

    /**
     * Returns true if the selected {@code Book} is different from the value remembered by the most recent
     * {@code rememberSelectedBookCard()} call.
     */
    public boolean isSelectedBookCardChanged() {
        List<Book> selectedItems = getRootNode().getSelectionModel().getSelectedItems();

        if (selectedItems.size() == 0) {
            return lastRememberedSelectedBookCard.isPresent();
        } else {
            return !lastRememberedSelectedBookCard.isPresent()
                    || !lastRememberedSelectedBookCard.get().equals(selectedItems.get(0));
        }
    }

    /**
     * Returns the size of the list.
     */
    public int getListSize() {
        return getRootNode().getItems().size();
    }

}
```
###### \java\seedu\address\commons\util\StringUtilTest.java
``` java
    @Test
    public void isValidUrl_nullGiven_throwsNullPointerException() {
        thrown.expect(NullPointerException.class);
        StringUtil.isValidUrl(null);
    }

    @Test
    public void isValidUrl_emptyString() {
        assertFalse(StringUtil.isValidUrl(""));
    }

    @Test
    public void isValidUrl_validUrl_success() {
        assertTrue(StringUtil.isValidUrl(
                "https://catalogue.nlb.gov.sg/cgi-bin/spydus.exe/FULL/EXPNOS/BIBENQ/6689797/241229563,1"));
    }

    @Test
    public void isValidUrl_invalidUrl_failure() {
        assertFalse(StringUtil.isValidUrl(
                "https:///catalogue.nlb.gov.sg/cgi-bin/spydus.exe/FULL/EXPNOS/BIBENQ/6689797/241229563,1"));
    }
}
```
###### \java\seedu\address\logic\commands\AddCommandTest.java
``` java
/**
 * Contains integration tests (interaction with the Model and UndoCommand) and unit tests for
 * {@code AddCommand}.
 */
public class AddCommandTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Model model;

    /**
     * By default, set up search results list as active list.
     */
    @Before
    public void setUp() {
        model = new ModelManager(new BookShelf(), new UserPrefs());
        prepareSearchResultListInModel(model);
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
        prepareSearchResultListInModel(expectedModel);
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
        prepareRecentBooksListInModel(model);

        ModelManager expectedModel = new ModelManager();
        prepareSearchResultListInModel(expectedModel);
        prepareRecentBooksListInModel(expectedModel);
        expectedModel.addBook(model.getRecentBooksList().get(0));

        assertExecutionSuccess(INDEX_FIRST_BOOK, model.getRecentBooksList().get(0), expectedModel);
    }

    @Test
    public void execute_invalidIndexRecentBooks_failure() {
        prepareRecentBooksListInModel(model);

        AddCommand addCommand = prepareCommand(Index.fromOneBased(model.getRecentBooksList().size() + 1));

        assertCommandFailure(addCommand, model, Messages.MESSAGE_INVALID_BOOK_DISPLAYED_INDEX);
    }

    @Test
    public void executeUndo_validIndex_success() throws Exception {
        ModelManager expectedModel = new ModelManager(model.getBookShelf(), new UserPrefs());
        prepareSearchResultListInModel(expectedModel);
        UndoStack undoStack = new UndoStack();
        UndoCommand undoCommand = prepareUndoCommand(model, undoStack);

        NetworkManager networkManagerMock = mock(NetworkManager.class);
        when(networkManagerMock.getBookDetails(model.getSearchResultsList().get(0).getGid().gid))
                .thenReturn(CompletableFuture.completedFuture(model.getSearchResultsList().get(0)));

        AddCommand addCommand = new AddCommand(INDEX_FIRST_BOOK, false);
        addCommand.setData(model, networkManagerMock, new CommandHistory(), new UndoStack());

        // add -> first book added
        addCommand.execute();
        undoStack.push(addCommand);

        // undo -> reverts bookshelf back to previous state
        assertCommandSuccess(undoCommand, model, UndoCommand.MESSAGE_SUCCESS, expectedModel);
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
     * Set up {@code model} with a non-empty search result list and
     * switch active list to search results list.
     */
    private void prepareSearchResultListInModel(Model model) {
        model.setActiveListType(ActiveListType.SEARCH_RESULTS);
        BookShelf bookShelf = getTypicalBookShelf();
        model.updateSearchResults(bookShelf);
    }

    /**
     * Set up {@code model} with a non-empty recently selected books list and
     * switch active list to recent books list.
     */
    private void prepareRecentBooksListInModel(Model model) {
        model.setActiveListType(ActiveListType.RECENT_BOOKS);
        BookShelf bookShelf = getTypicalBookShelf();
        bookShelf.getBookList().forEach(model::addRecentBook);
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
```
###### \java\seedu\address\logic\commands\LibraryCommandTest.java
``` java
/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code LibraryCommand}.
 */
public class LibraryCommandTest {

    @Rule
    public final EventsCollectorRule eventsCollectorRule = new EventsCollectorRule();
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Model model;

    /**
     * Default active list is book shelf.
     */
    @Before
    public void setUp() {
        model = new ModelManager(TypicalBooks.getTypicalBookShelf(), new UserPrefs());
        model.updateSearchResults(TypicalBooks.getTypicalBookShelf());
        model.addRecentBook(TypicalBooks.ARTEMIS);
    }

    @Test
    public void constructor_nullIndex_throwsNullPointerException() {
        thrown.expect(NullPointerException.class);
        new LibraryCommand(null);
    }

    @Test
    public void execute_validIndexBookShelf_success() {
        assertExecutionSuccess(INDEX_FIRST_BOOK, model.getDisplayBookList().get(0), model);
    }

    @Test
    public void execute_invalidIndexBookShelf_failure() {
        LibraryCommand libraryCommand = prepareCommand(Index.fromOneBased(model.getDisplayBookList().size() + 1));

        assertCommandFailure(libraryCommand, model, Messages.MESSAGE_INVALID_BOOK_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexSearchResults_success() {
        model.setActiveListType(ActiveListType.SEARCH_RESULTS);
        assertExecutionSuccess(INDEX_FIRST_BOOK, model.getSearchResultsList().get(0), model);
    }

    @Test
    public void execute_invalidIndexSearchResults_failure() {
        model.setActiveListType(ActiveListType.SEARCH_RESULTS);
        LibraryCommand libraryCommand = prepareCommand(Index.fromOneBased(model.getSearchResultsList().size() + 1));

        assertCommandFailure(libraryCommand, model, Messages.MESSAGE_INVALID_BOOK_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexRecentBooks_success() {
        model.setActiveListType(ActiveListType.RECENT_BOOKS);
        assertExecutionSuccess(INDEX_FIRST_BOOK, model.getRecentBooksList().get(0), model);
    }

    @Test
    public void execute_invalidIndexRecentBooks_failure() {
        model.setActiveListType(ActiveListType.RECENT_BOOKS);
        LibraryCommand libraryCommand = prepareCommand(Index.fromOneBased(model.getRecentBooksList().size() + 1));

        assertCommandFailure(libraryCommand, model, Messages.MESSAGE_INVALID_BOOK_DISPLAYED_INDEX);
    }

    @Test
    public void execute_networkError_raisesExpectedEvent() throws CommandException {
        LibraryCommand command = new LibraryCommand(INDEX_FIRST_BOOK);

        NetworkManager networkManagerMock = mock(NetworkManager.class);
        when(networkManagerMock.searchLibraryForBook(
                model.getDisplayBookList().get(INDEX_FIRST_BOOK.getZeroBased())))
                .thenReturn(TestUtil.getFailedFuture());

        command.setData(model, networkManagerMock, new CommandHistory(), new UndoStack());
        command.execute();

        NewResultAvailableEvent resultEvent = (NewResultAvailableEvent)
                eventsCollectorRule.eventsCollector.getMostRecent(NewResultAvailableEvent.class);
        assertEquals(LibraryCommand.MESSAGE_FAIL, resultEvent.message);
    }

    @Test
    public void equals() {
        LibraryCommand libraryFirstCommand = prepareCommand(INDEX_FIRST_BOOK);
        LibraryCommand librarySecondCommand = prepareCommand(INDEX_SECOND_BOOK);

        // same object -> returns true
        assertTrue(libraryFirstCommand.equals(libraryFirstCommand));

        // same values -> returns true
        LibraryCommand libraryFirstCommandCopy = prepareCommand(INDEX_FIRST_BOOK);
        assertTrue(libraryFirstCommand.equals(libraryFirstCommandCopy));

        // different types -> returns false
        assertFalse(libraryFirstCommand.equals(1));

        // null -> returns false
        assertFalse(libraryFirstCommand.equals(null));

        // different book -> returns false
        assertFalse(libraryFirstCommand.equals(librarySecondCommand));
    }

    /**
     * Executes a {@code LibraryCommand} with the given {@code index}, and checks that
     * {@code network.searchLibraryForBook(book)} is being called with the correct book.
     */
    private void assertExecutionSuccess(Index index, Book expectedBook, Model expectedModel) {
        LibraryCommand libraryCommand = new LibraryCommand(index);

        NetworkManager networkManagerMock = mock(NetworkManager.class);
        when(networkManagerMock.searchLibraryForBook(expectedBook))
                .thenReturn(CompletableFuture.completedFuture(expectedBook.toString()));

        libraryCommand.setData(model, networkManagerMock, new CommandHistory(), new UndoStack());

        assertCommandSuccess(libraryCommand, model, LibraryCommand.MESSAGE_SEARCHING, expectedModel);
        verify(networkManagerMock).searchLibraryForBook(expectedBook);
    }

    /**
     * Returns a {@code LibraryCommand} with the parameter {@code index}.
     */
    private LibraryCommand prepareCommand(Index index) {
        LibraryCommand command = new LibraryCommand(index);
        command.setData(model, mock(NetworkManager.class), new CommandHistory(), new UndoStack());
        return command;
    }
}
```
###### \java\seedu\address\logic\commands\RecentCommandTest.java
``` java
/**
 * Contains integration tests (interaction with the Model) and unit tests for RecentCommand.
 */
public class RecentCommandTest {

    private Model model;
    private Model expectedModel;
    private RecentCommand recentCommand;

    @Before
    public void setUp() {
        model = new ModelManager(getTypicalBookShelf(), new UserPrefs());
        expectedModel = new ModelManager(model.getBookShelf(), new UserPrefs());

        recentCommand = new RecentCommand();
        recentCommand.setData(model, mock(NetworkManager.class), new CommandHistory(), new UndoStack());
    }

    @Test
    public void execute_showsRecent() {
        assertCommandSuccess(recentCommand, model, RecentCommand.MESSAGE_SUCCESS, expectedModel);
    }
}
```
###### \java\seedu\address\logic\commands\ReviewsCommandTest.java
``` java
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
        prepareSearchResultListInModel(model);

        ReviewsCommand reviewsCommand = prepareCommand(INDEX_FIRST_BOOK);
        ModelManager expectedModel = new ModelManager(model.getBookShelf(), new UserPrefs());
        prepareSearchResultListInModel(expectedModel);

        assertCommandSuccess(reviewsCommand, model,
                prepareExpectedMessage(model.getSearchResultsList(), INDEX_FIRST_BOOK), expectedModel);
        assertTrue(eventsCollectorRule.eventsCollector.getMostRecent() instanceof ShowBookReviewsRequestEvent);
    }

    @Test
    public void execute_invalidIndexSearchResults_failure() {
        prepareSearchResultListInModel(model);

        ReviewsCommand reviewsCommand = prepareCommand(Index.fromOneBased(model.getSearchResultsList().size() + 1));

        assertCommandFailure(reviewsCommand, model, Messages.MESSAGE_INVALID_BOOK_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexRecentBooks_success() {
        prepareRecentBooksListInModel(model);

        ReviewsCommand reviewsCommand = prepareCommand(INDEX_FIRST_BOOK);
        ModelManager expectedModel = new ModelManager(model.getBookShelf(), new UserPrefs());
        prepareRecentBooksListInModel(expectedModel);

        assertCommandSuccess(reviewsCommand, model,
                prepareExpectedMessage(model.getRecentBooksList(), INDEX_FIRST_BOOK), expectedModel);
        assertTrue(eventsCollectorRule.eventsCollector.getMostRecent() instanceof ShowBookReviewsRequestEvent);
    }

    @Test
    public void execute_invalidIndexRecentBooks_failure() {
        prepareRecentBooksListInModel(model);

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
     * Set up {@code model} with a non-empty search result list and
     * switch active list to search results list.
     */
    private void prepareSearchResultListInModel(Model model) {
        model.setActiveListType(ActiveListType.SEARCH_RESULTS);
        BookShelf bookShelf = getTypicalBookShelf();
        model.updateSearchResults(bookShelf);
    }

    /**
     * Set up {@code model} with a non-empty recently selected books list and
     * switch active list to recent books list.
     */
    private void prepareRecentBooksListInModel(Model model) {
        model.setActiveListType(ActiveListType.RECENT_BOOKS);
        BookShelf bookShelf = getTypicalBookShelf();
        bookShelf.getBookList().forEach(model::addRecentBook);
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
```
###### \java\seedu\address\logic\parser\AddCommandParserTest.java
``` java
public class AddCommandParserTest {
    private AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_validArgs_returnsAddCommand() {
        assertParseSuccess(parser, "1", new AddCommand(INDEX_FIRST_BOOK));
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        // No args
        assertParseFailure(parser, "", String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));

        // Invalid arg
        assertParseFailure(parser, "a", String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));

        // Multiple args
        assertParseFailure(parser, "1 2", String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
    }
}
```
###### \java\seedu\address\logic\parser\LibraryCommandParserTest.java
``` java
public class LibraryCommandParserTest {
    private LibraryCommandParser parser = new LibraryCommandParser();

    @Test
    public void parse_validArgs_returnsLibraryCommand() {
        assertParseSuccess(parser, "1", new LibraryCommand(INDEX_FIRST_BOOK));
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        // No args
        assertParseFailure(parser, "", String.format(MESSAGE_INVALID_COMMAND_FORMAT, LibraryCommand.MESSAGE_USAGE));

        // Invalid arg
        assertParseFailure(parser, "a", String.format(MESSAGE_INVALID_COMMAND_FORMAT, LibraryCommand.MESSAGE_USAGE));

        // Multiple args
        assertParseFailure(parser, "1 2", String.format(MESSAGE_INVALID_COMMAND_FORMAT, LibraryCommand.MESSAGE_USAGE));
    }
}
```
###### \java\seedu\address\logic\parser\ReviewsCommandParserTest.java
``` java
public class ReviewsCommandParserTest {
    private ReviewsCommandParser parser = new ReviewsCommandParser();

    @Test
    public void parse_validArgs_returnsReviewsCommand() {
        assertParseSuccess(parser, "1", new ReviewsCommand(INDEX_FIRST_BOOK));
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        // No args
        assertParseFailure(parser, "", String.format(MESSAGE_INVALID_COMMAND_FORMAT, ReviewsCommand.MESSAGE_USAGE));

        // Invalid arg
        assertParseFailure(parser, "a", String.format(MESSAGE_INVALID_COMMAND_FORMAT, ReviewsCommand.MESSAGE_USAGE));

        // Multiple args
        assertParseFailure(parser, "1 2", String.format(MESSAGE_INVALID_COMMAND_FORMAT, ReviewsCommand.MESSAGE_USAGE));
    }
}
```
###### \java\seedu\address\model\book\UniqueBookCircularListTest.java
``` java
public class UniqueBookCircularListTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void addToFront_null_failure() throws Exception {
        UniqueBookCircularList uniqueBookCircularList = new UniqueBookCircularList(5);
        thrown.expect(NullPointerException.class);
        uniqueBookCircularList.addToFront(null);
    }

    @Test
    public void addToFront_validBook_success() throws Exception {
        UniqueBookCircularList uniqueBookCircularList = new UniqueBookCircularList();
        uniqueBookCircularList.addToFront(TypicalBooks.ARTEMIS);
        assertEquals(true, uniqueBookCircularList.asObservableList().contains(TypicalBooks.ARTEMIS));
    }

    @Test
    public void addToFront_repeatedBook_success() throws Exception {
        UniqueBookCircularList uniqueBookCircularList = new UniqueBookCircularList(5);
        uniqueBookCircularList.addToFront(TypicalBooks.ARTEMIS);
        uniqueBookCircularList.addToFront(TypicalBooks.ARTEMIS);

        ObservableList<Book> list = uniqueBookCircularList.asObservableList();
        assertEquals(true, list.contains(TypicalBooks.ARTEMIS));
        assertEquals(1, list.size());
    }

    @Test
    public void addToFront_latestBookAtFront_success() throws Exception {
        UniqueBookCircularList uniqueBookCircularList = new UniqueBookCircularList(5);
        uniqueBookCircularList.addToFront(TypicalBooks.ARTEMIS);
        uniqueBookCircularList.addToFront(TypicalBooks.BABYLON_ASHES);

        ObservableList<Book> list = uniqueBookCircularList.asObservableList();
        assertEquals(TypicalBooks.BABYLON_ASHES, list.get(0));
    }

    @Test
    public void addToFront_repeatedBookBroughtToFront_success() throws Exception {
        UniqueBookCircularList uniqueBookCircularList = new UniqueBookCircularList(5);
        uniqueBookCircularList.addToFront(TypicalBooks.ARTEMIS);
        uniqueBookCircularList.addToFront(TypicalBooks.BABYLON_ASHES);
        uniqueBookCircularList.addToFront(TypicalBooks.ARTEMIS);

        ObservableList<Book> list = uniqueBookCircularList.asObservableList();
        assertEquals(2, list.size());
        assertEquals(TypicalBooks.ARTEMIS, list.get(0));
        assertEquals(TypicalBooks.BABYLON_ASHES, list.get(1));
    }

    @Test
    public void addToFront_tooManyBooks_success() throws Exception {
        UniqueBookCircularList uniqueBookCircularList = new UniqueBookCircularList(2);
        uniqueBookCircularList.addToFront(TypicalBooks.ARTEMIS);
        uniqueBookCircularList.addToFront(TypicalBooks.BABYLON_ASHES);
        uniqueBookCircularList.addToFront(TypicalBooks.COLLAPSING_EMPIRE);

        ObservableList<Book> list = uniqueBookCircularList.asObservableList();
        assertEquals(2, list.size()); // max size 2
        assertEquals(false, list.contains(TypicalBooks.ARTEMIS)); // replaced
        assertEquals(true, list.contains(TypicalBooks.BABYLON_ASHES));
        assertEquals(true, list.contains(TypicalBooks.COLLAPSING_EMPIRE));
    }

    @Test
    public void asObservableList_modifyList_throwsUnsupportedOperationException() {
        UniqueBookCircularList uniqueBookCircularList = new UniqueBookCircularList(5);
        thrown.expect(UnsupportedOperationException.class);
        uniqueBookCircularList.asObservableList().remove(0);
    }

    @Test
    public void equals_sameObject_returnsTrue() throws Exception {
        UniqueBookCircularList uniqueBookCircularList = new UniqueBookCircularList(5);
        uniqueBookCircularList.addToFront(TypicalBooks.ARTEMIS);
        uniqueBookCircularList.addToFront(TypicalBooks.BABYLON_ASHES);
        assertEquals(true, uniqueBookCircularList.equals(uniqueBookCircularList));
    }

    @Test
    public void equals_sameContentSameOrder_returnsTrue() throws Exception {
        UniqueBookCircularList uniqueBookCircularList = new UniqueBookCircularList(5);
        uniqueBookCircularList.addToFront(TypicalBooks.ARTEMIS);
        uniqueBookCircularList.addToFront(TypicalBooks.BABYLON_ASHES);
        UniqueBookCircularList uniqueBookCircularList2 = new UniqueBookCircularList(5);
        uniqueBookCircularList2.addToFront(TypicalBooks.ARTEMIS);
        uniqueBookCircularList2.addToFront(TypicalBooks.BABYLON_ASHES);
        assertEquals(true, uniqueBookCircularList.equals(uniqueBookCircularList2));
    }

    @Test
    public void equals_sameContentDifferentOrder_returnsFalse() throws Exception {
        UniqueBookCircularList uniqueBookCircularList = new UniqueBookCircularList(5);
        uniqueBookCircularList.addToFront(TypicalBooks.ARTEMIS);
        uniqueBookCircularList.addToFront(TypicalBooks.BABYLON_ASHES);
        UniqueBookCircularList uniqueBookCircularList2 = new UniqueBookCircularList(5);
        uniqueBookCircularList2.addToFront(TypicalBooks.BABYLON_ASHES);
        uniqueBookCircularList2.addToFront(TypicalBooks.ARTEMIS);
        assertEquals(false, uniqueBookCircularList.equals(uniqueBookCircularList2));
    }

}
```
###### \java\seedu\address\network\library\NlbCatalogueApiTest.java
``` java
public class NlbCatalogueApiTest {

    private static final File VALID_RESPONSE_BRIEF_DISPLAY =
            new File("src/test/data/NlbResultTest/ValidResponseBriefDisplay.html");
    private static final String VALID_RESPONSE_BRIEF_DISPLAY_URL =
            "https://catalogue.nlb.gov.sg/cgi-bin/spydus.exe/FULL/EXPNOS/BIBENQ/6585278/226559740,1";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private NlbCatalogueApi nlbCatalogueApi;
    private HttpClient mockClient;

    @Before
    public void setUp() {
        mockClient = mock(HttpClient.class);
        nlbCatalogueApi = new NlbCatalogueApi(mockClient);
    }

    @Test
    public void makeSearchUrl_validBook_success() {
        assertEquals("https://catalogue.nlb.gov.sg/cgi-bin/spydus.exe/ENQ/EXPNOS/BIBENQ?ENTRY=Artemis+Andy+Weir"
                + "&ENTRY_NAME=BS&ENTRY_TYPE=K&GQ=Artemis+Andy+Weir&SORTS=SQL_REL_TITLE",
                NlbCatalogueApi.makeSearchUrl(TypicalBooks.ARTEMIS).replaceAll(" ", "+"));
    }

    @Test
    public void searchForBooks_validParam_success() throws IOException {
        when(mockClient.makeGetRequest(NlbCatalogueApi.makeSearchUrl(TypicalBooks.ARTEMIS)))
                .thenReturn(makeFutureResponse(200, FileUtil.readFromFile(VALID_RESPONSE_BRIEF_DISPLAY)));

        String result = nlbCatalogueApi.searchForBook(TypicalBooks.ARTEMIS).join();

        verify(mockClient).makeGetRequest(NlbCatalogueApi.makeSearchUrl(TypicalBooks.ARTEMIS));
        assertEquals(VALID_RESPONSE_BRIEF_DISPLAY_URL, result);
    }

    @Test
    public void searchForBooks_badResponseType_throwsCompletionException() {
        when(mockClient.makeGetRequest(NlbCatalogueApi.makeSearchUrl(TypicalBooks.ARTEMIS)))
                .thenReturn(makeFutureResponse(503, ""));

        thrown.expect(CompletionException.class);
        nlbCatalogueApi.searchForBook(TypicalBooks.ARTEMIS).join();
    }

    @Test
    public void searchForBooks_badReturnType_throwsCompletionException() throws IOException {
        when(mockClient.makeGetRequest(NlbCatalogueApi.makeSearchUrl(TypicalBooks.ARTEMIS)))
                .thenReturn(makeFutureResponse(200, "application/json",
                        FileUtil.readFromFile(JsonDeserializerTest.VALID_SEARCH_RESPONSE_FILE)));

        thrown.expect(CompletionException.class);
        nlbCatalogueApi.searchForBook(TypicalBooks.ARTEMIS).join();
    }

    /**
     * Returns a {@link CompletableFuture} that resolves to a {@link HttpResponse} of content type HTML.
     */
    private static CompletableFuture<HttpResponse> makeFutureResponse(int code, String response) {
        return makeFutureResponse(code, "text/html;", response);
    }

    /**
     * Returns a {@link CompletableFuture} that resolves to a {@link HttpResponse}.
     */
    private static CompletableFuture<HttpResponse> makeFutureResponse(int code, String contentType, String response) {
        return CompletableFuture.completedFuture(new HttpResponse(code, contentType, response));
    }
}
```
###### \java\seedu\address\network\library\NlbResultHelperTest.java
``` java
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
        Book book = new BookBuilder()
                .withTitle("xxxxxxxxxxxxxxxxxxxxxx").withAuthors("yyyyyyyyy").build();
        assertEquals(VALID_RESPONSE_NO_RESULTS_URL, NlbResultHelper.getUrl(content, book));

    }
}
```
###### \java\seedu\address\network\NetworkManagerTest.java
``` java
    @Test
    public void nlbCatalogueApiSearchForBooks_success() throws Exception {
        when(mockNlbCatalogueApi.searchForBook(BOOK_SUCESS))
                .thenReturn(CompletableFuture.completedFuture(BOOK_SUCCESS_RESULT));

        String result = networkManager.searchLibraryForBook(BOOK_SUCESS).get();

        verify(mockNlbCatalogueApi).searchForBook(BOOK_SUCESS);
        assertEquals(BOOK_SUCCESS_RESULT, result);
    }

    @Test
    public void nlbCatalogueApiSearchForBooks_failure() throws Exception {
        when(mockNlbCatalogueApi.searchForBook(BOOK_FAILURE))
                .thenReturn(TestUtil.getFailedFuture());

        CompletableFuture<String> result = networkManager.searchLibraryForBook(BOOK_FAILURE);
        verify(mockNlbCatalogueApi).searchForBook(BOOK_FAILURE);

        thrown.expect(ExecutionException.class);
        result.get();
    }
}
```
###### \java\seedu\address\storage\XmlRecentBooksStorageTest.java
``` java
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
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER + prefsFileInTestDataFolder
                : null;
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
        String filePath = testFolder.getRoot().getPath() + "TempRecentBooksData.xml";
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
    public void saveRecentBooksList_nullFilePath_throwsNullPointerException() throws IOException {
        thrown.expect(NullPointerException.class);
        saveRecentBooksList(new BookShelf(), null);
    }

}
```
###### \java\systemtests\AddCommandSystemTest.java
``` java
public class AddCommandSystemTest extends BibliotekSystemTest {

    @Test
    public void add() throws Exception {
        executeBackgroundCommand(SearchCommand.COMMAND_WORD + " hello", SearchCommand.MESSAGE_SEARCHING);

        Model model = getModel();
        ObservableList<Book> searchResultsList = model.getSearchResultsList();

        /* --------------------- Perform add operations on the search results list -------------------------- */

        /* Case: add a book to a non-empty book shelf, command with leading spaces and trailing spaces -> added */
        Book firstBook = searchResultsList.get(0);
        String command = "   " + AddCommand.COMMAND_WORD + "  1";
        assertCommandSuccess(command, firstBook);

        /* Case: undo adding firstBook to the list -> firstBook deleted */
        command = UndoCommand.COMMAND_WORD;
        String expectedResultMessage = UndoCommand.MESSAGE_SUCCESS;
        assertCommandSuccess(command, model, expectedResultMessage);

        /* Case: add to empty book shelf -> added */
        deleteAllBooks();

        executeBackgroundCommand(SearchCommand.COMMAND_WORD + " a/j r r tolkien", SearchCommand.MESSAGE_SEARCHING);

        model = getModel();
        searchResultsList = model.getSearchResultsList();

        command = AddCommand.COMMAND_WORD + " 1";
        firstBook = searchResultsList.get(0);

        assertCommandSuccess(command, firstBook);

        /* --------------------- Perform add operation while a book card is selected ------------------------ */

        /* Case: selects first card in the book list, add a book -> added, card selection remains unchanged */
        selectSearchResult(Index.fromOneBased(1));
        command = AddCommand.COMMAND_WORD + " 2";
        assertCommandSuccess(command, searchResultsList.get(1));

        /* --------------------- Perform add operations on the recent books list -------------------------- */

        /* Case: invalid index -> rejected */
        model = getModel();
        executeCommand("recent");
        assertCommandFailure(AddCommand.COMMAND_WORD + " " + (model.getRecentBooksList().size() + 1),
                MESSAGE_INVALID_BOOK_DISPLAYED_INDEX);

        /* Case: add a duplicate book -> rejected */
        executeCommand("list");
        selectBook(INDEX_FIRST_BOOK);
        executeCommand("recent");
        model = getModel();

        command = AddCommand.COMMAND_WORD + " 1";
        executeBackgroundCommand(command, AddCommand.MESSAGE_ADDING);
        assertApplicationDisplaysExpected("", AddCommand.MESSAGE_DUPLICATE_BOOK, model);

        /* Case: add a valid book -> added */
        executeBackgroundCommand(SearchCommand.COMMAND_WORD + " a/iain banks", SearchCommand.MESSAGE_SEARCHING);
        selectSearchResult(INDEX_FIRST_BOOK);
        executeCommand("recent");
        model = getModel();

        command = AddCommand.COMMAND_WORD + " 1";
        firstBook = model.getRecentBooksList().get(0);

        assertCommandSuccess(command, firstBook);

        /* ------------------------------- Perform invalid add operations ----------------------------------- */

        /* Case: add a duplicate book -> rejected */
        model = getModel();
        executeBackgroundCommand(command, AddCommand.MESSAGE_ADDING);
        assertApplicationDisplaysExpected("", AddCommand.MESSAGE_DUPLICATE_BOOK, model);

        /* Case: invalid index (0) -> rejected */
        assertCommandFailure(AddCommand.COMMAND_WORD + " " + 0,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));

        /* Case: invalid index (-1) -> rejected */
        assertCommandFailure(AddCommand.COMMAND_WORD + " " + -1,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));

        /* Case: invalid index (size + 1) -> rejected */
        int invalidIndex = getModel().getRecentBooksList().size() + 1;
        assertCommandFailure(AddCommand.COMMAND_WORD + " " + invalidIndex, MESSAGE_INVALID_BOOK_DISPLAYED_INDEX);

        /* Case: invalid arguments (alphabets) -> rejected */
        assertCommandFailure(AddCommand.COMMAND_WORD + " abc",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));

        /* Case: invalid arguments (extra argument) -> rejected */
        assertCommandFailure(AddCommand.COMMAND_WORD + " 1 2",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));

        /* Case: invalid keyword -> rejected */
        assertCommandFailure("adds 1", MESSAGE_UNKNOWN_COMMAND);

        /* Case: mixed case command word -> rejected */
        assertCommandFailure("Add 1", MESSAGE_UNKNOWN_COMMAND);

        /* Case: invalid active list type */
        executeCommand("list");
        assertCommandFailure(AddCommand.COMMAND_WORD + " " + INDEX_FIRST_BOOK.getOneBased(),
                AddCommand.MESSAGE_WRONG_ACTIVE_LIST);

        /* Case: add from empty search result list -> rejected */
        executeBackgroundCommand(SearchCommand.COMMAND_WORD + " !@#$%^&*()(*%$#@!#$%^&&*",
                SearchCommand.MESSAGE_SEARCHING);
        model.updateSearchResults(new BookShelf());
        assertCommandFailure(AddCommand.COMMAND_WORD + " " + INDEX_FIRST_BOOK.getOneBased(),
                MESSAGE_INVALID_BOOK_DISPLAYED_INDEX);
    }

    /**
     * Executes {@code command} and verifies that, after the web API has returned a result,<br>
     * 1. Command box displays an empty string.<br>
     * 2. Command box has the default style class.<br>
     * 3. Result display box displays the search successful message.<br>
     * 4. {@code Model}, {@code Storage} and {@code BookListPanel} equal to the corresponding components in
     * the current model added with {@code toAdd}.<br>
     * 5. Selected search results and recent books card remain unchanged.<br>
     * 6. Status bar's sync status changes.<br>
     * Verifications 1, 3 and 4 are performed by
     * {@code BibliotekSystemTest#assertApplicationDisplaysExpected(String, String, Model)}.<br>
     * @see BibliotekSystemTest#assertApplicationDisplaysExpected(String, String, Model)
     * @see BibliotekSystemTest#assertSelectedBookListCardChanged(Index)
     */
    private void assertCommandSuccess(String command, Book toAdd) throws Exception {
        Model expectedModel = getModel();

        executeCommand(command);
        assertCommandBoxShowsDefaultStyle();

        new GuiRobot().waitForEvent(() -> !getResultDisplay().getText().equals(AddCommand.MESSAGE_ADDING),
                GuiRobot.NETWORK_ACTION_TIMEOUT_MILLISECONDS);
        new GuiRobot().waitForEvent(() -> getCommandBox().isEnabled(), GuiRobot.NETWORK_ACTION_TIMEOUT_MILLISECONDS);

        String expectedResultMessage = String.format(AddCommand.MESSAGE_SUCCESS, toAdd);
        assertBookInBookShelf(toAdd);
        expectedModel.addBook(getModel().getBookShelf().getBookByIsbn(toAdd.getIsbn()).get());

        assertApplicationDisplaysExpected("", expectedResultMessage, expectedModel);
        assertSelectedSearchResultsCardUnchanged();
        assertSelectedRecentBooksCardUnchanged();
        assertCommandBoxShowsDefaultStyle();
        assertCommandBoxEnabled();
        assertStatusBarUnchangedExceptSyncStatus();
    }

    /**
     * Performs the same verification as {@code assertCommandSuccess(String, Book)} except asserts that
     * the,<br>
     * 1. Result display box displays {@code expectedResultMessage}.<br>
     * 2. {@code Model}, {@code Storage}, {@code BookListPanel}, and {@code SearchResultsPanel} equal to the
     * corresponding components in {@code expectedModel}.<br>
     * @see AddCommandSystemTest#assertCommandSuccess(String, Book)
     */
    private void assertCommandSuccess(String command, Model expectedModel, String expectedResultMessage) {
        executeCommand(command);
        assertApplicationDisplaysExpected("", expectedResultMessage, expectedModel);
        assertSelectedBookListCardUnchanged();
        assertSelectedSearchResultsCardUnchanged();
        assertSelectedRecentBooksCardUnchanged();
        assertCommandBoxShowsDefaultStyle();
        assertStatusBarUnchangedExceptSyncStatus();
    }

    /**
     * Checks that {@code book} is contained in the latest book shelf.
     */
    private void assertBookInBookShelf(Book book) {
        assertTrue(getModel().getBookShelf().getBookList().contains(book));
    }
}
```
###### \java\systemtests\LibraryCommandSystemTest.java
``` java
public class LibraryCommandSystemTest extends BibliotekSystemTest {

    @Test
    public void library() {

        /* ---------- Test on book shelf ------------- */

        ObservableList<Book> bookList = getModel().getDisplayBookList();

        assertCommandSuccess(LibraryCommand.COMMAND_WORD + " 1", bookList.get(0));
        assertCommandSuccess(LibraryCommand.COMMAND_WORD + " " + bookList.size(),
                bookList.get(bookList.size() - 1));

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
}
```
###### \java\systemtests\ReviewsCommandSystemTest.java
``` java
public class ReviewsCommandSystemTest extends BibliotekSystemTest {

    @Test
    public void reviews() {
        /* ------------ Perform reviews operations on the shown book list ------------ */
        String command = "   " + ReviewsCommand.COMMAND_WORD + " " + INDEX_FIRST_BOOK.getOneBased() + "   ";
        assertCommandSuccess(command, getModel().getDisplayBookList().get(INDEX_FIRST_BOOK.getZeroBased()));

        /* ------------ Perform reviews operations on the filtered book list ------------ */
        executeCommand(ListCommand.COMMAND_WORD + " s/unread");
        Index bookCount = Index.fromOneBased(getModel().getDisplayBookList().size());
        command = ReviewsCommand.COMMAND_WORD + " " + bookCount.getOneBased();
        assertCommandSuccess(command, getModel().getDisplayBookList().get(bookCount.getZeroBased()));

        /* ------------ Perform invalid reviews operations ------------ */
        /* Case: invalid index (0) -> rejected */
        assertCommandFailure(ReviewsCommand.COMMAND_WORD + " " + 0,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, ReviewsCommand.MESSAGE_USAGE));

        /* Case: invalid index (size + 1) -> rejected */
        int invalidIndex = getModel().getDisplayBookList().size() + 1;
        assertCommandFailure(ReviewsCommand.COMMAND_WORD + " " + invalidIndex,
                MESSAGE_INVALID_BOOK_DISPLAYED_INDEX);

        /* Case: mixed case command word -> rejected */
        assertCommandFailure("ReViEwS 1", MESSAGE_UNKNOWN_COMMAND);

        /* ------------ Perform reviews operations on the shown search results list ------------ */
        executeBackgroundCommand(SearchCommand.COMMAND_WORD + " hello", SearchCommand.MESSAGE_SEARCHING);
        assertCommandSuccess(ReviewsCommand.COMMAND_WORD + " 1", getModel().getSearchResultsList().get(0));
        assertCommandFailure(ReviewsCommand.COMMAND_WORD + " 39", MESSAGE_INVALID_BOOK_DISPLAYED_INDEX);

        /* ------------ Perform reviews operations on the shown recent books list ------------ */
        executeCommand(SelectCommand.COMMAND_WORD + " 1");
        executeCommand(RecentCommand.COMMAND_WORD);
        assertCommandSuccess(ReviewsCommand.COMMAND_WORD + " 1", getModel().getRecentBooksList().get(0));
        assertCommandFailure(ReviewsCommand.COMMAND_WORD + " 51", MESSAGE_INVALID_BOOK_DISPLAYED_INDEX);
    }

    /**
     * Executes {@code command} and verifies that, after the reviews page has loaded,<br>
     * 1. Command box displays an empty string.<br>
     * 2. Command box has the default style class.<br>
     * 3. Result display box displays the load successful message.<br>
     * 4. {@code Model} and {@code Storage} remain unchanged.<br>
     * 5. Any selections in {@code BookListPanel}, {@code SearchResultsPanel},
     *    and {@code RecentBooksPanel} are all deselected.<br>
     * 6. {@code BookReviewsPanel} is visible.
     * 7. Status bar remains unchanged.<br>
     * Verifications 1, 3 and 4 are performed by
     * {@code BibliotekSystemTest#assertApplicationDisplaysExpected(String, String, Model)}.<br>
     * @see BibliotekSystemTest#assertApplicationDisplaysExpected(String, String, Model)
     */
    private void assertCommandSuccess(String command, Book toLoad) {
        Model expectedModel = getModel();

        executeCommand(command);
        assertCommandBoxShowsDefaultStyle();

        String expectedResultMessage = String.format(ReviewsCommand.MESSAGE_SUCCESS, toLoad);

        assertApplicationDisplaysExpected("", expectedResultMessage, expectedModel);
        assertSelectedBookListCardDeselected();
        assertSelectedSearchResultsCardDeselected();
        assertSelectedRecentBooksCardDeselected();
        assertBookReviewsPanelVisible();
        assertStatusBarUnchanged();
    }
}
```