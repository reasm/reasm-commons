package org.reasm.commons.source;

import java.util.Map;

import javax.annotation.Nonnull;

/**
 * A factory of {@link LogicalLine} objects.
 *
 * @author Francis Gagné
 *
 * @see Parser#Parser(Syntax, Map, Map, LogicalLineFactory, BlockDirectiveLineFactory)
 */
public interface LogicalLineFactory {

    /**
     * Creates a new {@link LogicalLine}.
     *
     * @param attributes
     *            the logical line's attributes
     * @return the new {@link LogicalLine}
     */
    @Nonnull
    LogicalLine createLogicalLine(@Nonnull LogicalLineAttributes attributes);

    /**
     * Gets the type of {@link LogicalLine} that this factory produces.
     *
     * @return the specific subclass of {@link LogicalLine} that this factory produces
     */
    @Nonnull
    Class<? extends LogicalLine> getOutputType();

}
