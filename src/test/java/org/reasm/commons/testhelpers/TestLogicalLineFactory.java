package org.reasm.commons.testhelpers;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.commons.source.LogicalLine;
import org.reasm.commons.source.LogicalLineAttributes;
import org.reasm.commons.source.LogicalLineFactory;

/**
 * Factory for {@link TestLogicalLine}.
 *
 * @author Francis Gagné
 */
@Immutable
public final class TestLogicalLineFactory implements LogicalLineFactory {

    /** The single instance of the {@link TestLogicalLineFactory} class. */
    @Nonnull
    public static final TestLogicalLineFactory INSTANCE = new TestLogicalLineFactory();

    private TestLogicalLineFactory() {
    }

    @Override
    public final LogicalLine createLogicalLine(LogicalLineAttributes attributes) {
        return new TestLogicalLine(attributes);
    }

    @Override
    public final Class<? extends LogicalLine> getOutputType() {
        return TestLogicalLine.class;
    }

}
