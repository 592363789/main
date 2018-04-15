package seedu.address.network;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import seedu.address.model.BookShelf;
import seedu.address.model.ReadOnlyBookShelf;
import seedu.address.model.book.Book;
import seedu.address.network.api.google.GoogleBooksApi;
import seedu.address.network.library.NlbCatalogueApi;
import seedu.address.testutil.TestUtil;
import seedu.address.testutil.TypicalBooks;

//@@author takuyakanbr
public class NetworkManagerTest {
    private static final String PARAM_EMPTY_RESULT = "1";
    private static final String PARAM_SUCCESS = "12345";
    private static final String PARAM_FAILURE = "failure";

    private static final Book BOOK_SUCESS = TypicalBooks.ARTEMIS;
    private static final String BOOK_SUCCESS_RESULT = "Success";
    private static final Book BOOK_FAILURE = TypicalBooks.BABYLON_ASHES;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private NetworkManager networkManager;
    private GoogleBooksApi mockGoogleBooksApi;
    private NlbCatalogueApi mockNlbCatalogueApi;

    @Before
    public void setUp() {
        mockGoogleBooksApi = mock(GoogleBooksApi.class);
        mockNlbCatalogueApi = mock(NlbCatalogueApi.class);
        networkManager = new NetworkManager(mock(HttpClient.class), mockGoogleBooksApi, mockNlbCatalogueApi);
    }

    @Test
    public void handleGoogleApiSearchRequestEvent_emptyResult() throws Exception {
        when(mockGoogleBooksApi.searchBooks(PARAM_EMPTY_RESULT))
                .thenReturn(CompletableFuture.completedFuture(new BookShelf()));

        ReadOnlyBookShelf bookShelf = networkManager.searchBooks(PARAM_EMPTY_RESULT).get();

        verify(mockGoogleBooksApi).searchBooks(PARAM_EMPTY_RESULT);
        assertEquals(0, bookShelf.size());
    }

    @Test
    public void handleGoogleApiSearchRequestEvent_success() throws Exception {
        when(mockGoogleBooksApi.searchBooks(PARAM_SUCCESS))
                .thenReturn(CompletableFuture.completedFuture(TypicalBooks.getTypicalBookShelf()));

        ReadOnlyBookShelf bookShelf = networkManager.searchBooks(PARAM_SUCCESS).get();

        verify(mockGoogleBooksApi).searchBooks(PARAM_SUCCESS);
        assertEquals(5, bookShelf.size());
    }

    @Test
    public void handleGoogleApiSearchRequestEvent_failure() throws Exception {
        when(mockGoogleBooksApi.searchBooks(PARAM_FAILURE))
                .thenReturn(TestUtil.getFailedFuture());

        CompletableFuture<ReadOnlyBookShelf> bookShelf = networkManager.searchBooks(PARAM_FAILURE);
        verify(mockGoogleBooksApi).searchBooks(PARAM_FAILURE);

        thrown.expect(ExecutionException.class);
        bookShelf.get();
    }

    @Test
    public void handleGoogleApiBookDetailsRequestEvent_success() throws Exception {
        when(mockGoogleBooksApi.getBookDetails(PARAM_SUCCESS))
                .thenReturn(CompletableFuture.completedFuture(TypicalBooks.ARTEMIS));

        Book book = networkManager.getBookDetails(PARAM_SUCCESS).get();

        verify(mockGoogleBooksApi).getBookDetails(PARAM_SUCCESS);
        assertEquals(TypicalBooks.ARTEMIS, book);
    }

    @Test
    public void handleGoogleApiBookDetailsRequestEvent_failure() throws Exception {
        when(mockGoogleBooksApi.getBookDetails(PARAM_FAILURE))
                .thenReturn(TestUtil.getFailedFuture());

        CompletableFuture<Book> book = networkManager.getBookDetails(PARAM_FAILURE);
        verify(mockGoogleBooksApi).getBookDetails(PARAM_FAILURE);

        thrown.expect(ExecutionException.class);
        book.get();
    }

    //@@author qiu-siqi
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
        when(mockNlbCatalogueApi.searchForBook(BOOK_FAILURE)).thenReturn(TestUtil.getFailedFuture());

        CompletableFuture<String> result = networkManager.searchLibraryForBook(BOOK_FAILURE);
        verify(mockNlbCatalogueApi).searchForBook(BOOK_FAILURE);

        thrown.expect(ExecutionException.class);
        result.get();
    }
}
