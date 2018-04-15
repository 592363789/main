package seedu.address.logic;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static seedu.address.commons.core.Messages.MESSAGE_INVALID_BOOK_DISPLAYED_INDEX;
import static seedu.address.commons.core.Messages.MESSAGE_UNKNOWN_COMMAND;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import seedu.address.commons.core.Messages;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.HistoryCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.LockCommand;
import seedu.address.logic.commands.SetPasswordCommand;
import seedu.address.logic.commands.UnlockCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.alias.Alias;
import seedu.address.network.NetworkManager;


public class LogicManagerTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Model model = new ModelManager();
    private Logic logic = new LogicManager(model, mock(NetworkManager.class));

    @Before
    public void setUp() {
        LockManager.getInstance().initialize(LockManager.NO_PASSWORD);
    }

    @After
    public void tearDown() {
        LockManager.getInstance().initialize(LockManager.NO_PASSWORD);
    }

    @Test
    public void isValidCommand_validCommand_true() {
        assertTrue(logic.isValidCommand("delete 9"));
        assertTrue(logic.isValidCommand("list s/read by/pd"));
        assertTrue(logic.isValidCommand("   list   s/read    by/pd  "));
    }

    @Test
    public void isValidCommand_validAlias_true() {
        model.addAlias(new Alias("ls", "list", "s/read by/pd"));
        model.addAlias(new Alias("d", "delete", ""));
        assertTrue(logic.isValidCommand("ls"));
        assertTrue(logic.isValidCommand("d 9"));
        assertTrue(logic.isValidCommand("    ls    by/s    "));
    }

    @Test
    public void isValidCommand_invalidCommand_false() {
        assertFalse(logic.isValidCommand("listt"));
        assertFalse(logic.isValidCommand("delete"));
        assertFalse(logic.isValidCommand("del ete 9"));

        model.addAlias(new Alias("d", "delete", ""));
        assertFalse(logic.isValidCommand("d"));
    }

    @Test
    public void parse_emptyCommandText_throwsParseException() throws ParseException {
        thrown.expect(ParseException.class);
        logic.parse("");
    }

    @Test
    public void parse_aliasedCommand_aliasApplied() throws ParseException {
        model.addAlias(new Alias("ls", "list", "s/read by/pd"));
        model.addAlias(new Alias("d", "delete", ""));
        assertArrayEquals(new String[]{"list", " s/read by/pd"}, logic.parse("ls"));
        assertArrayEquals(new String[]{"list", " s/read by/pd s/unread"}, logic.parse("ls s/unread"));
        assertArrayEquals(new String[]{"delete", ""}, logic.parse("d"));
    }

    @Test
    public void parse_nonAliasedCommand_processed() throws ParseException {
        assertArrayEquals(new String[]{"list", ""}, logic.parse("list"));
        assertArrayEquals(new String[]{"list", " 123"}, logic.parse("list 123"));
        assertArrayEquals(new String[]{"list", " 123 s/read "}, logic.parse("list 123 s/read "));
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
        assertHistoryCorrect(invalidCommand);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, MESSAGE_INVALID_BOOK_DISPLAYED_INDEX);
        assertHistoryCorrect(deleteCommand);
    }

    @Test
    public void execute_emptyCommandText_emptyResult() throws CommandException, ParseException {
        CommandResult result = logic.execute("");
        assertTrue(CommandResult.isEmptyResult(result));
    }

    @Test
    public void execute_validCommand_success() {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand,
                String.format(ListCommand.MESSAGE_SUCCESS, model.getDisplayBookList().size()), model);
        assertHistoryCorrect(listCommand);
    }

    @Test
    public void execute_closeCommand_corrected() {
        assertCommandSuccess("lst", String.format(Messages.MESSAGE_CORRECTED_COMMAND, "list"), model);
        assertCommandSuccess("",
                String.format(ListCommand.MESSAGE_SUCCESS, model.getDisplayBookList().size()), model);
    }

    @Test
    public void execute_locked() {
        LockManager.getInstance().lock();
        assertCommandSuccess("list", Messages.MESSAGE_APP_LOCKED, model);
        assertCommandSuccess("help", HelpCommand.SHOWING_HELP_MESSAGE, model);
        assertCommandSuccess("unlock", UnlockCommand.MESSAGE_SUCCESS, model);
    }

    @Test
    public void execute_password_notInHistory() {
        assertCommandSuccess("list",
                String.format(ListCommand.MESSAGE_SUCCESS, model.getDisplayBookList().size()), model);
        assertHistoryCorrect("list");
        assertCommandSuccess("lock", LockCommand.MESSAGE_SUCCESS, model);
        assertCommandSuccess("unlock ", UnlockCommand.MESSAGE_SUCCESS, model);
        assertHistoryCorrect("lock", "history", "list");
        assertCommandSuccess("setpw new/xxx", SetPasswordCommand.MESSAGE_SUCCESS, model);
        assertHistoryCorrect("history", "lock", "history", "list");
    }

    @Test
    public void getActiveList_modifyList_throwsUnsupportedOperationException() {
        thrown.expect(UnsupportedOperationException.class);
        logic.getActiveList().remove(0);
    }

    @Test
    public void getDisplayAliasList_modifyList_throwsUnsupportedOperationException() {
        thrown.expect(UnsupportedOperationException.class);
        logic.getDisplayAliasList().remove(0);
    }

    /**
     * Executes the command, confirms that no exceptions are thrown and that the result message is correct.
     * Also confirms that {@code expectedModel} is as specified.
     * @see #assertCommandBehavior(Class, String, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage, Model expectedModel) {
        assertCommandBehavior(null, inputCommand, expectedMessage, expectedModel);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandBehavior(Class, String, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandBehavior(Class, String, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandBehavior(Class, String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<?> expectedException, String expectedMessage) {
        Model expectedModel = new ModelManager(model.getBookShelf(), new UserPrefs());
        assertCommandBehavior(expectedException, inputCommand, expectedMessage, expectedModel);
    }

    /**
     * Executes the command, confirms that the result message is correct and that the expected exception is thrown,
     * and also confirms that the following two parts of the LogicManager object's state are as expected:<br>
     *      - the internal model manager data are same as those in the {@code expectedModel} <br>
     *      - {@code expectedModel}'s address book was saved to the storage file.
     */
    private void assertCommandBehavior(Class<?> expectedException, String inputCommand,
                                           String expectedMessage, Model expectedModel) {

        try {
            CommandResult result = logic.execute(inputCommand);
            assertEquals(expectedException, null);
            assertEquals(expectedMessage, result.feedbackToUser);
        } catch (CommandException | ParseException e) {
            assertEquals(expectedException, e.getClass());
            assertEquals(expectedMessage, e.getMessage());
        }

        assertEquals(expectedModel, model);
    }

    /**
     * Asserts that the result display shows all the {@code expectedCommands} upon the execution of
     * {@code HistoryCommand}.
     */
    private void assertHistoryCorrect(String... expectedCommands) {
        try {
            CommandResult result = logic.execute(HistoryCommand.COMMAND_WORD);
            String expectedMessage = String.format(
                    HistoryCommand.MESSAGE_SUCCESS, String.join("\n", expectedCommands));
            assertEquals(expectedMessage, result.feedbackToUser);
        } catch (ParseException | CommandException e) {
            throw new AssertionError("Parsing and execution of HistoryCommand.COMMAND_WORD should succeed.", e);
        }
    }
}
