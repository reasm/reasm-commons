package org.reasm.commons.source;

import java.util.ArrayList;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.commons.parseerrors.ElseOrElseIfAfterElseParseError;
import org.reasm.commons.parseerrors.UnclosedBlockParseError;
import org.reasm.source.ParseError;
import org.reasm.source.SimpleCompositeSourceNode;
import org.reasm.source.SourceNode;

/**
 * Parser for <code>IF</code> blocks, with support for <code>ELSEIF</code> and <code>ELSE</code> clauses.
 *
 * @author Francis Gagné
 */
@Immutable
public abstract class IfBlockParser extends BlockParser {

    /**
     * Initializes a new IfBlockParser.
     */
    protected IfBlockParser() {
    }

    /**
     * Creates a source node that represents the <code>IF</code> block.
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
     * Determines whether the specified directive is an <code>ELSE</code> directive.
     *
     * @param blockDirective
     *            the {@link BlockDirective} to test
     * @return <code>true</code> if the directive is an <code>ELSE</code> directive, or <code>false</code> otherwise
     */
    protected abstract boolean isElseDirective(@Nonnull BlockDirective blockDirective);

    /**
     * Determines whether the specified directive is an <code>ELSEIF</code> directive.
     *
     * @param blockDirective
     *            the {@link BlockDirective} to test
     * @return <code>true</code> if the directive is an <code>ELSEIF</code> directive, or <code>false</code> otherwise
     */
    protected abstract boolean isElseIfDirective(@Nonnull BlockDirective blockDirective);

    /**
     * Determines whether the specified directive is an <code>ENDIF</code> directive.
     *
     * @param blockDirective
     *            the {@link BlockDirective} to test
     * @return <code>true</code> if the directive is an <code>ENDIF</code> directive, or <code>false</code> otherwise
     */
    protected abstract boolean isEndIfDirective(@Nonnull BlockDirective blockDirective);

    @Override
    final SourceNode parseBlock(SourceNodeProducer sourceNodeProducer, BlockDirectiveLine firstLine) {
        // The child nodes of an IfBlock are structured like this:
        //   (LogicalLine SimpleCompositeSourceNode)+ LogicalLine?
        // The LogicalLine in the repetition block is an IF, ELSEIF or ELSE directive.
        // The SimpleCompositeSourceNode following it is the body for that branch.
        // The last LogicalLine is an ENDIF directive (it may be missing).

        final ArrayList<SourceNode> nodes = new ArrayList<>();
        nodes.add(firstLine);

        ParseError parseError = null;
        ArrayList<SourceNode> bodyNodes = new ArrayList<>();
        boolean gotElse = false;

        while (!sourceNodeProducer.atEnd()) {
            final SourceNode sourceNode = sourceNodeProducer.next();

            // Check if this logical line has a block directive.
            final BlockDirective blockDirective = BlockDirective.getBlockDirective(sourceNode);

            // Get the mnemonic on this logical line, if any.
            boolean isElse = false;
            if (this.isEndIfDirective(blockDirective)) {
                nodes.add(new SimpleCompositeSourceNode(bodyNodes));
                nodes.add(sourceNode);
                return this.createBlock(nodes, parseError);
            } else if ((isElse = this.isElseDirective(blockDirective)) || this.isElseIfDirective(blockDirective)) {
                nodes.add(new SimpleCompositeSourceNode(bodyNodes));
                nodes.add(sourceNode);

                // SimpleCompositeSourceNode's constructor copies the contents of the list it receives,
                // so we can reuse our list.
                bodyNodes.clear();

                // If this is the first ELSE or ELSEIF clause
                // following the first ELSE clause of this IF block,
                // raise an error.
                if (gotElse && parseError == null) {
                    parseError = new ElseOrElseIfAfterElseParseError(blockDirective);
                }

                // If this is an ELSE clause,
                // set a flag to raise an error on subsequent ELSE or ELSEIF clauses.
                if (isElse) {
                    gotElse = true;
                }
            } else {
                sourceNodeProducer.getParser().processBlockBodyLine(sourceNodeProducer, bodyNodes, sourceNode, blockDirective);
            }
        }

        // We didn't find the end of the block: return with an error.
        nodes.add(new SimpleCompositeSourceNode(bodyNodes));
        return this.createBlock(nodes, new UnclosedBlockParseError(firstLine.getBlockDirective()));
    }

}
