package org.reasm.commons.source;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.reasm.commons.testhelpers.TestBlockDirectiveLineFactory;
import org.reasm.commons.testhelpers.TestLogicalLineFactory;
import org.reasm.source.CompositeSourceNode;
import org.reasm.source.SimpleCompositeSourceNode;
import org.reasm.source.SourceNode;

import ca.fragag.text.Document;

/**
 * Test class for {@link Parser}.
 *
 * @author Francis Gagné
 */
public class ParserTest {

    /**
     * Asserts that {@link Parser#parse(Document)} returns a {@link Block} with a single {@link Block0Block} child node when the
     * document contains a <code>BLOCK0</code> block.
     */
    @Test
    public void parseBlock() {
        final SourceNode block = TestParser.TEST_PARSER.parse(new Document(" BLOCK0\n NOP\n ENDBLOCK0"));
        assertThat(block.getLength(), is(23));
        assertThat(block.getParseError(), is(nullValue()));
        assertThat(block, is(instanceOf(Block.class)));
        final List<SourceNode> childNodes = ((Block) block).getChildNodes();
        assertThat(childNodes.size(), is(1));
        final SourceNode node = childNodes.get(0);
        assertThat(node.getLength(), is(23));
        assertThat(node.getParseError(), is(nullValue()));
        assertThat(node, is(instanceOf(Block0Block.class)));
    }

    /**
     * Asserts that {@link Parser#parse(Document)} returns a {@link Block} with a single {@link Block0Block} child node when the
     * document contains a <code>BLOCK0</code> block, even if the <code>BLOCK0</code> directive is preceded by a <code>!</code>.
     */
    @Test
    public void parseBlockNoMacro() {
        final SourceNode block = TestParser.TEST_PARSER.parse(new Document(" !BLOCK0\n NOP\n ENDBLOCK0"));
        assertThat(block.getLength(), is(24));
        assertThat(block.getParseError(), is(nullValue()));
        assertThat(block, is(instanceOf(Block.class)));
        final List<SourceNode> childNodes = ((Block) block).getChildNodes();
        assertThat(childNodes.size(), is(1));
        final SourceNode node = childNodes.get(0);
        assertThat(node.getLength(), is(24));
        assertThat(node.getParseError(), is(nullValue()));
        assertThat(node, is(instanceOf(Block0Block.class)));
    }

    /**
     * Asserts that {@link Parser#parse(Document)} returns a {@link Block} with no child nodes when the document is empty.
     */
    @Test
    public void parseEmptyDocument() {
        final SourceNode block = TestParser.TEST_PARSER.parse(new Document(""));
        assertThat(block.getLength(), is(0));
        assertThat(block.getParseError(), is(nullValue()));
        assertThat(block, is(instanceOf(Block.class)));
        assertThat(((CompositeSourceNode) block).getChildNodes(), is(empty()));
    }

    /**
     * Asserts that {@link Parser#parse(Document)} returns a {@link Block} with a single {@link LogicalLine} child node when the
     * document contains a single line with no mnemonic.
     */
    @Test
    public void parseNoMnemonic() {
        final SourceNode block = TestParser.TEST_PARSER.parse(new Document("; This is a comment"));
        assertThat(block.getLength(), is(19));
        assertThat(block.getParseError(), is(nullValue()));
        assertThat(block, is(instanceOf(Block.class)));
        final List<SourceNode> childNodes = ((Block) block).getChildNodes();
        assertThat(childNodes.size(), is(1));
        final SourceNode node = childNodes.get(0);
        assertThat(node.getLength(), is(19));
        assertThat(node.getParseError(), is(nullValue()));
        assertThat(node, is(instanceOf(LogicalLine.class)));
    }

    /**
     * Asserts that {@link Parser#parse(Document)} returns a {@link Block} with a single {@link LogicalLine} child node when the
     * document contains a single line with a mnemonic that doesn't start a block.
     */
    @Test
    public void parseNotABlock() {
        final SourceNode block = TestParser.TEST_PARSER.parse(new Document(" NOP"));
        assertThat(block.getLength(), is(4));
        assertThat(block.getParseError(), is(nullValue()));
        assertThat(block, is(instanceOf(Block.class)));
        final List<SourceNode> childNodes = ((Block) block).getChildNodes();
        assertThat(childNodes.size(), is(1));
        final SourceNode node = childNodes.get(0);
        assertThat(node.getLength(), is(4));
        assertThat(node.getParseError(), is(nullValue()));
        assertThat(node, is(instanceOf(LogicalLine.class)));
    }

    /**
     * Asserts that {@link Parser#Parser(Syntax, Map, Map, LogicalLineFactory, BlockDirectiveLineFactory)} throws a
     * {@link NullPointerException} when the <code>blockDirectiveLineFactory</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void parserNullBlockDirectiveLineFactory() {
        new Parser(TestParser.SYNTAX, TestParser.BLOCK_DIRECTIVES, TestParser.BLOCKS, TestLogicalLineFactory.INSTANCE, null);
    }

    /**
     * Asserts that {@link Parser#Parser(Syntax, Map, Map, LogicalLineFactory, BlockDirectiveLineFactory)} throws a
     * {@link NullPointerException} when the <code>blockDirectives</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void parserNullBlockDirectives() {
        new Parser(TestParser.SYNTAX, null, TestParser.BLOCKS, TestLogicalLineFactory.INSTANCE,
                TestBlockDirectiveLineFactory.INSTANCE);
    }

    /**
     * Asserts that {@link Parser#Parser(Syntax, Map, Map, LogicalLineFactory, BlockDirectiveLineFactory)} throws a
     * {@link NullPointerException} when the <code>blocks</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void parserNullBlocks() {
        new Parser(TestParser.SYNTAX, TestParser.BLOCK_DIRECTIVES, null, TestLogicalLineFactory.INSTANCE,
                TestBlockDirectiveLineFactory.INSTANCE);
    }

    /**
     * Asserts that {@link Parser#Parser(Syntax, Map, Map, LogicalLineFactory, BlockDirectiveLineFactory)} throws a
     * {@link NullPointerException} when the <code>logicalLineFactory</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void parserNullLogicalLineFactory() {
        new Parser(TestParser.SYNTAX, TestParser.BLOCK_DIRECTIVES, TestParser.BLOCKS, null, TestBlockDirectiveLineFactory.INSTANCE);
    }

    /**
     * Asserts that {@link Parser#Parser(Syntax, Map, Map, LogicalLineFactory, BlockDirectiveLineFactory)} throws a
     * {@link NullPointerException} when the <code>syntax</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void parserNullSyntax() {
        new Parser(null, TestParser.BLOCK_DIRECTIVES, TestParser.BLOCKS, TestLogicalLineFactory.INSTANCE,
                TestBlockDirectiveLineFactory.INSTANCE);
    }

    /**
     * Asserts that {@link Parser#reparse(Document, SourceNode, int, int, int)} reparses a document.
     */
    @Test
    public void reparseDelete() {
        final String oldText = " NOP\n BLOCK0\n MOVE #123,D0\n ENDBLOCK0\n NOP";
        final int replaceOffset = 20;
        final int lengthToRemove = 3;
        final String textToInsert = "";

        final Document oldDocument = new Document(oldText);
        final SourceNode oldNode = TestParser.TEST_PARSER.parse(oldDocument);

        final Document newDocument = oldDocument.replace(replaceOffset, lengthToRemove, textToInsert);
        assertThat(newDocument.toString(), is(" NOP\n BLOCK0\n MOVE #,D0\n ENDBLOCK0\n NOP"));

        final SourceNode newNode = TestParser.TEST_PARSER.reparse(newDocument, oldNode, replaceOffset, lengthToRemove,
                textToInsert.length());
        assertThat(newNode.getLength(), is(39));
    }

    /**
     * Asserts that {@link Parser#reparse(Document, SourceNode, int, int, int)} reparses a document.
     */
    @Test
    public void reparseInsert() {
        final String oldText = " NOP\n BLOCK0\n MOVE #,D0\n ENDBLOCK0\n NOP";
        final int replaceOffset = 20;
        final int lengthToRemove = 0;
        final String textToInsert = "123";

        final Document oldDocument = new Document(oldText);
        final SourceNode oldNode = TestParser.TEST_PARSER.parse(oldDocument);

        final Document newDocument = oldDocument.replace(replaceOffset, lengthToRemove, textToInsert);
        assertThat(newDocument.toString(), is(" NOP\n BLOCK0\n MOVE #123,D0\n ENDBLOCK0\n NOP"));

        final SourceNode newNode = TestParser.TEST_PARSER.reparse(newDocument, oldNode, replaceOffset, lengthToRemove,
                textToInsert.length());
        assertThat(newNode.getLength(), is(42));
    }

    /**
     * Asserts that {@link Parser#reparse(Document, SourceNode, int, int, int)} throws an {@link IllegalArgumentException} when the
     * length of the new document doesn't match the old root source node and the replacement.
     */
    @Test
    public void reparseNonsensical() {
        final String oldText = " NOP\n BLOCK0\n MOVE #0,D0\n ENDBLOCK0\n NOP";
        final int replaceOffset = 20;
        final int lengthToRemove = 1;
        final String textToInsert = "123";

        final Document oldDocument = new Document(oldText);
        final SourceNode oldNode = TestParser.TEST_PARSER.parse(oldDocument);

        final Document newDocument = new Document(" NOP\n BLOCK0\n MOVE #1234,D0\n ENDBLOCK0\n NOP");

        try {
            TestParser.TEST_PARSER.reparse(newDocument, oldNode, replaceOffset, lengthToRemove, textToInsert.length());
            fail("Parser.reparse() should have thrown an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Asserts that {@link Parser#reparse(Document, SourceNode, int, int, int)} returns the old root source node when the arguments
     * describe no replacement.
     */
    @Test
    public void reparseNoReplacement() {
        final String oldText = " NOP\n BLOCK0\n MOVE #0,D0\n ENDBLOCK0\n NOP";
        final int replaceOffset = 20;
        final int lengthToRemove = 0;
        final String textToInsert = "";

        final Document oldDocument = new Document(oldText);
        final SourceNode oldNode = TestParser.TEST_PARSER.parse(oldDocument);

        final Document newDocument = oldDocument.replace(replaceOffset, lengthToRemove, textToInsert);
        assertThat(newDocument.toString(), is(oldText));

        final SourceNode newNode = TestParser.TEST_PARSER.reparse(newDocument, oldNode, replaceOffset, lengthToRemove,
                textToInsert.length());
        assertThat(newNode, is(sameInstance(oldNode)));
    }

    /**
     * Asserts that {@link Parser#reparse(Document, SourceNode, int, int, int)} throws a {@link NullPointerException} when the
     * <code>oldSourceFileRootNode</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void reparseNullOldSourceFileRootNode() {
        TestParser.TEST_PARSER.reparse(new Document("new"), null, 0, 0, 3);
    }

    /**
     * Asserts that {@link Parser#reparse(Document, SourceNode, int, int, int)} throws a {@link NullPointerException} when the
     * <code>text</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void reparseNullText() {
        TestParser.TEST_PARSER.reparse(null, new SimpleCompositeSourceNode(Collections.<SourceNode> emptySet()), 0, 0, 3);
    }

    /**
     * Asserts that {@link Parser#reparse(Document, SourceNode, int, int, int)} reparses a document.
     */
    @Test
    public void reparseReplace() {
        final String oldText = " NOP\n BLOCK0\n MOVE #0,D0\n ENDBLOCK0\n NOP";
        final int replaceOffset = 20;
        final int lengthToRemove = 1;
        final String textToInsert = "123";

        final Document oldDocument = new Document(oldText);
        final SourceNode oldNode = TestParser.TEST_PARSER.parse(oldDocument);

        final Document newDocument = oldDocument.replace(replaceOffset, lengthToRemove, textToInsert);
        assertThat(newDocument.toString(), is(" NOP\n BLOCK0\n MOVE #123,D0\n ENDBLOCK0\n NOP"));

        final SourceNode newNode = TestParser.TEST_PARSER.reparse(newDocument, oldNode, replaceOffset, lengthToRemove,
                textToInsert.length());
        assertThat(newNode.getLength(), is(42));
    }

}
