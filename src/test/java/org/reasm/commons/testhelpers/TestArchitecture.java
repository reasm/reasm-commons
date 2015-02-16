package org.reasm.commons.testhelpers;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.Architecture;
import org.reasm.commons.source.TestParser;
import org.reasm.source.AbstractSourceFile;
import org.reasm.source.SourceNode;

import ca.fragag.text.Document;

/**
 * {@link Architecture} that parses using {@link TestParser}.
 *
 * @author Francis Gagné
 */
@Immutable
public final class TestArchitecture extends Architecture {

    /** The single instance of the {@link TestArchitecture} class. */
    @Nonnull
    public static final TestArchitecture INSTANCE = new TestArchitecture();

    private TestArchitecture() {
        super(null);
    }

    @Override
    public final SourceNode parse(Document text) {
        return TestParser.TEST_PARSER.parse(text);
    }

    @Override
    public SourceNode reparse(Document text, AbstractSourceFile<?> oldSourceFile, int replaceOffset, int lengthToRemove,
            int lengthToInsert) {
        return TestParser.TEST_PARSER.reparse(text, oldSourceFile.getParsed(this), replaceOffset, lengthToRemove, lengthToInsert);
    }

}
