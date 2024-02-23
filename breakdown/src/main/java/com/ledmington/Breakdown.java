package com.ledmington;

import com.ledmington.cpu.x86.ModRM;
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

        if (binary[pos] == (byte) 0x8d) {
            System.out.printf("0x%02x -> LEA opcode\n", binary[pos]);
        } else {
            System.err.printf("Unknown opcode 0x%02x\n", binary[pos]);
            System.exit(-1);
        }
        pos++;

        // ModR/m byte
        final ModRM modrm = new ModRM(binary[pos++]);
        System.out.printf(
                "0x%02x -> ModR/M byte (%s-%s-%s)\n",
                binary[pos - 1],
                BitUtils.toBinaryString(modrm.mod()).substring(6, 8),
                BitUtils.toBinaryString(modrm.reg()).substring(5, 8),
                BitUtils.toBinaryString(modrm.rm()).substring(5, 8));
        System.out.printf(
                "        Mod: %s -> %s\n",
                BitUtils.toBinaryString(modrm.mod()).substring(6, 8),
                switch (modrm.mod()) {
                    case 0x00 -> "no specific displacement";
                    case 0x01 -> "8-bit displacement";
                    case 0x02 -> "32-bit displacement";
                    case 0x03 -> "unknown";
                    default -> "invalid value";
                });
        System.out.printf(
                "        Reg: %s -> %s -> %s\n",
                BitUtils.toBinaryString(modrm.reg()).substring(5, 8),
                switch (modrm.reg()) {
                    case 0x00 -> "xAX";
                    case 0x01 -> "xCX";
                    case 0x02 -> "xDX";
                    case 0x03 -> "xBX";
                    case 0x04 -> "xSP";
                    case 0x05 -> "xBP";
                    case 0x06 -> "xSI";
                    case 0x07 -> "xDI";
                    default -> "invalid value";
                },
                (w ? "R" : "E")
                        + switch (modrm.reg()) {
                            case 0x00 -> "AX";
                            case 0x01 -> "CX";
                            case 0x02 -> "DX";
                            case 0x03 -> "BX";
                            case 0x04 -> "SP";
                            case 0x05 -> "BP";
                            case 0x06 -> "SI";
                            case 0x07 -> "DI";
                            default -> "invalid value";
                        });
    }
}
