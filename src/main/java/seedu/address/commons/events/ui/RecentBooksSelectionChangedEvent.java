package seedu.address.commons.events.ui;

import seedu.address.commons.events.BaseEvent;
import seedu.address.model.book.Book;

/**
 * Represents a selection change in the Recent Books Panel.
 */
public class RecentBooksSelectionChangedEvent extends BaseEvent {

    private final Book newSelection;

    public RecentBooksSelectionChangedEvent(Book newSelection) {
        this.newSelection = newSelection;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    public Book getNewSelection() {
        return newSelection;
    }
}
