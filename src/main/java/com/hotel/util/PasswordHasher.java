package com.hotel.util;

import org.mindrot.jbcrypt.BCrypt;


/**
 * Utility class for password hashing using BCrypt.
 */
public class PasswordHasher {

    /**
     * Hashes a plain text password.
     * 
     * @param password the plain text password
     * @return the hashed password
     */
    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    /**
     * Verifies a plain text password against a hashed password.
     * 
     * @param password the plain text password
     * @param hashed the hashed password to check against
     * @return true if the password matches, false otherwise
     */
    public static boolean check(String password, String hashed) {
        if (hashed == null || !hashed.startsWith("$2a$")) {
            return false;
        }
        return BCrypt.checkpw(password, hashed);
    }
}
