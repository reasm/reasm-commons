package org.reasm.commons.source;

import static org.junit.Assert.fail;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.AssemblyBuilder;
import org.reasm.source.CompositeSourceNode;
import org.reasm.source.ParseError;
import org.reasm.source.SourceNode;

/**
 * A <code>BLOCK0</code> block.
 *
 * @author Francis Gagné
 */
@Immutable
public final class Block0Block extends CompositeSourceNode {

    /**
     * Initializes a new Block0Block.
     *
     * @param childNodes
     *            the child nodes
     * @param parseError
     *            the parse error on the source node, or <code>null</code> if no parse error occurred
     */
    public Block0Block(@Nonnull Iterable<? extends SourceNode> childNodes, @CheckForNull ParseError parseError) {
        super(childNodes, parseError);
    }

    /**
     * Always throws an {@link AssertionError}.
     */
    @Override
    protected void assembleCore(AssemblyBuilder builder) throws IOException {
        fail();
    }

}
