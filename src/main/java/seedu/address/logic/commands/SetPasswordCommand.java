package seedu.address.logic.commands;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import seedu.address.commons.core.EventsCenter;
import seedu.address.commons.events.model.PasswordChangedEvent;
import seedu.address.logic.LockManager;

//@@author 592363789
/**
 * Changes the password for the app.
 */
public class SetPasswordCommand extends Command {

    public static final String COMMAND_WORD = "setpw";

    public static final String MESSAGE_SUCCESS = "Successfully changed your password.";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Change your password.\n"
            + "Parameters: [old/OLD_PASSWORD] [new/NEW_PASSWORD]\n"
            + "Example: " + COMMAND_WORD + " old/123456 new/abcde ";
    public static final String MESSAGE_WRONG_PASSWORD = "Incorrect old password. Please try again.";

    private String oldPassword;
    private String newPassword;

    public SetPasswordCommand(String oldPassword, String newPassword) {
        requireAllNonNull(oldPassword, newPassword);

        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    /**
     * Executes the command and returns the result message.
     *
     * @return feedback message of the operation result for display
     */
    @Override
    public CommandResult execute() {
        requireAllNonNull(oldPassword, newPassword);

        if (LockManager.getInstance().setPassword(oldPassword, newPassword)) {
            EventsCenter.getInstance().post(new PasswordChangedEvent(oldPassword, newPassword));
            return new CommandResult(MESSAGE_SUCCESS);
        } else {
            return new CommandResult(MESSAGE_WRONG_PASSWORD);
        }
    }

    @Override
    public boolean equals(Object other) {
        // short circuit if same object
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof SetPasswordCommand)) {
            return false;
        }

        // state check
        SetPasswordCommand e = (SetPasswordCommand) other;
        return oldPassword.equals(e.oldPassword) && newPassword.equals(e.newPassword);
    }
}
