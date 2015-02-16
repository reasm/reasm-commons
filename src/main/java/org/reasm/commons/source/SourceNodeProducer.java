package org.reasm.commons.source;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.reasm.SubstringBounds;
import org.reasm.source.SourceNode;

import ca.fragag.text.CharSequenceReader;

class SourceNodeProducer {

    @Nonnull
    private final Parser parser;
    @Nonnull
    private final CharSequenceReader<?> reader;

    SourceNodeProducer(@Nonnull Parser parser, @Nonnull CharSequenceReader<?> reader) {
        this.parser = parser;
        this.reader = reader;
    }

    @Nonnull
    public final Parser getParser() {
        return this.parser;
    }

    protected final boolean atEnd() {
        return this.reader.atEnd();
    }

    @Nonnull
    protected final CharSequenceReader<?> getReader() {
        return this.reader;
    }

    @Nonnull
    protected SourceNode next() {
        final LogicalLine logicalLine = this.parser.logicalLineFactory.createLogicalLine(LogicalLineParser.parse(this.reader,
                this.parser.syntax));
        final String mnemonic = this.getMnemonic(logicalLine);
        final BlockDirective blockDirective;
        if (mnemonic != null && (blockDirective = this.parser.blockDirectives.get(mnemonic)) != null) {
            return this.parser.blockDirectiveLineFactory.createBlockDirectiveLine(logicalLine, blockDirective);
        }

        return logicalLine;
    }

    @CheckForNull
    private final String getMnemonic(@Nonnull LogicalLine logicalLine) {
        final SubstringBounds mnemonicBounds = logicalLine.getMnemonicBounds();
        if (mnemonicBounds == null) {
            // There's no mnemonic on this line.
            return null;
        }

        final int backupPosition = this.reader.getCurrentPosition();
        try {
            // Temporarily move back the reader to the start of the mnemonic and read the mnemonic.
            this.reader.setCurrentPosition(backupPosition - logicalLine.getLength() + mnemonicBounds.getStart());
            final String mnemonic = this.reader.readSubstring(mnemonicBounds.getEnd() - mnemonicBounds.getStart());

            // Strip off "decorations" on the mnemonic.
            return this.parser.undecorateMnemonic(mnemonic);
        } finally {
            // Restore the reader's position.
            this.reader.setCurrentPosition(backupPosition);
        }
    }

}
