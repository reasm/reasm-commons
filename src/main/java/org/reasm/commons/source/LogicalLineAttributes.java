package org.reasm.commons.source;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.reasm.SubstringBounds;
import org.reasm.source.ParseError;

/**
 * Intermediate storage for the attributes of a {@link LogicalLine}.
 *
 * @author Francis Gagné
 *
 * @see LogicalLine#LogicalLine(LogicalLineAttributes)
 * @see LogicalLineFactory#createLogicalLine(LogicalLineAttributes)
 */
public final class LogicalLineAttributes {

    // This class exists to encapsulate the attributes calculated by the logical line parser
    // in order to avoid introducing runtime checks in LogicalLine's constructor
    // to ensure some invariants are maintained (e.g. that continuationCharacters is sorted;
    // that the arrays are not aliased, so they will not be mutated externally).

    final int length;
    @CheckForNull
    final ParseError parseError;
    @Nonnull
    final SubstringBounds[] labels;
    @CheckForNull
    final SubstringBounds mnemonic;
    @Nonnull
    final SubstringBounds[] operands;
    @CheckForNull
    final SubstringBounds comment;
    @Nonnull
    final int[] continuationCharacters;

    LogicalLineAttributes(int length, @CheckForNull ParseError parseError, @Nonnull SubstringBounds[] labels,
            @CheckForNull SubstringBounds mnemonic, @Nonnull SubstringBounds[] operands, @CheckForNull SubstringBounds comment,
            @Nonnull int[] continuationCharacters) {
        this.length = length;
        this.parseError = parseError;
        this.labels = labels;
        this.mnemonic = mnemonic;
        this.operands = operands;
        this.comment = comment;
        this.continuationCharacters = continuationCharacters;
    }

}
