# qiu-siqi
###### \java\seedu\address\logic\commands\AddCommand.java
``` java
/**
 * Adds a book to the book shelf.
 */
public class AddCommand extends UndoableCommand {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds the book identified by the index number used in the current search result.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_ADDING = "Adding the book into your book shelf...";
    public static final String MESSAGE_ADD_FAIL = "Failed to add book into your book shelf. "
            + "Make sure you are connected to the Internet.";
    public static final String MESSAGE_SUCCESS = "New book added: %1$s";
    public static final String MESSAGE_DUPLICATE_BOOK = "This book already exists in the book shelf";
    public static final String MESSAGE_WRONG_ACTIVE_LIST = "Items from the current list cannot be added.";

    private final Index targetIndex;

    private Book toAdd;
    private final boolean useJavafxThread;

    public AddCommand(Index targetIndex) {
        this(targetIndex, true);
    }

    /**
     * Creates a {@code AddCommand} that can choose not use the JavaFX thread to update the model and UI.
     * This constructor is provided for unit-testing purposes.
     */
    protected AddCommand(Index targetIndex, boolean useJavafxThread) {
        requireNonNull(targetIndex);
        this.targetIndex = targetIndex;
        this.useJavafxThread = useJavafxThread;
    }

    @Override
    public CommandResult executeUndoableCommand() {
        requireNonNull(toAdd);

        EventsCenter.getInstance().post(new DisableCommandBoxRequestEvent());
        makeAsyncBookDetailsRequest();
        return new CommandResult(MESSAGE_ADDING);
    }

    /**
     * Makes an asynchronous request to retrieve book details.
     */
    private void makeAsyncBookDetailsRequest() {
        network.getBookDetails(toAdd.getGid().gid)
                .thenAccept(this::onSuccessfulRequest)
                .exceptionally(e -> {
                    EventsCenter.getInstance().post(new NewResultAvailableEvent(AddCommand.MESSAGE_ADD_FAIL));
                    EventsCenter.getInstance().post(new EnableCommandBoxRequestEvent());
                    return null;
                });
    }

    /**
     * Handles the result of a successful request for book details.
     */
    private void onSuccessfulRequest(Book book) {
        if (useJavafxThread) {
            Platform.runLater(() -> addBook(book));
        } else {
            addBook(book);
        }
    }

    /**
     * Adds the given book to the book shelf and posts events to update the UI.
     */
    protected void addBook(Book book) {
        try {
            model.addBook(book);
            EventsCenter.getInstance().post(new NewResultAvailableEvent(
                    String.format(AddCommand.MESSAGE_SUCCESS, book)));
        } catch (DuplicateBookException e) {
            EventsCenter.getInstance().post(new NewResultAvailableEvent(AddCommand.MESSAGE_DUPLICATE_BOOK));
        }
        EventsCenter.getInstance().post(new EnableCommandBoxRequestEvent());
    }

    @Override
    protected void preprocessUndoableCommand() throws CommandException {
        requireNonNull(model);

        checkActiveListType();
        checkValidIndex();

        toAdd = model.getActiveList().get(targetIndex.getZeroBased());
    }

    /**
     * Throws a {@link CommandException} if the active list type is not supported by this command.
     */
    private void checkActiveListType() throws CommandException {
        if (model.getActiveListType() == ActiveListType.BOOK_SHELF) {
            throw new CommandException(MESSAGE_WRONG_ACTIVE_LIST);
        }
    }

    /**
     * Throws a {@link CommandException} if the given index is not valid.
     */
    private void checkValidIndex() throws CommandException {
        if (targetIndex.getZeroBased() >= model.getActiveList().size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_BOOK_DISPLAYED_INDEX);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof AddCommand // instanceof handles nulls
                && this.targetIndex.equals(((AddCommand) other).targetIndex)
                && Objects.equals(this.toAdd, ((AddCommand) other).toAdd));
    }
}
```
###### \java\seedu\address\logic\commands\LibraryCommand.java
``` java
/**
 * Searches for a book in the library.
 */
public class LibraryCommand extends Command {

    public static final String COMMAND_WORD = "library";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Views the availability of the book identified by the index number in the library.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_SUCCESS = "Showing availability of book: %1$s.";
    public static final String MESSAGE_FAIL = "Failed to search for book in library. "
            + "Make sure you are connected to the Internet.";
    public static final String MESSAGE_SEARCHING = "Searching for the book in the library...";
    public static final String MESSAGE_WRONG_ACTIVE_LIST = "Cannot view availability of books "
            + "in the current list.";

    private final Index targetIndex;

    public LibraryCommand(Index targetIndex) {
        requireNonNull(targetIndex);
        this.targetIndex = targetIndex;
    }

    @Override
    public CommandResult execute() throws CommandException {
        requireNonNull(model);

        checkValidIndex();

        makeAsyncBookInLibraryRequest(getBook(targetIndex));
        return new CommandResult(MESSAGE_SEARCHING);
    }

    /**
     * Throws a {@link CommandException} if the given index is not valid.
     */
    private void checkValidIndex() throws CommandException {
        if (targetIndex.getZeroBased() >= model.getActiveList().size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_BOOK_DISPLAYED_INDEX);
        }
    }

    /**
     * Assumes: {@code targetIndex} is a valid index.
     * Returns the book to search for.
     */
    private Book getBook(Index targetIndex) {
        return model.getActiveList().get(targetIndex.getZeroBased());
    }

    /**
     * Makes an asynchronous request to search for {@code book} in library.
     */
    private void makeAsyncBookInLibraryRequest(Book book) {
        network.searchLibraryForBook(book)
                .thenAccept(result -> onSuccessfulRequest(result, book))
                .exceptionally(e -> {
                    EventsCenter.getInstance().post(new NewResultAvailableEvent(MESSAGE_FAIL));
                    return null;
                });
    }

    /**
     * Handles the result of a successful search for the book in library.
     */
    private void onSuccessfulRequest(String result, Book book) {
        EventsCenter.getInstance().post(new ShowLibraryResultRequestEvent(result));
        EventsCenter.getInstance().post(new NewResultAvailableEvent(
                String.format(MESSAGE_SUCCESS, book)));
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof LibraryCommand // instanceof handles nulls
                && this.targetIndex.equals(((LibraryCommand) other).targetIndex));
    }
}
```
###### \java\seedu\address\logic\commands\RecentCommand.java
``` java
/**
 * Lists all recently selected books to the user.
 */
public class RecentCommand extends Command {

    public static final String COMMAND_WORD = "recent";
    public static final String MESSAGE_SUCCESS = "Listed all recently selected books.";

    @Override
    public CommandResult execute() {
        requireNonNull(model);
        EventsCenter.getInstance().post(new SwitchToRecentBooksRequestEvent());
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
```
###### \java\seedu\address\logic\commands\ReviewsCommand.java
``` java
/**
 * Shows reviews for a specified book.
 */
public class ReviewsCommand extends Command {

    public static final String COMMAND_WORD = "reviews";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Loads reviews of the book identified by the index number used in the current list.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_SUCCESS = "Showing reviews for book: %1$s.";
    public static final String MESSAGE_WRONG_ACTIVE_LIST = "Cannot load reviews for items "
            + "from the current list.";

    private final Index targetIndex;

    public ReviewsCommand(Index targetIndex) {
        requireNonNull(targetIndex);
        this.targetIndex = targetIndex;
    }

    @Override
    public CommandResult execute() throws CommandException {
        requireNonNull(model);

        checkValidIndex();
        Book toLoad = model.getActiveList().get(targetIndex.getZeroBased());

        EventsCenter.getInstance().post(new ShowBookReviewsRequestEvent(toLoad));
        return new CommandResult(String.format(MESSAGE_SUCCESS, toLoad));
    }

    /**
     * Throws a {@link CommandException} if the given index is not valid.
     */
    private void checkValidIndex() throws CommandException {
        if (targetIndex.getZeroBased() >= model.getActiveList().size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_BOOK_DISPLAYED_INDEX);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof ReviewsCommand // instanceof handles nulls
                && this.targetIndex.equals(((ReviewsCommand) other).targetIndex));
    }
}
```
###### \java\seedu\address\logic\parser\AddCommandParser.java
``` java
/**
 * Parses input arguments and creates a new AddCommand object
 */
public class AddCommandParser implements Parser<AddCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the AddCommand
     * and returns an AddCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    public AddCommand parse(String args) throws ParseException {
        try {
            Index index = ParserUtil.parseIndex(args);
            return new AddCommand(index);
        } catch (IllegalValueException ive) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        }
    }
}
```
###### \java\seedu\address\logic\parser\ReviewsCommandParser.java
``` java
/**
 * Parses input arguments and creates a new ReviewsCommand object.
 */
public class ReviewsCommandParser implements Parser<ReviewsCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the ReviewsCommand
     * and returns an ReviewsCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format.
     */
    public ReviewsCommand parse(String args) throws ParseException {
        try {
            Index index = ParserUtil.parseIndex(args);
            return new ReviewsCommand(index);
        } catch (IllegalValueException ive) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, ReviewsCommand.MESSAGE_USAGE));
        }
    }
}
```
###### \java\seedu\address\model\book\UniqueBookCircularList.java
``` java
/**
 * A list of items that enforces no nulls and uniqueness between its elements,
 * with maximum of a pre-set number of elements.
 * When the limit is reached, the earliest added element is removed to add the new element.
 *
 * Supports a minimal set of operations.
 */
public class UniqueBookCircularList extends UniqueList<Book> {

    private static final String ORIGINAL_KEY = "111111";
    private final int size;
    private final String key;

    public UniqueBookCircularList() {
        this.size = 50;
        this.key = ORIGINAL_KEY;
    }

    /**
     * Constructs a list where the maximum number of books in the list is {@code size}.
     */
    public UniqueBookCircularList(int size) {
        this.size = size;
        this.key = ORIGINAL_KEY;
    }

    /**
     * Adds a book to the front of the list.
     * Moves the book to the front of the list if it exists in the list.
     * Removes the earliest added book if the list is full before adding the new book.
     */
    public void addToFront(Book toAdd) {
        requireNonNull(toAdd);
        if (contains(toAdd)) {
            internalList.remove(toAdd);
        }
        if (internalList.size() >= size) {
            internalList.remove(size - 1);
        }
        internalList.add(0, toAdd);

        assert internalList.size() <= size;
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof UniqueBookCircularList // instanceof handles nulls
                && this.internalList.equals(((UniqueBookCircularList) other).internalList)
                && this.size == ((UniqueBookCircularList) other).size);
    }
}
```
###### \java\seedu\address\network\library\NlbCatalogueApi.java
``` java
/**
 * Provides access to the NLB catalogue.
 */
public class NlbCatalogueApi {

    private static final String SEARCH_URL =
            "https://catalogue.nlb.gov.sg/cgi-bin/spydus.exe/ENQ/EXPNOS/BIBENQ?ENTRY=%t %a&ENTRY_NAME=BS"
                    + "&ENTRY_TYPE=K&GQ=%t %a&SORTS=SQL_REL_TITLE";
    private static final String CONTENT_TYPE_HTML = "text/html";
    private static final int HTTP_STATUS_OK = 200;

    private final HttpClient httpClient;

    public NlbCatalogueApi(HttpClient httpClient) {
        requireNonNull(httpClient);
        this.httpClient = httpClient;
    }

    /**
     * Searches for a book in NLB catalogue.
     *
     * @param book book to search for.
     * @return CompleteableFuture which resolves to a single book page.
     */
    public CompletableFuture<String> searchForBook(Book book) {
        requireNonNull(book);
        return execute(makeSearchUrl(book), book);
    }

    /**
     * Obtains the search URL for a particular {@code book} to search for.
     */
    protected static String makeSearchUrl(Book book) {
        requireNonNull(book);
        return SEARCH_URL.replace("%t", book.getTitle().toString()).replace("%a", book.getAuthorsAsString());
    }

    /**
     * Asynchronously executes a HTTP GET request to the specified url to find the specified book.
     *
     * @param url the url used for the GET request.
     * @param book the book to search for.
     * @return CompleteableFuture which resolves to a single book page.
     */
    private CompletableFuture<String> execute(String url, Book book) {
        return httpClient
                .makeGetRequest(url)
                .thenApply(NlbCatalogueApi::requireHtmlContentType)
                .thenApply(NlbCatalogueApi::requireHttpStatusOk)
                .thenApply(HttpResponse::getResponseBody)
                .thenApply(result -> NlbResultHelper.getUrl(result, book));
    }

    /**
     * Throws a {@link CompletionException} if the content type of the response is not HTML.
     */
    private static HttpResponse requireHtmlContentType(HttpResponse response) {
        if (!response.getContentType().startsWith(CONTENT_TYPE_HTML)) {
            throw new CompletionException(
                    new IOException("Unexpected content type " + response.getContentType()));
        }
        return response;
    }

    /**
     * Throws a {@link CompletionException} if the HTTP status code of the response is not {@code 200: OK}.
     */
    private static HttpResponse requireHttpStatusOk(HttpResponse response) {
        if (response.getStatusCode() != HTTP_STATUS_OK) {
            throw new CompletionException(
                    new IOException("Get request failed with status code " + response.getStatusCode()));
        }
        return response;
    }
}
```
###### \java\seedu\address\network\library\NlbResultHelper.java
``` java
/**
 * Provides utilities to manage result from NLB catalogue search.
 */
public class NlbResultHelper {

    protected static final String URL_FULL_DISPLAY = "https://catalogue.nlb.gov.sg/cgi-bin/spydus.exe/"
            + "ENQ/EXPNOS/BIBENQ?ENTRY=%s&ENTRY_NAME=BS&ENTRY_TYPE=K&GQ=%s&SORTS=SQL_REL_TITLE";
    protected static final String NO_RESULTS_FOUND = "No results found.";
    private static final String FULL_DISPLAY_STRING = "<span>Full Display</span>";
    private static final String SEARCH_STRING = "/cgi-bin/spydus.exe/FULL/EXPNOS/BIBENQ/";
    private static final String URL_PREFIX = "https://catalogue.nlb.gov.sg";

    /**
     * Obtains a URL linking to the display of a single book, if search only found that single book.
     * Obtains the URL of the top search result, if {@code result} contains a list of search results.
     * Returns a custom String if the list is empty.
     *
     * @param result result from querying NLB catalogue.
     * @param book book that was queried for.
     * @return String containing the single book page as HTML content.
     */
    protected static String getUrl(String result, Book book) {
        requireAllNonNull(result, book);

        if (!isFullDisplay(result)) {
            return getTopResultUrl(result);
        }
        String searchTerms = book.getTitle().toString() + " " + book.getAuthorsAsString();
        return String.format(URL_FULL_DISPLAY, searchTerms, searchTerms);
    }

    /**
     * Checks whether {@code result} is the full display result page of a single book.
     */
    private static boolean isFullDisplay(String result) {
        return result.contains(FULL_DISPLAY_STRING);
    }

    /**
     * Assumes: {@code result} is not the full display result page.
     * Obtains the URL of the top search result, if any.
     */
    private static String getTopResultUrl(String result) {
        assert !isFullDisplay(result);

        int index = result.indexOf(SEARCH_STRING);
        if (index == -1) {
            return NO_RESULTS_FOUND;
        }
        return getUrlFromIndex(result, index);
    }

    /**
     * Assumes: {@code index} is valid.
     * Obtains the URL which is given starting at {@code index} in {@code result}.
     */
    private static String getUrlFromIndex(String result, int index) {
        int len = result.length();

        assert index >= 0;
        assert index < len;

        StringBuilder builder = new StringBuilder();
        builder.append(URL_PREFIX);

        for (int i = index;; i++) {
            if (i >= len || result.charAt(i) == '\"') {
                break;
            }
            builder.append(result.charAt(i));
        }

        return builder.toString();
    }
}
```
###### \java\seedu\address\storage\XmlRecentBooksStorage.java
``` java
/**
 * A class to access recently selected books data stored as an xml file on the hard disk.
 */
public class XmlRecentBooksStorage implements RecentBooksStorage {

    private static final Logger logger = LogsCenter.getLogger(XmlRecentBooksStorage.class);

    private String filePath;

    public XmlRecentBooksStorage(String filePath) {
        this.filePath = filePath;
    }

    public String getRecentBooksFilePath() {
        return filePath;
    }

    @Override
    public Optional<ReadOnlyBookShelf> readRecentBooksList() throws DataConversionException, IOException {
        return readRecentBooksList(filePath);
    }

    /**
     * Similar to {@link #readRecentBooksList()}
     * @param filePath location of the data. Cannot be null
     * @throws DataConversionException if the file is not in the correct format.
     */
    public Optional<ReadOnlyBookShelf> readRecentBooksList(String filePath) throws DataConversionException,
            FileNotFoundException {
        requireNonNull(filePath);

        File file = new File(filePath);

        if (!file.exists()) {
            logger.info("Recent books file "  + file + " not found");
            return Optional.empty();
        }

        XmlSerializableBookShelf xmlRecentBooksList = XmlFileStorage.loadBookShelfDataFromFile(new File(filePath));
        try {
            return Optional.of(xmlRecentBooksList.toModelType());
        } catch (IllegalValueException ive) {
            logger.info("Illegal values found in " + file + ": " + ive.getMessage());
            throw new DataConversionException(ive);
        }
    }

    @Override
    public void saveRecentBooksList(ReadOnlyBookShelf recentBooksList) throws IOException {
        saveRecentBooksList(recentBooksList, filePath);
    }

    /**
     * Similar to {@link #saveRecentBooksList(ReadOnlyBookShelf)}
     * @param filePath location of the data. Cannot be null
     */
    public void saveRecentBooksList(ReadOnlyBookShelf recentBooksList, String filePath) throws IOException {
        requireNonNull(recentBooksList);
        requireNonNull(filePath);

        File file = new File(filePath);
        FileUtil.createIfMissing(file);
        XmlFileStorage.saveBookShelfDataToFile(file, new XmlSerializableBookShelf(recentBooksList));
    }
}
```
###### \java\seedu\address\ui\BookDescriptionView.java
``` java
/**
 * The Region showing description of books.
 */
public class BookDescriptionView extends UiPart<Region> {

    private static final int HEIGHT_PAD = 20;

    private static final String FXML = "BookDescriptionView.fxml";

    @FXML
    private WebView description;

    private final WebEngine webEngine;

    public BookDescriptionView() {
        super(FXML);
        webEngine = description.getEngine();

        // disable interaction with web view
        description.setDisable(true);

        description.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                adjustHeight();
            }
        });
    }

    /**
     * Loads the description of {@code book}.
     */
    protected void loadContent(Book book) {
        description.getEngine().loadContent(getHtml(book.getDescription().toString()));
        // set transparent background for web view
        Accessor.getPageFor(webEngine).setBackgroundColor(0);
    }

    // Reused from http://tech.chitgoks.com/2014/09/13/how-to-fit-webview-height-based-on-its-content-in-java-fx-2-2/
    /**
     * Fit height of {@code WebView} according to height of content.
     */
    private void adjustHeight() {
        Platform.runLater(() -> {
            Object result = webEngine.executeScript("document.getElementById('description').offsetHeight");
            if (result instanceof Integer) {
                Integer height = (Integer) result;
                description.setPrefHeight(height + HEIGHT_PAD);
            }
        });
    }

    // Reused from http://tech.chitgoks.com/2014/09/13/how-to-fit-webview-height-based-on-its-content-in-java-fx-2-2/
    private String getHtml(String content) {
        return "<html><body><div id=\"description\">" + content + "</div></body></html>";
    }

    protected void setStyleSheet(String styleSheet) {
        description.getEngine().setUserStyleSheetLocation(getClass().getClassLoader()
                .getResource(styleSheet).toExternalForm());
    }

}
```
###### \java\seedu\address\ui\BookInLibraryPanel.java
``` java
/**
 * The region showing availability of the book in NLB.
 */
public class BookInLibraryPanel extends UiPart<Region> {

    private static final String FXML = "BookInLibraryPanel.fxml";
    private static final URL NLB_RESULT_SCRIPT_FILE = MainApp.class.getResource("/view/nlbResultScript.js");
    private static final URL CLEAR_PAGE_SCRIPT_FILE = MainApp.class.getResource("/view/clearPageScript.js");

    private final Logger logger = LogsCenter.getLogger(this.getClass());
    private final String nlbResultScript;
    private final String clearPageScript;

    @FXML
    private StackPane browserPlaceholder;
    private WebViewManager webViewManager;

    public BookInLibraryPanel(WebViewManager webViewManager) {
        super(FXML);

        this.webViewManager = webViewManager;
        registerAsAnEventHandler(this);
        getRoot().setVisible(false);

        try {
            nlbResultScript = Resources.toString(NLB_RESULT_SCRIPT_FILE, Charsets.UTF_8);
            clearPageScript = Resources.toString(CLEAR_PAGE_SCRIPT_FILE, Charsets.UTF_8);
        } catch (IOException e) {
            throw new AssertionError("Missing script file: " + e.getMessage());
        }

        // To prevent triggering events for typing inside the loaded Web page.
        getRoot().setOnKeyPressed(Event::consume);

        webViewManager.onLoadProgress(getRoot(), 0.59, () -> {
            webViewManager.executeScript(nlbResultScript);
            getRoot().setDisable(false);
        });
    }

    private void loadPageWithResult(String result) {
        webViewManager.load(browserPlaceholder, result);
    }

    private void clearPage() {
        webViewManager.executeScript(clearPageScript);
    }

    protected void hide() {
        getRoot().setVisible(false);
    }

    protected void show() {
        getRoot().setVisible(true);
    }

    @Subscribe
    private void handleShowBookInLibraryRequestEvent(ShowLibraryResultRequestEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        Platform.runLater(() -> {
            clearPage();
            // Prevent browser from getting focus
            getRoot().setDisable(true);
            getRoot().setVisible(true);
            loadPageWithResult(event.getResult());
        });
    }
}
```
###### \java\seedu\address\ui\WebViewManager.java
``` java
/**
 * Exposes required functionalities of {@link WebView}.
 *
 * There can only be one instance of {@code WebViewManager}, allowing the internal limiting
 * of number of WebViews used.
 */
public class WebViewManager {

    private static WebViewManager webViewManager;
    private WebView browser;
    private WebEngine engine;
    private List<ChangeListener<? super Number>> loadProgressListeners;
    private List<ChangeListener<? super Worker.State>> loadSuccessListeners;

    private WebViewManager() {
        browser = new WebView();
        engine = browser.getEngine();
        loadProgressListeners = new ArrayList<>();
        loadSuccessListeners = new ArrayList<>();
    }

    /**
     * Obtains the instance of {@code WebViewManager}.
     */
    public static WebViewManager getInstance() {
        if (webViewManager == null) {
            webViewManager = new WebViewManager();
        }
        return webViewManager;
    }

    /**
     * Loads {@code WebView} with the given content and shows it in {@code container}.
     *
     * @param container Parent to contain the loaded {@code WebView}.
     * @param toLoad content to load in {@code WebView}.
     */
    protected void load(Pane container, String toLoad) {
        addBrowserToPane(container);
        loadBasedOnContent(toLoad);
    }

    /**
     * Adds {@code WebView} as a child of {@code container}, if it is not already.
     *
     * @param container Parent to contain the {@code WebView}.
     */
    private void addBrowserToPane(Pane container) {
        if (!container.getChildren().contains(browser)) {
            container.getChildren().add(browser);
        }
    }

    /**
     * Loads {@code WebView} with content depending on type of {@code toLoad}.
     *
     * @param toLoad content to load in {@code WebView}.
     */
    private void loadBasedOnContent(String toLoad) {
        if (StringUtil.isValidUrl(toLoad)) {
            engine.load(toLoad);
        } else {
            engine.loadContent(toLoad);
        }
    }

    /**
     * Executes a script in the context of the current page in the {@code WebView}.
     *
     * @param scriptPath Path of the script to be called.
     */
    protected void executeScript(String scriptPath) {
        engine.executeScript(scriptPath);
    }

    /**
     * Specifies an action to be performed upon successfully loading any page, given that
     * {@code root} is visible.
     * This method should only be called once for each action, during initialization
     * of a class that will use a {@code WebView}.
     *
     * @param root Node which visibility determines whether to perform this action.
     * @param runnable Action to perform upon successfully loading a page.
     */
    protected void onLoadSuccess(Node root, Runnable runnable) {
        ChangeListener<? super Worker.State> newListener = (obs, oldState, newState) -> {
            if (root.isVisible() && newState == Worker.State.SUCCEEDED) {
                runnable.run();
            }
        };
        engine.getLoadWorker().stateProperty().addListener(newListener);
        loadSuccessListeners.add(newListener);
    }

    /**
     * Specifies an action to be performed if the loading of a page exceeds a given percentage, given that
     * {@code root} is visible. This action is performed multiple times until loading is completed.
     * This method should only be called once for each action, during initialization
     * of a class that will use a {@code WebView}.
     *
     * @param root Node which visibility determines whether to perform this action.
     * @param progress Percentage of the page loaded, from 0 to 1.
     * @param runnable Action to perform upon successfully loading a page.
     */
    protected void onLoadProgress(Node root, double progress, Runnable runnable) {
        ChangeListener<? super Number> newListener = (obs, oldNum, newNum) -> {
            if (root.isVisible() && engine.getLoadWorker().getProgress() > progress) {
                runnable.run();
            }
        };
        engine.getLoadWorker().progressProperty().addListener(newListener);
        loadProgressListeners.add(newListener);
    }

    /**
     * Returns the {@code WebView}, for testing purposes. It is not recommended to interact directly with
     * the {@code WebView} contained in this class.
     */
    public WebView getWebView() {
        return browser;
    }

    /**
     * Free up unneeded resources.
     */
    public void cleanUp() {
        webViewManager = null;
        Platform.runLater(() -> {
            loadProgressListeners.forEach(engine.getLoadWorker().progressProperty()::removeListener);
            loadSuccessListeners.forEach(engine.getLoadWorker().stateProperty()::removeListener);
        });
    }
}
```