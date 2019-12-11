package com.mono.dataConvert;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.mono.IDataPreprocessing;
import com.mono.VO.BusRoute;
import com.mono.VO.BusStop;
import com.mono.VO.GPS;
import com.mono.common.UnicodeReader;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;

/**
 * Created by mono-office on 2017/2/28.
 */
public class BusTable implements IDataPreprocessing{
    private static final Logger logger = LogManager.getLogger(BusTable.class.getName());
    private static String _CurrentDirectory = System.getProperty("user.dir"); //程序当前运行的路径
    private static String _StopFile = null; //巴士站点输入数据
    private static String _RouteFile = null; //巴士线路输入数据
    private static String _IDFile = null; //巴士站点对应表输入数据
    private static String _NameFile = null; //巴士线路ID和名称对应表输入数据
    private static String _outDirectory = null; //输出数据目录
    private static int _encoding; //输出文本编码
    private HashMap<String,String> _IDDic = null;
    private HashMap<String,String> _NameDic = null;
    private HashMap<String,BusStop> _bsDic = null;
    private List<BusRoute> _brArray = null;

    private static String fileSep = Matcher.quoteReplacement(File.separator);

    @Override
    public boolean init(Namespace ns) {
        logger.info("Loading initial parameter...");
        try {
            Path file;
            _StopFile = ns.get("stoptable");
            _RouteFile = ns.get("routetable");
            _IDFile = ns.get("IDtable");
            _NameFile = ns.get("nametable");
            _encoding = ns.get("encoding");
            _outDirectory = ns.get("output");

            if(_StopFile == null) throw new Exception("Error: 先进院公交站点表缺失!");
            if(!Paths.get(_StopFile).isAbsolute()) _StopFile = StringUtils.join(_CurrentDirectory, fileSep, _StopFile);
            file = Paths.get(_StopFile);
            if (!Files.exists(file)) throw new Exception("Error: 先进院公交站点表缺失!");

            if(_RouteFile == null) throw new Exception("Error: 先进院公交线路表缺失!");
            if(!Paths.get(_RouteFile).isAbsolute()) _RouteFile = StringUtils.join(_CurrentDirectory, fileSep, _RouteFile);
            file = Paths.get(_RouteFile);
            if (!Files.exists(file)) throw new Exception("Error: 先进院公交线路表缺失!");

            if(_IDFile == null) throw new Exception("Error: 先进院站点ID对应表缺失!");
            if(!Paths.get(_IDFile).isAbsolute()) _IDFile = StringUtils.join(_CurrentDirectory, fileSep, _IDFile);
            file = Paths.get(_IDFile);
            if (!Files.exists(file)) throw new Exception("Error: 先进院站点ID对应表缺失!");

            if(_NameFile == null) throw new Exception("Error: 先进院线路ID和名称对应表缺失!");
            if(!Paths.get(_NameFile).isAbsolute()) _NameFile = StringUtils.join(_CurrentDirectory, fileSep, _NameFile);
            file = Paths.get(_NameFile);
            if (!Files.exists(file)) throw new Exception("Error: 先进院线路ID和名称对应表缺失!");

            file = Paths.get(_outDirectory);
            if(!file.isAbsolute()) _outDirectory = StringUtils.join(_CurrentDirectory, fileSep, _outDirectory);

            file = Paths.get(_outDirectory);
            if (!Files.exists(file)) {
                Files.createDirectory(file);
            }

            logger.info("Initalization completed!");
            return true;
        } catch (Exception ex) {
            logger.error("Error: initalization error! " + ex.getMessage());
            return false;
        }
    }

    private void stopReader(File file) throws Exception {
        ICsvBeanReader beanReader = null;
        _bsDic = new HashMap<>();
        int count=0;

        try{
            CharsetDetector detector = new CharsetDetector();
            detector.setText(new BufferedInputStream(new FileInputStream(file)));
            CharsetMatch match = detector.detect();
            String charsetName = match.getName();
            UnicodeReader ur = new UnicodeReader(new FileInputStream(file), charsetName);
            BufferedReader reader = new BufferedReader(ur);

            beanReader = new CsvBeanReader(reader, CsvPreference.STANDARD_PREFERENCE);
            //beanReader = new CsvBeanReader(new FileReader(file), CsvPreference.STANDARD_PREFERENCE);
            final String[] header = new String[]{"stopID", "stopName", "X", "Y"};
            final CellProcessor[] processors = BusStop.getReadProcessors();

            BusStop bs;
            while((bs = beanReader.read(BusStop.class, header, processors)) != null ) {
                _bsDic.put(bs.getStopID(), bs);
            }

        }catch (Exception e){
            throw new Exception("读取先进院公交站点表错误,错误在第"+ count + "行." + "Caused by " + e.getMessage());
        } finally {
            if( beanReader != null ) {
                beanReader.close();
            }
        }
    }

    private void routeReader(File file) throws Exception {
        ICsvBeanReader beanReader = null;
        _brArray = new ArrayList<>();
        int count=0;

        try{
            CharsetDetector detector = new CharsetDetector();
            detector.setText(new BufferedInputStream(new FileInputStream(file)));
            CharsetMatch match = detector.detect();
            String charsetName = match.getName();
            UnicodeReader ur = new UnicodeReader(new FileInputStream(file), charsetName);
            BufferedReader reader = new BufferedReader(ur);
//
//            UnicodeReader ur = new UnicodeReader(new FileInputStream(file), "UTF-8");
//            BufferedReader reader = new BufferedReader(ur);

            beanReader = new CsvBeanReader(reader, CsvPreference.STANDARD_PREFERENCE);
            //beanReader = new CsvBeanReader(new FileReader(file), CsvPreference.STANDARD_PREFERENCE);
            final String[] header = new String[]{"routeID", "routeDir","stopID", "stopName","stopOrder",null, null};
            final CellProcessor[] processors = BusRoute.getReadProcessors();

            BusRoute br;
            while((br = beanReader.read(BusRoute.class, header, processors)) != null ) {
                _brArray.add(br);
                count++;
            }
        }catch (Exception e){
            throw new Exception("读取先进院公交线路表错误,错误在第"+ count + "行." + "Caused by " + e.getMessage());
        } finally {
            if( beanReader != null ) {
                beanReader.close();
            }
        }
    }

    private void IDReader(File file) throws Exception {
        CharsetDetector detector = new CharsetDetector();
        detector.setText(new BufferedInputStream(new FileInputStream(file)));
        CharsetMatch match = detector.detect();
        String charsetName = match.getName();
        UnicodeReader ur = new UnicodeReader(new FileInputStream(file), charsetName);
        BufferedReader reader = new BufferedReader(ur);
//        String charsetName = cpDetector.getFileEncode("E:\\Source code\\TrafficDataAnalysis\\MultiDataConvert\\TEST_DATA\\GBK");
//        System.out.println(charsetName);

        String line;
        _IDDic = new HashMap<>();
        int count=0;

        try{
            while((line = reader.readLine()) != null) {
                String[] a = line.split(",");
                _IDDic.put(a[0],a[1]);
            }
        }catch (Exception e){
            throw new Exception("读取先进院和仿真二期公交站点ID对应关系表错误,错误在第"+ count + "行." + "Caused by " + e.getMessage());
        } finally {
            reader.close();
        }
    }

    private void NameReader(File file) throws Exception {
        CharsetDetector detector = new CharsetDetector();
        detector.setText(new BufferedInputStream(new FileInputStream(file)));
        CharsetMatch match = detector.detect();
        String charsetName = match.getName();
        UnicodeReader ur = new UnicodeReader(new FileInputStream(file), charsetName);
        BufferedReader reader = new BufferedReader(ur);

        String line;
        _NameDic = new HashMap<>();
        int count=0;

        try{
            while((line = reader.readLine()) != null) {
                String[] a = line.split(",");
                _NameDic.put(a[0],a[1]);
            }
        }catch (Exception e){
            throw new Exception("读取先进院ID和公交线路名称对应关系表错误,错误在第"+ count + "行." + "Caused by " + e.getMessage());
        } finally {
            reader.close();
        }
    }

    @Override
    public void DataPreprocessing() {
        try{
            File file;

            long time1 = System.currentTimeMillis();

            logger.info("读取仿真二期公交站点文件...");
            file = new File(_StopFile);
            stopReader(file);

            logger.info("读取先进院公交线路文件...");
            file = new File(_RouteFile);
            routeReader(file);

            logger.info("读取先进院站点ID和仿真二期站点ID对应表文件...");
            file = new File(_IDFile);
            IDReader(file);

            logger.info("读取名称和ID对应表文件...");
            file = new File(_NameFile);
            NameReader(file);

            logger.info("生成公交线路站点顺序描述表...");
            logger.info("生成公交线路站点顺序表...");
            GenerateRouteDescTable();

//            logger.info("生成公交线路站点顺序表...");
//            GenerateStopOrderTable();

            long time2 = System.currentTimeMillis();
            long interval = time2 - time1;
            logger.info("静态公交文件处理完毕！共花费" + interval + "毫秒");

        }
        catch (Exception e){
            logger.error(e.getMessage());
        }
    }

    //生成公交线路站点顺序描述表
    private void GenerateRouteDescTable() throws Exception {
        int line_num = 0;
        String out_str=""; String out_str2="";
        String current_routeID=""; int current_dir=0; List<BusRoute> current_route=null;

        try{
            String out_file = _outDirectory + fileSep + "公交线路站点顺序描述表.csv";
            String out_file2 = _outDirectory + fileSep + "公交线路站点顺序表.csv";
            Writer writer = new OutputStreamWriter(new FileOutputStream(out_file, false), "GB2312");
            Writer writer2 = new OutputStreamWriter(new FileOutputStream(out_file2, false), "GB2312");
            BufferedWriter bw = new BufferedWriter(writer);
            BufferedWriter bw2 = new BufferedWriter(writer2);

            for(BusRoute br :_brArray){
                if(line_num == 0){
                    current_routeID = br.getRouteID();
                    current_dir = br.getRouteDir();
                    current_route = new ArrayList<>();
                    current_route.add(br);
                    line_num++;
                }
                else{
                    if(br.getRouteID().equals(current_routeID) && br.getRouteDir() == current_dir){
                        current_routeID = br.getRouteID();
                        current_dir = br.getRouteDir();
                        current_route.add(br);
                        line_num++;
                    }else{
                        Collections.sort(current_route, new Comparator<BusRoute>() {
                            @Override
                            public int compare(BusRoute o1, BusRoute o2) {
                                return (o1.getStopOrder() > (o2.getStopOrder())) ? 1 :
                                        ((o2.getStopOrder() == o1.getStopOrder()) ? 0 : -1);
                            }
                        });

                        String output_routename = _NameDic.get(current_routeID);
                        if(current_dir == 1){
                            output_routename= output_routename + "-up";
                        }
                        else if(current_dir == 2){
                            output_routename= output_routename + "-down";
                        }

                        out_str = output_routename + ",";
                        for(int i = 0; i< current_route.size(); i++){
                            String output_id =  _IDDic.get(current_route.get(i).getStopID());
                            if(i < current_route.size() - 1){
                                out_str = out_str + output_id + "、";
                            }
                            else{
                                out_str = out_str + output_id;
                            }

                            BusStop output_bs = _bsDic.get(output_id);
                            out_str2 = output_routename + "," + output_bs.getStopID() + "," + output_bs.getStopName() + "," + output_bs.getX() + "," +  output_bs.getY();
                            bw2.write(out_str2);
                            bw2.newLine();
                            bw2.flush();
                        }

                        bw.write(out_str);
                        bw.newLine();
                        bw.flush();

                        current_routeID = br.getRouteID();
                        current_dir = br.getRouteDir();
                        current_route = new ArrayList<>();
                        current_route.add(br);
                        line_num++;
                    }
                }
            }

            bw.close();
            writer.close();
        }catch (Exception ex){
            throw new Exception("生成公交线路站点顺序描述表错误." + ex.getMessage() + line_num);
        }
    }

    @Override
    public List<GPS> CsvReader(File file) throws IOException {
        return null;
    }

    @Override
    public void CsvWriter(String file) {

    }
}
