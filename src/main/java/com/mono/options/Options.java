package com.mono.options;

import net.sourceforge.argparse4j.annotation.Arg;

/**
 * Created by mono-office on 2017/1/20.
 */
public abstract class Options {
    @Arg(dest = "input")
    public String inputData;

    @Arg(dest = "maxThread")
    public int maxThread;
}
