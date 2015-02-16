package org.reasm.commons.source;

import java.util.ArrayList;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.commons.parseerrors.UnclosedBlockParseError;
import org.reasm.source.ParseError;
import org.reasm.source.SimpleCompositeSourceNode;
import org.reasm.source.SourceNode;

/**
 * Parser for blocks that end with a specific {@link BlockDirective}.
 *
 * @author Francis Gagné
 */
@Immutable
public abstract class BasicBlockParser extends BlockParser {

    @Nonnull
    private final BlockDirective endingDirective;

    /**
     * Initializes a new BasicBlockParser.
     *
     * @param endingDirective
     *            the {@link BlockDirective} that marks the end of the block
     */
    public BasicBlockParser(@Nonnull BlockDirective endingDirective) {
        this.endingDirective = endingDirective;
    }

    /**
     * Creates a source node that represents the block.
     *
     * @param childNodes
     *            the child nodes of the block
     * @param parseError
     *            the parse error on the block, if any
     * @return the block
     */
    @Nonnull
    protected abstract SourceNode createBlock(@Nonnull Iterable<? extends SourceNode> childNodes,
            @CheckForNull ParseError parseError);

    /**
     * Creates as source node that represents the block's body.
     * <p>
     * The default implementation creates a {@link SimpleCompositeSourceNode}.
     *
     * @param childNodes
     *            the child nodes of the block's body
     * @return the block's body
     */
    @Nonnull
    protected SourceNode createBodyBlock(@Nonnull Iterable<? extends SourceNode> childNodes) {
        return new SimpleCompositeSourceNode(childNodes);
    }

    @Override
    final SourceNode parseBlock(SourceNodeProducer sourceNodeProducer, BlockDirectiveLine firstLine) {
        final ArrayList<SourceNode> nodes = new ArrayList<>(3);
        nodes.add(firstLine);

        final ArrayList<SourceNode> bodyNodes = new ArrayList<>();

        while (!sourceNodeProducer.atEnd()) {
            final SourceNode sourceNode = sourceNodeProducer.next();

            // Check if this logical line has a block directive.
            final BlockDirective blockDirective = BlockDirective.getBlockDirective(sourceNode);

            if (blockDirective == this.endingDirective) {
                nodes.add(this.createBodyBlock(bodyNodes));
                nodes.add(sourceNode);
                return this.createBlock(nodes, null);
            }

            sourceNodeProducer.getParser().processBlockBodyLine(sourceNodeProducer, bodyNodes, sourceNode, blockDirective);
        }

        // We didn't find the end of the block: return with an error.
        nodes.add(this.createBodyBlock(bodyNodes));
        return this.createBlock(nodes, new UnclosedBlockParseError(firstLine.getBlockDirective()));
    }

}
