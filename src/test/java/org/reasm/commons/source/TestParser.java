package org.reasm.commons.source;

import java.util.Collections;
import java.util.Set;

import javax.annotation.Nonnull;

import org.reasm.commons.testhelpers.TestBlockDirectiveLineFactory;
import org.reasm.commons.testhelpers.TestLogicalLineFactory;
import org.reasm.source.CompositeSourceNode;
import org.reasm.source.ParseError;
import org.reasm.source.SourceNode;

import com.google.common.collect.ImmutableMap;

/**
 * Sample parser for tests.
 *
 * @author Francis Gagné
 */
public final class TestParser {

    private static final int[] EMPTY_INT_ARRAY = new int[0];

    @Nonnull
    static final Syntax SYNTAX = new Syntax(EMPTY_INT_ARRAY, EMPTY_INT_ARRAY);

    @Nonnull
    static final BlockDirective BLOCK0 = new BlockDirective("BLOCK0");
    @Nonnull
    private static final BlockDirective ENDBLOCK0 = new BlockDirective("ENDBLOCK0");

    @Nonnull
    static final BlockDirective BLOCK1 = new BlockDirective("BLOCK1");
    @Nonnull
    private static final BlockDirective ENDBLOCK1 = new BlockDirective("ENDBLOCK1");

    @Nonnull
    private static final BlockDirective IF = new BlockDirective("IF");
    @Nonnull
    static final BlockDirective ELSE = new BlockDirective("ELSE");
    @Nonnull
    static final BlockDirective ELSEIF = new BlockDirective("ELSEIF");
    @Nonnull
    static final BlockDirective ENDIF = new BlockDirective("ENDIF");

    @Nonnull
    static final ImmutableMap<String, BlockDirective> BLOCK_DIRECTIVES = ImmutableMap.<String, BlockDirective> builder()
            .put("BLOCK0", BLOCK0).put("ENDBLOCK0", ENDBLOCK0).put("BLOCK1", BLOCK1).put("ENDBLOCK1", ENDBLOCK1).put("IF", IF)
            .put("ELSE", ELSE).put("ELSEIF", ELSEIF).put("ENDIF", ENDIF).build();

    @Nonnull
    static final Iterable<Class<? extends SourceNode>> BLOCK0_BLOCK_TYPES = singleType(Block0Block.class);
    @Nonnull
    static final Iterable<Class<? extends SourceNode>> BLOCK1_BLOCK_TYPES = singleType(Block1Block.class);
    @Nonnull
    static final Iterable<Class<? extends SourceNode>> IF_BLOCK_TYPES = singleType(IfBlock.class);

    @Nonnull
    static final BlockParser BLOCK0_BLOCK_PARSER = new BasicBlockParser(ENDBLOCK0) {

        @Override
        public Iterable<Class<? extends SourceNode>> getOutputNodeTypes() {
            return BLOCK0_BLOCK_TYPES;
        }

        @Override
        protected SourceNode createBlock(Iterable<? extends SourceNode> childNodes, ParseError parseError) {
            return new Block0Block(childNodes, parseError);
        }

    };

    @Nonnull
    static final BlockParser BLOCK1_BLOCK_PARSER = new BasicBlockParser(ENDBLOCK1) {

        @Override
        public Iterable<Class<? extends SourceNode>> getOutputNodeTypes() {
            return BLOCK1_BLOCK_TYPES;
        }

        @Override
        protected SourceNode createBlock(Iterable<? extends SourceNode> childNodes, ParseError parseError) {
            return new Block1Block(childNodes, parseError);
        }

    };

    @Nonnull
    static final BlockParser IF_BLOCK_PARSER = new IfBlockParser() {

        @Override
        public Iterable<Class<? extends SourceNode>> getOutputNodeTypes() {
            return IF_BLOCK_TYPES;
        };

        @Override
        protected CompositeSourceNode createBlock(Iterable<? extends SourceNode> childNodes, ParseError parseError) {
            return new IfBlock(childNodes, parseError);
        }

        @Override
        protected boolean isElseDirective(BlockDirective blockDirective) {
            return blockDirective == ELSE;
        }

        @Override
        protected boolean isElseIfDirective(BlockDirective blockDirective) {
            return blockDirective == ELSEIF;
        }

        @Override
        protected boolean isEndIfDirective(BlockDirective blockDirective) {
            return blockDirective == ENDIF;
        }

    };

    @Nonnull
    static final ImmutableMap<BlockDirective, BlockParser> BLOCKS = ImmutableMap.of(BLOCK0, BLOCK0_BLOCK_PARSER, BLOCK1,
            BLOCK1_BLOCK_PARSER, IF, IF_BLOCK_PARSER);

    /** Sample parser for tests */
    @Nonnull
    public static final Parser TEST_PARSER = new Parser(SYNTAX, BLOCK_DIRECTIVES, BLOCKS, TestLogicalLineFactory.INSTANCE,
            TestBlockDirectiveLineFactory.INSTANCE);

    @Nonnull
    private static Set<Class<? extends SourceNode>> singleType(@Nonnull Class<? extends SourceNode> type) {
        return Collections.<Class<? extends SourceNode>> singleton(type);
    }

    // This class is not meant to be instantiated.
    private TestParser() {
    }

}
