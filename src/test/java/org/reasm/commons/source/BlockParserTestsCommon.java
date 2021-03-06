package org.reasm.commons.source;

import static ca.fragag.testhelpers.HasType.hasType;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.hamcrest.Matcher;
import org.reasm.commons.parseerrors.UnclosedBlockParseError;
import org.reasm.source.CompositeSourceNode;
import org.reasm.source.ParseError;
import org.reasm.source.SourceNode;

import ca.fragag.text.Document;

final class BlockParserTestsCommon {

    @Nonnull
    static final Matcher<Object> COMPLETE_BLOCK = is(nullValue());
    @Nonnull
    static final Matcher<Object> INCOMPLETE_BLOCK = hasType(UnclosedBlockParseError.class);

    static void parseBasicBlock(@Nonnull String code, @Nonnull Class<?> blockType, @Nonnull Class<?> bodyType,
            @Nonnull Matcher<? super ParseError> blockParseErrorMatcher,
            @CheckForNull Matcher<? super SourceNode> thirdChildNodeMatcher) {
        SourceNode node = TestParser.TEST_PARSER.parse(new Document(code));
        List<SourceNode> childNodes = ((CompositeSourceNode) node).getChildNodes();
        assertThat(childNodes.size(), is(1));

        node = childNodes.get(0);
        assertThat(node.getParseError(), blockParseErrorMatcher);
        assertThat(node, hasType(blockType));

        childNodes = ((CompositeSourceNode) node).getChildNodes();
        assertThat(childNodes.size(), is(thirdChildNodeMatcher == null ? 2 : 3));
        assertThat(childNodes.get(0), both(hasType(BlockDirectiveLine.class)).and(hasProperty("parseError", nullValue())));
        assertThat(childNodes.get(1), hasType(bodyType));
        if (thirdChildNodeMatcher != null) {
            assertThat(childNodes.get(2), both(thirdChildNodeMatcher).and(hasProperty("parseError", nullValue())));
        }
    }

    // This class is not meant to be instantiated.
    private BlockParserTestsCommon() {
    }

}
