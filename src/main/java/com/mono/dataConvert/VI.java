package com.mono.dataConvert;

import com.mono.IDataPreprocessing;
import com.mono.VO.GPS;
import com.mono.VO.VIData;
import com.mono.common.FileUtils;
import com.mono.common.ProgressBar;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.ICsvBeanWriter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by mono-office on 2017/1/20.
 */
public class VI implements IDataPreprocessing{
    private static final Logger logger = LogManager.getLogger(VI.class.getName());
    private static String _CurrentDirectory = System.getProperty("user.dir"); //程序当前运行的路径
    private static String _dataDirectory = null; //输入数据目录
    private static String _outFile = null; //输出数据文件
    private static int _encoding; //输出文本编码
    private static int _maxThread;
    private ICsvBeanWriter _CsvWriter;
    private Lock _locker =new ReentrantLock();

    public VI(){

    }

    @Override
    public boolean init(Namespace ns) {
        logger.info("Loading initial parameter...");
        try {
            Path file;

            _dataDirectory = ns.get("input");
            _maxThread = ns.get("maxThread");
            _encoding = ns.get("encoding");
            _outFile = ns.get("output");

//            file = Paths.get(_CurrentDirectory);
//            if(!Files.exists(file)) Files.createDirectory(file);

            file = Paths.get(_dataDirectory);
            _dataDirectory = Files.isDirectory(file) ? _dataDirectory : StringUtils.join(_CurrentDirectory, "\\", _dataDirectory);
            file = new File(_dataDirectory).toPath();
            if (!Files.exists(file)) throw new Exception("Error: input data is not exist!");

            file = Paths.get(_outFile);
            if(!file.isAbsolute()) _outFile = StringUtils.join(_CurrentDirectory, "\\" + _outFile, ".txt");

            file = Paths.get(_outFile);
            if (!Files.exists(file)) {
                Files.createFile(file);
            }

            _CsvWriter = FileUtils.outputEncodeing(_encoding, _outFile);

//            if(_encoding == 1){
//                _CsvWriter = new CsvBeanWriter(new OutputStreamWriter(new FileOutputStream(_outFile, false), StandardCharsets.UTF_8), CsvPreference.STANDARD_PREFERENCE);
//            }
//            else if(_encoding == 2){
//                _CsvWriter = new CsvBeanWriter(new OutputStreamWriter(new FileOutputStream(_outFile, false), "GB2312"), CsvPreference.STANDARD_PREFERENCE);
//            }

            logger.info("Initalization completed!");
            return true;
        } catch (Exception ex) {
            logger.error("Error: initalization error! " + ex.getMessage());
            return false;
        }
    }

    @Override
    public void DataPreprocessing() {
        try {
            long time1 = System.currentTimeMillis();

            List<File> files = new FileUtils().readlist(_dataDirectory);

            logger.info("Converation Processing...");

            int iCount = 1;
            final ProgressBar pb = new ProgressBar();
            ExecutorService ThreadPool = Executors.newFixedThreadPool(_maxThread);

            pb.Start(files.size(), 50);
            for (final File file : files) {
                String filename = FileUtils.GetFileName(file);
//                if (filename.length() >= 7) {
//                    filename = filename.substring(0, 7);
//                }

                final String finalFilename = filename;
                final int finalICount = iCount;

                ThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        List<VIData> VIArray;
                        try {
                            VIArray = CsvReader(file);
                            CsvWriter(_outFile, VIArray);
                        } catch (IOException e) {
                            logger.error("Error: handle file "+ finalFilename +"error." + e.getMessage());
                        }
                        pb.Report(finalICount);
                    }
                });
                //pb.Report(iCount);
                iCount++;
            }

            ThreadPool.shutdown();

            while(true){
                if(ThreadPool.isTerminated()){
                    pb.Complete(false);
                    logger.info("Data extraction completed!");

                    if (_CsvWriter != null) {
                        try {
                            _CsvWriter.close();
                        } catch (IOException e) {
                            logger.error("Error: export file error." + e.getMessage());
                        }
                    }

                    long time2 = System.currentTimeMillis();
                    long interval = time2 - time1;
                    logger.info("All tasks completed! Total cost " + interval / 1000 + "s");
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Error: handle file error." + e.getMessage());
        }
    }

    @Override
    public List<VIData> CsvReader(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        int lineCount = 0;

        List<VIData> vSP = new ArrayList<VIData>();
        String tempStr = "";

        while((tempStr = reader.readLine())!=null){
            try {
                VIData vi = new VIData();
                if(!tempStr.equals("")){
                    String arr[] = tempStr.split(",");

                    if(arr[0].trim().matches("\\s*") || arr[0].trim().equals("")){
                        throw new Exception("carID is null!");
                    }
                    vi.setCarID(arr[0].trim());

                    DateTimeFormatter format; DateTime dt;
                    format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
                    String sDatatime = arr[5].substring(0,19);

                    try{
                        dt = DateTime.parse(sDatatime, format);
                        vi.setPassTime(dt);
                    }
                    catch(Exception e1){
                        try{
                            dt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS").parseDateTime(sDatatime);
                            dt = DateTime.parse(dt.toString("yyyy-MM-dd HH:mm:ss"), format);
                            vi.setPassTime(dt);
                        }
                        catch(Exception e2){
                            try{
                                dt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.").parseDateTime(sDatatime);
                                dt = DateTime.parse(dt.toString("yyyy-MM-dd HH:mm:ss"), format);
                                vi.setPassTime(dt);
                            }
                            catch (Exception e3){
                                try{
                                    dt = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss").parseDateTime(sDatatime);
                                    dt = DateTime.parse(dt.toString("yyyy-MM-dd HH:mm:ss"), format);
                                    vi.setPassTime(dt);
                                }
                                catch(Exception e4){
                                    try{
                                        dt = DateTimeFormat.forPattern("yyyy-MM-dd H:mm:ss0").parseDateTime(sDatatime);
                                        dt = DateTime.parse(dt.toString("yyyy-MM-dd HH:mm:ss"), format);
                                        vi.setPassTime(dt);
                                    }
                                    catch(Exception e5){
                                        try{
                                            dt = DateTimeFormat.forPattern("yyyy-M-dd H:mm:ss00").parseDateTime(sDatatime);
                                            dt = DateTime.parse(dt.toString("yyyy-MM-dd HH:mm:ss"), format);
                                            vi.setPassTime(dt);
                                        }
                                        catch(Exception e6){
                                            try{
                                                dt = DateTimeFormat.forPattern("yyyy-M-dd  HH:mm:ss").parseDateTime(sDatatime);
                                                dt = DateTime.parse(dt.toString("yyyy-MM-dd HH:mm:ss"), format);
                                                vi.setPassTime(dt);
                                            }
                                            catch(Exception e){
                                                throw new Exception(e.getMessage());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if(arr[7].trim().matches("\\s*") || arr[7].trim().equals("")){
                        throw new Exception("detectorID is null!");
                    }
                    if(arr[7].trim().length() > 10){
                        throw new Exception("detectorID exceed length limit!");
                    }
                    vi.setDetectorID(arr[7].trim());

                    try{
                        if(arr[2].length()>1){
                            arr[2]="-1";
                        }

                        vi.setvType(Integer.parseInt(arr[2]));
                    }
                    catch(Exception e){
                        vi.setvType(-1);
                    }

                    try{
                        if(arr[8].length()>1){
                            arr[8]="-1";
                        }
                        vi.setIOType(Integer.parseInt(arr[8]));
                    }
                    catch(Exception e){
                        vi.setIOType(-1);
                    }

                    try{
                        if(arr[9].length()>1){
                            arr[9]="-1";
                        }
                        vi.setLaneID(Integer.parseInt(arr[9]));
                    }
                    catch(Exception e){
                        vi.setLaneID(-1);
                    }

                    vi.setFilter(0);
                    vSP.add(vi);
                }
            }
            catch (Exception e){
                logger.error("Error: Read file "+ file + " error. Error in the line " + lineCount + ". Caused by " + e.getMessage());
            }

            lineCount ++;
        }

        if(reader != null ) {
            reader.close();
        }

        return vSP;
    }

    public void CsvWriter(String file, List<VIData> Array) {
        final String[] header;
        final CellProcessor[] processors;
        _locker.lock();
        try {
            //header = new String[]{"CarID", "DetectorID", "LaneID", "PassTime", "vType", "Filter", "IOType"};
            header = new String[]{"CarID", "DetectorID", "LaneID", "PassTime", "vType", "Filter"};
            processors = VIData.getWriteProcessors();
            for (final VIData vi : Array) {
                _CsvWriter.write(vi, header, processors);
            }
        } catch (Exception e) {
            logger.error("Error: export file error." + e.getMessage());
        }
        finally {
            _locker.unlock();
        }
    }

    @Override
    public void CsvWriter(String file) {

    }
}
