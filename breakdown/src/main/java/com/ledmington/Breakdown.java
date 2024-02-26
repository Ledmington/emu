package com.ledmington;

import com.ledmington.cpu.x86.ModRM;
import com.ledmington.cpu.x86.Register;
import com.ledmington.cpu.x86.SIB;
import com.ledmington.utils.BitUtils;

public final class Breakdown {
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

    private static void breakdown(final byte[] binary) {
        int pos = 0;
        boolean w = false;
        boolean r = false;
        boolean x = false;
        boolean b = false;
        boolean hasAddressSizeOverridePrefix = false;

        if (binary[pos] == (byte) 0x67) { // address size override prefix
            System.out.printf(
                    "0x%02x -> address size override prefix -> 32-bit registers are used to compute memory location\n",
                    binary[pos]);
            hasAddressSizeOverridePrefix = true;
            pos++;
        }

        if (BitUtils.and(binary[pos], (byte) 0xf0) == (byte) 0x40) {
            w = BitUtils.and(binary[pos], (byte) 0x08) != 0;
            r = BitUtils.and(binary[pos], (byte) 0x04) != 0;
            x = BitUtils.and(binary[pos], (byte) 0x02) != 0;
            b = BitUtils.and(binary[pos], (byte) 0x01) != 0;
            System.out.printf(
                    "0x%02x -> REX prefix -> %s%s%s%s\n",
                    binary[pos], (w ? ".W" : ""), (r ? ".R" : ""), (x ? ".X" : ""), (b ? ".B" : ""));
            if (w) {
                System.out.println("        .W -> first operand is 64 bits");
            }
            if (r) {
                System.out.println("        .R -> the ModR/M Reg field is extended");
            }
            if (x) {
                System.out.println("        .X -> the SIB Index field is extended");
            }
            if (b) {
                System.out.println(
                        "        .B -> the Opcode Reg field, the ModR/M R/M field or the SIB Base field is extended");
            }

            pos++;
        }

        System.out.printf("0x%02x -> ", binary[pos]);
        switch (binary[pos]) {
            case (byte) 0x8d:
                System.out.println("LEA opcode");
                break;
            case (byte) 0x89:
                System.out.println("MOV opcode");
                break;
            default:
                System.err.println("unknown");
                System.exit(-1);
                break;
        }
        pos++;

        boolean isSIBByteNeeded = false;
        boolean isDisplacementNeeded = false;
        int displacementBytes = 0;

        // ModR/m byte
        final ModRM modrm = new ModRM(binary[pos++]);
        System.out.printf(
                "0x%02x -> ModR/M byte (%s-%s-%s)\n",
                binary[pos - 1],
                BitUtils.toBinaryString(modrm.mod()).substring(6, 8),
                BitUtils.toBinaryString(modrm.reg()).substring(5, 8),
                BitUtils.toBinaryString(modrm.rm()).substring(5, 8));
        System.out.printf(
                "        Mod: %s -> ", BitUtils.toBinaryString(modrm.mod()).substring(6, 8));

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
        final String reg = Register.fromCode(modrm.reg(), w, r).toIntelSyntax();
        System.out.printf(
                "        Reg: %s -> %s\n", BitUtils.toBinaryString(modrm.reg()).substring(5, 8), reg);
        String rm =
                Register.fromCode(modrm.rm(), !hasAddressSizeOverridePrefix, b).toIntelSyntax();
        if (modrm.mod() == (byte) 0x00 && rm.endsWith("bp")) {
            rm = hasAddressSizeOverridePrefix ? "eip" : "rip";
        }
        System.out.printf(
                "        R/M: %s -> %s", BitUtils.toBinaryString(modrm.rm()).substring(5, 8), rm);
        if (modrm.mod() != (byte) 0x03 && modrm.rm() == (byte) 0x04) {
            System.out.print(" (a SIB byte follows)");
            isSIBByteNeeded = true;
        }
        System.out.println();

        if (isSIBByteNeeded) {
            final SIB sib = new SIB(binary[pos++]);
            System.out.printf(
                    "0x%02x -> SIB byte (%s-%s-%s)\n",
                    binary[pos - 1],
                    BitUtils.toBinaryString(sib.scale()).substring(6, 8),
                    BitUtils.toBinaryString(sib.index()).substring(5, 8),
                    BitUtils.toBinaryString(sib.base()).substring(5, 8));
            System.out.printf(
                    "        Scale: %s -> *%d\n",
                    BitUtils.toBinaryString(sib.scale()).substring(6, 8), 1 << BitUtils.asInt(sib.scale()));
            final String index = Register.fromCode(sib.index(), !hasAddressSizeOverridePrefix, x)
                    .toIntelSyntax();
            System.out.printf(
                    "        Index: %s -> %s\n",
                    BitUtils.toBinaryString(sib.index()).substring(5, 8), index);
            final String base = Register.fromCode(sib.base(), !hasAddressSizeOverridePrefix, b)
                    .toIntelSyntax();
            System.out.printf(
                    "        Base: %s -> %s\n",
                    BitUtils.toBinaryString(sib.base()).substring(5, 8), base);
        }

        if (isDisplacementNeeded) {
            switch (displacementBytes) {
                case 1:
                    System.out.printf("0x%02x -> displacement\n", binary[pos]);
                    pos++;
                    break;
                case 4:
                    System.out.printf(
                            "0x%02x%02x%02x%02x -> displacement\n",
                            binary[pos + 3], binary[pos + 2], binary[pos + 1], binary[pos]);
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
}
