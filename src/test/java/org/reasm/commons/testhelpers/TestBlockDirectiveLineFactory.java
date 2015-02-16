package org.reasm.commons.testhelpers;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.commons.source.BlockDirective;
import org.reasm.commons.source.BlockDirectiveLine;
import org.reasm.commons.source.BlockDirectiveLineFactory;
import org.reasm.commons.source.LogicalLine;

/**
 * Factory for {@link TestBlockDirectiveLine}.
 *
 * @author Francis Gagné
 */
@Immutable
public final class TestBlockDirectiveLineFactory implements BlockDirectiveLineFactory {

    /** The single instance of the {@link TestBlockDirectiveLineFactory} class. */
    @Nonnull
    public static final TestBlockDirectiveLineFactory INSTANCE = new TestBlockDirectiveLineFactory();

    private TestBlockDirectiveLineFactory() {
    }

    @Override
    public final BlockDirectiveLine createBlockDirectiveLine(LogicalLine logicalLine, BlockDirective blockDirective) {
        return new TestBlockDirectiveLine(logicalLine, blockDirective);
    }

    @Override
    public final Class<? extends BlockDirectiveLine> getOutputType() {
        return TestBlockDirectiveLine.class;
    }

}
