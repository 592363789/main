package seedu.address.commons.events.model;
//@@author 592363789
import seedu.address.commons.events.BaseEvent;

/** Indicates a change in the password */
public class PasswordChangedEvent extends BaseEvent {

    public final String oldPassword;
    public final String newPassword;

    public PasswordChangedEvent(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }
    //@@author
    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
