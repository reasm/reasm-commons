package org.reasm.commons.source;

import static ca.fragag.testhelpers.HasType.hasType;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.reasm.commons.source.BlockParserTestsCommon.COMPLETE_BLOCK;
import static org.reasm.commons.source.BlockParserTestsCommon.INCOMPLETE_BLOCK;

import java.util.List;

import javax.annotation.Nonnull;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.reasm.SubstringBounds;
import org.reasm.commons.testhelpers.TestBlockDirectiveLine;
import org.reasm.commons.testhelpers.TestBlockDirectiveLineFactory;
import org.reasm.commons.testhelpers.TestLogicalLine;
import org.reasm.commons.testhelpers.TestLogicalLineFactory;
import org.reasm.source.CompositeSourceNode;
import org.reasm.source.ParseError;
import org.reasm.source.SimpleCompositeSourceNode;
import org.reasm.source.SourceNode;

import ca.fragag.text.Document;
import ca.fragag.text.DocumentReader;

/**
 * Test class for {@link BasicBlockParser}.
 *
 * @author Francis Gagné
 */
public class BasicBlockParserTest {

    private static List<SourceNode> parseBlock(@Nonnull String code, @Nonnull Matcher<? super ParseError> blockParseErrorMatcher,
            int childrenCount) {
        final DocumentReader reader = new DocumentReader(new Document(code), 8);
        final BlockDirectiveLine firstLine = TestBlockDirectiveLineFactory.INSTANCE.createBlockDirectiveLine(
                TestLogicalLineFactory.INSTANCE.createLogicalLine(new LogicalLineAttributes(8, null, new SubstringBounds[0],
                        new SubstringBounds(1, 4), new SubstringBounds[0], null, new int[0])), TestParser.BLOCK0);

        final SourceNode block = TestParser.BLOCK0_BLOCK_PARSER.parseBlock(new SourceNodeProducer(TestParser.TEST_PARSER, reader),
                firstLine);
        assertThat(block.getLength(), is(code.length()));
        assertThat(block.getParseError(), blockParseErrorMatcher);
        assertThat(block, hasType(Block0Block.class));

        final List<SourceNode> childNodes = ((CompositeSourceNode) block).getChildNodes();
        assertThat(childNodes.size(), is(childrenCount));

        final SourceNode blockStart = childNodes.get(0);
        assertThat(blockStart, is((SourceNode) firstLine));

        final SourceNode blockBody = childNodes.get(1);
        assertThat(blockBody, hasType(SimpleCompositeSourceNode.class));

        return childNodes;
    }

    private static CompositeSourceNode parseCompleteBlock(@Nonnull String code) {
        final List<SourceNode> childNodes = parseBlock(code, COMPLETE_BLOCK, 3);

        final SourceNode blockEnd = childNodes.get(2);
        assertThat(blockEnd.getLength(), is(10));
        assertThat(blockEnd.getParseError(), is(nullValue()));
        assertThat(blockEnd, hasType(TestBlockDirectiveLine.class));

        return (CompositeSourceNode) childNodes.get(1);
    }

    private static CompositeSourceNode parseIncompleteBlock(@Nonnull String code) {
        final Matcher<Object> blockParseErrorMatcher = both(INCOMPLETE_BLOCK).and(
                hasProperty("startingBlockDirective", equalTo(TestParser.BLOCK0)));
        final List<SourceNode> childNodes = parseBlock(code, blockParseErrorMatcher, 2);
        return (CompositeSourceNode) childNodes.get(1);
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(SourceNodeProducer, BlockDirectiveLine)} correctly parses a block with an
     * empty body.
     */
    @Test
    public void parseBlockEmptyBody() {
        final String code = " BLOCK0\n ENDBLOCK0";
        final CompositeSourceNode body = parseCompleteBlock(code);
        final List<SourceNode> bodyNodes = body.getChildNodes();
        assertThat(bodyNodes.size(), is(0));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(SourceNodeProducer, BlockDirectiveLine)} correctly parses a block with a
     * logical line in its body that has no mnemonic.
     */
    @Test
    public void parseBlockLineWithNoMnemonic() {
        final String code = " BLOCK0\nfoo:\n ENDBLOCK0";
        final CompositeSourceNode body = parseCompleteBlock(code);
        final List<SourceNode> bodyNodes = body.getChildNodes();
        assertThat(bodyNodes.size(), is(1));

        final SourceNode bodyNode0 = bodyNodes.get(0);
        assertThat(bodyNode0.getLength(), is(5));
        assertThat(bodyNode0.getParseError(), is(nullValue()));
        assertThat(bodyNode0, hasType(TestLogicalLine.class));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(SourceNodeProducer, BlockDirectiveLine)} correctly parses a block with the
     * end directive missing.
     */
    @Test
    public void parseBlockMissingEndDirective() {
        final String code = " BLOCK0\n NOP";
        final CompositeSourceNode body = parseIncompleteBlock(code);
        final List<SourceNode> bodyNodes = body.getChildNodes();
        assertThat(bodyNodes.size(), is(1));

        final SourceNode bodyNode0 = bodyNodes.get(0);
        assertThat(bodyNode0.getLength(), is(4));
        assertThat(bodyNode0.getParseError(), is(nullValue()));
        assertThat(bodyNode0, hasType(TestLogicalLine.class));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(SourceNodeProducer, BlockDirectiveLine)} correctly parses a block with a
     * nested block in its body.
     */
    @Test
    public void parseBlockNestedBlock() {
        final String code = " BLOCK0\n BLOCK1\n NOP\n ENDBLOCK1\n ENDBLOCK0";
        final CompositeSourceNode body = parseCompleteBlock(code);
        final List<SourceNode> bodyNodes = body.getChildNodes();
        assertThat(bodyNodes.size(), is(1));

        final SourceNode bodyNode0 = bodyNodes.get(0);
        assertThat(bodyNode0.getLength(), is(24));
        assertThat(bodyNode0.getParseError(), is(nullValue()));
        assertThat(bodyNode0, hasType(Block1Block.class));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(SourceNodeProducer, BlockDirectiveLine)} correctly parses a block with a
     * nested block of the same type in its body.
     */
    @Test
    public void parseBlockNestedBlockSameType() {
        final String code = " BLOCK0\n BLOCK0\n NOP\n ENDBLOCK0\n ENDBLOCK0";
        final CompositeSourceNode body = parseCompleteBlock(code);
        final List<SourceNode> bodyNodes = body.getChildNodes();
        assertThat(bodyNodes.size(), is(1));

        final SourceNode bodyNode0 = bodyNodes.get(0);
        assertThat(bodyNode0.getLength(), is(24));
        assertThat(bodyNode0.getParseError(), is(nullValue()));
        assertThat(bodyNode0, hasType(Block0Block.class));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(SourceNodeProducer, BlockDirectiveLine)} correctly parses a block with 1
     * logical line in its body.
     */
    @Test
    public void parseBlockOneLineBody() {
        final String code = " BLOCK0\n NOP\n ENDBLOCK0";
        final CompositeSourceNode body = parseCompleteBlock(code);
        final List<SourceNode> bodyNodes = body.getChildNodes();
        assertThat(bodyNodes.size(), is(1));

        final SourceNode bodyNode0 = bodyNodes.get(0);
        assertThat(bodyNode0.getLength(), is(5));
        assertThat(bodyNode0.getParseError(), is(nullValue()));
        assertThat(bodyNode0, hasType(TestLogicalLine.class));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(SourceNodeProducer, BlockDirectiveLine)} correctly parses a block with 2
     * logical lines in its body.
     */
    @Test
    public void parseBlockTwoLineBody() {
        final String code = " BLOCK0\n FIRST\n SECOND\n ENDBLOCK0";
        final CompositeSourceNode body = parseCompleteBlock(code);
        final List<SourceNode> bodyNodes = body.getChildNodes();
        assertThat(bodyNodes.size(), is(2));

        final SourceNode bodyNode0 = bodyNodes.get(0);
        assertThat(bodyNode0.getLength(), is(7));
        assertThat(bodyNode0.getParseError(), is(nullValue()));
        assertThat(bodyNode0, hasType(TestLogicalLine.class));

        final SourceNode bodyNode1 = bodyNodes.get(1);
        assertThat(bodyNode1.getLength(), is(8));
        assertThat(bodyNode1.getParseError(), is(nullValue()));
        assertThat(bodyNode1, hasType(TestLogicalLine.class));
    }

}
