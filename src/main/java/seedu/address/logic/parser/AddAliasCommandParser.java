package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COMMAND;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.AddAliasCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.alias.Alias;

//@@author takuyakanbr
/**
 * Parses input arguments and creates a new {@code AddAliasCommand} object.
 */
public class AddAliasCommandParser implements Parser<AddAliasCommand> {

    private static final Pattern ALIAS_FORMAT = Pattern.compile("^\\S+$");
    private static final Pattern COMMAND_FORMAT = Pattern.compile("(?<prefix>((?! \\w+\\/.*)[\\S ])+)(?<namedArgs>.*)");

    @Override
    public AddAliasCommand parse(String args) throws ParseException {
        requireNonNull(args);

        ArgumentMultimap argMultimap = createArgumentMultimap(args);
        String aliasName = getAliasName(argMultimap);
        Matcher matcher = createCommandMatcher(argMultimap);

        return new AddAliasCommand(createAlias(aliasName, matcher));
    }

    /**
     * Tokenizes the given {@code args} and returns an {@code ArgumentMultimap} containing the results.
     * @throws ParseException if {@code PREFIX_COMMAND} cannot be found.
     */
    private static ArgumentMultimap createArgumentMultimap(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_COMMAND);
        checkCommandFormat(argMultimap.getValue(PREFIX_COMMAND).isPresent());
        return argMultimap;
    }

    /**
     * Gets and returns the alias name from the {@code argMultimap}.
     * @throws ParseException if the alias name is not in the expected format.
     */
    private static String getAliasName(ArgumentMultimap argMultimap) throws ParseException {
        String aliasName = argMultimap.getPreamble().trim();
        checkCommandFormat(ALIAS_FORMAT.matcher(aliasName).matches());
        return aliasName;
    }

    /**
     * Creates and returns a {@code Matcher} containing information about the command to be aliased.
     * @throws ParseException if the command is not in the expected format.
     */
    private static Matcher createCommandMatcher(ArgumentMultimap argMultimap) throws ParseException {
        Matcher matcher = COMMAND_FORMAT.matcher(argMultimap.getValue(PREFIX_COMMAND).get());
        checkCommandFormat(matcher.matches());
        return matcher;
    }

    private static Alias createAlias(String aliasName, Matcher commandMatcher) {
        return new Alias(aliasName, commandMatcher.group("prefix"), commandMatcher.group("namedArgs"));
    }

    private static void checkCommandFormat(boolean condition) throws ParseException {
        if (!condition) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddAliasCommand.MESSAGE_USAGE));
        }
    }
}
