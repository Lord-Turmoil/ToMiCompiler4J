/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.pass.impl.pass;

import tomic.llvm.ir.value.User;
import tomic.llvm.ir.value.Value;
import tomic.llvm.ir.value.inst.Instruction;

import java.util.ArrayList;

public class PassExt {
    private PassExt() {}

    public static void replaceOperand(Instruction oldOperand, Value newOperand) {
        ArrayList<User> users = new ArrayList<>(oldOperand.getUsers());
        users.forEach(user -> user.replaceOperand(oldOperand, newOperand));
    }
}
