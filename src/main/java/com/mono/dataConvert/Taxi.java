package com.mono.dataConvert;

import com.mono.IDataPreprocessing;
import com.mono.common.FileUtils;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sun.security.util.Length;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;

public class Taxi extends FCD implements IDataPreprocessing {
    private static final Logger logger = LogManager.getLogger(FCD.class.getName());
    private static String fileSep = Matcher.quoteReplacement(File.separator);

    @Override
    public boolean init(Namespace ns) {
        super.init(ns);
        try {
            String sDeepestFolder = new FileUtils().deepestFolder(new File(_dataDirectory));
            String[] temp = sDeepestFolder.split(fileSep);
            _date = StringUtils.join(temp[temp.length - 3], temp[temp.length - 2], temp[temp.length - 1]);
            System.out.println(_date);
//            _outDirectory =
            return true;
        }catch (Exception ex) {
            logger.error("Error: initalization error! " + ex.getMessage());
            return false;
        }
    }

    @Override
    public void DataPreprocessing() {
        try {
            long time1 = System.currentTimeMillis();

            logger.info("Creating 288 output files...");
            super.CreateOutputFiles();
        } catch (Exception e) {
            logger.error("Error: handle file error." + e.getMessage());
        }

    }
}
