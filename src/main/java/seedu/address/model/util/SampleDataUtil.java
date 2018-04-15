package seedu.address.model.util;

import java.net.URL;

import seedu.address.MainApp;
import seedu.address.commons.util.XmlUtil;
import seedu.address.model.ReadOnlyBookShelf;
import seedu.address.storage.XmlSerializableBookShelf;

/**
 * Contains utility methods for populating {@code BookShelf} with sample data.
 */
public final class SampleDataUtil {

    private static final URL SAMPLE_BOOK_SHELF = MainApp.class.getResource("/data/sampleBookShelf.xml");

    private SampleDataUtil() {} // prevents instantiation

    public static ReadOnlyBookShelf getSampleBookShelf() {
        try {
            XmlSerializableBookShelf data = XmlUtil.getDataFromUrl(SAMPLE_BOOK_SHELF, XmlSerializableBookShelf.class);
            return data.toModelType();
        } catch (Exception e) {
            throw new AssertionError("Cannot load sample book shelf data.");
        }
    }

}
