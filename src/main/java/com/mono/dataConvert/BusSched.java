package com.mono.dataConvert;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.mono.IDataPreprocessing;
import com.mono.VO.*;
import com.mono.common.FileUtils;
import com.mono.common.ProgressBar;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;

/**
 * Created by mono-office on 2017/4/6.
 */
public class BusSched implements IDataPreprocessing {
    private static final Logger logger = LogManager.getLogger(BusSched.class.getName());
    private static String _CurrentDirectory = System.getProperty("user.dir") ; //程序当前运行的路径
    private static String _input = null; //输入数据
    private static String _outputDir = null; //输出数据目录
    private static String _outputName;
    private static int _fieldCarID;  //车辆ID字段顺序
    private static int _fieldArriveTime;  //到站时间字段顺序
    private static int _fieldRouteID;  //线路ID字段顺序
    private static int _fieldDirection;  //线路上下行字段顺序
    private static int _fieldStopID;  //站点ID字段顺序
    private static int _fieldStopName;  //站点名称字段顺序
    private static int _fieldScheduleID; //班车ID
    private static int _fieldStopOrder;  //站点顺序字段顺序
    private static String _CharSep;
    private static int _maxThread;

    private static String fileSep = Matcher.quoteReplacement(File.separator);

    @Override
    public boolean init(Namespace ns){
        try{
            Path file;

            _input = ns.get("input");
            _maxThread = ns.get("maxThread");
            _outputDir = ns.get("output");

            file = Paths.get(_input);
            if(!Files.isRegularFile(file)){
                _input = Files.isDirectory(file) ? _input : StringUtils.join(_CurrentDirectory, fileSep, _input);
            }
            file = new File(_input).toPath();
            if (!Files.exists(file)) throw new Exception("Error: input data is not exist!");

            file = Paths.get(_outputDir);
            if(_outputDir == null){ _outputName = "busSchedule_convert_result"; }
            if(!Files.isRegularFile(file)){
                if (!file.isAbsolute()) {
                    _outputName = file.getFileName().toString();
                    _outputDir = StringUtils.join(_CurrentDirectory, fileSep + _outputName);
                }
            }else{
                throw new Exception("Error: Parameter output must be a directory.");
            }
            file = Paths.get(_outputDir);
            if (!Files.exists(file)) Files.createDirectory(file);

            Properties prop = new Properties();
            prop.load(this.getClass().getResourceAsStream("/fieldOrder.properties"));
            _fieldCarID = Integer.valueOf(prop.getProperty("busSchedule.CarID"));
            _fieldArriveTime = Integer.valueOf(prop.getProperty("busSchedule.ArriveTime"));
            _fieldRouteID = Integer.valueOf(prop.getProperty("busSchedule.RouteID"));
            _fieldScheduleID = Integer.valueOf(prop.getProperty("busSchedule.ScheduleID"));
            _fieldDirection = Integer.valueOf(prop.getProperty("busSchedule.Direction"));
            _fieldStopID= Integer.valueOf(prop.getProperty("busSchedule.StopID"));
            _fieldStopName = Integer.valueOf(prop.getProperty("busSchedule.StopName"));
            _fieldStopOrder = Integer.valueOf(prop.getProperty("busSchedule.StopOrder"));
            _CharSep = prop.getProperty("busSchedule.ExportSep");

            logger.info("Initalization completed!");
            return true;
        }catch (Exception ex){
            logger.error("Error: initalization error! " + ex.getMessage());
            return false;
        }
    }

    @Override
    public void DataPreprocessing() {
        int iCount = 1;

        try{
            long time1 = System.currentTimeMillis();

            List<File> files;
            if(Files.isRegularFile(Paths.get(_input))){
                files = new ArrayList<>();
                files.add(new File(_input));
            }else{
                files = new FileUtils().readlist(_input);
            }

            for (final File file : files) {
                String filename = FileUtils.GetFileName(file);
                HashMap<String, BusScheduleList> resDic;
                try {
                    logger.info(file.getName() + " is in processing...");
                    resDic = ScheduleReader(file);
                    logger.info(file.getName() + " is processed successfully!");

                    logger.info("The result of file " + file.getName() + " is in exporting...");
                    if (resDic.size() > 1) CsvWriter(resDic);
                    logger.info("The result of file " + file.getName() + " is exported susccessfully!");
                    logger.info(file.getName() + " is converted completed!");
                } catch (IOException e) {
                    logger.error("Error: handle file " + filename + " error." + e.getMessage());
                }
            }

            long time2 = System.currentTimeMillis();
            long interval = time2 - time1;
            logger.info("All tasks completed! Total cost " + interval / 1000 + "s");
        }catch (Exception e){
            logger.error("Error: handle file error." + e.getMessage());
        }
    }

    @Override
    public List<GPS> CsvReader(File file) throws IOException {
        return null;
    }

    public HashMap<String, BusScheduleList> ScheduleReader(File file) throws IOException{
//        EncodingDetector detector=new Icu4jEncodingDetector();
//        Charset charset = detector.detect(new BufferedInputStream(new FileInputStream(file)), new Metadata());
//        Path path = Paths.get(file);
//        byte[] data = Files.readAllBytes(path);
        String encoding = FileUtils.getFileEncode(file);
        //System.out.println("The Content in " + encoding);

        InputStreamReader isr = new InputStreamReader(new FileInputStream(file), encoding);
        BufferedReader reader = new BufferedReader(isr);
        int lineCount = 0;
        String tempStr;

        List<BusArrive> baList = new ArrayList<>();
        HashMap<String, BusScheduleList> resDic = new HashMap<>();
        List<BusSchedule> bschList = new ArrayList<>();
        BusScheduleList bsl = new BusScheduleList();
        BusArrive curba = new BusArrive();

        long totolCount = FileUtils.lineCount(file.toString());
        if(totolCount == 0) return resDic;

        final ProgressBar pb = new ProgressBar();
        pb.Start(totolCount, 50);

        while((tempStr = reader.readLine())!=null){
            try {
                if(!tempStr.equals("") && tempStr.contains(",")){
                    String arr[] = tempStr.split(",");

                    if(lineCount == 0) {
                        curba = new BusArrive(arr[_fieldScheduleID], arr[_fieldCarID], arr[_fieldRouteID], arr[_fieldDirection], arr[_fieldArriveTime], arr[_fieldStopName],arr[_fieldStopOrder]);
                        baList = new ArrayList<>();
                    }

                    if(!arr[_fieldScheduleID].equals(curba.get_ScheduleID()) || lineCount == totolCount){
                        String outputName = curba.get_routeID() + "_" + curba.get_direction();

                        BusSchedule bsche = new BusSchedule();
                        bsche.set_CarID(curba.get_carID());
                        bsche.set_StopLst(baList);
                        //int maxNum = maxOrderNum(bsche.get_maxStopOrder(), baList);
                        if(resDic.containsKey(outputName)){
                            bsl = resDic.get(outputName);
                            bschList = bsl.get_bsList();
                        }else{
                            bsl = new BusScheduleList();
                            bschList = new ArrayList<>();
                            bsl.set_bsList(bschList);
                        }

                        //int maxNum = maxOrderNum(bsche.get_maxStopOrder(), baList);
                        bschList.add(bsche);
                        int maxNum =  bsche.get_maxStopOrder();
                        if(maxNum > bsl.get_maxOrder())
                            bsl.set_maxOrder(maxNum);
                        bsl.set_bsList(bschList);
//                        if(maxNum > bschList.get(0).get_maxStopOrder())
//                            bschList.get(0).set_maxStopOrder(maxNum);
                        resDic.put(outputName, bsl);

                        baList = new ArrayList<>();
                        curba = new BusArrive(arr[_fieldScheduleID], arr[_fieldCarID], arr[_fieldRouteID], arr[_fieldDirection], arr[_fieldArriveTime], arr[_fieldStopName],arr[_fieldStopOrder]);;
                        baList.add(curba);
                    }else{
                        curba = new BusArrive(arr[_fieldScheduleID], arr[_fieldCarID], arr[_fieldRouteID], arr[_fieldDirection], arr[_fieldArriveTime], arr[_fieldStopName],arr[_fieldStopOrder]);;
                        baList.add(curba);
                    }

                    pb.Report(lineCount);
                    lineCount++;
                }
            }catch (Exception e){
                logger.error("Error: Read file "+ file + " error. Error in the line " + lineCount + ". Caused by " + e.getMessage());
                lineCount++;
            }
        }

        pb.Complete(false);
        //BusScheduleList bsArray = (BusScheduleList)resDic.get("07590_2");
        return resDic;
    }

    public void CsvWriter(HashMap<String, BusScheduleList> resDic) {
        try {
            ExecutorService ThreadPool = Executors.newFixedThreadPool(_maxThread);

            final ProgressBar pb = new ProgressBar();
            pb.Start(resDic.size(), 50);

            int iCount = 0;
            for (Object o : resDic.entrySet()) {
                final int finalICount = iCount;
                ThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        Map.Entry entry = (Map.Entry) o;
                        String filename = (String) entry.getKey();
                        String out_file = _outputDir + fileSep + filename + ".csv";
                        Writer writer = null; BufferedWriter bw = null;

//                        if(filename.equals("07590_2")){
//                            System.out.println("error");
//                        }

                        try {
                            writer = new OutputStreamWriter(new FileOutputStream(out_file, false), "GB2312");
                            bw = new BufferedWriter(writer);
                            BusScheduleList bsl = (BusScheduleList) entry.getValue();
                            List<BusSchedule> bsArray = bsl.get_bsList();

                            //按照出发时间排序
                            Collections.sort(bsArray, new Comparator<BusSchedule>() {
                                @Override
                                public int compare(BusSchedule o1, BusSchedule o2) {
                                    return (o1.get_StopLst().get(0).get_ArriveTime().isAfter(o2.get_StopLst().get(0).get_ArriveTime())) ? 1 :
                                            ((o2.get_StopLst().get(0).get_ArriveTime().isEqual(o1.get_StopLst().get(0).get_ArriveTime())) ? 0 : -1);
                                }
                            });

                            //BusSchedule maxStop = maxStop(bsArray);

                            int maxNum = bsl.get_maxOrder();
                            //String headline = maxStop.toCSVString(maxNum, "head",",");
                            String headline = headline(maxNum, bsArray);
                            bw.write(headline); bw.newLine(); bw.flush();
                            for(BusSchedule bs: bsArray){
                                String lineStr = bs.toCSVString(maxNum, _CharSep);
                                bw.write(lineStr); bw.newLine(); bw.flush();
                            }
                        } catch (Exception e) {
                            logger.error("Export file " + filename + " error! Caused by " + e.getMessage());
                        }finally {
                            if(bw!=null) try {
                                bw.close();
                            } catch (IOException e) {
                                logger.error("Export file " + filename + " error! Caused by " + e.getMessage());
                            }
                            if(writer!=null) try {
                                writer.close();
                            } catch (IOException e) {
                                logger.error("Export file " + filename + " error! Caused by " + e.getMessage());
                            }
                        }

                        pb.Report(finalICount);
                    }
                });
                iCount++;
            }

            ThreadPool.shutdown();
            while (true) {
                if (ThreadPool.isTerminated()) {
                    pb.Complete(false);
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Error: export file error." + e.getMessage());
        }
    }

    //计算每个班次的最大序号
    private int maxOrderNum(int CurrentMax, List<BusArrive> baArray){
        //int maxOrder = 0;

        for(BusArrive ba : baArray){
            if(ba.get_StopOrder()>CurrentMax)
                CurrentMax = ba.get_StopOrder();
        }

        return  CurrentMax;
    }

    private BusSchedule maxStop(List<BusSchedule> bsArray){
        BusSchedule maxStop = new BusSchedule();
        for(BusSchedule bs : bsArray){
            int curStopNum = bs.get_maxStopOrder();
            if(curStopNum > maxStop.get_maxStopOrder()){
                maxStop = bs;
            }
        }

        return  maxStop;
    }

    //遍历所有班次到达站的名称，并且重新生成一个包含最多站名的列表
    private String headline(int maxOrder, List<BusSchedule> bsArray){

        //int maxOrder = bsArray.get(0).get_maxStopOrder();
        String resArray[]=new String[maxOrder+1]; //多出的一列是用来显示车牌的
        resArray[0] = "";

        for(BusSchedule bs : bsArray){
            List<BusArrive> baArray =  bs.get_StopLst();
            for(BusArrive ba : baArray){
                int order = ba.get_StopOrder();
                resArray[order] = ba.getStopName();
            }
        }

        return FileUtils.ArrayToString(resArray, _CharSep);
    }

    @Override
    public void CsvWriter(String file) {

    }
}
