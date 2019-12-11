package com.mono.dataConvert;

import com.mono.IDataPreprocessing;
import com.mono.VO.MCData;
import com.mono.common.FileUtils;
import com.mono.common.ProgressBar;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

public class MetroCard implements IDataPreprocessing {
    private static final Logger logger = LogManager.getLogger(MetroCard.class.getName());
    private static String _CurrentDirectory = System.getProperty("user.dir"); //程序当前运行的路径
    private static String _ninput = null; //输入新数据
    private static String _oinput = null; //输入旧数据
    private static String _outputPath;
    private static String _tokenizer; //分隔符
    private String _nOutName; //新刷卡数据转换后的文件名称
    private String _oOutName; //旧刷卡数据转换后的文件名称
    private ICsvBeanWriter _CsvWriter; //最终合并的文件
    private static CsvPreference _csvDelimited; //分隔符

    @Override
    public boolean init(Namespace ns) {
        try {
            String suffix = "";
            String inputNoSuffix = "";

            _ninput = ns.get("new");
            _oinput = ns.get("old");
            _outputPath = ns.get("output");

            Properties prop = new Properties();
            prop.load(this.getClass().getResourceAsStream("/fieldOrder.properties"));
            _tokenizer = String.valueOf(prop.getProperty("metroCard.delimiter"));

            if (_tokenizer.equals("/t")) {
                _csvDelimited = new CsvPreference.Builder('"', 9, "\n").build();
            } else if (_tokenizer.equals(",")){
                _csvDelimited = new CsvPreference.Builder('"', 44, "\n").build();
            } else {
                _csvDelimited = new CsvPreference.Builder('"', _tokenizer.charAt(0), "\n").build();
            }

            if(_ninput != null){
                _ninput = FileUtils.checkFileValid(_ninput, _CurrentDirectory);
                if(_ninput == null) throw new Exception("Error: input new metrocard data is not exist!");

                if (_ninput.lastIndexOf(".") == -1) {
                    suffix = "";
                } else {
                    suffix = _ninput.substring(_ninput.lastIndexOf("."));
                }
                inputNoSuffix = FileUtils.getNoSuffix(_ninput);

                _nOutName = StringUtils.join(_CurrentDirectory, "\\convered_", inputNoSuffix, suffix);
            }

            if(_oinput != null){
                _oinput = FileUtils.checkFileValid(_oinput, _CurrentDirectory);
                if(_oinput == null) throw new Exception("Error: input old metrocard data is not exist!");
                if (_oinput.lastIndexOf(".") == -1) {
                    suffix = "";
                } else {
                    suffix = _oinput.substring(_oinput.lastIndexOf("."));
                }
                inputNoSuffix = FileUtils.getNoSuffix(_oinput);

                _oOutName = StringUtils.join(_CurrentDirectory, "\\convered_", inputNoSuffix, suffix);
            }

//            String inputName = file.getFileName().toString();
//            String inputNoSuffix = inputName.substring(0, inputName.lastIndexOf("."));
            Path file = Paths.get(_outputPath);
            if (_outputPath == null)
                _outputPath = StringUtils.join(_CurrentDirectory, "\\converted", suffix);

            if (!file.isAbsolute()) {
                _outputPath = StringUtils.join(_CurrentDirectory, "\\", _outputPath);
            }

            file = Paths.get(_outputPath);
            if (!Files.exists(file)) Files.createFile(file);

            _CsvWriter = new CsvBeanWriter(new OutputStreamWriter(new FileOutputStream(_outputPath), StandardCharsets.UTF_8), _csvDelimited);

            logger.info("Initalization completed!");
            return true;
        } catch (Exception ex) {
            logger.error("Error: initalization error! " + ex.getMessage());
            return false;
        }
    }

    @Override
    public void DataPreprocessing() {
        long time1 = System.currentTimeMillis();
        final String[] outheader = new String[]{"CARDID", "TRADEType", "TRADEDate", "TERMINALID"};

        try{
            _CsvWriter.writeHeader(outheader);
            _CsvWriter.close();

            if(_ninput == null && _oinput == null) return;
            if(_ninput == null && _oinput != null) {
                OldMCDataProcessing();
            }
            if(_ninput != null && _oinput == null) {
                NewMCDataProcessing();
            }
            if(_ninput != null && _oinput != null){
                NewMCDataProcessing();
                OldMCDataProcessing();

//            Thread t1 = new Thread() {
//                @Override
//                public void run() {
//                    NewMCDataProcessing();
//                }
//            };
//            Thread t2 = new Thread() {
//                @Override
//                public void run() {
//                    OldMCDataProcessing();
//                }
//            };
//            t1.start();
//            t2.start();
//
//            try{
//                t1.join();
//                t2.join();
//            }catch(Exception e){
//                e.printStackTrace();
//            }
                logger.info("Data merging...");
                if(!FileUtils.mergeFiles(_nOutName, _outputPath)) throw new Exception("Error: merge new metrocard data!");
                if(!FileUtils.mergeFiles(_oOutName, _outputPath)) throw new Exception("Error: merge old metrocard data!");
                logger.info("Data merge completed!");
            }

            long time2 = System.currentTimeMillis();
            long interval = time2 - time1;
            logger.info("All tasks completed! Total cost " + interval / 1000 + "s");
        }catch(Exception e){
            logger.error("Error: export file error." + e.getMessage());
        }
    }

    private void NewMCDataProcessing() {
        File file = new File(_ninput);
        ICsvBeanReader beanReader = null;
        ICsvBeanWriter nCsvWriter = null;

        final ProgressBar pb = new ProgressBar();
        int lineCount = 1;

        try {
            logger.info("New metrocard data converting...");

            long totolCount = FileUtils.lineCount(file.toString());
            pb.Start(totolCount, 50);

            BufferedReader reader = new BufferedReader(new FileReader(file));
            beanReader = new CsvBeanReader(reader, _csvDelimited);
            beanReader.getHeader(true);

            nCsvWriter = new CsvBeanWriter(new OutputStreamWriter(
                    new FileOutputStream(_nOutName, false), StandardCharsets.UTF_8), _csvDelimited);

            final String[] inheader = new String[]{"cardID", "tradeType", "inPos", "inTime", "inTerminal", "outPos", "outTime", "outTerminal"};
            final CellProcessor[] inProcessors = MCData.getReadProcessors2();
            final CellProcessor[] outProcessors = MCData.getWriteProcessors();

            MCData mc = null;

            final String[] outheader = new String[]{"cardID", "tradeType", "tradeDate", "terminalID"};

            while ((mc = beanReader.read(MCData.class, inheader, inProcessors)) != null) {
                if(mc.getTradeType() == 50) {
                    MCData outMC = new MCData();
                    outMC.setCardID(mc.getCardID());
                    outMC.setTradeType(21);
                    outMC.setTradeDate(mc.getInTime());
                    outMC.setTerminalID(mc.getInTerminal());

                    nCsvWriter.write(outMC, outheader, outProcessors);

                    outMC = new MCData();
                    outMC.setCardID(mc.getCardID());
                    outMC.setTradeType(22);
                    outMC.setTradeDate(mc.getOutTime());
                    outMC.setTerminalID(mc.getOutTerminal());

                    nCsvWriter.write(outMC, outheader, outProcessors);
                }

                pb.Report(lineCount);
                lineCount++;
            }

            pb.Complete(false);
            logger.info("New metrocard data conversion completed!");
        } catch (Exception e) {
            logger.error("Error: convert new metrocard file error!" + "Error in the line" + lineCount + ". Caused by " + e.getMessage());
        }finally {
            try{
                if(beanReader != null){
                    beanReader.close();
                }
                if(nCsvWriter != null){
                    nCsvWriter.close();
                }
            }catch(Exception e){
                logger.error("Error: handle file error." + e.getMessage());
            }
        }
    }

    private void OldMCDataProcessing(){
        File file = new File(_oinput);
        ICsvBeanReader beanReader = null;
        ICsvBeanWriter oCsvWriter = null;
        final ProgressBar pb = new ProgressBar();
        int lineCount = 1;

        try {
            logger.info("Old metrocard data converting...");

            long totolCount = FileUtils.lineCount(file.toString());
            pb.Start(totolCount, 50);

            BufferedReader reader = new BufferedReader(new FileReader(file));
            beanReader = new CsvBeanReader(reader, _csvDelimited);
            beanReader.getHeader(true);

            oCsvWriter = new CsvBeanWriter(new OutputStreamWriter(
                    new FileOutputStream(_oOutName, false), StandardCharsets.UTF_8), _csvDelimited);

            final String[] header = new String[]{"cardID", "tradeType", "tradeDate", "terminalID"};
            final CellProcessor[] inProcessors = MCData.getReadProcessors();
            final CellProcessor[] outProcessors = MCData.getWriteProcessors();

            MCData mc = null;
            while((mc = beanReader.read(MCData.class, header, inProcessors)) != null ) {
                if(mc.getTradeType() == 21 || mc.getTradeType() == 22){
                    oCsvWriter.write(mc, header, outProcessors);
                }

                pb.Report(lineCount);
                lineCount++;
            }
            pb.Complete(false);
            logger.info("Old metrocard data conversion completed!");
        } catch (Exception e) {
            logger.error("Error: convert old metrocard file error!" + "Error in the line" + lineCount + ". Caused by " + e.getMessage());
        }finally {
            try{
                if(beanReader != null){
                    beanReader.close();
                }
                if(oCsvWriter != null){
                    oCsvWriter.close();
                }
            }catch(Exception e){
                logger.error("Error: handle file error." + e.getMessage());
            }
        }
    }

    @Override
    public List<?> CsvReader(File file) throws IOException {
        return null;
    }

    @Override
    public void CsvWriter(String file) {

    }
}
