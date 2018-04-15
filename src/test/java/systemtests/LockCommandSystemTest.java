package systemtests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import guitests.GuiRobot;
import guitests.guihandles.HelpWindowHandle;
import seedu.address.commons.core.Messages;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.LockCommand;
import seedu.address.logic.commands.RecentCommand;
import seedu.address.logic.commands.SearchCommand;
import seedu.address.logic.commands.SetPasswordCommand;
import seedu.address.logic.commands.UnlockCommand;
import seedu.address.model.Model;

public class LockCommandSystemTest extends BibliotekSystemTest {

    private final GuiRobot guiRobot = new GuiRobot();

    @Test
    public void lock() {
        assertLockCommandSuccess();

        /* ----------------------------------- Perform blocked commands --------------------------------------------- */

        assertCommandBlocked(ListCommand.COMMAND_WORD);
        assertCommandBlocked(RecentCommand.COMMAND_WORD);
        assertCommandBlocked(SetPasswordCommand.COMMAND_WORD + " new/123");
        assertCommandBlocked(SearchCommand.COMMAND_WORD + " hello");

        /* ----------------------------------- Perform unblocked commands ------------------------------------------- */

        /* case: help command -> success */
        executeCommand(HelpCommand.COMMAND_WORD);
        assertTrue(HelpWindowHandle.isWindowPresent());

        guiRobot.pauseForHuman();
        new HelpWindowHandle(guiRobot.getStage(HelpWindowHandle.HELP_WINDOW_TITLE)).close();
        getMainWindowHandle().focus();

        /* case: unlock command -> success */
        executeCommand(UnlockCommand.COMMAND_WORD);

        Model expectedModel = getModel();
        expectedModel.updateBookListFilter(Model.PREDICATE_SHOW_ALL_BOOKS);
        assertApplicationDisplaysExpected("", UnlockCommand.MESSAGE_SUCCESS, expectedModel);
    }

    /**
     * Executes {@code command} and verifies that the command box displays an empty string, and the result
     * display box displays {@code Messages.MESSAGE_APP_LOCKED}.
     */
    private void assertCommandBlocked(String command) {
        Model expectedModel = getModel();
        expectedModel.updateBookListFilter(Model.PREDICATE_HIDE_ALL_BOOKS);

        executeCommand(command);
        assertApplicationDisplaysExpected("", Messages.MESSAGE_APP_LOCKED, expectedModel);
    }

    /**
     * Executes {@code command} and verifies that the command box displays an empty string, the result display
     * box displays {@code LockCommand#MESSAGE_SUCCESS} and the model related components remain unchanged.
     * These verifications are done by
     * {@code BibliotekSystemTest#assertApplicationDisplaysExpected(String, String, Model)}.<br>
     * Also verifies that the command box has the default style class, the status bar's sync status does not
     * change, and the welcome panel is visible.
     * @see BibliotekSystemTest#assertApplicationDisplaysExpected(String, String, Model)
     */
    private void assertLockCommandSuccess() {
        Model expectedModel = getModel();
        expectedModel.updateBookListFilter(Model.PREDICATE_HIDE_ALL_BOOKS);

        executeCommand(LockCommand.COMMAND_WORD);
        assertApplicationDisplaysExpected("", LockCommand.MESSAGE_SUCCESS, expectedModel);
        assertWelcomePanelVisible();
        assertCommandBoxShowsDefaultStyle();
        assertStatusBarUnchanged();
    }
}
