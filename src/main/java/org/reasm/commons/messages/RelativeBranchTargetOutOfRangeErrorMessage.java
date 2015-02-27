package org.reasm.commons.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when the distance between a branch instruction and its target is too large
 * for the instruction.
 *
 * @author Francis Gagné
 */
public class RelativeBranchTargetOutOfRangeErrorMessage extends AssemblyErrorMessage {

    private final long displacement;

    /**
     * Initializes a new BranchTargetOutOfRangeErrorMessage.
     *
     * @param displacement
     *            the distance between the branch instruction and its target, as it would be encoded in the instruction
     */
    public RelativeBranchTargetOutOfRangeErrorMessage(long displacement) {
        super("Target out of range for relative branch (displacement of " + displacement + " bytes)");
        this.displacement = displacement;
    }

    /**
     * Gets the distance between the branch instruction and its target, as it would be encoded in the instruction.
     *
     * @return the displacement
     */
    public final long getDisplacement() {
        return this.displacement;
    }

}
