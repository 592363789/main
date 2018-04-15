package seedu.address.logic.parser;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.alias.Alias;
import seedu.address.network.NetworkManager;

public class HintParserTest {

    private HintParser hintParser;

    @Before
    public void setUp() {
        Model model = new ModelManager();
        Logic logic = new LogicManager(model, mock(NetworkManager.class));
        hintParser = new HintParser(logic);
        model.addAlias(new Alias("ls", "list", "s/read by/pd"));
        model.addAlias(new Alias("d", "delete", ""));
    }

    @Test
    public void generateHint_emptyCommandText_emptyHint() {
        assertEquals("", hintParser.generateHint(""));
    }

    @Test
    public void generateHint_commandTextWithCompleteCommand_defaultHintForCommand() {
        assertEquals(" add a command alias", hintParser.generateHint("addalias ls cmd/list"));
        assertEquals(" add a book", hintParser.generateHint("add 1"));
        assertEquals(" list all command aliases", hintParser.generateHint("aliases"));
        assertEquals(" clear book shelf", hintParser.generateHint("clear"));
        assertEquals(" delete a book", hintParser.generateHint("delete 1"));
        assertEquals(" edit a book", hintParser.generateHint("edit 1 s/read p/low r/5"));
        assertEquals(" exit the app", hintParser.generateHint("exit"));
        assertEquals(" show command history", hintParser.generateHint("history"));
        assertEquals(" list, filter, and sort books", hintParser.generateHint("list t/1 a/1 c/1 s/r p/l r/5 by/s"));
        assertEquals(" lock the app", hintParser.generateHint("lock"));
        assertEquals(" search for books", hintParser.generateHint("search 1 i/1 t/1 a/1 c/1"));
        assertEquals(" change the password", hintParser.generateHint("setpw old/123 new/12345"));
        assertEquals(" unlock the app", hintParser.generateHint("unlock 12345"));
    }

    @Test
    public void generateHint_commandTextWithIncompleteCommand_parameterHint() {
        assertEquals(" INDEX", hintParser.generateHint("add"));
        assertEquals("INDEX", hintParser.generateHint("add "));
        assertEquals(" cmd/COMMAND", hintParser.generateHint("addalias ls"));
        assertEquals(" INDEX", hintParser.generateHint("edit"));
        assertEquals(" [p/PRIORITY] [r/RATING]", hintParser.generateHint("edit 1 s/read"));
        assertEquals(" [s/STATUS] [p/PRIORITY] [r/RATING]", hintParser.generateHint("list t/1 a/1 c/1 by/s"));
        assertEquals(" [KEY_WORDS] [i/ISBN] [t/TITLE] [a/AUTHOR] [c/CATEGORY]", hintParser.generateHint("search"));
        assertEquals(" [a/AUTHOR]", hintParser.generateHint("search i/1 t/1 c/1"));
        assertEquals(" [old/OLD_PASSWORD] [new/NEW_PASSWORD]", hintParser.generateHint("setpw"));
    }

    @Test
    public void generateHint_commandTextWithEndingPrefix_parameterHint() {
        assertEquals("md/COMMAND", hintParser.generateHint("addalias ls c"));
        assertEquals("d/COMMAND", hintParser.generateHint("addalias ls cm"));
        assertEquals("/COMMAND", hintParser.generateHint("addalias ls cmd"));
        assertEquals("COMMAND", hintParser.generateHint("addalias ls cmd/"));
        assertEquals(" add a command alias", hintParser.generateHint("addalias ls cmd/l"));
        assertEquals("cmd/COMMAND", hintParser.generateHint("addalias ls cm "));

        assertEquals("/TITLE", hintParser.generateHint("list t"));
        assertEquals("TITLE", hintParser.generateHint("list t/"));
        assertEquals("/STATUS", hintParser.generateHint("list t/ s"));
        assertEquals(" INDEX", hintParser.generateHint("edit p")); // requires an index
    }
}
