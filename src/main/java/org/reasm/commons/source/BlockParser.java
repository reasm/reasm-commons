package org.reasm.commons.source;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.source.SourceNode;

/**
 * Base class for block parsers.
 *
 * @author Francis Gagné
 */
@Immutable
public abstract class BlockParser {

    /**
     * Gets the list of types of {@link SourceNode} that this block parser can emit.
     *
     * @return an {@link Iterable} that iterates over the types of {@link SourceNode} that this block parser can emit
     */
    @Nonnull
    public abstract Iterable<Class<? extends SourceNode>> getOutputNodeTypes();

    /**
     * Parses a block.
     *
     * @param sourceNodeProducer
     *            a {@link SourceNodeProducer} that emits source nodes from the source file being parsed
     * @param firstLine
     *            the first line (that has already been parsed) of the block
     * @return the parsed block
     */
    @Nonnull
    abstract SourceNode parseBlock(@Nonnull SourceNodeProducer sourceNodeProducer, @Nonnull BlockDirectiveLine firstLine);

}
