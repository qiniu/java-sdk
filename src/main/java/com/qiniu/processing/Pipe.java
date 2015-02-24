package com.qiniu.processing;

import java.util.ArrayList;


public final class Pipe {
    private ArrayList<Operation> commands;
    private boolean persistent;

    private Pipe(boolean persistent) {
        commands = new ArrayList<Operation>();
        this.persistent = persistent;
    }

    public static Pipe create() {
        return new Pipe(false);
    }

    public static Pipe createPersistent() {
        return new Pipe(true);
    }

    public Pipe append(Operation cmd) {
        commands.add(cmd);
        return this;
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        boolean noStart = false;
        for (Operation cmd : commands) {
            if (!persistent && cmd.onlyPersistent()) {
                throw new IllegalArgumentException(cmd.toString() + " is only for persistent");
            }
            if (noStart) {
                b.append("|");
            }
            b.append(cmd.toString());
            noStart = true;
        }
        return b.toString();
    }
}
