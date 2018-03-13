package seedu.address.model.book;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import seedu.address.model.UniqueList;

/**
 * Contains data about a single book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Book {

    private final UniqueList<Author> authors;
    private final Title title;
    private final UniqueList<Category> categories;
    private final Description description;
    private final Rate rate;

    // TODO add more fields: gid, isbn13, publisher, publishedDate

    /**
     * Every field must be present and not null.
     */
    public Book(Set<Author> authors, Title title, Set<Category> categories, Description description) {
        requireAllNonNull(authors, title, categories, description);
        this.authors = new UniqueList<>(authors);
        this.title = title;
        this.categories = new UniqueList<>(categories);
        this.description = description;
        this.rate = new Rate("-1");
    }

    public Book(Set<Author> authors, Title title, Set<Category> categories, Description description, Rate rate) {
        requireAllNonNull(authors, title, categories, description, rate);
        this.authors = new UniqueList<>(authors);
        this.title = title;
        this.categories = new UniqueList<>(categories);
        this.description = description;
        this.rate = rate;
    }

    /**
     * Returns an immutable authors set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Author> getAuthors() {
        return Collections.unmodifiableSet(authors.toSet());
    }

    public Title getTitle() {
        return title;
    }

    /**
     * Returns an immutable categories set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Category> getCategories() {
        return Collections.unmodifiableSet(categories.toSet());
    }

    public Description getDescription() {
        return description;
    }

    public Rate getRate() {
        return rate;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Book)) {
            return false;
        }

        Book otherBook = (Book) other;
        return otherBook.getTitle().equals(this.getTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(title);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(getTitle())
                .append(" - Authors: ");
        getAuthors().forEach(author -> builder.append("[").append(author).append("]"));
        builder.append(" Categories: ");
        getCategories().forEach(category -> builder.append("[").append(category).append("]"));
        return builder.toString();
    }

}
