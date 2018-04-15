package seedu.address.logic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.FileUtil;
import seedu.address.commons.util.StringUtil;

//@@author 592363789
/**
 *  Class for encrypt and decrypt file and key.
 */
public class CipherEngine {
    private static final Logger logger = LogsCenter.getLogger(CipherEngine.class);

    private static final String PBKDF_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int PBKDF_ITERATION_COUNT = 22000;
    private static final int PBKDF_KEY_LENGTH = 256;
    private static final int SALT_BYTE_LENGTH = 64;
    private static final int IV_BYTE_LENGTH = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     *  Encrypts file at {@code fileName} using {@code password}.
     */
    public static void encryptFile(String fileName, String password) {
        String tempFileName = StringUtil.generateRandomPrefix() + "_temp.enc";
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(fileName);
            fos = new FileOutputStream(tempFileName);
            encryptStream(password, fis, fos);
        } catch (Exception e) {
            logger.warning("Could not encrypt file " + fileName);
            logger.warning(StringUtil.getDetails(e));
        } finally {
            closeStreams(fis, fos);
        }

        replaceFile(fileName, tempFileName);
    }

    /**
     *  Decrypts file at {@code fileName} using {@code password}.
     */
    public static void decryptFile(String fileName, String password) {
        String tempFileName = StringUtil.generateRandomPrefix() + "_temp.enc";
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(fileName);
            fos = new FileOutputStream(tempFileName);
            decryptStream(password, fis, fos);
        } catch (Exception e) {
            logger.warning("Could not decrypt file " + fileName);
            logger.warning(StringUtil.getDetails(e));
        } finally {
            closeStreams(fis, fos);
        }

        replaceFile(fileName, tempFileName);
    }

    /**
     * Replaces the file at {@code toReplace} with the file at {@code replacement}.
     */
    private static void replaceFile(String toReplace, String replacement) {
        File dest = new File(toReplace);
        File src = new File(replacement);

        try {
            FileUtil.copyFile(src, dest);
            src.delete();
        } catch (IOException e) {
            logger.warning("Could not replace file " + toReplace + " with " + replacement);
        }
    }

    //@@author
    /**
     * Returns a salted and hashed password to be used for storage.
     */
    public static String hashPassword(String password) throws Exception {
        DerivedKey derivedKey = deriveKey(password, null, PBKDF_ITERATION_COUNT, PBKDF_KEY_LENGTH);
        Base64.Encoder enc = Base64.getEncoder();
        return PBKDF_ALGORITHM + "$" + PBKDF_ITERATION_COUNT + "$" + PBKDF_KEY_LENGTH + "$"
                + enc.encodeToString(derivedKey.hash) + "$" + enc.encodeToString(derivedKey.salt);
    }

    /**
     * Returns true if the given {@code password} matches the {@code passwordHash}, or false if otherwise.
     */
    protected static boolean checkPassword(String password, String passwordHash) throws Exception {
        String[] split = passwordHash.split("\\$");
        if (split.length != 5) {
            throw new IllegalArgumentException("Invalid hash.");
        }

        int iterationCount = Integer.parseInt(split[1]);
        int keyLength = Integer.parseInt(split[2]);
        Base64.Decoder dec = Base64.getDecoder();

        DerivedKey derivedKey = deriveKey(password, dec.decode(split[4]), iterationCount, keyLength);
        return Arrays.equals(dec.decode(split[3]), derivedKey.hash);
    }

    /**
     * Returns true if the given {@code hash} is a valid password hash, or false if otherwise.
     */
    protected static boolean isValidPasswordHash(String hash) {
        String[] split = hash.split("\\$");
        if (split.length != 5) {
            return false;
        }
        return split[1].matches("\\d+") && split[2].matches("\\d+");
    }

    // Reused from http://www.novixys.com/blog/aes-encryption-decryption-password-java/
    /**
     * Encrypts the input stream using the given password, writing the results to the output stream.
     */
    private static void encryptStream(String password, InputStream input, OutputStream output) throws Exception {
        DerivedKey derivedKey = deriveKey(password, null, PBKDF_ITERATION_COUNT, PBKDF_KEY_LENGTH);
        SecretKeySpec skey = new SecretKeySpec(derivedKey.hash, "AES");

        byte[] iv = new byte[IV_BYTE_LENGTH];
        RANDOM.nextBytes(iv);
        IvParameterSpec ivspec = new IvParameterSpec(iv);

        output.write(derivedKey.salt);
        output.write(iv);

        Cipher ci = Cipher.getInstance(AES_ALGORITHM);
        ci.init(Cipher.ENCRYPT_MODE, skey, ivspec);

        processFile(ci, input, output);
    }

    // Reused from http://www.novixys.com/blog/aes-encryption-decryption-password-java/
    /**
     * Decrypts the input stream using the given password, writing the results to the output stream.
     */
    private static void decryptStream(String password, InputStream input, OutputStream output) throws Exception {
        byte[] salt = new byte[SALT_BYTE_LENGTH];
        byte[] iv = new byte[IV_BYTE_LENGTH];
        input.read(salt);
        input.read(iv);

        DerivedKey derivedKey = deriveKey(password, salt, PBKDF_ITERATION_COUNT, PBKDF_KEY_LENGTH);
        SecretKeySpec skey = new SecretKeySpec(derivedKey.hash, "AES");

        Cipher ci = Cipher.getInstance(AES_ALGORITHM);
        ci.init(Cipher.DECRYPT_MODE, skey, new IvParameterSpec(iv));

        processFile(ci, input, output);
    }

    // Reused from http://www.novixys.com/blog/aes-encryption-decryption-password-java/
    /**
     * Uses the given cipher to process the input stream, writing the results to the output stream.
     */
    private static void processFile(Cipher cipher, InputStream input, OutputStream output)
            throws BadPaddingException, IllegalBlockSizeException, IOException {
        byte[] ibuf = new byte[1024];
        int len;
        while ((len = input.read(ibuf)) != -1) {
            byte[] obuf = cipher.update(ibuf, 0, len);
            if (obuf != null) {
                output.write(obuf);
            }
        }
        byte[] obuf = cipher.doFinal();
        if (obuf != null) {
            output.write(obuf);
        }
    }

    /**
     * Flushes and closes the given streams.
     */
    private static void closeStreams(InputStream input, OutputStream output) {
        try {
            input.close();
            output.flush();
            output.close();
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }
    }

    /**
     * Derives a key from the given {@code password}. The {@code salt} used for the encryption
     * can optionally be specified. If {@code salt} is {@code null}, a new salt will be generated.
     */
    private static DerivedKey deriveKey(String password, byte[] salt,
                                        int iterationCount, int keyLength) throws Exception {
        if (salt == null) {
            salt = new byte[SALT_BYTE_LENGTH];
            RANDOM.nextBytes(salt);
        }
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterationCount, keyLength);
        SecretKey key = SecretKeyFactory.getInstance(PBKDF_ALGORITHM).generateSecret(spec);
        return new DerivedKey(salt, key.getEncoded());
    }

    /** Represents the results of a key derivation function. Contains the salt and hash. */
    private static class DerivedKey {
        private final byte[] salt;
        private final byte[] hash;

        private DerivedKey(byte[] salt, byte[] hash) {
            this.salt = salt;
            this.hash = hash;
        }
    }
}
