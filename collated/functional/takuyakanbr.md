# takuyakanbr
###### \java\seedu\address\commons\core\Theme.java
``` java
/**
 * Represents an application theme.
 */
public enum Theme {
    WHITE("view/WhiteTheme.css"),
    LIGHT("view/LightTheme.css"),
    DARK("view/DarkTheme.css");

    public static final Theme DEFAULT_THEME = WHITE;

    private final String cssFile;

    Theme(String cssFile) {
        this.cssFile = cssFile;
    }

    public String getThemeName() {
        return name().toLowerCase();
    }

    public String getCssFile() {
        return cssFile;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * Returns the theme with a theme name that matches the specified {@code themeName}.
     *
     * @throws InvalidThemeException if no matching theme can be found.
     */
    public static Theme getThemeByName(String themeName) throws InvalidThemeException {
        requireNonNull(themeName);

        for (Theme theme : values()) {
            if (themeName.equalsIgnoreCase(theme.getThemeName())) {
                return theme;
            }
        }

        throw new InvalidThemeException("Invalid theme name: " + themeName);
    }

}
```
###### \java\seedu\address\commons\util\StringUtil.java
``` java
    /**
     * Returns a random 8 character string to be used as a prefix to a filename.
     */
    public static String generateRandomPrefix() {
        byte[] randomBytes = new byte[RANDOM_BYTE_LENGTH];
        new Random().nextBytes(randomBytes);
        byte[] encodedBytes = Base64.getEncoder().encode(randomBytes);
        return new String(encodedBytes).replace("/", "-");
    }
}
```
###### \java\seedu\address\logic\commands\AddAliasCommand.java
``` java
/**
 * Adds a command alias to the alias list.
 */
public class AddAliasCommand extends Command {

    public static final String COMMAND_WORD = "addalias";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a new alias for a command.\n"
            + "Parameters: ALIAS_NAME "
            + PREFIX_COMMAND + "COMMAND\n"
            + "Example: " + COMMAND_WORD + "urd "
            + PREFIX_COMMAND + "list s/unread";

    public static final String MESSAGE_NEW = "Added a new alias: %1$s.";
    public static final String MESSAGE_UPDATE = "Updated an existing alias: %1$s.";

    private final Alias toAdd;

    /**
     * Creates an {@code AddAliasCommand}.
     *
     * @param toAdd the alias to be added.
     */
    public AddAliasCommand(Alias toAdd) {
        requireNonNull(toAdd);
        this.toAdd = toAdd;
    }

    @Override
    public CommandResult execute() {
        boolean isUpdating = model.getAlias(toAdd.getName()).isPresent();
        model.addAlias(toAdd);

        String message = isUpdating ? MESSAGE_UPDATE : MESSAGE_NEW;
        return new CommandResult(String.format(message, toAdd));
    }

```
###### \java\seedu\address\logic\commands\AliasesCommand.java
``` java
/**
 * Shows a list of all command aliases.
 */
public class AliasesCommand extends Command {

    public static final String COMMAND_WORD = "aliases";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Shows a listing of your command aliases.\n"
            + "Example: " + COMMAND_WORD;

    public static final String MESSAGE_SUCCESS = "Listed %s aliases.";

    @Override
    public CommandResult execute() {
        EventsCenter.getInstance().post(new ShowAliasListRequestEvent());
        return new CommandResult(String.format(MESSAGE_SUCCESS, model.getAliasList().size()));
    }
}
```
###### \java\seedu\address\logic\commands\DeleteAliasCommand.java
``` java
/**
 * Deletes the alias identified by the alias name from the alias list.
 */
public class DeleteAliasCommand extends Command {

    public static final String COMMAND_WORD = "deletealias";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Deletes the alias identified by the alias name.\n"
            + "Parameters: ALIAS_NAME\n"
            + "Example: " + COMMAND_WORD + "urd";

    public static final String MESSAGE_SUCCESS = "Deleted alias: %1$s.";
    public static final String MESSAGE_NOT_FOUND = "Alias does not exist: %1$s.";

    private final String toDelete;

    /**
     * Creates an {@code DeleteAliasCommand}.
     *
     * @param toDelete the name of the alias to be deleted.
     */
    public DeleteAliasCommand(String toDelete) {
        requireNonNull(toDelete);
        this.toDelete = toDelete.trim().toLowerCase();
    }

    @Override
    public CommandResult execute() {
        Optional<Alias> alias = model.getAlias(toDelete);
        if (!alias.isPresent()) {
            return new CommandResult(String.format(MESSAGE_NOT_FOUND, toDelete));
        }

        model.removeAlias(toDelete);
        return new CommandResult(String.format(MESSAGE_SUCCESS, alias.get()));
    }

```
###### \java\seedu\address\logic\commands\ListCommand.java
``` java
/**
 * Lists all books in the book shelf to the user.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Shows a listing of your books, with optional filtering and sorting.\n"
            + "Parameters: "
            + "[" + PREFIX_TITLE + "TITLE] "
            + "[" + PREFIX_AUTHOR + "AUTHOR] "
            + "[" + PREFIX_CATEGORY + "CATEGORY]"
            + "[" + PREFIX_STATUS + "STATUS]"
            + "[" + PREFIX_PRIORITY + "PRIORITY]"
            + "[" + PREFIX_RATING + "RATING]"
            + "[" + PREFIX_SORT_BY + "SORT_BY]\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_STATUS + " reading "
            + PREFIX_SORT_BY + "priority";

    public static final String MESSAGE_SUCCESS = "Listed %s books.";

    private final FilterDescriptor filterDescriptor;
    private final Comparator<Book> bookComparator;

    /**
     * Creates a ListCommand to show a listing of books.
     *
     * @param filterDescriptor the filters used for filtering the books to be displayed.
     * @param bookComparator the comparator used for sorting the books to be displayed.
     */
    public ListCommand(FilterDescriptor filterDescriptor, Comparator<Book> bookComparator) {
        requireAllNonNull(filterDescriptor, bookComparator);
        this.filterDescriptor = filterDescriptor;
        this.bookComparator = bookComparator;
    }

    @Override
    public CommandResult execute() {
        // Clear to prevent invalid selections due to changes in observed list
        EventsCenter.getInstance().post(new ClearMainContentRequestEvent());

        model.updateBookListSorter(bookComparator);
        model.updateBookListFilter(filterDescriptor.buildCombinedFilter());
        model.setActiveListType(ActiveListType.BOOK_SHELF);
        EventsCenter.getInstance().post(new ActiveListChangedEvent());
        return new CommandResult(String.format(MESSAGE_SUCCESS, model.getDisplayBookList().size()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof ListCommand // instanceof handles nulls
                && this.filterDescriptor.equals(((ListCommand) other).filterDescriptor) // state check
                && this.bookComparator.equals(((ListCommand) other).bookComparator));
    }

    /**
     * Stores the filters used for filtering the books to be displayed.
     */
    public static class FilterDescriptor {
        private final List<Predicate<Book>> filters;
        private Set<String> filterCodes; // used internally for equality testing

        public FilterDescriptor() {
            filters = new LinkedList<>();
            filterCodes = new HashSet<>();
        }

        public FilterDescriptor(FilterDescriptor descriptor) {
            filters = new LinkedList<>(descriptor.filters);
            filterCodes = new HashSet<>(descriptor.filterCodes);
        }

        /** Adds a filter that selects books with titles containing the given {@code title}. */
        public void addTitleFilter(String title) {
            filters.add(book -> book.getTitle().title.toLowerCase().contains(title.toLowerCase()));
            filterCodes.add("t[" + title.toLowerCase() + "]");
        }

        /** Adds a filter that selects books with authors containing the given {@code author}. */
        public void addAuthorFilter(String author) {
            filters.add(book -> book.getAuthors().stream()
                    .anyMatch(bookAuthor -> bookAuthor.fullName.toLowerCase().contains(author.toLowerCase())));
            filterCodes.add("a[" + author.toLowerCase() + "]");
        }

        /** Adds a filter that selects books with categories containing the given {@code category}. */
        public void addCategoryFilter(String category) {
            filters.add(book -> book.getCategories().stream()
                    .anyMatch(bookCategory -> bookCategory.category.toLowerCase().contains(category.toLowerCase())));
            filterCodes.add("c[" + category.toLowerCase() + "]");
        }

        /** Adds a filter that selects books with status matching the given {@code status}. */
        public void addStatusFilter(Status status) {
            filters.add(book -> status.equals(book.getStatus()));
            filterCodes.add("s[" + status + "]");
        }

        /** Adds a filter that selects books with priority matching the given {@code priority}. */
        public void addPriorityFilter(Priority priority) {
            filters.add(book -> priority.equals(book.getPriority()));
            filterCodes.add("p[" + priority + "]");
        }

        /** Adds a filter that selects books with ratings matching the given {@code rating}. */
        public void addRatingFilter(Rating rating) {
            filters.add(book -> rating.equals(book.getRating()));
            filterCodes.add("r[" + rating + "]");
        }

        protected Predicate<Book> buildCombinedFilter() {
            List<Predicate<Book>> partialFilters = new LinkedList<>(filters);
            return book -> partialFilters.stream().allMatch(filter -> filter.test(book));
        }

        @Override
        public boolean equals(Object other) {
            return other == this // short circuit if same object
                    || (other instanceof FilterDescriptor // instanceof handles nulls
                    && this.filterCodes.equals(((FilterDescriptor) other).filterCodes)); // state check
        }
    }
}
```
###### \java\seedu\address\logic\commands\SearchCommand.java
``` java
/**
 * Searches for books on Google Books that matches a set of parameters.
 */
public class SearchCommand extends Command {

    public static final String COMMAND_WORD = "search";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Searches for books online.\n"
            + "Parameters: [KEY_WORDS] "
            + "[" + PREFIX_ISBN + "ISBN] "
            + "[" + PREFIX_TITLE + "TITLE] "
            + "[" + PREFIX_AUTHOR + "AUTHOR] "
            + "[" + PREFIX_CATEGORY + "CATEGORY]\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_TITLE + " Artemis "
            + PREFIX_AUTHOR + "Andy Weir";

    public static final String MESSAGE_SEARCHING = "Searching for matching books...";
    public static final String MESSAGE_EMPTY_QUERY = "No search parameter specified.";
    public static final String MESSAGE_SEARCH_FAIL = "Failed to retrieve information from online service.";
    public static final String MESSAGE_SEARCH_SUCCESS = "Found %s matching books.";

    private final SearchDescriptor searchDescriptor;
    private final boolean useJavafxThread;

    /**
     * Creates a SearchCommand to search for matching books.
     * @param searchDescriptor parameters to search with.
     */
    public SearchCommand(SearchDescriptor searchDescriptor) {
        this(searchDescriptor, true);
    }

    /**
     * Creates a {@code SearchCommand} that can choose not use the JavaFX thread to update the model and UI.
     * This constructor is provided for unit-testing purposes.
     */
    protected SearchCommand(SearchDescriptor searchDescriptor, boolean useJavafxThread) {
        requireNonNull(searchDescriptor);
        assert searchDescriptor.isValid();

        this.searchDescriptor = new SearchDescriptor(searchDescriptor);
        this.useJavafxThread = useJavafxThread;
    }

    @Override
    public CommandResult execute() {
        EventsCenter.getInstance().post(new DisableCommandBoxRequestEvent());
        makeAsyncSearchRequest();
        return new CommandResult(MESSAGE_SEARCHING);
    }

    /**
     * Makes an asynchronous request to search for books.
     */
    private void makeAsyncSearchRequest() {
        String searchString = searchDescriptor.toSearchString();
        assert !searchString.trim().isEmpty();

        network.searchBooks(searchString)
                .thenAccept(this::onSuccessfulRequest)
                .exceptionally(e -> {
                    EventsCenter.getInstance().post(new NewResultAvailableEvent(SearchCommand.MESSAGE_SEARCH_FAIL));
                    EventsCenter.getInstance().post(new EnableCommandBoxRequestEvent());
                    return null;
                });
    }

    /**
     * Handles the result of a successful request to search for books.
     */
    private void onSuccessfulRequest(ReadOnlyBookShelf bookShelf) {
        requireNonNull(bookShelf);
        if (useJavafxThread) {
            Platform.runLater(() -> displaySearchResults(bookShelf));
        } else {
            displaySearchResults(bookShelf);
        }
    }

    /**
     * Updates the model with the given search results and posts events to update the UI.
     */
    private void displaySearchResults(ReadOnlyBookShelf bookShelf) {
        model.updateSearchResults(bookShelf);
        model.setActiveListType(ActiveListType.SEARCH_RESULTS);
        EventsCenter.getInstance().post(new ActiveListChangedEvent());
        EventsCenter.getInstance().post(new NewResultAvailableEvent(
                String.format(SearchCommand.MESSAGE_SEARCH_SUCCESS, bookShelf.size())));
        EventsCenter.getInstance().post(new EnableCommandBoxRequestEvent());
    }

    @Override
    public boolean equals(Object other) {
        // short circuit if same object
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof SearchCommand)) {
            return false;
        }

        // state check
        SearchCommand e = (SearchCommand) other;
        return searchDescriptor.equals(e.searchDescriptor);
    }

    /**
     * Stores the parameters to search with.
     */
    public static class SearchDescriptor {
        private String keyWords;
        private String isbn;
        private String title;
        private String author;
        private String category;

        public SearchDescriptor() {}

        /**
         * Copy constructor.
         */
        public SearchDescriptor(SearchDescriptor toCopy) {
            this.keyWords = toCopy.keyWords;
            this.isbn = toCopy.isbn;
            this.title = toCopy.title;
            this.author = toCopy.author;
            this.category = toCopy.category;
        }

        /**
         * Returns true if at least one field is not empty.
         */
        public boolean isValid() {
            return CollectionUtil.isAnyNonNull(keyWords, isbn, title, author, category);
        }

        public Optional<String> getKeyWords() {
            return Optional.ofNullable(keyWords);
        }

        public void setKeyWords(String keyWords) {
            this.keyWords = keyWords;
        }

        public Optional<String> getIsbn() {
            return Optional.ofNullable(isbn);
        }

        public void setIsbn(String isbn) {
            this.isbn = isbn;
        }

        public Optional<String> getTitle() {
            return Optional.ofNullable(title);
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Optional<String> getAuthor() {
            return Optional.ofNullable(author);
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public Optional<String> getCategory() {
            return Optional.ofNullable(category);
        }

        public void setCategory(String category) {
            this.category = category;
        }

        /** Returns the search string to be used as part of the API url. */
        public String toSearchString() {
            StringBuilder builder = new StringBuilder();
            getKeyWords().ifPresent(searchTerm -> builder.append(searchTerm).append(" "));
            getIsbn().ifPresent(isbn -> builder.append("isbn:").append(isbn).append(" "));
            getTitle().ifPresent(title -> builder.append("intitle:").append(title).append(" "));
            getAuthor().ifPresent(author -> builder.append("inauthor:").append(author).append(" "));
            getCategory().ifPresent(category -> builder.append("subject:").append(category));
            return builder.toString().trim();
        }

        @Override
        public String toString() {
            return toSearchString();
        }

        @Override
        public boolean equals(Object other) {
            // short circuit if same object
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof SearchDescriptor)) {
                return false;
            }

            // state check
            SearchDescriptor e = (SearchDescriptor) other;

            return getKeyWords().equals(e.getKeyWords())
                    && getIsbn().equals(e.getIsbn())
                    && getTitle().equals(e.getTitle())
                    && getAuthor().equals(e.getAuthor())
                    && getCategory().equals(e.getCategory());
        }
    }
}
```
###### \java\seedu\address\logic\commands\ThemeCommand.java
``` java
/**
 * Changes the application theme.
 */
public class ThemeCommand extends Command {

    public static final String COMMAND_WORD = "theme";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Changes the theme used by the application.\n"
            + "Parameters: THEME_NAME\n"
            + "Example: " + COMMAND_WORD + " dark";

    public static final String MESSAGE_SUCCESS = "Application theme changed to: %1$s";
    public static final String MESSAGE_INVALID_THEME = "Invalid application theme: %1$s";

    private final Theme newTheme;

    public ThemeCommand(Theme newTheme) {
        requireNonNull(newTheme);
        this.newTheme = newTheme;
    }

    @Override
    public CommandResult execute() {
        EventsCenter.getInstance().post(new ChangeThemeRequestEvent(newTheme));
        return new CommandResult(String.format(MESSAGE_SUCCESS, newTheme.getThemeName()));
    }

```
###### \java\seedu\address\logic\parser\AddAliasCommandParser.java
``` java
/**
 * Parses input arguments and creates a new {@code AddAliasCommand} object.
 */
public class AddAliasCommandParser implements Parser<AddAliasCommand> {

    private static final Pattern ALIAS_FORMAT = Pattern.compile("^\\S+$");
    private static final Pattern COMMAND_FORMAT = Pattern.compile("(?<prefix>((?! \\w+\\/.*)[\\S ])+)(?<namedArgs>.*)");

    @Override
    public AddAliasCommand parse(String args) throws ParseException {
        requireNonNull(args);

        ArgumentMultimap argMultimap = createArgumentMultimap(args);
        String aliasName = getAliasName(argMultimap);
        Matcher matcher = createCommandMatcher(argMultimap);

        return new AddAliasCommand(createAlias(aliasName, matcher));
    }

    /**
     * Tokenizes the given {@code args} and returns an {@code ArgumentMultimap} containing the results.
     * @throws ParseException if {@code PREFIX_COMMAND} cannot be found.
     */
    private static ArgumentMultimap createArgumentMultimap(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_COMMAND);
        checkCommandFormat(argMultimap.getValue(PREFIX_COMMAND).isPresent());
        return argMultimap;
    }

    /**
     * Gets and returns the alias name from the {@code argMultimap}.
     * @throws ParseException if the alias name is not in the expected format.
     */
    private static String getAliasName(ArgumentMultimap argMultimap) throws ParseException {
        String aliasName = argMultimap.getPreamble().trim();
        checkCommandFormat(ALIAS_FORMAT.matcher(aliasName).matches());
        return aliasName;
    }

    /**
     * Creates and returns a {@code Matcher} containing information about the command to be aliased.
     * @throws ParseException if the command is not in the expected format.
     */
    private static Matcher createCommandMatcher(ArgumentMultimap argMultimap) throws ParseException {
        Matcher matcher = COMMAND_FORMAT.matcher(argMultimap.getValue(PREFIX_COMMAND).get());
        checkCommandFormat(matcher.matches());
        return matcher;
    }

    private static Alias createAlias(String aliasName, Matcher commandMatcher) {
        return new Alias(aliasName, commandMatcher.group("prefix"), commandMatcher.group("namedArgs"));
    }

    private static void checkCommandFormat(boolean condition) throws ParseException {
        if (!condition) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddAliasCommand.MESSAGE_USAGE));
        }
    }
}
```
###### \java\seedu\address\logic\parser\BookShelfParser.java
``` java
    /**
     * Used for pre-processing the user input, before the application of a command alias.
     */
    private static final Pattern ALIASED_COMMAND_FORMAT =
            Pattern.compile(" *(?<aliasName>\\S+)(?<unnamedArgs>((?! [\\w]+\\/.*)[\\S ])*)(?<namedArgs>.*)");

    private final ReadOnlyAliasList aliases;

    public BookShelfParser(ReadOnlyAliasList aliases) {
        requireNonNull(aliases);
        this.aliases = aliases;
    }

    /**
     * Applies a command alias (if any) to the user input, and returns the result.
     * If no command alias can be applied, the user input will be returned unchanged.
     *
     * @param userInput full user input string.
     * @return the processed input after the application of command alias.
     * @throws ParseException if the user input does not conform to the expected format.
     */
    public String applyCommandAlias(String userInput) throws ParseException {
        final Matcher matcher = getMatcherForPattern(userInput, ALIASED_COMMAND_FORMAT);

        final String aliasName = matcher.group("aliasName");
        if (!aliases.getAliasByName(aliasName).isPresent()) {
            return userInput;
        }

        return buildCommand(aliases.getAliasByName(aliasName).get(),
                matcher.group("unnamedArgs"), matcher.group("namedArgs"));
    }

    /**
     * Builds and returns a command string from the given alias, unnamed arguments, and named arguments.
     */
    private static String buildCommand(Alias alias, String unnamedArgs, String namedArgs) {
        return Stream.of(alias.getPrefix(), unnamedArgs, alias.getNamedArgs(), namedArgs)
                // keep trailing spaces as they may affect the command hint shown to user
                .map(StringUtil::leftTrim)
                .filter(str -> !str.trim().isEmpty())
                .reduce("", (a, b) -> StringUtil.leftTrim(a + " " + b));
    }

```
###### \java\seedu\address\logic\parser\SearchCommandParser.java
``` java
/**
 * Parses input arguments and creates a new SearchCommand object.
 */
public class SearchCommandParser implements Parser<SearchCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the SearchCommand
     * and returns a SearchCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format.
     */
    @Override
    public SearchCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_ISBN, PREFIX_TITLE, PREFIX_AUTHOR, PREFIX_CATEGORY);

        SearchDescriptor searchDescriptor = new SearchDescriptor();

        String keyWords = argMultimap.getPreamble();
        if (keyWords.trim().length() > 0) {
            searchDescriptor.setKeyWords(keyWords);
        }

        argMultimap.getValue(PREFIX_ISBN).ifPresent(searchDescriptor::setIsbn);
        argMultimap.getValue(PREFIX_TITLE).ifPresent(searchDescriptor::setTitle);
        argMultimap.getValue(PREFIX_AUTHOR).ifPresent(searchDescriptor::setAuthor);
        argMultimap.getValue(PREFIX_CATEGORY).ifPresent(searchDescriptor::setCategory);

        if (!searchDescriptor.isValid()) {
            throw new ParseException(SearchCommand.MESSAGE_EMPTY_QUERY);
        }

        return new SearchCommand(searchDescriptor);
    }
}
```
###### \java\seedu\address\model\alias\Alias.java
``` java
/**
 * Contains data about a single alias.
 * Guarantees: immutable.
 */
public class Alias {
    /**
     * A {@code Comparator} for aliases that sorts by name, in alphabetical order.
     */
    public static final Comparator<Alias> ALIAS_NAME_COMPARATOR = Comparator.comparing(Alias::getName);

    private final String name;
    private final String prefix;
    private final String namedArgs;

    /**
     * Creates an {@code Alias} with the specified {@code name}, {@code prefix}, and {@code namedArgs}.
     * All fields must be non-null.
     */
    public Alias(String name, String prefix, String namedArgs) {
        requireAllNonNull(name, prefix, namedArgs);
        this.name = name.trim().toLowerCase();
        this.prefix = prefix.trim();
        this.namedArgs = namedArgs.trim();
    }
```
###### \java\seedu\address\model\alias\UniqueAliasList.java
``` java
/**
 * Represents a unique collection of aliases. Does not allow nulls.
 *
 * Supports a minimal set of list operations.
 */
public class UniqueAliasList extends UniqueList<Alias> implements ReadOnlyAliasList {

    /**
     * Creates an empty {@code UniqueAliasList}.
     */
    public UniqueAliasList() {
        super();
    }

    /**
     * Creates a {@code UniqueAliasList} using the aliases in the {@code toBeCopied}.
     */
    public UniqueAliasList(ReadOnlyAliasList toBeCopied) {
        super();
        requireNonNull(toBeCopied);
        toBeCopied.getAliasList().forEach(this::add);
    }

    /**
     * Adds an alias to this list. If there is an existing alias
     * with the same name, that alias will be replaced.
     */
    @Override
    public void add(Alias toAdd) {
        requireNonNull(toAdd);
        getAliasByName(toAdd.getName()).ifPresent(internalList::remove);
        internalList.add(toAdd);
        assert CollectionUtil.elementsAreUnique(internalList);
    }

    /**
     * Removes the alias with the specified {@code name}.
     * Does nothing if no matching alias was found.
     */
    public void remove(String name) {
        requireNonNull(name);
        getAliasByName(name).ifPresent(internalList::remove);
    }

    @Override
    public Optional<Alias> getAliasByName(String name) {
        requireNonNull(name);
        return internalList.stream()
                .filter(alias -> name.trim().equalsIgnoreCase(alias.getName()))
                .findFirst();
    }

    @Override
    public ObservableList<Alias> getAliasList() {
        return asObservableList();
    }

    @Override
    public int size() {
        return internalList.size();
    }
}
```
###### \java\seedu\address\network\api\google\GoogleBooksApi.java
``` java
/**
 * Provides access to the Google Books API.
 */
public class GoogleBooksApi {

    protected static final String URL_SEARCH_BOOKS =
            "https://www.googleapis.com/books/v1/volumes?maxResults=40&printType=books&q=%s";
    protected static final String URL_BOOK_DETAILS = "https://www.googleapis.com/books/v1/volumes/%s";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final int HTTP_STATUS_OK = 200;
    private static final int MAX_SEARCH_RESULTS_COUNT = 30;

    private final HttpClient httpClient;
    private final JsonDeserializer deserializer;

    public GoogleBooksApi(HttpClient httpClient) {
        requireNonNull(httpClient);
        this.httpClient = httpClient;
        this.deserializer = new JsonDeserializer();
    }

    /**
     * Searches for books on Google Books that matches a set of parameters.
     *
     * @param parameters search parameters.
     * @return a CompletableFuture that resolves to a ReadOnlyBookShelf.
     */
    public CompletableFuture<ReadOnlyBookShelf> searchBooks(String parameters) {
        String requestUrl = String.format(URL_SEARCH_BOOKS, StringUtil.urlEncode(parameters));
        return executeGetAndApply(requestUrl, json ->
                deserializer.convertJsonStringToBookShelf(json, MAX_SEARCH_RESULTS_COUNT));
    }

    /**
     * Retrieves the details of a single book on Google Books.
     *
     * @param bookId the ID of the book on Google Books.
     * @return a CompletableFuture that resolves to a Book.
     */
    public CompletableFuture<Book> getBookDetails(String bookId) {
        String requestUrl = String.format(URL_BOOK_DETAILS, StringUtil.urlEncode(bookId));
        return executeGetAndApply(requestUrl, deserializer::convertJsonStringToBook);
    }

    /**
     * Asynchronously executes a HTTP GET request to the specified url and
     * applies the specified function to transform the resulting response body.
     *
     * @param url the url used for the GET request.
     * @param fn the function that will be applied on the response body of the GET request.
     * @param <T> the return type of the function to be applied.
     * @return a CompleteableFuture that resolves to the result of the given function.
     */
    private <T> CompletableFuture<T> executeGetAndApply(String url, Function<String, ? extends T> fn) {
        return httpClient
                .makeGetRequest(url)
                .thenApply(GoogleBooksApi::requireJsonContentType)
                .thenApply(GoogleBooksApi::requireHttpStatusOk)
                .thenApply(HttpResponse::getResponseBody)
                .thenApply(fn);
    }

    /**
     * Throws a {@link CompletionException} if the content type of the response is not JSON.
     */
    private static HttpResponse requireJsonContentType(HttpResponse response) {
        if (!response.getContentType().startsWith(CONTENT_TYPE_JSON)) {
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
###### \java\seedu\address\network\api\google\JsonBookDetails.java
``` java
/**
 * A temporary data holder used for deserialization of the JSON response
 * from the book details endpoint of Google Books API.
 */
public class JsonBookDetails {
    private int error = 0;
    private String id = "";
    private JsonVolumeInfo volumeInfo = new JsonVolumeInfo();

    // This should fail if the API returns an error, due to incompatible types.
    public void setError(int error) {
        assert false;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setVolumeInfo(JsonVolumeInfo volumeInfo) {
        this.volumeInfo = volumeInfo;
    }

    /**
     * Converts this data holder object into the model's Book object.
     */
    public Book toModelType() throws InvalidBookException {
        Isbn isbn = getIsbnFromIndustryIdentifiers(volumeInfo.industryIdentifiers);

        if (isbn == null) {
            throw new InvalidBookException("No ISBN is found for the book.");
        }

        return new Book(new Gid(id), isbn,
                BookDataUtil.getAuthorList(volumeInfo.authors), new Title(volumeInfo.title),
                getCategoryList(volumeInfo.categories), new Description(volumeInfo.description),
                new Publisher(volumeInfo.publisher),
                new PublicationDate(volumeInfo.publishedDate));
    }

    private static Isbn getIsbnFromIndustryIdentifiers(JsonIndustryIdentifiers[] iiArray) {
        return Stream.of(iiArray)
                .filter(ii -> ii.type.equals("ISBN_13"))
                .findFirst()
                .map(ii -> new Isbn(ii.identifier))
                .orElse(null);
    }

    private static List<Category> getCategoryList(String[] categories) {
        return Stream.of(categories)
                .flatMap(category -> Stream.of(category.split("/")))
                .map(token -> new Category(token.trim()))
                .collect(Collectors.toList());
    }

    /** Temporary data holder used for deserialization. */
    private static class JsonVolumeInfo {
        private String title = "";
        private String[] authors = new String[0];
        private String publisher = "";
        private String publishedDate = "";
        private String description = "";
        private JsonIndustryIdentifiers[] industryIdentifiers = new JsonIndustryIdentifiers[0];
        private String[] categories = new String[0];

```
###### \java\seedu\address\network\api\google\JsonDeserializer.java
``` java
/**
 * Provides utilities to deserialize JSON responses.
 */
public class JsonDeserializer {

    private static final Logger logger = LogsCenter.getLogger(JsonDeserializer.class);

    private final ObjectMapper mapper;

    JsonDeserializer() {
        mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Converts the JSON string from Google Books API into a book.
     */
    public Book convertJsonStringToBook(String json) {
        try {
            JsonBookDetails jsonBookDetails = mapper.readValue(json, JsonBookDetails.class);
            return jsonBookDetails.toModelType();
        } catch (IOException | InvalidBookException e) {
            logger.warning("Failed to convert JSON to book.");
            throw new CompletionException(e);
        }
    }

    /**
     * Converts the JSON string from Google Books API into a book shelf,
     * with the specified limit on the number of books to populate the book shelf with.
     */
    public ReadOnlyBookShelf convertJsonStringToBookShelf(String json, int maxBookCount) {
        assert maxBookCount >= 0;
        try {
            JsonSearchResults jsonSearchResults = mapper.readValue(json, JsonSearchResults.class);
            return jsonSearchResults.toModelType(maxBookCount);
        } catch (IOException e) {
            logger.warning("Failed to convert JSON to book shelf.");
            throw new CompletionException(e);
        }
    }
}
```
###### \java\seedu\address\network\api\google\JsonSearchResults.java
``` java
/**
 * A temporary data holder used for deserialization of the JSON response
 * from the book searching endpoint of Google Books API.
 */
public class JsonSearchResults {

    private static final Logger logger = LogsCenter.getLogger(JsonSearchResults.class);

    private int error = 0;
    private int totalItems = 0;
    private JsonVolume[] items = new JsonVolume[0];

    // This should fail if the API returns an error, due to incompatible types.
    public void setError(int error) {
        assert false;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public void setItems(JsonVolume[] items) {
        this.items = items;
    }

    /**
     * Converts this data holder object into the model's BookShelf object,
     * with the specified limit on the number of books to populate the book shelf with.
     */
    public ReadOnlyBookShelf toModelType(int maxBookCount) {
        BookShelf bookShelf = new BookShelf();

        int bookCount = 0;
        for (JsonVolume volume : items) {
            try {
                Book book = convertToBook(volume);
                bookShelf.addBook(book);
                ++bookCount;
                if (bookCount >= maxBookCount) {
                    break;
                }
            } catch (InvalidBookException | DuplicateBookException e) {
                logger.warning(e.getMessage());
            }
        }
        return bookShelf;
    }

    /**
     * Converts a JsonVolume into a Book.
     */
    private static Book convertToBook(JsonVolume volume) throws InvalidBookException {
        JsonVolumeInfo volumeInfo = volume.volumeInfo;
        Isbn isbn = getIsbnFromIndustryIdentifiers(volumeInfo.industryIdentifiers);

        if (isbn == null) {
            throw new InvalidBookException("Found book without ISBN");
        }

        return new Book(new Gid(volume.id), isbn,
                BookDataUtil.getAuthorList(volumeInfo.authors), new Title(volumeInfo.title),
                BookDataUtil.getCategoryList(volumeInfo.categories), new Description(volumeInfo.description),
                new Publisher(volumeInfo.publisher), new PublicationDate(volumeInfo.publishedDate));
    }

    private static Isbn getIsbnFromIndustryIdentifiers(JsonIndustryIdentifiers[] iiArray) {
        return Stream.of(iiArray)
                .filter(ii -> ii.type.equals("ISBN_13"))
                .findFirst()
                .map(ii -> new Isbn(ii.identifier))
                .orElse(null);
    }


    /** Temporary data holder used for deserialization. */
    private static class JsonVolume {
        private String id = "";
        private JsonVolumeInfo volumeInfo = new JsonVolumeInfo();

```
###### \java\seedu\address\network\api\google\JsonSearchResults.java
``` java
    /** Temporary data holder used for deserialization. */
    private static class JsonVolumeInfo {
        private String title = "";
        private String[] authors = new String[0];
        private String publisher = "";
        private String publishedDate = "";
        private String description = "";
        private JsonIndustryIdentifiers[] industryIdentifiers = new JsonIndustryIdentifiers[0];
        private String[] categories = new String[0];

```
###### \java\seedu\address\network\HttpClient.java
``` java
/**
 * A wrapper around the {@link AsyncHttpClient} class from async-http-client.
 */
public class HttpClient {

    private static final Logger logger = LogsCenter.getLogger(HttpClient.class);
    private static final int CONNECTION_TIMEOUT_MILLIS = 1000 * 10; // 10 seconds
    private static final int READ_TIMEOUT_MILLIS = 1000 * 10; // 10 seconds
    private static final int REQUEST_TIMEOUT_MILLIS = 1000 * 10; // 10 seconds
    private static final long DELAY_ON_EXCEPTION = 300L;

    private final AsyncHttpClient asyncHttpClient;

    protected HttpClient() {
        this.asyncHttpClient = Dsl.asyncHttpClient(Dsl.config()
                .setConnectTimeout(CONNECTION_TIMEOUT_MILLIS)
                .setReadTimeout(READ_TIMEOUT_MILLIS)
                .setRequestTimeout(REQUEST_TIMEOUT_MILLIS));
    }

    protected HttpClient(AsyncHttpClient asyncHttpClient) {
        requireNonNull(asyncHttpClient);
        this.asyncHttpClient = asyncHttpClient;
    }

    /**
     * Asynchronously executes a HTTP GET request to the specified url.
     */
    public CompletableFuture<HttpResponse> makeGetRequest(String url) {
        return asyncHttpClient
                .prepareGet(url)
                .execute()
                .toCompletableFuture()
                .thenApply(HttpResponse::new)
                .handleAsync(HttpClient::waitOnException);
    }

    /**
     * Waits for a short period of time in the event of exception.
     * Prevents API methods from completing before necessary pre-processing is completed.
     */
    private static <T> T waitOnException(T result, Throwable error) {
        if (error != null) {
            try {
                Thread.sleep(DELAY_ON_EXCEPTION);
            } catch (InterruptedException ie) {
                logger.warning(StringUtil.getDetails(ie));
            }
            throw new CompletionException(error);
        }
        return result;
    }

    /**
     * Stops and closes the underlying {@link AsyncHttpClient}.
     */
    public void close() {
        try {
            if (!asyncHttpClient.isClosed()) {
                asyncHttpClient.close();
            }
        } catch (IOException e) {
            logger.warning("Failed to shut down AsyncHttpClient.");
        }
    }
}
```
###### \java\seedu\address\network\HttpResponse.java
``` java
/**
 * A wrapper around the {@link Response} class from async-http-client.
 */
public class HttpResponse {

    private final int statusCode;
    private final String contentType;
    private final String responseBody;

    public HttpResponse(int statusCode, String contentType, String responseBody) {
        requireAllNonNull(contentType, responseBody);
        this.statusCode = statusCode;
        this.contentType = contentType;
        this.responseBody = responseBody;
    }

    public HttpResponse(Response response) {
        this(response.getStatusCode(), response.getContentType(), response.getResponseBody());
    }

```
###### \java\seedu\address\network\Network.java
``` java
/**
 * The API of the Network component.
 */
public interface Network {

    /**
     * Searches for books on Google Books that matches a set of parameters.
     *
     * @param parameters search parameters.
     * @return a CompletableFuture that resolves to a ReadOnlyBookShelf.
     */
    CompletableFuture<ReadOnlyBookShelf> searchBooks(String parameters);

    /**
     * Retrieves the details of a single book on Google Books.
     *
     * @param bookId the ID of the book on Google Books.
     * @return a CompletableFuture that resolves to a Book.
     */
    CompletableFuture<Book> getBookDetails(String bookId);

    /**
     * Searches for a book in the library.
     *
     * @param book book to search for.
     * @return CompletableFuture that resolves to a String with the search results.
     */
    CompletableFuture<String> searchLibraryForBook(Book book);

    /**
     * Stops the Network component.
     */
    void stop();
}
```
###### \java\seedu\address\network\NetworkManager.java
``` java
/**
 * Provides networking functionality (making web API calls).
 *
 * The methods on this class (except {@code stop()}) are asynchronous, and returns a
 * {@code CompletableFuture} that will resolve to the desired object or data.
 */
public class NetworkManager extends ComponentManager implements Network {

    private static final Logger logger = LogsCenter.getLogger(NetworkManager.class);

    private final HttpClient httpClient;
    private final GoogleBooksApi googleBooksApi;
    private final NlbCatalogueApi nlbCatalogueApi;

    public NetworkManager() {
        super();
        httpClient = new HttpClient();
        googleBooksApi = new GoogleBooksApi(httpClient);
        nlbCatalogueApi = new NlbCatalogueApi(httpClient);
    }

    protected NetworkManager(HttpClient httpClient, GoogleBooksApi googleBooksApi, NlbCatalogueApi nlbCatalogueApi) {
        super();
        requireAllNonNull(httpClient, googleBooksApi, nlbCatalogueApi);
        this.httpClient = httpClient;
        this.googleBooksApi = googleBooksApi;
        this.nlbCatalogueApi = nlbCatalogueApi;
    }

    @Override
    public CompletableFuture<ReadOnlyBookShelf> searchBooks(String parameters) {
        return googleBooksApi.searchBooks(parameters)
                .thenApply(bookShelf -> {
                    logger.info("Search books succeeded: " + parameters);
                    return bookShelf;
                })
                .exceptionally(e -> {
                    logger.warning("Search books failed: " + StringUtil.getDetails(e));
                    throw new CompletionException(e);
                });
    }

    @Override
    public CompletableFuture<Book> getBookDetails(String bookId) {
        return googleBooksApi.getBookDetails(bookId)
                .thenApply(book -> {
                    logger.info("Get book details succeeded: " + bookId);
                    return book;
                })
                .exceptionally(e -> {
                    logger.warning("Get book details failed: " + StringUtil.getDetails(e));
                    throw new CompletionException(e);
                });
    }

    @Override
    public CompletableFuture<String> searchLibraryForBook(Book book) {
        return nlbCatalogueApi.searchForBook(book)
                .thenApply(result -> {
                    logger.info("Search books in library succeeded: " + book);
                    return result;
                })
                .exceptionally(e -> {
                    logger.warning("Search books in library failed: " + StringUtil.getDetails(e));
                    throw new CompletionException(e);
                });
    }

    @Override
    public void stop() {
        httpClient.close();
    }

}
```
###### \resources\view\BookDetailsPanel.fxml
``` fxml
<StackPane fx:id="bookDetailsPane" xmlns="http://javafx.com/javafx/8" xmlns:fx="http://javafx.com/fxml/1">
  <padding>
    <Insets bottom="10" left="5" right="10" top="10"/>
  </padding>
  <ScrollPane fx:id="scrollPane" fitToWidth="true" hbarPolicy="NEVER" styleClass="anchor-pane">
    <VBox id="bookDetailsPane" styleClass="content-pane">
      <Label fx:id="title" styleClass="label-header" text="\$title" wrapText="true" VBox.vgrow="SOMETIMES"/>
      <BorderPane>
        <left>
          <ImageView fx:id="frontCover" fitHeight="210.0" fitWidth="128.0" pickOnBounds="true"
                     preserveRatio="true" BorderPane.alignment="CENTER">
            <BorderPane.margin>
              <Insets left="4.0" right="10.0"/>
            </BorderPane.margin>
          </ImageView>
        </left>
        <center>
          <VBox BorderPane.alignment="CENTER">
            <FlowPane styleClass="pills">
              <Label fx:id="status"/>
              <Label fx:id="priority"/>
              <Label fx:id="rating"/>
              <VBox.margin>
                <Insets top="3.0"/>
              </VBox.margin>
            </FlowPane>
            <GridPane BorderPane.alignment="BOTTOM_CENTER">
              <columnConstraints>
                <ColumnConstraints hgrow="SOMETIMES" maxWidth="160.0" minWidth="130.0" prefWidth="130.0"/>
                <ColumnConstraints hgrow="SOMETIMES" maxWidth="350.0" minWidth="10.0" prefWidth="280.0"/>
              </columnConstraints>
              <rowConstraints>
                <RowConstraints minHeight="30.0" prefHeight="30.0" vgrow="SOMETIMES"/>
                <RowConstraints minHeight="30.0" vgrow="SOMETIMES"/>
                <RowConstraints minHeight="30.0" vgrow="SOMETIMES"/>
                <RowConstraints minHeight="30.0" prefHeight="30.0" vgrow="SOMETIMES"/>
                <RowConstraints minHeight="30.0" prefHeight="30.0" vgrow="SOMETIMES"/>
              </rowConstraints>
              <children>
                <Label styleClass="label-muted" text="ISBN"/>
                <Label styleClass="label-muted" text="Authors" GridPane.rowIndex="1"/>
                <Label styleClass="label-muted" text="Categories" GridPane.rowIndex="2"/>
                <Label styleClass="label-muted" text="Publisher" GridPane.rowIndex="3"/>
                <Label styleClass="label-muted" text="Publication Date" GridPane.rowIndex="4"/>
                <Label fx:id="isbn" text="\$isbn" GridPane.columnIndex="1"/>
                <FlowPane fx:id="authors" alignment="CENTER_LEFT" hgap="7.0" nodeOrientation="LEFT_TO_RIGHT"
                          styleClass="pills-authors" vgap="5.0" GridPane.columnIndex="1" GridPane.rowIndex="1"
                          GridPane.vgrow="SOMETIMES">
                </FlowPane>
                <FlowPane fx:id="categories" alignment="CENTER_LEFT" nodeOrientation="LEFT_TO_RIGHT"
                          styleClass="pills-categories" GridPane.columnIndex="1" GridPane.rowIndex="2"
                          GridPane.vgrow="SOMETIMES">
                </FlowPane>
                <Label fx:id="publisher" text="\$publisher" GridPane.columnIndex="1" GridPane.rowIndex="3"/>
                <Label fx:id="publicationDate" text="\$publicationDate" GridPane.columnIndex="1" GridPane.rowIndex="4"/>
              </children>
              <VBox.margin>
                <Insets top="8.0"/>
              </VBox.margin>
            </GridPane>
          </VBox>
        </center>
        <VBox.margin>
          <Insets top="8.0" bottom="5.0"/>
        </VBox.margin>
      </BorderPane>
      <StackPane fx:id="descriptionPlaceholder" VBox.vgrow="NEVER"/>
    </VBox>
  </ScrollPane>
</StackPane>
```
###### \resources\view\WelcomePanel.fxml
``` fxml
<StackPane fx:id="welcomePanel" xmlns="http://javafx.com/javafx/8" xmlns:fx="http://javafx.com/fxml/1">
  <ScrollPane fitToHeight="true" fitToWidth="true" hbarPolicy="NEVER">
    <content>
      <BorderPane fx:id="welcomeContainer">
        <center>
          <VBox alignment="CENTER" BorderPane.alignment="CENTER">
            <ImageView fx:id="appLogo" fitHeight="128.0" fitWidth="128.0" pickOnBounds="true" preserveRatio="true">
              <Image url="@/images/book_flat_128.png"/>
              <VBox.margin>
                <Insets bottom="10"/>
              </VBox.margin>
            </ImageView>
            <Label styleClass="label-subheading" text="Bibliotek"/>
          </VBox>
        </center>
        <bottom>
          <GridPane>
            <columnConstraints>
              <ColumnConstraints minWidth="10.0" percentWidth="15.0"/>
              <ColumnConstraints minWidth="10.0" percentWidth="70.0"/>
              <ColumnConstraints minWidth="10.0" percentWidth="15.0"/>
            </columnConstraints>
            <rowConstraints>
              <RowConstraints fillHeight="false" vgrow="ALWAYS"/>
            </rowConstraints>
            <Label fx:id="qotd" alignment="CENTER" maxWidth="400" styleClass="qotd" text="\$qotd" textAlignment="CENTER"
                   wrapText="true" GridPane.columnIndex="1" GridPane.halignment="CENTER" GridPane.hgrow="ALWAYS"
                   GridPane.valignment="CENTER" GridPane.vgrow="NEVER">
              <padding>
                <Insets bottom="12.0"/>
              </padding>
            </Label>
          </GridPane>
        </bottom>
      </BorderPane>
    </content>
  </ScrollPane>
  <padding>
    <Insets bottom="10" left="5" right="10" top="10"/>
  </padding>
</StackPane>
```
