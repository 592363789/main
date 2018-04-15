package seedu.address.logic;

//@@author 592363789
/**
 * Manages the locked/unlocked state of the app and the password.
 */
public class LockManager {

    public static final String NO_PASSWORD = "";

    private static LockManager lockManager;

    private String password = NO_PASSWORD;
    private String passwordHash = NO_PASSWORD;
    private boolean isLocked = false;
    private boolean hasLoggedIn = false;

    private LockManager() { }

    public static LockManager getInstance() {
        if (lockManager == null) {
            lockManager = new LockManager();
        }
        return lockManager;
    }

    /**
     * Sets up the {@code LockManager}. The state is set to locked if
     * there is a non-empty and valid {@code passwordHash}.
     */
    public void initialize(String passwordHash) {
        if (passwordHash.trim().length() > 0 && CipherEngine.isValidPasswordHash(passwordHash.trim())) {
            this.passwordHash = passwordHash;
        } else {
            this.passwordHash = NO_PASSWORD;
            this.password = NO_PASSWORD;
        }
        isLocked = !this.passwordHash.isEmpty();
        hasLoggedIn = !isLocked;
    }

    public boolean hasLoggedIn() {
        return hasLoggedIn;
    }

    /**
     * Changes the password to {@code newPassword} if {@code oldPassword} matches the old password.
     * @return whether the password is changed.
     */
    public boolean setPassword(String oldPassword, String newPassword) {
        if (!checkPassword(oldPassword)) {
            return false;
        }

        String newPasswordHash;
        try {
            newPasswordHash = newPassword.isEmpty() ? "" : CipherEngine.hashPassword(newPassword);
        } catch (Exception e) {
            return false;
        }

        password = newPassword;
        passwordHash = newPasswordHash;
        return true;
    }

    /**
     * Locks the app.
     */
    public void lock() {
        isLocked = true;
    }

    /**
     * Unlocks the app if {@code password} matches the current password.
     * @return whether the app is unlocked.
     */
    public boolean unlock(String password) {
        if (!isLocked() || !checkPassword(password)) {
            return false;
        }

        isLocked = false;
        hasLoggedIn = true;
        this.password = password;
        return true;
    }

    public String getPassword() {
        return password;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public boolean isPasswordProtected() {
        return !passwordHash.isEmpty();
    }

    //@@author
    /** Returns true if the given {@code password} matches the current password. */
    private boolean checkPassword(String password) {
        if (!isPasswordProtected() && password.isEmpty()) {
            return true;
        }

        try {
            return CipherEngine.checkPassword(password, passwordHash);
        } catch (Exception e) {
            return false;
        }
    }
}
