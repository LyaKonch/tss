package bravo.utitity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for hashing operations.
 * <p>
 * Provides methods for generating secure hash values
 * using the SHA-256 algorithm.
 * <p>
 * This class is final and has a private constructor to prevent instantiation.
 */
public final class HashUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private HashUtils() {}

    /**
     * Generates a SHA-256 hash of the given input string and returns
     * its hexadecimal representation.
     *
     * @param input the input string to be hashed
     * @return the SHA-256 hash as a hexadecimal string
     * @throws RuntimeException if the SHA-256 algorithm is not available
     */
    public static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}