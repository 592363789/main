package seedu.address.commons.events.ui;

import seedu.address.commons.events.BaseEvent;

/**
 * An event requesting to view the book list.
 */
public class ShowBookListRequestEvent extends BaseEvent {

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

}
