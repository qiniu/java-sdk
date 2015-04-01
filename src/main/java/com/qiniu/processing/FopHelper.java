package com.qiniu.processing;

import com.qiniu.processing.util.Operation;
import com.qiniu.processing.util.Pipe;
import com.qiniu.util.StringUtils;

/**
 * 辅助生成fops指令字符串。
 *
 * 生成的fops指令可用于上传策略中 persistentOps 或(和) 触发持久化处理 的 指令
 */
public class FopHelper {
    private FopHelper(){

    }

    /**
     * 生成一个完整的fop指令
     *
     * @param cmds
     * @return
     */
    public static String genFop(Operation... cmds) {
        return genFops(genPipe(cmds));
    }

    /**
     * 生成一个表示完整的fop指令的pipe实例。
     * 一个pipe中包含多个cmd，每个以管道符分割。
     * @param cmds
     * @return
     */
    public static Pipe genPipe(Operation... cmds) {
        Pipe p = Pipe.create();
        for (Operation cmd : cmds) {
            p.append(cmd);
        }
        return p;
    }

    /**
     * 向 Pipe 追加管道指令
     *
     * @param cmds
     * @return
     */
    public static Pipe appendPipe(Pipe p, Operation... cmds) {
        p = p == null ? Pipe.create() : p;
        for (Operation cmd : cmds) {
            p.append(cmd);
        }
        return p;
    }

    /**
     * 生成多个fop指令的字符串形式。
     * 每个 fop 以分号(;)隔开。
     *
     * @param pipes
     * @return
     */
    public static String genFops(Pipe... pipes) {
        return StringUtils.join(pipes, ";", null);
    }
}
