package seedu.address;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import com.google.common.eventbus.Subscribe;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import seedu.address.commons.core.Config;
import seedu.address.commons.core.EventsCenter;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.Version;
import seedu.address.commons.events.model.AppUnlockedEvent;
import seedu.address.commons.events.model.PasswordChangedEvent;
import seedu.address.commons.events.ui.ExitAppRequestEvent;
import seedu.address.commons.events.ui.NewResultAvailableEvent;
import seedu.address.commons.exceptions.DataConversionException;
import seedu.address.commons.util.ConfigUtil;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.CipherEngine;
import seedu.address.logic.LockManager;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.BookShelf;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyBookShelf;
import seedu.address.model.UserPrefs;
import seedu.address.model.alias.ReadOnlyAliasList;
import seedu.address.model.alias.UniqueAliasList;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.network.Network;
import seedu.address.network.NetworkManager;
import seedu.address.storage.AliasListStorage;
import seedu.address.storage.BookShelfStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.RecentBooksStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.storage.UserPrefsStorage;
import seedu.address.storage.XmlAliasListStorage;
import seedu.address.storage.XmlBookShelfStorage;
import seedu.address.storage.XmlRecentBooksStorage;
import seedu.address.ui.Ui;
import seedu.address.ui.UiManager;

/**
 * The main entry point to the application.
 */
public class MainApp extends Application {

    public static final Version VERSION = new Version(1, 5, 0, false);

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);

    protected Ui ui;
    protected Logic logic;
    protected Network network;
    protected Storage storage;
    protected Model model;
    protected Config config;
    protected UserPrefs userPrefs;


    @Override
    public void init() throws Exception {
        logger.info("=============================[ Initializing Bibliotek ]===========================");
        super.init();

        config = initConfig(getApplicationParameter("config"));

        UserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(config.getUserPrefsFilePath());
        userPrefs = initPrefs(userPrefsStorage);
        initLockManager(userPrefs);

        BookShelfStorage bookShelfStorage = new XmlBookShelfStorage(userPrefs.getBookShelfFilePath());
        RecentBooksStorage recentBooksStorage = new XmlRecentBooksStorage(userPrefs.getRecentBooksFilePath());
        AliasListStorage aliasListStorage = new XmlAliasListStorage(userPrefs.getAliasListFilePath());
        storage = new StorageManager(bookShelfStorage, userPrefsStorage, recentBooksStorage, aliasListStorage);

        initLogging(config);

        model = initModelManager(storage, userPrefs);

        network = new NetworkManager();

        logic = new LogicManager(model, network);

        ui = new UiManager(logic, config, userPrefs);

        initEventsCenter();
    }

    private String getApplicationParameter(String parameterName) {
        Map<String, String> applicationParameters = getParameters().getNamed();
        return applicationParameters.get(parameterName);
    }

    /**
     * Returns a {@code ModelManager} with the data from {@code storage}'s book shelf and {@code userPrefs}. <br>
     * The data from the sample book shelf will be used instead if {@code storage}'s book shelf is not found,
     * or an empty book shelf will be used instead if errors occur when reading {@code storage}'s book shelf.
     */
    private Model initModelManager(Storage storage, UserPrefs userPrefs) {
        ReadOnlyBookShelf initialData = new BookShelf();
        ReadOnlyBookShelf recentBooksData = new BookShelf();
        ReadOnlyAliasList aliasListData = new UniqueAliasList();
        try {
            Optional<ReadOnlyBookShelf> bookShelfOptional = storage.readBookShelf();
            if (!bookShelfOptional.isPresent()) {
                logger.info("Data file not found. Will be starting with a sample BookShelf");
            }
            initialData = bookShelfOptional.orElseGet(SampleDataUtil::getSampleBookShelf);
        } catch (DataConversionException e) {
            logger.warning("Data file not in the correct format. Will be starting with an empty BookShelf");
        } catch (IOException e) {
            logger.warning("Problem while reading from the file. Will be starting with an empty BookShelf");
        }

        try {
            Optional<ReadOnlyBookShelf> recentBooksOptional = storage.readRecentBooksList();
            recentBooksData = recentBooksOptional.orElse(new BookShelf());
        } catch (DataConversionException e) {
            logger.warning("Data file not in the correct format. Will be starting with an empty recent list");
        } catch (IOException e) {
            logger.warning("Problem while reading from the file. Will be starting with an empty recent list");
        }

        try {
            Optional<ReadOnlyAliasList> aliasListOptional = storage.readAliasList();
            aliasListData = aliasListOptional.orElse(new UniqueAliasList());
        } catch (DataConversionException e) {
            logger.warning("Data file not in the correct format. Will be starting with an empty alias list");
        } catch (IOException e) {
            logger.warning("Problem while reading from the file. Will be starting with an empty alias list");
        }

        return new ModelManager(initialData, userPrefs, recentBooksData, aliasListData);
    }

    private void initLogging(Config config) {
        LogsCenter.init(config);
    }

    /**
     * Returns a {@code Config} using the file at {@code configFilePath}. <br>
     * The default file path {@code Config#DEFAULT_CONFIG_FILE} will be used instead
     * if {@code configFilePath} is null.
     */
    protected Config initConfig(String configFilePath) {
        Config initializedConfig;
        String configFilePathUsed;

        configFilePathUsed = Config.DEFAULT_CONFIG_FILE;

        if (configFilePath != null) {
            logger.info("Custom Config file specified " + configFilePath);
            configFilePathUsed = configFilePath;
        }

        logger.info("Using config file : " + configFilePathUsed);

        try {
            Optional<Config> configOptional = ConfigUtil.readConfig(configFilePathUsed);
            initializedConfig = configOptional.orElse(new Config());
        } catch (DataConversionException e) {
            logger.warning("Config file at " + configFilePathUsed + " is not in the correct format. "
                    + "Using default config properties");
            initializedConfig = new Config();
        }

        //Update config file in case it was missing to begin with or there are new/unused fields
        try {
            ConfigUtil.saveConfig(initializedConfig, configFilePathUsed);
        } catch (IOException e) {
            logger.warning("Failed to save config file : " + StringUtil.getDetails(e));
        }
        return initializedConfig;
    }

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     */
    protected UserPrefs initPrefs(UserPrefsStorage storage) {
        String prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using prefs file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataConversionException e) {
            logger.warning("UserPrefs file at " + prefsFilePath + " is not in the correct format. "
                    + "Using default user prefs");
            initializedPrefs = new UserPrefs();
        } catch (IOException e) {
            logger.warning("Problem while reading from the file. Will be starting with an empty AddressBook");
            initializedPrefs = new UserPrefs();
        }

        //Update prefs file in case it was missing to begin with or there are new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save config file : " + StringUtil.getDetails(e));
        }

        return initializedPrefs;
    }

    private void initEventsCenter() {
        EventsCenter.getInstance().registerHandler(this);
    }

    /**
     * Initialize {@link LockManager} based on the password hash in {@code userPrefs}.
     */
    private void initLockManager(UserPrefs userPrefs) {
        LockManager.getInstance().initialize(userPrefs.getPasswordHash());
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting Bibliotek " + MainApp.VERSION);
        ui.start(primaryStage);

        if (LockManager.getInstance().isPasswordProtected()) {
            EventsCenter.getInstance().post(new NewResultAvailableEvent("Unlock the app using the unlock command."));
        }
    }

    @Override
    public void stop() {
        logger.info("============================ [ Stopping Bibliotek ] =============================");
        network.stop();
        ui.stop();
        try {
            storage.saveUserPrefs(userPrefs);
            storage.saveRecentBooksList(model.getRecentBooksListAsBookShelf());
        } catch (IOException e) {
            logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
        }
        Platform.exit();
        System.exit(0);
    }

    @Subscribe
    public void handleAppUnlockedEvent(AppUnlockedEvent event) throws Exception {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        if (!storage.isBookShelfLoaded()) {
            storage.readBookShelf().ifPresent(bookShelf -> model.resetData(bookShelf));
            storage.readRecentBooksList().ifPresent(bookShelf -> model.resetRecentBooks(bookShelf));
        }
    }

    @Subscribe
    public void handlePasswordChangedEvent(PasswordChangedEvent event) throws Exception {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        userPrefs.setPasswordHash(CipherEngine.hashPassword(event.newPassword));
        try {
            storage.saveUserPrefs(userPrefs);
            if (event.oldPassword.length() > 0) {
                CipherEngine.decryptFile(storage.getBookShelfFilePath(), event.oldPassword);
                CipherEngine.decryptFile(storage.getRecentBooksFilePath(), event.oldPassword);
            }
            if (event.newPassword.length() > 0) {
                CipherEngine.encryptFile(storage.getBookShelfFilePath(), event.newPassword);
                CipherEngine.encryptFile(storage.getRecentBooksFilePath(), event.newPassword);
            }
        } catch (IOException e) {
            logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
        }
    }

    @Subscribe
    public void handleExitAppRequestEvent(ExitAppRequestEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        this.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
