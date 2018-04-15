package seedu.address.logic.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static seedu.address.logic.commands.CommandTestUtil.deleteFirstBook;
import static seedu.address.logic.commands.CommandTestUtil.showBookAtIndex;
import static seedu.address.testutil.TypicalBooks.getTypicalBookShelf;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_BOOK;

import org.junit.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.book.Book;
import seedu.address.model.book.exceptions.BookNotFoundException;
import seedu.address.model.book.exceptions.DuplicateBookException;

public class UndoableCommandTest {
    private final Model model = new ModelManager(getTypicalBookShelf(), new UserPrefs());
    private final DummyCommand dummyCommand = new DummyCommand(model);

    private Model expectedModel = new ModelManager(getTypicalBookShelf(), new UserPrefs());

    @Test
    public void executeUndo() throws Exception {
        dummyCommand.execute();
        deleteFirstBook(expectedModel);
        assertEquals(expectedModel, model);

        showBookAtIndex(model, INDEX_FIRST_BOOK);

        dummyCommand.undo();
        expectedModel = new ModelManager(getTypicalBookShelf(), new UserPrefs());
        assertEquals(expectedModel, model);
    }

    /**
     * Deletes the first person in the model's filtered list.
     */
    class DummyCommand extends UndoableCommand {
        private Book bookToDelete;

        DummyCommand(Model model) {
            this.model = model;
        }

        @Override
        protected String undo() {
            try {
                model.addBook(bookToDelete);
                return "Success";
            } catch (DuplicateBookException e) {
                fail("Impossible: bookToDelete was deleted.");
                return "Failure";
            }
        }

        @Override
        public CommandResult executeUndoableCommand() {
            bookToDelete = model.getDisplayBookList().get(0);
            try {
                model.deleteBook(bookToDelete);
            } catch (BookNotFoundException pnfe) {
                fail("Impossible: bookToDelete was retrieved from model.");
            }
            return new CommandResult("");
        }
    }
}
