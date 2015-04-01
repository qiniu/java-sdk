package com.qiniu.processing.util;

import java.util.ArrayList;


public final class Pipe {
    private ArrayList<Operation> commands;

    private Pipe() {
        commands = new ArrayList<Operation>();
    }

    public static Pipe create() {
        return new Pipe();
    }

    public Pipe append(Operation cmd) {
        commands.add(cmd);
        return this;
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        boolean noStart = false;
        for (Operation cmd : commands) {
            if (noStart) {
                b.append("|");
            }
            b.append(cmd.build());
            noStart = true;
        }
        return b.toString();
    }
}
