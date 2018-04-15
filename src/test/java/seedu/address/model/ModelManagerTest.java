package seedu.address.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_BOOKS;
import static seedu.address.testutil.TypicalBooks.ARTEMIS;
import static seedu.address.testutil.TypicalBooks.BABYLON_ASHES;
import static seedu.address.testutil.TypicalBooks.getTypicalBookShelf;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import seedu.address.logic.LockManager;
import seedu.address.model.alias.Alias;
import seedu.address.testutil.BookShelfBuilder;

public class ModelManagerTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @After
    public void tearDown() {
        LockManager.getInstance().initialize(LockManager.NO_PASSWORD);
    }

    @Test
    public void constructor_isPasswordProtected_hideAllBooks() {
        LockManager.getInstance().initialize("invalid$11$11$invalid$invalid");
        ModelManager modelManager = new ModelManager(getTypicalBookShelf(), new UserPrefs());
        assertEquals(0, modelManager.getDisplayBookList().size());
    }

    @Test
    public void getDisplayBookList_modifyList_throwsUnsupportedOperationException() {
        ModelManager modelManager = new ModelManager();
        thrown.expect(UnsupportedOperationException.class);
        modelManager.getDisplayBookList().remove(0);
    }

    @Test
    public void getRecentBooksList_modifyList_throwsUnsupportedOperationException() {
        ModelManager modelManager = new ModelManager();
        thrown.expect(UnsupportedOperationException.class);
        modelManager.getRecentBooksList().remove(0);
    }

    @Test
    public void getDisplayAliasList_modifyList_throwsUnsupportedOperationException() {
        ModelManager modelManager = new ModelManager();
        thrown.expect(UnsupportedOperationException.class);
        modelManager.getDisplayAliasList().remove(0);
    }

    @Test
    public void equals() {
        BookShelf bookShelf = new BookShelfBuilder().withBook(ARTEMIS).withBook(BABYLON_ASHES).build();
        BookShelf differentBookShelf = new BookShelf();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        ModelManager modelManager = new ModelManager(bookShelf, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(bookShelf, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same values -> returns true
        modelManager.updateSearchResults(bookShelf);
        modelManagerCopy.updateSearchResults(bookShelf);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same values -> returns true
        modelManager.addRecentBook(ARTEMIS);
        modelManagerCopy.addRecentBook(ARTEMIS);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different bookShelf -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentBookShelf, userPrefs)));

        // different searchResults -> returns false
        assertFalse(modelManager.equals(new ModelManager(bookShelf, userPrefs)));

        // different recentBooks -> returns false
        modelManagerCopy.addRecentBook(BABYLON_ASHES);
        assertFalse(modelManager.equals(modelManagerCopy));

        // different alias list -> returns false
        modelManagerCopy = new ModelManager(bookShelf, userPrefs);
        modelManagerCopy.addAlias(new Alias("1", "1", "1"));
        assertFalse(modelManager.equals(modelManagerCopy));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateBookListFilter(PREDICATE_SHOW_ALL_BOOKS);

        // different userPrefs -> returns true
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setBookShelfName("differentName");
        ModelManager modelManagerDiffPrefs = new ModelManager(bookShelf, differentUserPrefs);
        assertTrue(modelManagerDiffPrefs.equals(new ModelManager(bookShelf, new UserPrefs())));
    }
}
