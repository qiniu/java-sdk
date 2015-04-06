package com.qiniu.processing.util;

import java.util.ArrayList;

/**
 * 一个Pipe实例表示一条完整的fop指令
 */
public final class Command {
    private ArrayList<Operation> commands;

    private Command() {
        commands = new ArrayList<Operation>();
    }

    public static Command create() {
        return new Command();
    }

    public Command append(Operation operation) {
        commands.add(operation);
        return this;
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        boolean noStart = false;
        for (Operation operation : commands) {
            if (noStart) {
                b.append("|");
            }
            b.append(operation.build());
            noStart = true;
        }
        return b.toString();
    }
}
