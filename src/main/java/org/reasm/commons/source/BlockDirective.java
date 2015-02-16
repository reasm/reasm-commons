package org.reasm.commons.source;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.source.SourceNode;

/**
 * A directive recognized as a block delimiter by the parser.
 *
 * @author Francis Gagné
 */
@Immutable
public final class BlockDirective {

    /**
     * Gets the {@link BlockDirective} of a {@link SourceNode}, if it has one.
     *
     * @param sourceNode
     *            a {@link SourceNode}
     * @return {@link BlockDirectiveLine#getBlockDirective()} if the node is a {@link BlockDirectiveLine}, or <code>null</code>
     *         otherwise
     */
    @CheckForNull
    public static BlockDirective getBlockDirective(@CheckForNull SourceNode sourceNode) {
        if (sourceNode instanceof BlockDirectiveLine) {
            return ((BlockDirectiveLine) sourceNode).getBlockDirective();
        }

        return null;
    }

    @Nonnull
    private final String mnemonic;

    /**
     * Initializes a new BlockDirective.
     *
     * @param mnemonic
     *            the mnemonic that identifies this block directive
     */
    public BlockDirective(@Nonnull String mnemonic) {
        this.mnemonic = mnemonic;
    }

    /**
     * Gets the mnemonic that identifies this block directive.
     *
     * @return the mnemonic
     */
    @Nonnull
    public final String getMnemonic() {
        return this.mnemonic;
    }

}
