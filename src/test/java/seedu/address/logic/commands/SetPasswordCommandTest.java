package seedu.address.logic.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static seedu.address.testutil.TypicalBooks.getTypicalBookShelf;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import seedu.address.logic.CommandHistory;
import seedu.address.logic.LockManager;
import seedu.address.logic.UndoStack;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.network.NetworkManager;

//@@author 592363789
public class SetPasswordCommandTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Model model;

    @Before
    public void setUp() {
        model = new ModelManager(getTypicalBookShelf(), new UserPrefs());
        LockManager.getInstance().initialize(LockManager.NO_PASSWORD);
        LockManager.getInstance().setPassword(LockManager.NO_PASSWORD, "testing");
    }

    @After
    public void tearDown() {
        LockManager.getInstance().initialize(LockManager.NO_PASSWORD);
    }

    @Test
    public void constructor_nullOldPw_throwsNullPointerException() {
        thrown.expect(NullPointerException.class);
        new SetPasswordCommand(null, " ");
    }

    @Test
    public void constructor_nullNewPw_throwsNullPointerException() {
        thrown.expect(NullPointerException.class);
        new SetPasswordCommand(" ", null);
    }

    @Test
    public void execute_correctPw_success() {
        SetPasswordCommand command = prepareCommand("testing", "hello");
        CommandResult result = command.execute();
        assertEquals(SetPasswordCommand.MESSAGE_SUCCESS, result.feedbackToUser);
    }

    @Test
    public void execute_wrongPw_failure() {
        SetPasswordCommand command = prepareCommand("wrongpw", "hello");
        CommandResult result = command.execute();
        assertEquals(SetPasswordCommand.MESSAGE_WRONG_PASSWORD, result.feedbackToUser);
    }

    @Test
    public void equals() {
        SetPasswordCommand setPasswordCommand = new SetPasswordCommand("testing", "newkey");

        SetPasswordCommand thesameCommand = new SetPasswordCommand("testing", "newkey");

        // same value -> returns true
        assertTrue(setPasswordCommand.equals(thesameCommand));

        // same object -> returns true
        assertTrue(setPasswordCommand.equals(setPasswordCommand));

        // null -> returns false
        assertFalse(setPasswordCommand.equals(null));

        // different commandtypes -> returns false
        assertFalse(setPasswordCommand.equals(new ClearCommand()));

        // different types -> return false
        assertFalse(setPasswordCommand.equals(0));
    }

    /**
     * Returns an {@code SetPasswordCommand} with the parameter {@code oldPw} and {@code newPw}.
     */
    private SetPasswordCommand prepareCommand(String oldPw, String newPw) {
        SetPasswordCommand setPasswordCommand = new SetPasswordCommand(oldPw, newPw);
        setPasswordCommand.setData(model, mock(NetworkManager.class), new CommandHistory(), new UndoStack());
        return setPasswordCommand;
    }

}
