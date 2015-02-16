package org.reasm.commons.source;

import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * Defines some syntax rules for an assembly language.
 *
 * @author Francis Gagné
 */
@Immutable
public final class Syntax {

    /**
     * Determines whether the specified code point is a binary digit.
     *
     * @param codePoint
     *            the code point to check
     * @return <code>true</code> if the code point is either U+0030 or U+0031; otherwise, <code>false</code>
     */
    public static boolean isBinDigit(int codePoint) {
        return codePoint == '0' || codePoint == '1';
    }

    /**
     * Determines whether the specified code point is a decimal digit.
     *
     * @param codePoint
     *            the code point to check
     * @return <code>true</code> if the code point is in the range U+0030 to U+0039 (inclusive); otherwise, <code>false</code>
     */
    public static boolean isDigit(int codePoint) {
        return codePoint >= '0' && codePoint <= '9';
    }

    /**
     * Determines whether the specified code point is a hexadecimal digit.
     *
     * @param codePoint
     *            the code point to check
     * @return <code>true</code> if the code point is in one of these ranges: U+0030 to U+0039 (inclusive), U+0041 to U+0046
     *         (inclusive) or U+0061 to U+0066 (inclusive); otherwise, <code>false</code>
     */
    public static boolean isHexDigit(int codePoint) {
        return isDigit(codePoint) || codePoint >= 'A' && codePoint <= 'F' || codePoint >= 'a' && codePoint <= 'f';
    }

    /**
     * Determines whether the specified code point is valid as the first code point of an identifier.
     *
     * @param codePoint
     *            the code point to check
     * @return <code>true</code> if the code point is valid as the first code point of an identifier; otherwise, <code>false</code>
     */
    public static boolean isValidNumberInitialCodePoint(int codePoint) {
        return codePoint == '.' || isDigit(codePoint);
    }

    /**
     * Determines whether the specified code point represents whitespace or not.
     *
     * @param codePoint
     *            the code point to test
     * @return <code>true</code> if the code point represents whitespace, otherwise <code>false</code>
     */
    public static boolean isWhitespace(int codePoint) {
        switch (codePoint) {
        case '\t':
        case '\n':
        case '\f':
        case '\r':
        case ' ':
            return true;
        }

        return false;
    }

    @Nonnull
    private final int[] invalidIdentifierCodePoints;

    @Nonnull
    private final int[] invalidIdentifierInitialCodePoints;

    /**
     * Initializes a new Syntax.
     *
     * @param invalidIdentifierCodePoints
     *            an array that contains the code points that are invalid anywhere in an identifier
     * @param invalidIdentifierInitialCodePoints
     *            an array that contains the code points that are invalid as the initial code point of an identifier
     */
    public Syntax(@Nonnull int[] invalidIdentifierCodePoints, @Nonnull int[] invalidIdentifierInitialCodePoints) {
        if (invalidIdentifierCodePoints == null) {
            throw new NullPointerException("invalidIdentifierCodePoints");
        }

        if (invalidIdentifierInitialCodePoints == null) {
            throw new NullPointerException("invalidIdentifierInitialCodePoints");
        }

        this.invalidIdentifierCodePoints = invalidIdentifierCodePoints.clone();
        this.invalidIdentifierInitialCodePoints = invalidIdentifierInitialCodePoints.clone();

        Arrays.sort(this.invalidIdentifierCodePoints);
        Arrays.sort(this.invalidIdentifierInitialCodePoints);
    }

    /**
     * Determines whether the specified code point is valid in an identifier.
     *
     * @param codePoint
     *            the code point to check
     * @return <code>true</code> if the code point is valid in an identifier; otherwise, <code>false</code>
     */
    public final boolean isValidIdentifierCodePoint(int codePoint) {
        switch (codePoint) {
        case '(': // grouping left parenthesis; start of function call argument list
        case ')': // grouping right parenthesis; end of function call argument list
        case ',': // operand separator, argument separator
        case ';': // start of comment
            return false;
        }

        return !isWhitespace(codePoint) && Arrays.binarySearch(this.invalidIdentifierCodePoints, codePoint) < 0
                && Character.isValidCodePoint(codePoint);
    }

    /**
     * Determines whether the specified code point is valid as the first code point of an identifier.
     *
     * @param codePoint
     *            the code point to check
     * @return <code>true</code> if the code point is valid as the first code point of an identifier; otherwise, <code>false</code>
     */
    public final boolean isValidIdentifierInitialCodePoint(int codePoint) {
        return !isValidNumberInitialCodePoint(codePoint)
                && Arrays.binarySearch(this.invalidIdentifierInitialCodePoints, codePoint) < 0
                && this.isValidIdentifierCodePoint(codePoint);
    }
}
