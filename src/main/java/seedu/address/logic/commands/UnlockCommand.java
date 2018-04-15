package seedu.address.logic.commands;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import seedu.address.commons.core.EventsCenter;
import seedu.address.commons.events.model.AppUnlockedEvent;
import seedu.address.commons.events.ui.ActiveListChangedEvent;
import seedu.address.logic.LockManager;
import seedu.address.model.ActiveListType;
import seedu.address.model.Model;

//@@author 592363789
/**
 * Unlocks the app.
 */
public class UnlockCommand extends Command {

    public static final String COMMAND_WORD = "unlock";

    public static final String MESSAGE_USAGE = COMMAND_WORD  + ": Unlock the app.\n"
            + "Parameters: PASSWORD\n"
            + "Example: " + COMMAND_WORD + " 123456";

    public static final String MESSAGE_SUCCESS = "Successfully unlocked the app.";
    protected static final String MESSAGE_NOT_LOCKED = "The app is not locked.";
    protected static final String MESSAGE_WRONG_PASSWORD = "Incorrect password. Please try again.";

    private String password;

    public UnlockCommand(String password) {
        this.password = password.trim();
    }

    @Override
    public CommandResult execute() {
        requireAllNonNull(model, password);

        if (!LockManager.getInstance().isLocked()) {
            return new CommandResult(MESSAGE_NOT_LOCKED);
        }

        if (LockManager.getInstance().unlock(password)) {
            EventsCenter.getInstance().post(new AppUnlockedEvent());
            model.updateBookListFilter(Model.PREDICATE_SHOW_ALL_BOOKS);

            model.setActiveListType(ActiveListType.BOOK_SHELF);
            EventsCenter.getInstance().post(new ActiveListChangedEvent());
            return new CommandResult(MESSAGE_SUCCESS);
        } else {
            return new CommandResult(MESSAGE_WRONG_PASSWORD);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof UnlockCommand // instanceof handles nulls
                && this.password.equals(((UnlockCommand) other).password)); // state check
    }
}
