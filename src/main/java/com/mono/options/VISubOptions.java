package com.mono.options;

import net.sourceforge.argparse4j.annotation.Arg;

/**
 * Created by mono-office on 2017/1/20.
 */
public class VISubOptions extends Options{
    @Arg(dest = "encoding")
    public int encoding;

    @Arg(dest = "output")
    public String outFileName;
}
