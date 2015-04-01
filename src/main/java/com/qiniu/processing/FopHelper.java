package com.qiniu.processing;

import com.qiniu.processing.util.Operation;
import com.qiniu.processing.util.Pipe;
import com.qiniu.util.StringUtils;

/**
 * Created by Simon on 2015/4/1.
 */
public class FopHelper {
    public static String genFops(Operation... cmds) {
        return genFops(genPipe(cmds));
    }

    public static Pipe genPipe(Operation... cmds) {
        Pipe p = Pipe.create();
        for (Operation cmd : cmds) {
            p.append(cmd);
        }
        return p;
    }

    public static String genFops(Pipe... pipes) {
        return StringUtils.join(pipes, ";", null);
    }
}
