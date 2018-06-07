package com.mono;

import com.mono.dataConvert.VI;
import com.mono.options.Verb;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparsers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by mono-office on 2017/1/20.
 */
public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class.getName());

    public static void main(String[] args) {
       // logger.info("hello world!");
        ArgumentParser parser = ArgumentParsers.newArgumentParser("data-preprocessing", true, "-", "@")
                .description("Data format converation of multiple data sources.")
                .defaultHelp(true)
                .version("${prog} v1.0");
        parser.addArgument("--version").action(Arguments.version());

        Subparsers subparsers = parser.addSubparsers()
                .title("verb commands")
                .description("Valid verb commands");

        subparsers = Verb.Set(subparsers);

        try {
            Namespace ns = parser.parseArgs(args);
            Object obj = ns.get("func");
            if(((IDataPreprocessing)obj).init(ns))
                ((IDataPreprocessing)obj).DataPreprocessing();

            //System.out.println(ConvertOpts.lpFileName);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }
    }
}
