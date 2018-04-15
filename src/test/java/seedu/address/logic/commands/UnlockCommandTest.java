package seedu.address.logic.commands;

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

//@@author 592363789
public class UnlockCommandTest {

    private static final String DEFAULT_PW = "testing";
    private Model model;

    @Before
    public void setUp() {
        model = new ModelManager(getTypicalBookShelf(), new UserPrefs());
        // set password + lock
        LockManager.getInstance().initialize(LockManager.NO_PASSWORD);
        LockManager.getInstance().setPassword(LockManager.NO_PASSWORD, DEFAULT_PW);
        LockManager.getInstance().lock();
    }

    @After
    public void tearDown() {
        LockManager.getInstance().initialize(LockManager.NO_PASSWORD);
    }

    @Test
    public void equals() {

        UnlockCommand unlockCommand = new UnlockCommand(DEFAULT_PW);

        UnlockCommand sameCommand = new UnlockCommand(DEFAULT_PW);

        // same value -> returns true
        assertTrue(unlockCommand.equals(sameCommand));

        // same object -> returns true
        assertTrue(unlockCommand.equals(unlockCommand));

        // null -> returns false
        assertFalse(unlockCommand.equals(null));

        // different commandtypes -> returns false
        assertFalse(unlockCommand.equals(new ClearCommand()));

        // different types -> return false
        assertFalse(unlockCommand.equals(0));

    }

    @Test
    public void samePasswordTest() {
        CommandResult commandResult = prepareUnlockCommand(DEFAULT_PW).execute();

        assertEquals(UnlockCommand.MESSAGE_SUCCESS, commandResult.feedbackToUser);
    }

    @Test
    public void wrongPasswordTest() {
        CommandResult commandResult = prepareUnlockCommand("").execute();

        assertEquals(UnlockCommand.MESSAGE_WRONG_PASSWORD, commandResult.feedbackToUser);
    }

    @Test
    public void differentPasswordTest() {
        CommandResult commandResult = prepareUnlockCommand(DEFAULT_PW + "x").execute();

        assertEquals(UnlockCommand.MESSAGE_WRONG_PASSWORD, commandResult.feedbackToUser);
    }

    @Test
    public void notLockedTest() {
        LockManager.getInstance().unlock(DEFAULT_PW);
        CommandResult commandResult = prepareUnlockCommand(DEFAULT_PW).execute();

        assertEquals(UnlockCommand.MESSAGE_NOT_LOCKED, commandResult.feedbackToUser);
    }

    private UnlockCommand prepareUnlockCommand(String key) {
        UnlockCommand unlock = new UnlockCommand(key);
        unlock.setData(model, mock(Network.class), new CommandHistory(), new UndoStack());
        return unlock;
    }

}
