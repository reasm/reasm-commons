package org.reasm.commons.messages;

import java.util.Objects;

import javax.annotation.Nonnull;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when an invalid token is encountered in an operand.
 *
 * @author Francis Gagné
 */
public class InvalidTokenErrorMessage extends AssemblyErrorMessage {

    @Nonnull
    private final String token;

    /**
     * Initializes a new InvalidTokenErrorMessage.
     *
     * @param token
     *            the invalid token
     */
    public InvalidTokenErrorMessage(@Nonnull String token) {
        super("Invalid token: <" + Objects.requireNonNull(token, "token") + ">");
        this.token = token;
    }

    /**
     * Gets the invalid token that caused this message.
     *
     * @return the invalid token
     */
    @Nonnull
    public final String getToken() {
        return this.token;
    }

}
