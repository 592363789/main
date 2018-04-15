package guitests.guihandles;

import java.net.URL;

import javafx.scene.Node;
import seedu.address.ui.WebViewManager;

//@@author qiu-siqi
/**
 * Provides a handle for the {@code BookInLibraryPanel} of the UI.
 */
public class BookInLibraryPanelHandle extends NodeHandle<Node> {
    public static final String BOOK_IN_LIBRARY_PANEL_ID = "#bookInLibraryPanel";

    public BookInLibraryPanelHandle(Node bookInLibraryPanelNode) {
        super(bookInLibraryPanelNode);
    }

    public boolean isVisible() {
        return getRootNode().isVisible();
    }

    /**
     * Returns the {@code URL} of the currently loaded page.
     */
    public URL getLoadedUrl() {
        return WebViewUtil.getLoadedUrl(WebViewManager.getInstance().getWebView());
    }

}
