# fishTT
###### \java\seedu\address\commons\util\TextUtil.java
``` java
public class TextUtil {

    private static final Text HELPER;

    /**
     * Return's Text Width based on {@code TextField textField, String text}.
     */
    public static double computeTextWidth(TextField textField, String text, double maxWidth) {
        HELPER.setText(text);
        HELPER.setFont(textField.getFont());
        HELPER.setStyle(textField.getStyle());

        HELPER.setWrappingWidth(0.0D);
        HELPER.setLineSpacing(0.0D);
        double d = Math.min(HELPER.prefWidth(-1.0D), maxWidth);
        HELPER.setWrappingWidth((int) Math.ceil(d));
        return Math.ceil(HELPER.getLayoutBounds().getWidth());
    }

    static {
        HELPER = new Text();
    }
}
```
###### \java\seedu\address\logic\parser\HintParser.java
``` java
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

```
###### \java\seedu\address\logic\parser\HintParser.java
``` java
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

```
###### \java\seedu\address\ui\WelcomePanel.java
``` java
    private String getRandomQuote() throws IOException {
        List<String> lines = Resources.readLines(QUOTES_FILE, Charsets.UTF_8);

        // choose a random one from the list
        Random r = new Random();
        String randomQuote = lines.get(r.nextInt(lines.size()));
        return "\"" + randomQuote + "\"";
    }

```
###### \resources\view\CommandBoxHints.fxml
``` fxml
<?import javafx.scene.Cursor?>
<?import javafx.scene.control.TextField?>

<TextField fx:id="commandBoxHints" editable="false" onMouseClicked="#handleOnClick" pickOnBounds="false" text="Enter command here..."
           xmlns="http://javafx.com/javafx/8" xmlns:fx="http://javafx.com/fxml/1">
   <cursor>
      <Cursor fx:constant="TEXT" />
   </cursor>
</TextField>
```
