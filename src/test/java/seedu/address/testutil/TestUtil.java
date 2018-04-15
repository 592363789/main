package seedu.address.testutil;

import static seedu.address.testutil.TypicalBooks.getTypicalBookShelf;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.FileUtil;
import seedu.address.commons.util.StringUtil;
import seedu.address.model.ActiveListType;
import seedu.address.model.BookShelf;
import seedu.address.model.Model;
import seedu.address.model.book.Book;

/**
 * A utility class for test cases.
 */
public class TestUtil {

    /**
     * Folder used for temp files created during testing. Ignored by Git.
     */
    private static final String SANDBOX_FOLDER = FileUtil.getPath("./build/tmp/sandbox/");
    private static final String PREFIX_SEPARATOR = "_";

    /**
     * Appends {@code fileName} to the sandbox folder path and returns the resulting string.
     * Creates the sandbox folder if it doesn't exist.
     */
    public static String getFilePathInSandboxFolder(String fileName) {
        try {
            FileUtil.createDirs(new File(SANDBOX_FOLDER));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String randomizedFileName = StringUtil.generateRandomPrefix() + PREFIX_SEPARATOR + fileName;
        return SANDBOX_FOLDER + randomizedFileName;
    }

    /**
     * Returns the middle index of the book in the {@code model}'s display book list.
     */
    public static Index getMidIndex(Model model) {
        return Index.fromOneBased(model.getDisplayBookList().size() / 2);
    }

    /**
     * Returns the last index of the book in the {@code model}'s display book list.
     */
    public static Index getLastIndex(Model model) {
        return Index.fromOneBased(model.getDisplayBookList().size());
    }

    /**
     * Returns the book in the {@code model}'s display book list at {@code index}.
     */
    public static Book getBook(Model model, Index index) {
        return model.getDisplayBookList().get(index.getZeroBased());
    }

    //@@author takuyakanbr
    /**
     * Returns a {@link CompletableFuture} that has already completed exceptionally
     * with an {@code IOException}.
     */
    public static <T> CompletableFuture<T> getFailedFuture() {
        return CompletableFuture.completedFuture(null).thenApply(obj -> {
            throw new CompletionException(new IOException());
        });
    }

    //@@author qiu-siqi
    /**
     * Sets up {@code model} with a non-empty search result list and
     * switches active list to search results list.
     */
    public static void prepareSearchResultListInModel(Model model) {
        model.setActiveListType(ActiveListType.SEARCH_RESULTS);
        BookShelf bookShelf = getTypicalBookShelf();
        model.updateSearchResults(bookShelf);
    }

    /**
     * Sets up {@code model} with a non-empty recently selected books list and
     * switches active list to recent books list.
     */
    public static void prepareRecentBooksListInModel(Model model) {
        model.setActiveListType(ActiveListType.RECENT_BOOKS);
        BookShelf bookShelf = getTypicalBookShelf();
        bookShelf.getBookList().forEach(model::addRecentBook);
    }
}
