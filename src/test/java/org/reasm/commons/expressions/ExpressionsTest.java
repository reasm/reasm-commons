package org.reasm.commons.expressions;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.reasm.AssemblyMessage;
import org.reasm.commons.messages.UnrecognizedEscapeSequenceWarningMessage;
import org.reasm.testhelpers.AssemblyMessageCollector;
import org.reasm.testhelpers.EquivalentAssemblyMessage;

import ca.fragag.Consumer;

/**
 * Test class for {@link Expressions}.
 *
 * @author Francis Gagné
 */
public class ExpressionsTest {

    /**
     * Parameterized test class for {@link Expressions#parseString(CharSequence, Consumer)}.
     *
     * @author Francis Gagné
     */
    @RunWith(Parameterized.class)
    public static class ParseStringTest {

        @Nonnull
        private static final AssemblyMessage[] NO_ASSEMBLY_MESSAGES = new AssemblyMessage[0];

        @Nonnull
        private static final AssemblyMessage[] DONT_CHECK_ASSEMBLY_MESSAGES = null;

        @Nonnull
        private static final AssemblyMessage UNRECOGNIZED_ESCAPE_SEQUENCE_WARNING_MESSAGE = new UnrecognizedEscapeSequenceWarningMessage(
                'z');

        @Nonnull
        private static final ArrayList<Object[]> TEST_DATA = new ArrayList<>();

        static {
            // An empty string delimited by apostrophes (single quotes)
            addDataItem("''", "");

            // An empty string delimited by double quotes
            addDataItem("\"\"", "");

            // A string delimited by apostrophes (single quotes)
            addDataItem("'abcdef'", "abcdef");

            // A string delimited by double quotes
            addDataItem("\"abcdef\"", "abcdef");

            // A string delimited by apostrophes (single quotes) with all valid escape sequences plus one invalid escape sequence
            addDataItem("'\\0\\\"\\\'\\\\\\a\\b\\f\\n\\r\\t\\z'", "\0\"\'\\\u0007\b\f\n\r\tz",
                    UNRECOGNIZED_ESCAPE_SEQUENCE_WARNING_MESSAGE);

            // A string delimited by double quotes with all valid escape sequences plus one invalid escape sequence
            addDataItem("\"\\0\\\"\\\'\\\\\\a\\b\\f\\n\\r\\t\\z\"", "\0\"\'\\\u0007\b\f\n\r\tz",
                    UNRECOGNIZED_ESCAPE_SEQUENCE_WARNING_MESSAGE);

            // A string delimited by apostrophes (single quotes) with all valid escape sequences plus one invalid escape sequence (parse with assemblyMessageConsumer == null)
            addDataItem("'\\0\\\"\\\'\\\\\\a\\b\\f\\n\\r\\t\\z'", "\0\"\'\\\u0007\b\f\n\r\tz", DONT_CHECK_ASSEMBLY_MESSAGES);

            // A string delimited by double quotes with all valid escape sequences plus one invalid escape sequence (parse with assemblyMessageConsumer == null)
            addDataItem("\"\\0\\\"\\\'\\\\\\a\\b\\f\\n\\r\\t\\z\"", "\0\"\'\\\u0007\b\f\n\r\tz", DONT_CHECK_ASSEMBLY_MESSAGES);

            // A string delimited by apostrophes (single quotes) with an escaped backslash
            addDataItem("'\\\\'", "\\");

            // A string delimited by double quotes with an escaped backslash
            addDataItem("\"\\\\\"", "\\");
        }

        /**
         * Gets the test data for this parameterized test.
         *
         * @return the test data
         */
        @Nonnull
        @Parameters
        public static List<Object[]> data() {
            return TEST_DATA;
        }

        private static void addDataItem(@Nonnull String string, @Nonnull String expectedResult) {
            addDataItem(string, expectedResult, NO_ASSEMBLY_MESSAGES);
        }

        private static void addDataItem(@Nonnull String string, @Nonnull String expectedResult,
                @CheckForNull AssemblyMessage... expectedAssemblyMessages) {
            TEST_DATA.add(new Object[] { string, expectedResult, expectedAssemblyMessages });
        }

        @Nonnull
        private final String string;
        @Nonnull
        private final String expectedResult;
        @CheckForNull
        private final List<Matcher<? super AssemblyMessage>> expectedAssemblyMessageMatchers;

        /**
         * Initializes a new ParseStringTest.
         *
         * @param string
         *            the string to parse
         * @param expectedResult
         *            the expected result
         * @param expectedAssemblyMessages
         *            the expected assembly messages that should be generated when parsing the expression
         */
        public ParseStringTest(@Nonnull String string, @Nonnull String expectedResult,
                @CheckForNull AssemblyMessage... expectedAssemblyMessages) {
            this.string = string;
            this.expectedResult = expectedResult;

            if (expectedAssemblyMessages == null) {
                this.expectedAssemblyMessageMatchers = null;
            } else {
                final ArrayList<Matcher<? super AssemblyMessage>> matchers = new ArrayList<>(expectedAssemblyMessages.length);
                for (AssemblyMessage expectedAssemblyMessage : expectedAssemblyMessages) {
                    matchers.add(new EquivalentAssemblyMessage(expectedAssemblyMessage));
                }

                this.expectedAssemblyMessageMatchers = matchers;
            }
        }

        /**
         * Asserts that {@link Expressions#parseString(CharSequence, Consumer)} correctly parses a string.
         */
        @Test
        public void parseString() {
            final ArrayList<AssemblyMessage> messages = new ArrayList<>();
            final Consumer<AssemblyMessage> assemblyMessageConsumer = this.expectedAssemblyMessageMatchers == null ? null
                    : new AssemblyMessageCollector(messages);
            final String result = Expressions.parseString(this.string, assemblyMessageConsumer);
            assertThat(result, is(this.expectedResult));

            if (this.expectedAssemblyMessageMatchers != null) {
                if (this.expectedAssemblyMessageMatchers.isEmpty()) {
                    assertThat(messages, is(empty()));
                } else {
                    assertThat(messages, contains(this.expectedAssemblyMessageMatchers));
                }
            }
        }

    }

    /**
     * Asserts that {@link Expressions#parseString(CharSequence, Consumer)}
     */
    @Test(expected = NullPointerException.class)
    public void parseStringNullText() {
        Expressions.parseString(null, new AssemblyMessageCollector(new ArrayList<AssemblyMessage>()));
    }

    /**
     * Asserts that {@link Expressions#serializeString(String)} correctly serializes a string that contains no special characters.
     */
    @Test
    public void serializeStringBasic() {
        assertThat(Expressions.serializeString("abc"), is("\"abc\""));
    }

    /**
     * Asserts that {@link Expressions#serializeString(String)} correctly serializes a string that contains special characters.
     */
    @Test
    public void serializeStringEscapes() {
        assertThat(Expressions.serializeString("\0\u0007\b\t\n\f\r\""), is("\"\\0\\a\\b\\t\\n\\f\\r\\\"\""));
    }

    /**
     * Asserts that {@link Expressions#serializeString(String)} throws a {@link NullPointerException} when the <code>string</code>
     * argument is <code>null</code>
     */
    @Test(expected = NullPointerException.class)
    public void serializeStringNull() {
        Expressions.serializeString(null);
    }

}
