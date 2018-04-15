package seedu.address.commons.events.model;

import seedu.address.commons.events.BaseEvent;
import seedu.address.model.alias.ReadOnlyAliasList;

/**
 * Indicates the {@code UniqueAliasList} in the model has changed.
 */
public class AliasListChangedEvent extends BaseEvent {

    public final ReadOnlyAliasList data;

    public AliasListChangedEvent(ReadOnlyAliasList data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "number of aliases " + data.size();
    }
}
