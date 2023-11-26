/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.mips.memory;

public class Registers {
    public static final int INVALID = -1;
    public static final int ZERO = 0;
    public static final int AT = 1; // assembler temporary register
    public static final int V0 = 2; // return register
    public static final int V1 = 3; // return register
    public static final int A0 = 4; // argument register
    public static final int A1 = 5; // argument register
    public static final int A2 = 6; // argument register
    public static final int A3 = 7; // argument register
    public static final int T0 = 8;
    public static final int T1 = 9;
    public static final int T2 = 10;
    public static final int T3 = 11;
    public static final int T4 = 12;
    public static final int T5 = 13;
    public static final int T6 = 14;
    public static final int T7 = 15;
    public static final int S0 = 16;
    public static final int S1 = 17;
    public static final int S2 = 18;
    public static final int S3 = 19;
    public static final int S4 = 20;
    public static final int S5 = 21;
    public static final int S6 = 22;
    public static final int S7 = 23;
    public static final int T8 = 24;    // for array index
    public static final int T9 = 25;    // for array index
    public static final int K0 = 26;
    public static final int K1 = 27;
    public static final int GP = 28;
    public static final int SP = 29;
    public static final int FP = 30;
    public static final int RA = 31;

    public static final int[] ARGUMENT_REGISTERS = new int[]{ A0, A1, A2, A3 };
    public static final int[] CALLEE_SAVED_REGISTERS = new int[]{ S0, S1, S2, S3, S4, S5, S6, S7, FP, RA };
    public static final int[] CALLER_SAVED_REGISTERS = new int[]{ T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, V0, V1, AT };
    public static final int[] ALL_REGISTERS = new int[]{ ZERO, AT, V0, V1, A0, A1, A2, A3, T0, T1, T2, T3, T4, T5, T6, T7, S0, S1, S2, S3, S4, S5, S6, S7, T8, T9, K0, K1, GP, SP, FP, RA };

    public static final String[] REGISTER_NAMES = new String[]{
            "zero", "at", "v0", "v1", "a0", "a1", "a2", "a3",
            "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7",
            "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7",
            "t8", "t9", "k0", "k1", "gp", "sp", "fp", "ra" };

    public static String name(int id) {
        if (id == -1) {
            return "INVALID";
        }

        return REGISTER_NAMES[id];
    }

    public static final int HOT_PRIORITY = 0;
    public static final int HIGH_PRIORITY = 5;
    public static final int MEDIUM_PRIORITY = 10;
    public static final int LOW_PRIORITY = 15;
    public static final int COLD_PRIORITY = 75159;

}
