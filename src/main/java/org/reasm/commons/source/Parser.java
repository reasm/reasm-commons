package org.reasm.commons.source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.reasm.source.SourceNode;

import ca.fragag.text.Document;
import ca.fragag.text.DocumentReader;

/**
 * A generic parser for source files.
 *
 * @author Francis Gagné
 */
public class Parser {

    private static Set<Class<? extends SourceNode>> calcOutputNodeTypes(@Nonnull LogicalLineFactory logicalLineFactory,
            @Nonnull BlockDirectiveLineFactory blockDirectiveLineFactory, @Nonnull Map<BlockDirective, BlockParser> blocks) {
        // This set contains the subclasses of SourceNode that can be generated as direct children of the root node.
        // Nodes of these types can be recycled.
        // Other types of nodes are part of the internal structure of other composite nodes.
        // For simplicity, we always reparse them. However, their children can be recycled.
        final Set<Class<? extends SourceNode>> outputNodeTypes = new HashSet<>();

        outputNodeTypes.add(logicalLineFactory.getOutputType());
        outputNodeTypes.add(blockDirectiveLineFactory.getOutputType());

        for (BlockParser blockParser : blocks.values()) {
            for (Class<? extends SourceNode> outputNodeType : blockParser.getOutputNodeTypes()) {
                outputNodeTypes.add(outputNodeType);
            }
        }

        final Set<Class<? extends SourceNode>> unmodifiableSet = Collections.unmodifiableSet(outputNodeTypes);
        return unmodifiableSet;
    }

    @Nonnull
    final Syntax syntax;
    @Nonnull
    final Map<String, BlockDirective> blockDirectives;
    @Nonnull
    private final Map<BlockDirective, BlockParser> blocks;
    @Nonnull
    final LogicalLineFactory logicalLineFactory;
    @Nonnull
    final BlockDirectiveLineFactory blockDirectiveLineFactory;
    @Nonnull
    final Set<Class<? extends SourceNode>> outputNodeTypes;

    /**
     * Initializes a new Parser.
     *
     * @param syntax
     *            the assembly language's syntax rules
     * @param blockDirectives
     *            a {@link Map} of directive mnemonics to {@link BlockDirective}
     * @param blocks
     *            a {@link Map} of {@link BlockDirective} to the corresponding {@link BlockParser}
     * @param logicalLineFactory
     *            the {@link LogicalLineFactory} for this parser
     * @param blockDirectiveLineFactory
     *            the {@link BlockDirectiveLineFactory} for this parser
     */
    public Parser(@Nonnull final Syntax syntax, @Nonnull Map<String, BlockDirective> blockDirectives,
            @Nonnull Map<BlockDirective, BlockParser> blocks, @Nonnull LogicalLineFactory logicalLineFactory,
            @Nonnull BlockDirectiveLineFactory blockDirectiveLineFactory) {
        if (syntax == null) {
            throw new NullPointerException("syntax");
        }

        if (blockDirectives == null) {
            throw new NullPointerException("blockDirectives");
        }

        if (blocks == null) {
            throw new NullPointerException("blocks");
        }

        if (logicalLineFactory == null) {
            throw new NullPointerException("logicalLineFactory");
        }

        if (blockDirectiveLineFactory == null) {
            throw new NullPointerException("blockDirectiveLineFactory");
        }

        this.syntax = syntax;
        this.blockDirectives = blockDirectives;
        this.blocks = blocks;
        this.logicalLineFactory = logicalLineFactory;
        this.blockDirectiveLineFactory = blockDirectiveLineFactory;

        this.outputNodeTypes = calcOutputNodeTypes(logicalLineFactory, blockDirectiveLineFactory, blocks);
    }

    /**
     * Parses the contents of a source file.
     *
     * @param text
     *            the contents of the source file
     * @return a {@link SourceNode} that is the root of the source file's abstract syntax tree
     */
    @Nonnull
    public final SourceNode parse(@Nonnull Document text) {
        return this.parse(new SourceNodeProducer(this, new DocumentReader(text)));
    }

    /**
     * Re-parses the contents of a source file after it has been altered.
     *
     * @param text
     *            the new contents of the source file
     * @param oldSourceFileRootNode
     *            the root source node of the old source
     * @param replaceOffset
     *            the offset at which the replace occurred
     * @param lengthToRemove
     *            the length of text from the old source file that was removed
     * @param lengthToInsert
     *            the length of text from the new source file that was inserted
     * @return a {@link SourceNode} that is the root of the source file's abstract syntax tree
     */
    @Nonnull
    public final SourceNode reparse(@Nonnull Document text, @Nonnull SourceNode oldSourceFileRootNode, int replaceOffset,
            int lengthToRemove, int lengthToInsert) {
        if (text == null) {
            throw new NullPointerException("text");
        }

        if (oldSourceFileRootNode == null) {
            throw new NullPointerException("oldSourceFileRootNode");
        }

        // Basic sanity check
        if (text.length() != oldSourceFileRootNode.getLength() - lengthToRemove + lengthToInsert) {
            throw new IllegalArgumentException(
                    "The length of the new document doesn't match the old root source node and the replacement");
        }

        // Optimization: if there is no replacement, return the old source node
        if (lengthToRemove == 0 && lengthToInsert == 0) {
            return oldSourceFileRootNode;
        }

        try {
            return this.parse(new ReparserSourceNodeProducer(this, new DocumentReader(text), oldSourceFileRootNode, replaceOffset,
                    lengthToRemove, lengthToInsert));
        } catch (RuntimeException e) {
            return this.parse(text);
        }
    }

    /**
     * Removes "decorations" on a mnemonic, i.e. characters that are parsed as part of the mnemonic, but are not part of the
     * mnemonic identifier.
     * <p>
     * The default implementation removes a leading '!' character from the mnemonic.
     *
     * @param mnemonic
     *            the mnemonic to remove decorations from
     * @return the undecorated mnemonic
     */
    @Nonnull
    public String undecorateMnemonic(@Nonnull String mnemonic) {
        // If the mnemonic starts with '!', remove that character. '!' is used to bypass macros.
        if (mnemonic.startsWith("!")) {
            mnemonic = mnemonic.substring(1);
        }

        return mnemonic;
    }

    final SourceNode parse(@Nonnull SourceNodeProducer sourceNodeProducer) {
        final ArrayList<SourceNode> nodes = new ArrayList<>();
        while (!sourceNodeProducer.atEnd()) {
            final SourceNode sourceNode = sourceNodeProducer.next();

            // Get the block directive on this logical line, if any.
            final BlockDirective blockDirective = BlockDirective.getBlockDirective(sourceNode);

            this.processBlockBodyLine(sourceNodeProducer, nodes, sourceNode, blockDirective);
        }

        return new Block(nodes, null);
    }

    final void processBlockBodyLine(@Nonnull SourceNodeProducer sourceNodeProducer, @Nonnull ArrayList<SourceNode> childNodes,
            @Nonnull SourceNode sourceNode, @CheckForNull BlockDirective blockDirective) {
        // Check if this mnemonic starts a block.
        final BlockParser blockParser = blockDirective == null ? null : this.blocks.get(blockDirective);

        // If the mnemonic doesn't start a block, add the logical line to the child nodes list.
        // Otherwise, parse a block.
        if (blockParser == null) {
            childNodes.add(sourceNode);
        } else {
            childNodes.add(blockParser.parseBlock(sourceNodeProducer, (BlockDirectiveLine) sourceNode));
        }
    }

}
