package com.ledmington;

import java.util.Map;

import com.ledmington.cpu.x86.ModRM;
import com.ledmington.cpu.x86.Opcode;
import com.ledmington.cpu.x86.Registers;
import com.ledmington.cpu.x86.SIB;
import com.ledmington.utils.BitUtils;
import com.ledmington.utils.ImmutableMap;

public final class Breakdown {

    private static final Map<Byte, Opcode> opcodes = ImmutableMap.<Byte, Opcode>builder()
            .put((byte) 0x01, Opcode.ADD)
            .put((byte) 0x39, Opcode.CMP)
            .put((byte) 0x89, Opcode.MOV)
            .put((byte) 0xb9, Opcode.MOV)
            .build();

    private static final Map<Byte, Opcode> multiByteOpcodes = ImmutableMap.<Byte, Opcode>builder()
            .put((byte) 0x44, Opcode.CMOVE)
            .put((byte) 0x84, Opcode.JE)
            .put((byte) 0x87, Opcode.JA)
            .put((byte) 0x8f, Opcode.JG)
            .put((byte) 0xb6, Opcode.MOVZX)
            .put((byte) 0xb7, Opcode.MOVZX)
            .build();

    public static void main(final String[] args) {
        if (args.length == 0) {
            System.err.println("I was expecting a hexadecimal string to decode");
            System.exit(-1);
        }

        String hexCode = String.join("", args);

        if (hexCode.length() % 2 != 0) {
            System.out.println("WARNING: Length of input string is odd, adding a '0' at the end");
            hexCode = hexCode + "0";
        }

        final int codeLength = hexCode.length() / 2;
        final byte[] binary = new byte[codeLength];
        for (int i = 0; i < codeLength; i++) {
            binary[i] = BitUtils.parseByte(hexCode.substring(i * 2, i * 2 + 2));
        }

        breakdown(binary);
    }

    private static boolean isMultibyteOpcodePrefix(final byte b) {
        return b == (byte) 0x0f;
    }

    private static void breakdown(final byte[] binary) {
        int pos = 0;
        boolean w = false;
        boolean r = false;
        boolean x = false;
        boolean b = false;
        boolean hasLegacyGroup1Prefix = false;
        boolean hasLegacyGroup2Prefix = false;
        boolean hasOperandSizeOverridePrefix = false;
        boolean hasAddressSizeOverridePrefix = false;

        // parse legacy prefixes
        for (int i = 0; i < 4; i++) {
            byte p = binary[pos++];

            if (isLegacyPrefixGroup1(p)) {
                if (hasLegacyGroup1Prefix) {
                    throw new IllegalStateException(
                            String.format("Found duplicate legacy prefix group 1 at byte 0x%08x", pos));
                }
                System.out.printf("0x%02x   -> Legacy prefix group 1\n", p);
                hasLegacyGroup1Prefix = true;
            } else if (isLegacyPrefixGroup2(p)) {
                if (hasLegacyGroup2Prefix) {
                    throw new IllegalStateException(
                            String.format("Found duplicate legacy prefix group 2 at byte 0x%08x", pos));
                }
                System.out.printf("0x%02x   -> Legacy prefix group 2\n", p);
                hasLegacyGroup2Prefix = true;
            } else if (p == (byte) 0x66) {
                if (hasOperandSizeOverridePrefix) {
                    throw new IllegalStateException(
                            String.format("Found duplicate operand size override prefix at byte 0x%08x", pos));
                }
                System.out.printf("0x%02x   -> Operand size override prefix\n", p);
                hasOperandSizeOverridePrefix = true;
            } else if (p == (byte) 0x67) {
                if (hasAddressSizeOverridePrefix) {
                    throw new IllegalStateException(
                            String.format("Found duplicate address size override prefix at byte 0x%08x", pos));
                }
                System.out.printf("0x%02x   -> Address size override prefix\n", p);
                hasAddressSizeOverridePrefix = true;
            } else {
                pos--;
                break;
            }
        }

        // parse REX prefix
        if (BitUtils.and(binary[pos], (byte) 0xf0) == (byte) 0x40) {
            w = BitUtils.and(binary[pos], (byte) 0x08) != 0;
            r = BitUtils.and(binary[pos], (byte) 0x04) != 0;
            x = BitUtils.and(binary[pos], (byte) 0x02) != 0;
            b = BitUtils.and(binary[pos], (byte) 0x01) != 0;
            System.out.printf(
                    "0x%02x   -> REX prefix -> %s%s%s%s\n",
                    binary[pos], (w ? ".W" : ""), (r ? ".R" : ""), (x ? ".X" : ""), (b ? ".B" : ""));
            if (w) {
                System.out.println("          .W -> first operand is 64 bits");
            }
            if (r) {
                System.out.println("          .R -> the ModR/M Reg field is extended");
            }
            if (x) {
                System.out.println("          .X -> the SIB Index field is extended");
            }
            if (b) {
                System.out.println(
                        "          .B -> the Opcode Reg field, the ModR/M R/M field or the SIB Base field is extended");
            }

            pos++;
        }

        if (isMultibyteOpcodePrefix(binary[pos])) {
            System.out.printf("0x%02x%02x -> ", binary[pos], binary[pos + 1]);
            if (multiByteOpcodes.containsKey(binary[pos + 1])) {
                System.out.println(multiByteOpcodes.get(binary[pos + 1]) + " opcode");
            } else {
                System.out.println("unknown opcode");
                System.exit(-1);
            }
            pos++;
        } else if (binary[pos] == (byte) 0x80
                || binary[pos] == (byte) 0x81
                || binary[pos] == (byte) 0x82
                || binary[pos] == (byte) 0x83) {
            System.out.printf("0x%02x%02x -> ", binary[pos], binary[pos + 1]);
            final ModRM modrm = new ModRM(binary[pos + 1]);
            System.out.println(
                    switch (modrm.reg()) {
                        case (byte) 0x00 -> "ADD";
                        case (byte) 0x01 -> "OR";
                        case (byte) 0x02 -> "ADC";
                        case (byte) 0x03 -> "SBB";
                        case (byte) 0x04 -> "AND";
                        case (byte) 0x05 -> "SUB";
                        case (byte) 0x06 -> "XOR";
                        case (byte) 0x07 -> "CMP";
                        default -> "unknown";
                    });
        } else if (binary[pos] == (byte) 0xff) {
            System.out.printf("0x%02x%02x -> ", binary[pos], binary[pos + 1]);
            final ModRM modrm = new ModRM(binary[pos + 1]);
            System.out.println(
                    switch (modrm.reg()) {
                        case (byte) 0x00 -> "INC";
                        case (byte) 0x01 -> "DEC";
                        case (byte) 0x02 -> "CALL";
                        case (byte) 0x03 -> "CALL";
                        case (byte) 0x04 -> "JMP";
                        case (byte) 0x05 -> "JMP";
                        case (byte) 0x06 -> "PUSH";
                        case (byte) 0x07 -> "<reserved>";
                        default -> "unknown";
                    });
        } else {
            System.out.printf("0x%02x   -> ", binary[pos]);
            if (opcodes.containsKey(binary[pos])) {
                System.out.println(opcodes.get(binary[pos]) + " opcode");
            } else {
                System.out.println("unknown opcode");
                System.exit(-1);
            }
        }
        pos++;

        boolean isSIBByteNeeded = false;
        boolean isDisplacementNeeded = false;
        int displacementBytes = 0;

        // ModR/m byte
        final ModRM modrm = new ModRM(binary[pos++]);
        System.out.printf(
                "0x%02x   -> ModR/M byte (%s-%s-%s)\n",
                binary[pos - 1],
                BitUtils.toBinaryString(modrm.mod()).substring(6, 8),
                BitUtils.toBinaryString(modrm.reg()).substring(5, 8),
                BitUtils.toBinaryString(modrm.rm()).substring(5, 8));
        System.out.printf(
                "          Mod: %s -> ", BitUtils.toBinaryString(modrm.mod()).substring(6, 8));

        switch (modrm.mod()) {
            case 0x00:
                if (modrm.rm() == (byte) 0x05) {
                    System.out.print("32-bit displacement");
                    isDisplacementNeeded = true;
                    displacementBytes = 4;
                } else {
                    System.out.print("no specific displacement");
                }
                break;
            case 0x01:
                System.out.print("8-bit displacement");
                isDisplacementNeeded = true;
                displacementBytes = 1;
                break;
            case 0x02:
                System.out.print("32-bit displacement");
                isDisplacementNeeded = true;
                displacementBytes = 4;
                break;
            case 0x03:
                System.out.print("no specific displacement");
                break;
            default:
                System.out.print("invalid value");
                break;
        }
        System.out.println();
        final String reg = Registers.fromCode(modrm.reg(), w, r, hasOperandSizeOverridePrefix)
                .toIntelSyntax();
        System.out.printf(
                "          Reg: %s -> %s\n",
                BitUtils.toBinaryString(modrm.reg()).substring(5, 8), reg);
        String rm = Registers.fromCode(modrm.rm(), !hasAddressSizeOverridePrefix, b, hasOperandSizeOverridePrefix)
                .toIntelSyntax();
        if (modrm.mod() == (byte) 0x00 && rm.endsWith("bp")) {
            rm = hasAddressSizeOverridePrefix ? "eip" : "rip";
        }
        System.out.printf(
                "          R/M: %s -> %s", BitUtils.toBinaryString(modrm.rm()).substring(5, 8), rm);
        if (modrm.mod() != (byte) 0x03 && modrm.rm() == (byte) 0x04) {
            System.out.print(" (a SIB byte follows)");
            isSIBByteNeeded = true;
        }
        System.out.println();

        if (isSIBByteNeeded) {
            final SIB sib = new SIB(binary[pos++]);
            System.out.printf(
                    "0x%02x   -> SIB byte (%s-%s-%s)\n",
                    binary[pos - 1],
                    BitUtils.toBinaryString(sib.scale()).substring(6, 8),
                    BitUtils.toBinaryString(sib.index()).substring(5, 8),
                    BitUtils.toBinaryString(sib.base()).substring(5, 8));
            System.out.printf(
                    "          Scale: %s -> *%d\n",
                    BitUtils.toBinaryString(sib.scale()).substring(6, 8), 1 << BitUtils.asInt(sib.scale()));
            final String index = Registers.fromCode(
                            sib.index(), !hasAddressSizeOverridePrefix, x, hasOperandSizeOverridePrefix)
                    .toIntelSyntax();
            System.out.printf(
                    "          Index: %s -> %s\n",
                    BitUtils.toBinaryString(sib.index()).substring(5, 8), index);
            final String base = Registers.fromCode(
                            sib.base(), !hasAddressSizeOverridePrefix, b, hasOperandSizeOverridePrefix)
                    .toIntelSyntax();
            System.out.printf(
                    "          Base: %s -> %s\n",
                    BitUtils.toBinaryString(sib.base()).substring(5, 8), base);
        }

        if (isDisplacementNeeded) {
            switch (displacementBytes) {
                case 1:
                    System.out.printf("0x%02x  ", binary[pos]);
                    if (binary[pos] < 0) {
                        System.out.printf(" -> -0x%x", (~binary[pos]) + 1);
                    }
                    System.out.println(" -> displacement");
                    pos++;
                    break;
                case 4:
                    final int disp32 = (BitUtils.asInt(binary[pos + 3]) << 24)
                            | (BitUtils.asInt(binary[pos + 2]) << 16)
                            | (BitUtils.asInt(binary[pos + 1]) << 8)
                            | BitUtils.asInt(binary[pos]);
                    System.out.printf("0x%08x  ", disp32);
                    if (disp32 < 0) {
                        System.out.printf(" -> -0x%x", (~disp32) + 1);
                    }
                    System.out.println(" -> displacement");
                    pos += 4;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid displacement");
            }
        }

        if (pos < binary.length) {
            System.out.print("Unused bytes: ");
            for (; pos < binary.length; pos++) {
                System.out.printf("%02x ", binary[pos]);
            }
            System.out.println();
        }
    }

    private static boolean isLegacyPrefixGroup1(final byte prefix) {
        final byte LOCK_PREFIX = (byte) 0xf0;
        final byte REPNE_PREFIX = (byte) 0xf2; // REPNE / REPNZ
        final byte REP_PREFIX = (byte) 0xf3; // REP / REPE / REPZ
        return prefix == LOCK_PREFIX || prefix == REPNE_PREFIX || prefix == REP_PREFIX;
    }

    private static boolean isLegacyPrefixGroup2(final byte prefix) {
        final byte CS_SEGMENT_OVERRIDE_PREFIX = (byte) 0x2e;
        final byte SS_SEGMENT_OVERRIDE_PREFIX = (byte) 0x36;
        final byte DS_SEGMENT_OVERRIDE_PREFIX = (byte) 0x3e;
        final byte ES_SEGMENT_OVERRIDE_PREFIX = (byte) 0x26;
        final byte FS_SEGMENT_OVERRIDE_PREFIX = (byte) 0x64;
        final byte GS_SEGMENT_OVERRIDE_PREFIX = (byte) 0x65;
        final byte BRANCH_NOT_TAKEN_PREFIX = (byte) 0x2e;
        final byte BRANCH_TAKEN_PREFIX = (byte) 0x3e;
        return prefix == CS_SEGMENT_OVERRIDE_PREFIX
                || prefix == SS_SEGMENT_OVERRIDE_PREFIX
                || prefix == DS_SEGMENT_OVERRIDE_PREFIX
                || prefix == ES_SEGMENT_OVERRIDE_PREFIX
                || prefix == FS_SEGMENT_OVERRIDE_PREFIX
                || prefix == GS_SEGMENT_OVERRIDE_PREFIX
                || prefix == BRANCH_NOT_TAKEN_PREFIX
                || prefix == BRANCH_TAKEN_PREFIX;
    }
}
