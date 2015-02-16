package org.reasm.commons.testhelpers;

import static org.junit.Assert.fail;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.AssemblyBuilder;
import org.reasm.commons.source.BlockDirective;
import org.reasm.commons.source.BlockDirectiveLine;
import org.reasm.commons.source.LogicalLine;

/**
 * {@link BlockDirectiveLine} with a dummy implementation for {@link #assembleCore(AssemblyBuilder)}.
 *
 * @author Francis Gagné
 */
@Immutable
public final class TestBlockDirectiveLine extends BlockDirectiveLine {

    TestBlockDirectiveLine(@Nonnull LogicalLine logicalLine, @Nonnull BlockDirective blockDirective) {
        super(logicalLine, blockDirective);
    }

    /**
     * Always throws an {@link AssertionError}.
     */
    @Override
    protected void assembleCore(AssemblyBuilder builder) throws IOException {
        fail();
    }

}
