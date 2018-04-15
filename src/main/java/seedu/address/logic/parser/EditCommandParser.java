package seedu.address.logic.parser;
//@@author 592363789
import static java.util.Objects.requireNonNull;
import static seedu.address.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PRIORITY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RATING;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new {@code EditCommand} object.
 */
public class EditCommandParser implements Parser<EditCommand> {
    /**
     * Parses the given {@code String} of arguments in the context of the {@code EditCommand}
     * and returns a {@code EditCommand} object for execution.
     * @throws ParseException if the user input does not conform the expected format.
     */
    public EditCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args,
                PREFIX_RATING, PREFIX_PRIORITY, PREFIX_STATUS);

        Index index;
        try {
            index = ParserUtil.parseIndex(argMultimap.getPreamble());
        } catch (IllegalValueException ive) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));
        }

        EditDescriptor editDescriptor = new EditDescriptor();

        if (argMultimap.getValue(PREFIX_STATUS).isPresent()) {
            editDescriptor.setStatus(ParserUtil.parseStatus(argMultimap.getValue(PREFIX_STATUS).get()));
        }

        if (argMultimap.getValue(PREFIX_PRIORITY).isPresent()) {
            editDescriptor.setPriority(ParserUtil.parsePriority(argMultimap.getValue(PREFIX_PRIORITY).get()));
        }

        if (argMultimap.getValue(PREFIX_RATING).isPresent()) {
            editDescriptor.setRating(ParserUtil.parseRating(argMultimap.getValue(PREFIX_RATING).get()));
        }

        if (!editDescriptor.isValid()) {
            throw new ParseException(EditCommand.MESSAGE_NO_PARAMETERS);
        }

        return new EditCommand(index, editDescriptor);
    }
}
