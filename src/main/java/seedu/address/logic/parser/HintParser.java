package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_AUTHOR;
import static seedu.address.logic.parser.CliSyntax.PREFIX_AUTHOR_STRING;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CATEGORY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CATEGORY_STRING;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COMMAND;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COMMAND_STRING;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ISBN;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ISBN_STRING;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NEW;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NEW_STRING;
import static seedu.address.logic.parser.CliSyntax.PREFIX_OLD;
import static seedu.address.logic.parser.CliSyntax.PREFIX_OLD_STRING;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PRIORITY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PRIORITY_STRING;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RATING;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RATING_STRING;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SORT_BY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SORT_BY_STRING;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS_STRING;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TITLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TITLE_STRING;

import java.util.Optional;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.AddAliasCommand;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.AliasesCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteAliasCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.HistoryCommand;
import seedu.address.logic.commands.LibraryCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.LockCommand;
import seedu.address.logic.commands.RecentCommand;
import seedu.address.logic.commands.ReviewsCommand;
import seedu.address.logic.commands.SearchCommand;
import seedu.address.logic.commands.SelectCommand;
import seedu.address.logic.commands.SetPasswordCommand;
import seedu.address.logic.commands.ThemeCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.UnlockCommand;
import seedu.address.logic.parser.exceptions.ParseException;

//@@author fishTT
/**
 * Class that is responsible for generating hints based on user input.
 * Contains one public method generateHint which returns an appropriate hint based on input.
 */
public class HintParser {

    private final Logic logic;

    public HintParser(Logic logic) {
        this.logic = logic;
    }

    /**
     * Parses {@code String input} and returns an appropriate hint.
     */
    public String generateHint(String input) {
        String[] command;

        try {
            command = logic.parse(input);
        } catch (ParseException e) {
            return "";
        }

        return generateHintContent(command[0], command[1]);
    }

    /**
     * Returns an appropriate hint based on commandWord and arguments.
     */
    private static String generateHintContent(String commandWord, String arguments) {
        switch (commandWord) {
        case AddAliasCommand.COMMAND_WORD:
            return generateAddAliasHint(arguments);
        case AddCommand.COMMAND_WORD:
            return generateHintForIndexedCommand(arguments, " add a book");
        case AliasesCommand.COMMAND_WORD:
            return " list all command aliases";
        case ClearCommand.COMMAND_WORD:
            return " clear book shelf";
        case DeleteAliasCommand.COMMAND_WORD:
            return generateDeleteAliasHint(arguments);
        case DeleteCommand.COMMAND_WORD:
            return generateHintForIndexedCommand(arguments, " delete a book");
        case EditCommand.COMMAND_WORD:
            return generateEditHint(arguments);
        case ExitCommand.COMMAND_WORD:
            return " exit the app";
        case HelpCommand.COMMAND_WORD:
            return " show user guide";
        case HistoryCommand.COMMAND_WORD:
            return " show command history";
        case LibraryCommand.COMMAND_WORD:
            return generateHintForIndexedCommand(arguments, " find book in library");
        case ListCommand.COMMAND_WORD:
            return generateListHint(arguments);
        case LockCommand.COMMAND_WORD:
            return " lock the app";
        case RecentCommand.COMMAND_WORD:
            return " view recently selected books";
        case ReviewsCommand.COMMAND_WORD:
            return generateHintForIndexedCommand(arguments, " view book review");
        case SearchCommand.COMMAND_WORD:
            return generateSearchHint(arguments);
        case SelectCommand.COMMAND_WORD:
            return generateHintForIndexedCommand(arguments, " select a book");
        case SetPasswordCommand.COMMAND_WORD:
            return generateSetPasswordHint(arguments);
        case ThemeCommand.COMMAND_WORD:
            return generateThemeHint(arguments);
        case UndoCommand.COMMAND_WORD:
            return " undo last modification";
        case UnlockCommand.COMMAND_WORD:
            return generateUnlockHint(arguments);
        default:
            return "";
        }
    }

    //@@author
    /**
     * Parses the end of arguments to check if user is currently typing a prefix that is in prefixes.
     * Returns hint if user is typing a prefix.
     * Returns empty Optional if user is not typing a prefix.
     */
    private static Optional<String> generatePrefixHintBasedOnEndArgs(String arguments, Prefix... prefixes) {
        for (Prefix p : prefixes) {
            String prefixString = p.getPrefix();
            String parameter = prefixIntoParameter(p);
            for (int i = 1; i <= prefixString.length(); ++i) {
                if (arguments.endsWith(" " + prefixString.substring(0, i))) {
                    return Optional.of(prefixString.substring(i, prefixString.length()) + parameter);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Parses arguments to check for all optional parameters that have not been used,
     * from among the optional parameters as specified by {@code prefixes}.
     * @return a hint showing all the parameters that are not present,
     *         or {@code defaultHint} if all parameters are present.
     */
    private static String showUnusedParameters(String arguments, String defaultHint,
                                               String preamble, Prefix... prefixes) {
        ArgumentMultimap argumentMultimap = ArgumentTokenizer.tokenize(arguments, prefixes);

        boolean hasPrefixes = false;
        StringBuilder sb = new StringBuilder();
        for (Prefix p : prefixes) {
            Optional<String> parameterOptional = argumentMultimap.getValue(p);
            if (parameterOptional.isPresent()) {
                hasPrefixes = true;
                continue;
            }
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append("[").append(p.getPrefix()).append(prefixIntoParameter(p)).append("]");
        }

        if (!hasPrefixes && preamble != null && argumentMultimap.getPreamble().trim().isEmpty()) {
            sb.insert(0, "[" + preamble + "] ");
        }

        if (sb.length() == 0) {
            return defaultHint;
        }
        return getHintPadding(arguments) + sb.toString();
    }

    //@@author fishTT
    /**
     * Parses arguments to check for the next required parameter that has not been used,
     * from among the required parameters as specified by {@code prefixes}.
     * @return a hint for the parameter that is not present, or {@code defaultHint} if all parameters are present.
     */
    private static String showNextParameter(String arguments, String defaultHint, Prefix... prefixes) {
        ArgumentMultimap argumentMultimap = ArgumentTokenizer.tokenize(arguments, prefixes);

        for (Prefix p : prefixes) {
            Optional<String> parameterOptional = argumentMultimap.getValue(p);
            if (!parameterOptional.isPresent()) {
                return getHintPadding(arguments) + p.getPrefix() + prefixIntoParameter(p);
            }
        }
        return defaultHint;
    }

    /**
     * Returns a parameter based on {@code prefix}.
     */
    private static String prefixIntoParameter(Prefix prefix) {
        switch (prefix.toString()) {
        case PREFIX_AUTHOR_STRING:
            return "AUTHOR";
        case PREFIX_CATEGORY_STRING:
            return "CATEGORY";
        case PREFIX_ISBN_STRING:
            return "ISBN";
        case PREFIX_TITLE_STRING:
            return "TITLE";
        case PREFIX_STATUS_STRING:
            return "STATUS";
        case PREFIX_PRIORITY_STRING:
            return "PRIORITY";
        case PREFIX_RATING_STRING:
            return "RATING";
        case PREFIX_SORT_BY_STRING:
            return "SORT_BY";
        case PREFIX_OLD_STRING:
            return "OLD_PASSWORD";
        case PREFIX_NEW_STRING:
            return "NEW_PASSWORD";
        case PREFIX_COMMAND_STRING:
            return "COMMAND";
        default:
            return "KEYWORD";
        }
    }

    /**
     * Parses arguments to check if index is present.
     * Checks on userInput to handle whitespace.
     * Returns "index" if index is not present, else returns an empty Optional.
     */
    private static Optional<String> generateIndexHint(String arguments) {
        try {
            ParserUtil.parseIndex(arguments);
            return Optional.empty();
        } catch (IllegalValueException ive) {
            if (arguments.matches(".*\\s\\d+\\s.*")) {
                return Optional.empty();
            }
            return Optional.of(getHintPadding(arguments) + "INDEX");
        }
    }

    //@@author
    /**
     * Parses arguments to check if a preamble is present.
     * Returns {@code parameterName} if preamble is not present, else returns an empty Optional.
     */
    private static Optional<String> generatePreambleHint(String arguments, String parameterName) {
        if (arguments.matches("\\s+\\S+.*")) {
            return Optional.empty();
        }
        return Optional.of(getHintPadding(arguments) + parameterName);
    }

    /**
     * Returns an empty string if {@code arguments} starts with a space,
     * or a string containing a single space if not.
     */
    private static String getHintPadding(String arguments) {
        return arguments.endsWith(" ") ? "" : " ";
    }

    /** Returns a hint for commands that accepts only an index. */
    private static String generateHintForIndexedCommand(String arguments, String defaultMessage) {
        return generateIndexHint(arguments).orElse(defaultMessage);
    }

    /** Returns a hint specific to the addalias command. */
    private static String generateAddAliasHint(String arguments) {
        Optional<String> preambleHintOptional = generatePreambleHint(arguments, "ALIAS_NAME");
        if (preambleHintOptional.isPresent()) {
            return preambleHintOptional.get();
        }
        Optional<String> endHintOptional = generatePrefixHintBasedOnEndArgs(arguments, PREFIX_COMMAND);
        return endHintOptional.orElseGet(() -> showNextParameter(arguments, " add a command alias", PREFIX_COMMAND));
    }

    /** Returns a hint specific to the deletealias command. */
    private static String generateDeleteAliasHint(String arguments) {
        return generatePreambleHint(arguments, "ALIAS_NAME").orElse(" delete a command alias");
    }

    /**Returns a hint specific to the edit command. */
    private static String generateEditHint(String arguments) {
        Optional<String> indexHintOptional = generateIndexHint(arguments);
        if (indexHintOptional.isPresent()) {
            return indexHintOptional.get();
        }

        Prefix[] prefixes = new Prefix[]{PREFIX_STATUS, PREFIX_PRIORITY, PREFIX_RATING};
        Optional<String> endHintOptional = generatePrefixHintBasedOnEndArgs(arguments, prefixes);
        return endHintOptional.orElseGet(() -> showUnusedParameters(arguments, " edit a book", null, prefixes));
    }

    /** Returns a hint specific to the list command. */
    private static String generateListHint(String arguments) {
        Prefix[] prefixes = new Prefix[]{PREFIX_TITLE, PREFIX_AUTHOR, PREFIX_CATEGORY, PREFIX_STATUS,
            PREFIX_PRIORITY, PREFIX_RATING, PREFIX_SORT_BY};
        Optional<String> endHintOptional = generatePrefixHintBasedOnEndArgs(arguments, prefixes);
        return endHintOptional.orElseGet(() -> showUnusedParameters(arguments,
                " list, filter, and sort books", null, prefixes));
    }

    /** Returns a hint specific to the search command. */
    private static String generateSearchHint(String arguments) {
        Prefix[] prefixes = new Prefix[]{PREFIX_ISBN, PREFIX_TITLE, PREFIX_AUTHOR, PREFIX_CATEGORY};
        Optional<String> endHintOptional = generatePrefixHintBasedOnEndArgs(arguments, prefixes);
        return endHintOptional.orElseGet(() -> showUnusedParameters(arguments,
                " search for books", "KEY_WORDS", prefixes));
    }

    /** Returns a hint specific to the setpw command. */
    private static String generateSetPasswordHint(String arguments) {
        Prefix[] prefixes = new Prefix[]{PREFIX_OLD, PREFIX_NEW};
        Optional<String> endHintOptional = generatePrefixHintBasedOnEndArgs(arguments, prefixes);
        return endHintOptional.orElseGet(() -> showUnusedParameters(arguments,
                " change the password", null, prefixes));
    }

    /** Returns a hint specific to the theme command. */
    private static String generateThemeHint(String arguments) {
        return generatePreambleHint(arguments, "THEME").orElse(" change app theme");
    }

    /** Returns a hint specific to the unlock command. */
    private static String generateUnlockHint(String arguments) {
        return generatePreambleHint(arguments, "PASSWORD").orElse(" unlock the app");
    }
}
