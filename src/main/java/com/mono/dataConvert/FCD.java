package com.mono.dataConvert;

import com.mono.IDataPreprocessing;
import com.mono.VO.GPS;
import com.mono.common.FileUtils;
import com.mono.common.ProgressBar;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;

import static com.mono.common.FileUtils.completeZero;
import static com.mono.common.FileUtils.findFirstCharPos;

/**
 * Created by mono-office on 2017/3/9.
 */
public class FCD implements IDataPreprocessing{
    private static final Logger logger = LogManager.getLogger(FCD.class.getName());
    protected static String _CurrentDirectory = System.getProperty("user.dir"); //程序当前运行的路径
    protected static String _dataDirectory = null; //输入数据目录
    protected static String _lpFile = null; //车牌文件名称
    protected static String _outDirectory = null; //输出数据目录
    protected static int _maxThread;
    protected HashSet<String> _lpDics = null;
    protected String _date = null;
    private ICsvBeanWriter[] _bwList = null;
    private Lock _locker =new ReentrantLock();

    private static String fileSep = Matcher.quoteReplacement(File.separator);

    public FCD(){

    }

    private boolean LoadlpFile() throws Exception {
        String line;

        //得到车牌信息
        InputStreamReader read = new InputStreamReader(new FileInputStream(_lpFile), "UTF-8");//考虑到编码格式
        BufferedReader reader = new BufferedReader(read);
        _lpDics = new HashSet<String>();

        while ((line = reader.readLine()) != null) {
            String[] a = line.split(",");

            _lpDics.add(a[0].substring(1,a[0].length()));
        }
        return true;
    }

    private ByteBuffer Convertor(int typenum, byte[] br) {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[typenum], 0, typenum).order(ByteOrder.BIG_ENDIAN);
        buffer.put(br);
        buffer.flip();
        return buffer;
    }

    @Override
    public boolean init(Namespace ns) {
        logger.info("Loading initial parameter...");
        try {
            Path file;

            _dataDirectory = ns.get("input");
            _lpFile = ns.get("lpFile");
            _maxThread = ns.get("maxThread");
            _outDirectory = ns.get("output");

//            file = Paths.get(_CurrentDirectory);
//            if (!Files.exists(file)) Files.createDirectory(file);

            file = Paths.get(_dataDirectory);
            _dataDirectory = Files.isDirectory(file) ? _dataDirectory : StringUtils.join(_CurrentDirectory, fileSep, _dataDirectory);
            file = new File(_dataDirectory).toPath();
            if (!Files.exists(file)) throw new Exception("Error: input data is not exist!");

            if (!(_lpFile == null)) {
                file = Paths.get(_lpFile);
                _lpFile = Files.isRegularFile(file) ? _lpFile : StringUtils.join(_CurrentDirectory, fileSep, _lpFile);
                file = Paths.get(_lpFile);
                if (!Files.exists(file)) throw new Exception("Error: input lpFile is not exist!");
            }

            file = Paths.get(_outDirectory);
            _date = file.getFileName().toString();
            if (!file.isAbsolute()) {
                //_date = file.getFileName().toString();
                _outDirectory = StringUtils.join(_CurrentDirectory, fileSep, _date);
            }

            file = Paths.get(_outDirectory);
            if (!Files.exists(file)) {
                Files.createDirectories(file);
            }

            logger.info("Initalization completed!");
            return true;
        } catch (Exception ex) {
            logger.error("Error: initalization error! " + ex.getMessage());
            return false;
        }
    }

    //创建288个保存输出结果的文件，文件名格式为GPS_yyyymmdd_001.txt
    protected void CreateOutputFiles() throws IOException {
        _bwList = new ICsvBeanWriter[288];

        for(int i =1; i<= 288; i++){
            String sOrder = "";
            if(i>=1 && i<10){
                sOrder = "00" + i;
            } else if (i>=10 && i<100){
                sOrder = "0" + i;
            } else{
                sOrder = String.valueOf(i);
            }

            String output_name = _outDirectory + fileSep + "GPS_" + _date + "_" + sOrder + ".txt";

            Path file; BufferedWriter bw;
            file = Paths.get(output_name);
            if(!Files.exists(file)){
                Files.createFile(file);
            }
            Writer writer = new OutputStreamWriter(new FileOutputStream(output_name, false), StandardCharsets.UTF_8);
            ICsvBeanWriter CsvWriter = new CsvBeanWriter(writer, CsvPreference.STANDARD_PREFERENCE);
            //bw = Files.newBufferedWriter(Paths.get(output_name), StandardCharsets.UTF_8);
            _bwList[i-1] = CsvWriter;
        }
    }

    @Override
    public void DataPreprocessing() {
        boolean bFlag;
        int ilpCount=1;

        try {
            long time1 = System.currentTimeMillis();

            logger.info("Creating 288 output files...");

            CreateOutputFiles();

            List<File> files = new FileUtils().readlist(_dataDirectory);

            logger.info("Loading license plate file...");

            if (_lpFile != null)
                if (!LoadlpFile()) throw new Exception("Load license plate file failed！");

            logger.info("Load license plate file completed！");

            int iCount = 1;
            final ProgressBar pb = new ProgressBar();
            ExecutorService ThreadPool = Executors.newFixedThreadPool(_maxThread);

            logger.info("Data converting...");
            pb.Start(files.size(), 50);
            for (final File file : files) {
                bFlag = true;
                //String filename = file.getName().substring(0, file.getName().indexOf("."));
                String filename = FileUtils.GetFileName(file);
//                if (filename.length() >= 7) {
//                    filename = filename.substring(0, 7);
//                }
                String lpName;
                if (_lpFile != null) {
                    int pos = findFirstCharPos(filename);
                    if(pos==-1){
                        bFlag = false;
                    }else{
                        lpName = filename.substring(pos, filename.length());
                        if (!_lpDics.contains(lpName)) bFlag = false;
                    }
                }

                if (bFlag) {
                    final String finalFilename = filename;
                    final int finalICount = iCount;
                    ThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            List<GPS> GPSArray;
                            try {
                                GPSArray = CsvReader(file);
                                if (GPSArray.size() > 1) CsvWriter(GPSArray);
                            } catch (IOException e) {
                                logger.error("Error: handle file " + finalFilename + "error." + e.getMessage());
                            }
                            pb.Report(finalICount);
                        }
                    });
                    ilpCount++;
                }
                iCount++;
            }

            ThreadPool.shutdown();

            while (true) {
                if (ThreadPool.isTerminated()) {
                    pb.Complete(false);
                    logger.info("Data conversion completed!");

                    for(ICsvBeanWriter bw : _bwList){
                        if (bw != null) {
                            bw.close();
                        }
                    }

                    long time2 = System.currentTimeMillis();
                    long interval = time2 - time1;
                    logger.info("All tasks completed! Total cost " + interval / 1000 + "s");
                    System.out.println(ilpCount);
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Error: handle file error." + e.getMessage());
        }
    }

    @Override
    public List<GPS> CsvReader(File file) throws IOException {
        ByteBuffer buffer;
        List<GPS> vSP = new ArrayList<GPS>();

        String id = FileUtils.GetFileName(file);
        int pos = findFirstCharPos(id);
        id = id.substring(pos,id.length());
        FileInputStream FS = new FileInputStream(file);

        try {
            int len = FS.available();
            byte[] r = new byte[len];
            FS.read(r);

            byte[] temp;
            for (int i = 0; i < len / 39; i++) {
                GPS gps = new GPS();
                gps.setSource("H");
                gps.setID(id);

                temp = new byte[4];
                for (int j = 0; j < 4; j++) {
                    temp[j] = r[i * 39 + j];
                }
                buffer = Convertor(4, temp);
                //float a = buffer.getFloat();
                //gps.setLongitude(buffer.getFloat());
                gps.setLongitude(buffer.getFloat());

                temp = new byte[4];
                for (int j = 0; j < 4; j++) {
                    temp[j] = r[i * 39 + 4 + j];
                }
                buffer = Convertor(4, temp);
                //float b = buffer.getFloat();
                gps.setLatitude(buffer.getFloat());

                temp = new byte[8];
                for (int j = 0; j < 8; j++) {
                    temp[j] = r[i * 39 + 8 + j];
                }
                buffer = Convertor(8, temp);
                long GPStime = buffer.getLong();
                DateTime dt = GPS.ConvertIntDatetime(GPStime);
                gps.setTime(dt);
                gps.setYear(String.valueOf(dt.getYear()));
                gps.setMonth(completeZero(2, dt.getMonthOfYear()));
                gps.setDay(completeZero(2, dt.getDayOfMonth()));
                gps.setHour(completeZero(2, dt.getHourOfDay()));
                gps.setMinute(completeZero(2, dt.getMinuteOfHour()));
                gps.setSecond(completeZero(2, dt.getSecondOfMinute()));
                gps.setValid((short)1);

                if(r[i * 39 + 16] == 96 || r[i * 39 + 16] == 0){
                    gps.setState((short)0);
                }else if(r[i * 39 + 16] == 97 || r[i * 39 + 16] == 1){
                    gps.setState((short)1);
                }else{
                    gps.setState((short)0);
                }

                temp = new byte[2];
                for (int j = 0; j < 2; j++) {
                    temp[j] = r[i * 39 + j + 17];
                }
                buffer = Convertor(2, temp);
                gps.setSpeed(buffer.getShort());

                temp = new byte[2];
                for (int j = 0; j < 2; j++) {
                    temp[j] = r[i * 39 + j + 19];
                }
                buffer = Convertor(2, temp);
                //gps.setDirection((short) (buffer.getShort() * 45));
                gps.setDirection((buffer.getShort()));

                if (gps.getLongitude() >= -180 && gps.getLongitude() <= 180
                        && gps.getLatitude() >= -90 && gps.getLatitude() <= 90)
                    vSP.add(gps);
            }

            Collections.sort(vSP, new Comparator<GPS>() {
                @Override
                public int compare(GPS o1, GPS o2) {
                    return (o1.getTime().isAfter(o2.getTime())) ? 1 :
                            ((o2.getTime().isEqual(o1.getTime())) ? 0 : -1);
                }
            });
            return vSP;
        } catch (Exception e) {
            logger.error("Error: convert file error!" + e.getMessage()+ id);
            return null;
        } finally {
            FS.close();
        }
    }

    public void CsvWriter(List<GPS> GpsArray) {
        ICsvBeanWriter CsvWriter = null;
        _locker.lock();
        final CellProcessor[] processors = GPS.getWriteProcessors2();
        final String[] header = new String[]{"source", "ID", "year", "month", "day", "hour", "minute", "second",
                "Longitude", "Latitude", "Speed", "Direction", "State", "valid"};

        try {
            //String out_file = _outDirectory + Matcher.quoteReplacement(File.separator) + file + ".csv";
            for (final GPS gps : GpsArray) {
                int ts = gps.getTime().getMinuteOfDay();
                CsvWriter = _bwList[(int) Math.floor(ts / 5)];

                CsvWriter.write(gps, header, processors);
            }
        } catch (Exception e) {
            logger.error("Error: export file error." + e.getMessage());
        }finally {
            _locker.unlock();
        }
    }

    @Override
    public void CsvWriter(String file) {

    }
}
