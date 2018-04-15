package seedu.address.logic.commands;
//@@author 592363789
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static seedu.address.testutil.TypicalBooks.getTypicalBookShelf;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import seedu.address.logic.CommandHistory;
import seedu.address.logic.LockManager;
import seedu.address.logic.UndoStack;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.network.Network;

public class LockCommandTest {

    private Model model;

    @Before
    public void setUp() {
        model = new ModelManager(getTypicalBookShelf(), new UserPrefs());
    }

    @After
    public void tearDown() {
        LockManager.getInstance().initialize(LockManager.NO_PASSWORD);
    }

    @Test
    public void equals() {
        LockCommand lockCommand = new LockCommand();

        // same object -> returns true
        assertTrue(lockCommand.equals(lockCommand));

        // null -> returns false
        assertFalse(lockCommand.equals(null));

        // different commandtypes -> returns false
        assertFalse(lockCommand.equals(new ClearCommand()));

        // different types -> return false
        assertFalse(lockCommand.equals(0));
    }

    @Test
    public void execute_success() {
        LockCommand lockCommand = new LockCommand();
        lockCommand.setData(model, mock(Network.class), new CommandHistory(), new UndoStack());
        CommandResult result = lockCommand.execute();
        assertEquals(LockCommand.MESSAGE_SUCCESS, result.feedbackToUser);
        assertTrue(LockManager.getInstance().isLocked());
    }
}
