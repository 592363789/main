package seedu.address.logic;

import static seedu.address.commons.core.Messages.MESSAGE_APP_LOCKED;
import static seedu.address.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.logging.Logger;
import java.util.regex.Matcher;

import com.google.common.eventbus.Subscribe;

import javafx.collections.ObservableList;
import seedu.address.commons.core.ComponentManager;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.Messages;
import seedu.address.commons.events.ui.BookListSelectionChangedEvent;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.SetPasswordCommand;
import seedu.address.logic.commands.UnlockCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.BookShelfParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ActiveListType;
import seedu.address.model.Model;
import seedu.address.model.alias.Alias;
import seedu.address.model.book.Book;
import seedu.address.network.Network;

/**
 * The main LogicManager of the app.
 */
public class LogicManager extends ComponentManager implements Logic {
    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Network network;
    private final CommandHistory history;
    private final BookShelfParser bookShelfParser;
    private final UndoStack undoStack;

    private String correctedCommand;

    public LogicManager(Model model, Network network) {
        this.model = model;
        this.network = network;
        history = new CommandHistory();
        bookShelfParser = new BookShelfParser(model.getAliasList());
        undoStack = new UndoStack();
    }

    @Override
    public boolean isValidCommand(String commandText) {
        try {
            String processedText = bookShelfParser.applyCommandAlias(commandText);
            bookShelfParser.parseCommand(processedText);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    @Override
    public String[] parse(String commandText) throws ParseException {
        String processedText = bookShelfParser.applyCommandAlias(commandText);
        final Matcher matcher = BookShelfParser.BASIC_COMMAND_FORMAT.matcher(processedText);
        if (!matcher.matches()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
        }

        final String commandWord = matcher.group("commandWord");
        final String arguments = matcher.group("arguments");

        return new String[] {commandWord, arguments};
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        if (correctedCommand == null && commandText.equals("")) {
            return CommandResult.emptyResult();
        }

        logger.info("----------------[USER COMMAND][" + commandText + "]");

        boolean addToHistory = true;
        try {
            Command command = getCommand(commandText);

            if (LockManager.getInstance().isLocked()
                    && !(command instanceof HelpCommand || command instanceof UnlockCommand)) {
                return new CommandResult(MESSAGE_APP_LOCKED);
            }

            if (command instanceof UnlockCommand || command instanceof SetPasswordCommand) {
                addToHistory = false;
            }

            command.setData(model, network, history, undoStack);
            CommandResult result = command.execute();
            undoStack.push(command);
            return result;
        } catch (ParseException e) {
            return attemptCommandAutoCorrection(commandText, e);
        } finally {
            if (addToHistory) {
                history.add(commandText);
            }
        }
    }

    /**
     * Attempts command auto-correction if {@code e} is a {@code ParseException} due to the user input
     * being unable to be matched to any valid command word.
     * @param processedText The command as entered by the user, after accounting for aliases.
     * @param e The exception thrown by {@link BookShelfParser}.
     * @return message asking user whether he meant the corrected command.
     * @throws ParseException If auto correction failed to find any closely related command.
     */
    private CommandResult attemptCommandAutoCorrection(String processedText, ParseException e) throws ParseException {
        if (!e.getMessage().equals(Messages.MESSAGE_UNKNOWN_COMMAND)) {
            throw e;
        }
        correctedCommand = CommandAutocorrection.attemptCommandAutoCorrection(this, processedText);
        return new CommandResult(String.format(Messages.MESSAGE_CORRECTED_COMMAND, correctedCommand));
    }

    /**
     * Obtains the command represented by {@code processedText}. If user presses enter (empty String) following a
     * command correction, that corrected command will be returned.
     * @param commandText The command as entered by the user.
     * @return the command obtained.
     * @throws ParseException If {@code processedText} cannot be parsed.
     */
    private Command getCommand(String commandText) throws ParseException {
        Command command;
        if (correctedCommand != null && commandText.equals("")) {
            command = bookShelfParser.parseCommand(correctedCommand);
        } else {
            String processedText = bookShelfParser.applyCommandAlias(commandText);
            command = bookShelfParser.parseCommand(processedText);
        }
        correctedCommand = null;
        return command;
    }

    @Override
    public ObservableList<Book> getActiveList() {
        return model.getActiveList();
    }

    @Override
    public ObservableList<Alias> getDisplayAliasList() {
        return model.getDisplayAliasList();
    }

    @Override
    public ListElementPointer getHistorySnapshot() {
        return new ListElementPointer(history.getHistory());
    }

    @Subscribe
    private void handleBookListSelectionChangedEvent(BookListSelectionChangedEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        if (model.getActiveListType() != ActiveListType.RECENT_BOOKS) {
            model.addRecentBook(event.getNewSelection());
        }
    }
}
