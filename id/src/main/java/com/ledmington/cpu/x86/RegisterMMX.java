package com.ledmington.cpu.x86;

/**
 * SSE registers.
 */
public final class RegisterMMX extends Register {

    /**
     * The register MM0.
     */
    public static final RegisterMMX MM0 = new RegisterMMX("mm0");

    /**
     * The register MM1.
     */
    public static final RegisterMMX MM1 = new RegisterMMX("mm1");

    /**
     * The register MM2.
     */
    public static final RegisterMMX MM2 = new RegisterMMX("mm2");

    /**
     * The register MM3.
     */
    public static final RegisterMMX MM3 = new RegisterMMX("mm3");

    /**
     * The register MM4.
     */
    public static final RegisterMMX MM4 = new RegisterMMX("mm4");

    /**
     * The register MM5.
     */
    public static final RegisterMMX MM5 = new RegisterMMX("mm5");

    /**
     * The register MM6.
     */
    public static final RegisterMMX MM6 = new RegisterMMX("mm6");

    /**
     * The register MM7.
     */
    public static final RegisterMMX MM7 = new RegisterMMX("mm7");

    private RegisterMMX(final String mnemonic) {
        super(mnemonic);
    }

    public static RegisterMMX fromByte(final byte b) {
        return switch (b) {
            case 0x00 -> MM0;
            case 0x01 -> MM1;
            case 0x02 -> MM2;
            case 0x03 -> MM3;
            case 0x04 -> MM4;
            case 0x05 -> MM5;
            case 0x06 -> MM6;
            case 0x07 -> MM7;
            default -> throw new IllegalArgumentException(String.format("Unknown register byte 0x%02x", b));
        };
    }

    public int bits() {
        return 64;
    }
}
