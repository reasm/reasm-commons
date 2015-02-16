package org.reasm.commons.testhelpers;

import static org.junit.Assert.fail;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.reasm.AssemblyBuilder;
import org.reasm.commons.source.LogicalLine;
import org.reasm.commons.source.LogicalLineAttributes;

/**
 * {@link LogicalLine} with a dummy implementation for {@link #assembleCore(AssemblyBuilder)}.
 *
 * @author Francis Gagné
 */
public final class TestLogicalLine extends LogicalLine {

    TestLogicalLine(@Nonnull LogicalLineAttributes attributes) {
        super(attributes);
    }

    /**
     * Always throws an {@link AssertionError}.
     */
    @Override
    protected void assembleCore(AssemblyBuilder builder) throws IOException {
        fail();
    }

}
