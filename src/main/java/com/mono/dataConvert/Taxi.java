package com.mono.dataConvert;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.mono.IDataPreprocessing;
import com.mono.VO.GPS;
import com.mono.common.FileUtils;
import com.mono.common.ProgressBar;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import sun.security.util.Length;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;

import static com.mono.common.FileUtils.completeZero;
import static com.mono.common.FileUtils.findFirstCharPos;

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
            return true;
        }catch (Exception ex) {
            logger.error("Error: initalization error! " + ex.getMessage());
            return false;
        }
    }

    @Override
    public void DataPreprocessing() {
        try {
            super.DataPreprocessing();
        } catch (Exception e) {
            logger.error("Error: handle file error." + e.getMessage());
        }
    }

    @Override
    public List<GPS> CsvReader(File file) throws IOException {
        ByteBuffer buffer;
        List<GPS> vSP = new ArrayList<>();
        int lineCount = 0;
        String tempStr;

        String id = FileUtils.GetFileName(file);
        int pos = findFirstCharPos(id);
        id = id.substring(pos,id.length());

//        String encoding = FileUtils.getFileEncode(file);
        InputStreamReader isr = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(isr);

        long totolCount = FileUtils.lineCount(file.toString());
        if(totolCount == 0) return vSP;

        try {
            while((tempStr = reader.readLine())!=null){
                if(!tempStr.equals("") && tempStr.contains(":")){
                    String arr[] = tempStr.split(":");

                    //如果定位错误或者时间错误，则跳过
//                    if(arr[12].equals("1") || arr[12].equals("2") || arr[12].equals("3")) continue;

                    GPS gps = new GPS();
                    gps.setSource("H");
                    gps.setID(id);

                    gps.setLongitude(Float.parseFloat(arr[7])/600000);
                    gps.setLatitude(Float.parseFloat(arr[8])/600000);

                    String tDate = arr[2].split("/")[0];
                    String tTime = arr[2].split("/")[1];
                    String tDateTime = tDate + " " + tTime;
                    DateTime dt = DateTime.parse(tDateTime, DateTimeFormat.forPattern("yyyyMMdd HHmmss"));

                    gps.setTime(dt);
                    gps.setYear(String.valueOf(dt.getYear()));
                    gps.setMonth(completeZero(2, dt.getMonthOfYear()));
                    gps.setDay(completeZero(2, dt.getDayOfMonth()));
                    gps.setHour(completeZero(2, dt.getHourOfDay()));
                    gps.setMinute(completeZero(2, dt.getMinuteOfHour()));
                    gps.setSecond(completeZero(2, dt.getSecondOfMinute()));
                    gps.setValid((short)1);
                    gps.setState((short)1);
                    gps.setSpeed(Integer.parseInt(arr[3]));
                    gps.setDirection(Integer.parseInt(arr[4]));

                    if (gps.getLongitude() >= -180 && gps.getLongitude() <= 180
                                && gps.getLatitude() >= -90 && gps.getLatitude() <= 90)
                            vSP.add(gps);
                    }
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
            reader.close();
        }
    }
}
