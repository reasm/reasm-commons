package org.reasm.commons.source;

import java.util.Map;

import javax.annotation.Nonnull;

/**
 * A factory of {@link BlockDirectiveLine} objects.
 *
 * @author Francis Gagné
 *
 * @see Parser#Parser(Syntax, Map, Map, LogicalLineFactory, BlockDirectiveLineFactory)
 */
public interface BlockDirectiveLineFactory {

    /**
     * Creates a new {@link BlockDirectiveLine}.
     *
     * @param logicalLine
     *            the {@link LogicalLine} that this BlockDirectiveLine wraps
     * @param blockDirective
     *            the {@link BlockDirective} on the logical line
     * @return the new {@link BlockDirectiveLine}
     */
    @Nonnull
    BlockDirectiveLine createBlockDirectiveLine(@Nonnull LogicalLine logicalLine, @Nonnull BlockDirective blockDirective);

    /**
     * Gets the type of {@link BlockDirectiveLine} that this factory produces.
     *
     * @return the specific subclass of {@link BlockDirectiveLine} that this factory produces
     */
    @Nonnull
    Class<? extends BlockDirectiveLine> getOutputType();

}
