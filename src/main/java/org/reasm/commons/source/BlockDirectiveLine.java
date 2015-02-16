package org.reasm.commons.source;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.source.SourceNode;

/**
 * Wraps a {@link LogicalLine} whose mnemonic is a directive that delimits a block.
 *
 * @author Francis Gagné
 */
@Immutable
public abstract class BlockDirectiveLine extends SourceNode {

    @Nonnull
    private final LogicalLine logicalLine;
    @Nonnull
    private final BlockDirective blockDirective;

    /**
     * Initializes a new BlockDirectiveLine.
     *
     * @param logicalLine
     *            the {@link LogicalLine} that this BlockDirectiveLine wraps
     * @param blockDirective
     *            the {@link BlockDirective} on the logical line
     */
    protected BlockDirectiveLine(@Nonnull LogicalLine logicalLine, @Nonnull BlockDirective blockDirective) {
        super(logicalLine.getLength(), logicalLine.getParseError());
        this.logicalLine = logicalLine;
        this.blockDirective = blockDirective;
    }

    /**
     * Gets the {@link BlockDirective} on this line.
     *
     * @return the {@link BlockDirective}
     */
    @Nonnull
    public final BlockDirective getBlockDirective() {
        return this.blockDirective;
    }

    /**
     * Gets the wrapped {@link LogicalLine}.
     *
     * @return the {@link LogicalLine}
     */
    @Nonnull
    public final LogicalLine getLogicalLine() {
        return this.logicalLine;
    }

}
