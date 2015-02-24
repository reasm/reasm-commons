package org.reasm.commons.expressions;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.reasm.AssemblyMessage;
import org.reasm.commons.messages.UnrecognizedEscapeSequenceWarningMessage;

import ca.fragag.Consumer;

/**
 * Contains methods related to expressions.
 *
 * @author Francis Gagné
 */
public final class Expressions {

    @Nonnull
    private static final ThreadLocal<ObjectPool<StringBuilder>> STRING_BUILDER_POOL = new ThreadLocal<ObjectPool<StringBuilder>>() {

        @Override
        protected ObjectPool<StringBuilder> initialValue() {
            return new GenericObjectPool<>(new BasePoolableObjectFactory<StringBuilder>() {

                @Override
                public StringBuilder makeObject() {
                    return new StringBuilder();
                };

                @Override
                public void passivateObject(StringBuilder sb) {
                    sb.setLength(0);
                };

            });
        };

    };

    /**
     * Parses a quoted string.
     *
     * @param string
     *            the quoted string, including the initial and final delimiters
     * @param assemblyMessageConsumer
     *            a {@link Consumer} that will receive assembly messages for problems that occurred while parsing the string
     * @return the string's contents
     */
    public static String parseString(@Nonnull CharSequence string, @CheckForNull Consumer<AssemblyMessage> assemblyMessageConsumer) {
        if (string == null) {
            throw new NullPointerException("string");
        }

        final StringBuilder stringValue = acquireStringBuilder();
        try {
            // The token contains the initial and final quote or apostrophe delimiters.
            int codePoint;
            for (int i = 1; i < string.length() - 1; i += Character.charCount(codePoint)) {
                codePoint = Character.codePointAt(string, i);

                if (codePoint == '\\') {
                    i++;
                    codePoint = Character.codePointAt(string, i);

                    switch (codePoint) {
                    // Output these characters as-is, but don't raise a warning.
                    case '"':
                    case '\'':
                    case '\\':
                        break;

                    // Replace these characters with another character.
                    case '0':
                        codePoint = 0; // null
                        break;
                    case 'a':
                        codePoint = 7; // bell
                        break;
                    case 'b':
                        codePoint = '\b'; // backspace
                        break;
                    case 't':
                        codePoint = '\t'; // horizontal tab
                        break;
                    case 'n':
                        codePoint = '\n'; // line feed
                        break;
                    case 'f':
                        codePoint = '\f'; // form feed
                        break;
                    case 'r':
                        codePoint = '\r'; // carriage return
                        break;

                    // Output all other characters as-is, and raise a warning.
                    default:
                        if (assemblyMessageConsumer != null) {
                            assemblyMessageConsumer.accept(new UnrecognizedEscapeSequenceWarningMessage(codePoint));
                        }

                        break;
                    }
                }

                stringValue.appendCodePoint(codePoint);
            }

            return stringValue.toString();
        } finally {
            releaseStringBuilder(stringValue);
        }
    }

    /**
     * Serializes a string such that it can be parsed by {@link #parseString(CharSequence, Consumer)}. The string is surrounded with
     * quotes and characters are escaped where necessary.
     *
     * @param string
     *            the string to serialize
     * @return the serialized string
     */
    @Nonnull
    public static String serializeString(@Nonnull String string) {
        if (string == null) {
            throw new NullPointerException("string");
        }

        final StringBuilder sb = new StringBuilder();
        sb.append('"');

        int codePoint;
        for (int i = 0; i < string.length(); i += Character.charCount(codePoint)) {
            codePoint = string.codePointAt(i);

            switch (codePoint) {
            case 0:
                sb.append("\\0");
                break;

            case 7: // bell
                sb.append("\\a");
                break;

            case '\b':
                sb.append("\\b");
                break;

            case '\t':
                sb.append("\\t");
                break;

            case '\n':
                sb.append("\\n");
                break;

            case '\f':
                sb.append("\\f");
                break;

            case '\r':
                sb.append("\\r");
                break;

            case '"':
                sb.append("\\\"");
                break;

            default:
                sb.appendCodePoint(codePoint);
                break;
            }
        }

        return sb.append('"').toString();
    }

    @Nonnull
    private static StringBuilder acquireStringBuilder() {
        try {
            return STRING_BUILDER_POOL.get().borrowObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void releaseStringBuilder(@Nonnull StringBuilder sb) {
        try {
            STRING_BUILDER_POOL.get().returnObject(sb);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // This class is not meant to be instantiated.
    private Expressions() {
    }

}
