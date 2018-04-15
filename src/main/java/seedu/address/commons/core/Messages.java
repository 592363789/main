package seedu.address.commons.core;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command";
    public static final String MESSAGE_CORRECTED_COMMAND = "Did you mean: %s?\nPress Enter to accept the suggestion.";
    public static final String MESSAGE_APP_LOCKED = "The app is locked, please unlock it first!";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format! \n%1$s";
    public static final String MESSAGE_INVALID_BOOK_DISPLAYED_INDEX = "The book index provided is invalid";
    public static final String MESSAGE_INVALID_STATUS = "Invalid status entered. "
            + "Allowed values are: READ, R, UNREAD, U, READING, and RD.";
    public static final String MESSAGE_INVALID_PRIORITY = "Invalid priority entered. "
            + "Allowed values are: NONE, N, LOW, L, MEDIUM, M, HIGH, and H.";
    public static final String MESSAGE_INVALID_RATING = "Invalid rating entered. "
            + "Please enter a valid integer between -1 and 5 (both inclusive).";
    public static final String MESSAGE_INVALID_SORT_BY = "Invalid sorting mode entered. "
            + "Allowed values are: TITLE, T, STATUS, S, PRIORITY, P, RATING, and R. "
            + "Append a 'D' to sort in descending order.";

}
