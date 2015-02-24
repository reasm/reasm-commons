package org.reasm.commons.source;

import javax.annotation.Nonnull;

import org.reasm.SubstringBounds;
import org.reasm.source.SourceLocation;

import ca.fragag.text.DocumentReader;

/**
 * An object for progressively reading from a portion of a logical line.
 *
 * @author Francis Gagné
 */
public final class LogicalLineReader {

    private DocumentReader reader;
    private LogicalLine logicalLine;
    private int startOfLogicalLine;
    private int endOfRange;

    /**
     * Advances this reader to the next code point, skipping continuation characters.
     */
    public final void advance() {
        this.reader.advance();
        this.skipContinuationCharacters();
    }

    /**
     * Determines whether the end of the range set by the last call to {@link #setRange(SourceLocation, SubstringBounds)} or
     * {@link #setRange(SourceLocation, int, int)} has been reached.
     *
     * @return <code>true</code> if the end of the range has been reached, or <code>false</code> otherwise
     */
    public final boolean atEnd() {
        return this.reader.getCurrentPosition() >= this.endOfRange;
    }

    /**
     * Gets this reader's current position, for restoring later with {@link #restorePosition(int)}.
     *
     * @return the reader's current position
     */
    public final int backupPosition() {
        return this.reader.getCurrentPosition();
    }

    /**
     * Gets the code point at this reader's current position.
     *
     * @return the current code point
     */
    public final int getCurrentCodePoint() {
        return this.reader.getCurrentCodePoint();
    }

    /**
     * Reads the text on the logical line from this reader's current position to the end of the range set by the last call to
     * {@link #setRange(SourceLocation, SubstringBounds)} or {@link #setRange(SourceLocation, int, int)} and returns the text as a
     * {@link String}. The reader is advanced to the end of the range.
     *
     * @return a {@link String} containing the text on the rest of the range
     */
    @Nonnull
    public final String readToString() {
        final StringBuilder sb = new StringBuilder();
        while (!this.atEnd()) {
            sb.appendCodePoint(this.reader.getCurrentCodePoint());
            this.advance();
        }

        return sb.toString();
    }

    /**
     * Sets this reader's current position. This method doesn't check if the specified position lies within the active range or if
     * that position would be skipped due to continuation characters. It should only be called with values returned from
     * {@link #backupPosition()} since the last call to {@link #setRange(SourceLocation, SubstringBounds)} or
     * {@link #setRange(SourceLocation, int, int)}.
     *
     * @param position
     *            the new reader's position
     */
    public final void restorePosition(int position) {
        this.reader.setCurrentPosition(position);
    }

    /**
     * Sets the logical line and the range within that logical line to read from.
     *
     * @param sourceLocation
     *            a {@link SourceLocation} that references a {@link LogicalLine}
     * @param start
     *            the start (inclusive) position of the range, relative to the start of the logical line
     * @param end
     *            the end (exclusive) position of the range, relative to the start of the logical line
     */
    public final void setRange(@Nonnull SourceLocation sourceLocation, int start, int end) {
        if (sourceLocation == null) {
            throw new NullPointerException("sourceLocation");
        }

        // Obtain the logical line before setting any fields.
        final LogicalLine logicalLine = SourceLocationUtils.getLogicalLineRequired(sourceLocation);

        if (this.reader == null || sourceLocation.getFile().getText() != this.reader.getDocument()) {
            this.reader = new DocumentReader(sourceLocation.getFile().getText());
        }

        this.logicalLine = logicalLine;
        this.startOfLogicalLine = sourceLocation.getTextPosition();
        this.reader.setCurrentPosition(this.startOfLogicalLine + start);
        this.endOfRange = this.startOfLogicalLine + end;
        this.skipContinuationCharacters();
    }

    /**
     * Sets the logical line and the range within that logical line to read from.
     *
     * @param sourceLocation
     *            a {@link SourceLocation} that references a {@link LogicalLine}
     * @param bounds
     *            a {@link SubstringBounds} that contains the start (inclusive) and the end (exclusive) position of the range,
     *            relative to the start of the logical line
     */
    public final void setRange(@Nonnull SourceLocation sourceLocation, @Nonnull SubstringBounds bounds) {
        if (sourceLocation == null) {
            throw new NullPointerException("sourceLocation");
        }

        if (bounds == null) {
            throw new NullPointerException("bounds");
        }

        this.setRange(sourceLocation, bounds.getStart(), bounds.getEnd());
    }

    /**
     * Advances this reader to the next code point that is not whitespace, skipping continuation characters on the way.
     */
    public final void skipWhitespace() {
        while (!this.atEnd() && Syntax.isWhitespace(this.reader.getCurrentCodePoint())) {
            this.advance();
        }
    }

    private final void skipContinuationCharacters() {
        while (this.logicalLine.isContinuationCharacter(this.reader.getCurrentPosition() - this.startOfLogicalLine)) {
            this.reader.advance();

            // Skip the line separator and the leading whitespace on the following line.
            while (!this.atEnd() && Syntax.isWhitespace(this.reader.getCurrentCodePoint())) {
                this.reader.advance();
            }
        }
    }

}
