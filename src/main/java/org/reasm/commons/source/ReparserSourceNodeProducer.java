package org.reasm.commons.source;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.source.CompositeSourceNode;
import org.reasm.source.SourceNode;

import ca.fragag.collections.PeekableIterator;
import ca.fragag.text.CharSequenceReader;

final class ReparserSourceNodeProducer extends SourceNodeProducer {

    @Immutable
    private enum Step {
        RECYCLE_BEFORE, PARSE_REPLACEMENT, SYNCHRONIZE, RECYCLE_AFTER
    }

    @Nonnull
    private final SourceNode oldSourceFileRootNode;
    private final int replaceOffset;
    private final int lengthToRemove;
    private final int lengthToInsert;
    private final int endReplaceOffset;
    @Nonnull
    private final ArrayList<PeekableIterator<SourceNode>> oldNodeIteratorStack;
    private int oldPosition;
    @Nonnull
    private Step step;

    ReparserSourceNodeProducer(@Nonnull Parser parser, @Nonnull CharSequenceReader<?> reader,
            @Nonnull SourceNode oldSourceFileRootNode, int replaceOffset, int lengthToRemove, int lengthToInsert) {
        super(parser, reader);
        this.oldSourceFileRootNode = oldSourceFileRootNode;
        this.replaceOffset = replaceOffset;
        this.lengthToRemove = lengthToRemove;
        this.lengthToInsert = lengthToInsert;
        this.endReplaceOffset = replaceOffset + lengthToInsert;
        this.oldNodeIteratorStack = new ArrayList<>();
        this.step = Step.RECYCLE_BEFORE;
        this.pushOldNodeIterator(this.oldSourceFileRootNode);
    }

    @Override
    protected final SourceNode next() {
        final CharSequenceReader<?> reader = this.getReader();

        // Jump to the last step we were in last time next() returned.
        switch (this.step) {
        case RECYCLE_BEFORE:
            // Recycle the nodes before the replacement.
            if (reader.getCurrentPosition() < this.replaceOffset) {
                for (;;) {
                    final SourceNode node = this.peekOldNode();
                    final int nodeLength = node.getLength();
                    final int endPosition = reader.getCurrentPosition() + nodeLength;

                    // Don't recycle the node if it crosses the replacement.
                    // Don't recycle the node if its ends where the replacement begins and the node has a parse error.
                    if (endPosition < this.replaceOffset || endPosition == this.replaceOffset && node.getParseError() == null) {
                        // Don't recycle the node if that type of node cannot appear here.
                        if (this.isParsableNode(node)) {
                            // Don't recycle the node if it doesn't end with a line feed or a carriage return.
                            // If characters are appended to a file that doesn't end with a line feed,
                            // we must parse the last logical line again.
                            // Also, we can't recycle a node that ends with a carriage return
                            // if the following character in the new document is a line feed
                            // (which is a character that was just inserted),
                            // because we must parse the node again
                            // to include the line feed in it.
                            final char nodeLastChar = reader.getCharSequence().charAt(endPosition - 1);
                            if (nodeLastChar == '\n'
                                    || nodeLastChar == '\r'
                                    && (reader.getCharSequence().length() <= endPosition || reader.getCharSequence().charAt(
                                            endPosition) != '\n')) {
                                this.nextOldNode();
                                this.oldPosition += nodeLength;
                                reader.setCurrentPosition(endPosition);
                                return node;
                            }
                        }
                    }

                    if (!(node instanceof CompositeSourceNode)) {
                        break;
                    }

                    this.pushOldNodeIterator(this.nextOldNode());
                }
            }

            this.step = Step.PARSE_REPLACEMENT;
            //$FALL-THROUGH$

        case PARSE_REPLACEMENT:
            // Re-parse the nodes that cross the replacement.
            if (reader.getCurrentPosition() < this.endReplaceOffset) {
                return super.next();
            }

            this.step = Step.SYNCHRONIZE;
            //$FALL-THROUGH$

        case SYNCHRONIZE:
            // Synchronize the old and the new document.
            while (this.oldPosition - this.lengthToRemove < reader.getCurrentPosition() - this.lengthToInsert) {
                final SourceNode node = this.nextOldNode();
                this.oldPosition += node.getLength();
            }

            if (this.oldPosition - this.lengthToRemove > reader.getCurrentPosition() - this.lengthToInsert) {
                return super.next();
            }

            this.step = Step.RECYCLE_AFTER;
            //$FALL-THROUGH$

        case RECYCLE_AFTER:
            // Recycle the nodes after the replacement.
            this.popExhaustedOldNodeIterators();
            if (this.oldNodeIteratorStack.isEmpty()) {
                throw new NoSuchElementException();
            }

            for (;;) {
                final SourceNode node = this.nextOldNode();
                if (this.isParsableNode(node)) {
                    final int nodeLength = node.getLength();
                    final int endPosition = reader.getCurrentPosition() + nodeLength;
                    this.oldPosition += nodeLength;
                    reader.setCurrentPosition(endPosition);
                    return node;
                }

                this.pushOldNodeIterator(node);
            }

        default:
            throw new AssertionError();
        }
    }

    private final boolean isParsableNode(@Nonnull SourceNode node) {
        return this.getParser().outputNodeTypes.contains(node.getClass());
    }

    @Nonnull
    private final SourceNode nextOldNode() {
        this.popExhaustedOldNodeIterators();
        return this.oldNodeIteratorStack.get(this.oldNodeIteratorStack.size() - 1).next();
    }

    @Nonnull
    private final SourceNode peekOldNode() {
        this.popExhaustedOldNodeIterators();
        return this.oldNodeIteratorStack.get(this.oldNodeIteratorStack.size() - 1).peek();
    }

    private final void popExhaustedOldNodeIterators() {
        while (!this.oldNodeIteratorStack.isEmpty()
                && !this.oldNodeIteratorStack.get(this.oldNodeIteratorStack.size() - 1).hasNext()) {
            this.oldNodeIteratorStack.remove(this.oldNodeIteratorStack.size() - 1);
        }
    }

    private final void pushOldNodeIterator(@Nonnull SourceNode node) {
        this.oldNodeIteratorStack.add(new PeekableIterator<>(((CompositeSourceNode) node).getChildNodes().iterator()));
    }

}
