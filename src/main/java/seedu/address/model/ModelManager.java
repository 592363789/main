package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.model.alias.Alias.ALIAS_NAME_COMPARATOR;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import seedu.address.commons.core.ComponentManager;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.events.model.AliasListChangedEvent;
import seedu.address.commons.events.model.BookShelfChangedEvent;
import seedu.address.logic.LockManager;
import seedu.address.model.alias.Alias;
import seedu.address.model.alias.ReadOnlyAliasList;
import seedu.address.model.alias.UniqueAliasList;
import seedu.address.model.book.Book;
import seedu.address.model.book.UniqueBookCircularList;
import seedu.address.model.book.exceptions.BookNotFoundException;
import seedu.address.model.book.exceptions.DuplicateBookException;

/**
 * Represents the in-memory model of the book shelf data.
 * All changes to any model should be synchronized.
 */
public class ModelManager extends ComponentManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private ActiveListType activeListType;
    private final BookShelf bookShelf;
    private final FilteredList<Book> filteredBookList;
    private final SortedList<Book> sortedBookList;
    private final ObservableList<Book> displayBookList;
    private final BookShelf searchResults;
    private UniqueBookCircularList recentBooks;
    private final UniqueAliasList aliases;
    private final ObservableList<Alias> displayAliasList;

    /**
     * Initializes a ModelManager with the given bookShelf, userPrefs, recentBooksList, and aliasList.
     */
    public ModelManager(ReadOnlyBookShelf bookShelf, UserPrefs userPrefs,
                        ReadOnlyBookShelf recentBooksList, ReadOnlyAliasList aliasList) {
        super();
        requireAllNonNull(bookShelf, userPrefs, recentBooksList);

        logger.fine("Initializing with book shelf: " + bookShelf + " and user prefs " + userPrefs
                + " and recent books: " + recentBooksList);

        this.activeListType = ActiveListType.BOOK_SHELF;
        this.bookShelf = new BookShelf(bookShelf);
        this.filteredBookList = new FilteredList<>(this.bookShelf.getBookList(), PREDICATE_SHOW_ALL_BOOKS);
        this.sortedBookList = new SortedList<>(this.filteredBookList, DEFAULT_BOOK_COMPARATOR);
        this.displayBookList = sortedBookList;
        this.searchResults = new BookShelf();
        this.recentBooks = new UniqueBookCircularList(recentBooksList.getBookList());
        this.aliases = new UniqueAliasList(aliasList);
        this.displayAliasList = new SortedList<>(this.aliases.asObservableList(), ALIAS_NAME_COMPARATOR);

        if (LockManager.getInstance().isPasswordProtected()) {
            updateBookListFilter(PREDICATE_HIDE_ALL_BOOKS);
        }
    }

    public ModelManager(ReadOnlyBookShelf bookShelf, UserPrefs userPrefs) {
        this(bookShelf, userPrefs, new BookShelf(), new UniqueAliasList());
    }

    public ModelManager() {
        this(new BookShelf(), new UserPrefs());
    }

    @Override
    public ActiveListType getActiveListType() {
        return activeListType;
    }

    @Override
    public void setActiveListType(ActiveListType type) {
        this.activeListType = type;
    }

    @Override
    public ObservableList<Book> getActiveList() {
        switch (activeListType) {
        case BOOK_SHELF: {
            return getDisplayBookList();
        }
        case SEARCH_RESULTS: {
            return getSearchResultsList();
        }
        case RECENT_BOOKS: {
            return getRecentBooksList();
        }
        default:
            // Should never end up here
            return getDisplayBookList();
        }
    }

    @Override
    public void resetData(ReadOnlyBookShelf newData) {
        bookShelf.resetData(newData);
        indicateBookShelfChanged();
    }

    @Override
    public ReadOnlyBookShelf getBookShelf() {
        return bookShelf;
    }

    /** Raises an event to indicate the book shelf has changed */
    private void indicateBookShelfChanged() {
        raise(new BookShelfChangedEvent(bookShelf));
    }

    @Override
    public void deleteBook(Book target) throws BookNotFoundException {
        bookShelf.removeBook(target);
        indicateBookShelfChanged();
    }

    @Override
    public void addBook(Book book) throws DuplicateBookException {
        bookShelf.addBook(book);
        updateBookListFilter(PREDICATE_SHOW_ALL_BOOKS);
        updateBookListSorter(DEFAULT_BOOK_COMPARATOR);
        indicateBookShelfChanged();
    }

    @Override
    public void updateBook(Book target, Book editedBook) throws BookNotFoundException, DuplicateBookException {
        requireAllNonNull(target, editedBook);

        bookShelf.updateBook(target, editedBook);
        indicateBookShelfChanged();
    }

    //=========== Display Book List Accessors ==============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Book} backed by the internal list of
     * {@code bookShelf}.
     */
    @Override
    public ObservableList<Book> getDisplayBookList() {
        return FXCollections.unmodifiableObservableList(displayBookList);
    }

    @Override
    public Predicate<? super Book> getBookListFilter() {
        return filteredBookList.getPredicate();
    }

    @Override
    public Comparator<? super Book> getBookListSorter() {
        return sortedBookList.getComparator();
    }

    @Override
    public void updateBookListFilter(Predicate<? super Book> predicate) {
        requireNonNull(predicate);
        filteredBookList.setPredicate(predicate);
    }

    @Override
    public void updateBookListSorter(Comparator<? super Book> comparator) {
        requireNonNull(comparator);
        sortedBookList.setComparator(comparator);
    }

    //=========== Search Results ===========================================================================

    /**
     * Returns an unmodifable view of the list of {@code Book} backed by the internal list of {@code searchResults}.
     * */
    @Override
    public ObservableList<Book> getSearchResultsList() {
        return FXCollections.unmodifiableObservableList(searchResults.getBookList());
    }

    @Override
    public void updateSearchResults(ReadOnlyBookShelf newResults) {
        searchResults.resetData(newResults);
    }

    //=========== Recent books ===========================================================================

    @Override
    public ObservableList<Book> getRecentBooksList() {
        return FXCollections.unmodifiableObservableList(recentBooks.asObservableList());
    }

    @Override
    public void resetRecentBooks(ReadOnlyBookShelf newData) {
        recentBooks = new UniqueBookCircularList(newData.getBookList());
    }

    public ReadOnlyBookShelf getRecentBooksListAsBookShelf() {
        BookShelf bookShelf = new BookShelf();
        try {
            bookShelf.setBooks(getRecentBooksList());
        } catch (DuplicateBookException e) {
            logger.warning("Should never throw DuplicateBookException");
        }
        return bookShelf;
    }

    @Override
    public void addRecentBook(Book newBook) {
        requireNonNull(newBook);
        recentBooks.addToFront(newBook);
    }

    //=========== Alias List =============================================================================

    @Override
    public ReadOnlyAliasList getAliasList() {
        return aliases;
    }

    @Override
    public ObservableList<Alias> getDisplayAliasList() {
        return FXCollections.unmodifiableObservableList(displayAliasList);
    }

    @Override
    public void addAlias(Alias alias) {
        aliases.add(alias);
        indicateAliasListChanged();
    }

    @Override
    public Optional<Alias> getAlias(String name) {
        return aliases.getAliasByName(name);
    }

    @Override
    public void removeAlias(String name) {
        aliases.remove(name);
        indicateAliasListChanged();
    }

    /** Raises an event to indicate the alias list has changed */
    private void indicateAliasListChanged() {
        raise(new AliasListChangedEvent(aliases));
    }

    @Override
    public boolean equals(Object obj) {
        // short circuit if same object
        if (obj == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(obj instanceof ModelManager)) {
            return false;
        }

        // state check
        ModelManager other = (ModelManager) obj;
        return bookShelf.equals(other.bookShelf)
                && displayBookList.equals(other.displayBookList)
                && searchResults.equals(other.searchResults)
                && recentBooks.equals(other.recentBooks)
                && aliases.equals(other.aliases);
    }

}
