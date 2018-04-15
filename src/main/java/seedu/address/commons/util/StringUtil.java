package seedu.address.commons.util;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Random;

/**
 * Helper functions for handling strings.
 */
public class StringUtil {

    private static final int RANDOM_BYTE_LENGTH = 9;

    /**
     * Returns true if the {@code sentence} contains the {@code word}.
     *   Ignores case, but a full word match is required.
     *   <br>examples:<pre>
     *       containsWordIgnoreCase("ABc def", "abc") == true
     *       containsWordIgnoreCase("ABc def", "DEF") == true
     *       containsWordIgnoreCase("ABc def", "AB") == false //not a full word match
     *       </pre>
     * @param sentence cannot be null
     * @param word cannot be null, cannot be empty, must be a single word
     */
    public static boolean containsWordIgnoreCase(String sentence, String word) {
        requireNonNull(sentence);
        requireNonNull(word);

        String preppedWord = word.trim();
        checkArgument(!preppedWord.isEmpty(), "Word parameter cannot be empty");
        checkArgument(preppedWord.split("\\s+").length == 1, "Word parameter should be a single word");

        String preppedSentence = sentence;
        String[] wordsInPreppedSentence = preppedSentence.split("\\s+");

        for (String wordInSentence: wordsInPreppedSentence) {
            if (wordInSentence.equalsIgnoreCase(preppedWord)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a detailed message of the t, including the stack trace.
     */
    public static String getDetails(Throwable t) {
        requireNonNull(t);
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return t.getMessage() + "\n" + sw.toString();
    }

    /**
     * Returns true if {@code s} represents a non-zero unsigned integer
     * e.g. 1, 2, 3, ..., {@code Integer.MAX_VALUE} <br>
     * Will return false for any other non-null string input
     * e.g. empty string, "-1", "0", "+1", and " 2 " (untrimmed), "3 0" (contains whitespace), "1 a" (contains letters)
     * @throws NullPointerException if {@code s} is null.
     */
    public static boolean isNonZeroUnsignedInteger(String s) {
        requireNonNull(s);

        try {
            int value = Integer.parseInt(s);
            return value > 0 && !s.startsWith("+"); // "+1" is successfully parsed by Integer#parseInt(String)
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    /**
     * Returns the URL encoded form of the string {@code s}, or any empty string
     * if UTF-8 encoding is not supported.
     */
    public static String urlEncode(String s) {
        requireNonNull(s);
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    // Reused from http://www.extensionmethod.net/1555/csharp/string/isvalidurl
    /**
     * Checks whether the string {@code s} denotes a valid URL.
     */
    public static boolean isValidUrl(String s) {
        requireNonNull(s);
        return s.matches("http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=,'#]*)?");
    }

    /**
     * Replaces the {@code index} index of {@code target} with {@code replacement}.
     * @throws IndexOutOfBoundsException if {@code index} is invalid.
     */
    public static String replace(String target, char replacement, int index) {
        return target.substring(0, index) + replacement + target.substring(index + 1, target.length());
    }

    /**
     * Adds {@code toAdd} behind the {@code index} index of {@code target}.
     * @throws IndexOutOfBoundsException if {@code index} is invalid.
     */
    public static String addAfter(String target, char toAdd, int index) {
        return target.substring(0, index + 1) + toAdd + target.substring(index + 1, target.length());
    }

    /**
     * Removes the character at {@code index} index of {@code target}.
     * @throws IndexOutOfBoundsException if {@code index} is invalid.
     */
    public static String removeAt(String target, int index) {
        return target.substring(0, index) + target.substring(index + 1, target.length());
    }

    /**
     * Returns a string with the leading whitespace removed.
     */
    public static String leftTrim(String s) {
        int index = 0;
        while (index < s.length() && s.charAt(index) == ' ') {
            ++index;
        }
        return s.substring(index);
    }

    //@@author takuyakanbr
    /**
     * Returns a random 8 character string to be used as a prefix to a filename.
     */
    public static String generateRandomPrefix() {
        byte[] randomBytes = new byte[RANDOM_BYTE_LENGTH];
        new Random().nextBytes(randomBytes);
        byte[] encodedBytes = Base64.getEncoder().encode(randomBytes);
        return new String(encodedBytes).replace("/", "-");
    }
}
