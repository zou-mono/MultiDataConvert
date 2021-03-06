package com.mono.common;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import org.apache.commons.lang3.StringUtils;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mono-office on 11/9/2016.
 */
public class FileUtils  {
    public List<File> resultFileName;
    private String sDeepestFolder = "";
    private static String fileSep = Matcher.quoteReplacement(File.separator);

    //寻找当前目录的最深一级子目录
    public String deepestFolder(File file){
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {// 判断是否文件夹
                //resultFileName.add(f.getPath());
                sDeepestFolder = f.getPath();
                deepestFolder(f);// 调用自身,查找子目录
            } else
                return sDeepestFolder;
        }
        return sDeepestFolder;
    }

    public List<File> readlist(String path){
        resultFileName = new ArrayList();//建立ArrayList对象
        GetFiles(new File(path));//得到文件夹
        return resultFileName;//把ArrayList转化为string[]
    }

    private List<File> GetFiles(File file){
        File[] files = file.listFiles();
        if(files==null)return resultFileName;// 判断目录下是不是空的
        for (File f : files) {
            if(f.isDirectory()){// 判断是否文件夹
                //resultFileName.add(f.getPath());
                GetFiles(f);// 调用自身,查找子目录
            }else
                resultFileName.add(f);
        }
        return resultFileName;
    }

    //判断字符串是否包含中文字符
    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    public static String GetFileName(File file) throws UnsupportedEncodingException {
        String str = file.getName();

        //新出租车车牌URL解码
        if(!isContainChinese(str)){
            String encoderString = URLDecoder.decode(str, "GBK");
            str = encoderString.split("_")[1];
        }

        if (str == null) return null;

        // Get position of last '.'.
        int pos = str.lastIndexOf(".");

        // If there wasn't any '.' just return the string as is.
        if (pos == -1) return str;

        // Otherwise return the string, up to the dot.
        return str.substring(0, pos);
    }

    public static boolean CheckPathValid(String path) {
        try {
            Paths.get(path);
        }catch (InvalidPathException ex) {
            return false;
        }
        catch (NullPointerException ex){
            return false;
        }
        return true;
    }

    public static boolean CheckFilenameValid(String file) {
        File f = new File(file);
        try {
            f.getCanonicalPath();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static CsvBeanWriter outputEncodeing(int encoding, String outFile) throws FileNotFoundException, UnsupportedEncodingException {
        if(encoding == 1){
            return new CsvBeanWriter(new OutputStreamWriter(new FileOutputStream(outFile, false), StandardCharsets.UTF_8), CsvPreference.STANDARD_PREFERENCE);
        }
        else if(encoding == 2){
            return new CsvBeanWriter(new OutputStreamWriter(new FileOutputStream(outFile, false), "GB2312"), CsvPreference.STANDARD_PREFERENCE);
        }

        return null;
    }

    public static String checkFileValid(String input, String currentDirectory){
        Path file;

        try{
            file = Paths.get(input);
            if (!Files.isRegularFile(file)) {
                input = Files.isDirectory(file) ? input : StringUtils.join(currentDirectory, fileSep, input);
            }
            file = new File(input).toPath();
            if (!Files.exists(file))
                return null;

            return input;
        }catch(Exception e){
            return null;
        }
    }

    public static boolean mergeFiles(String source, String target){
        try (FileInputStream is = new FileInputStream(source);
             FileChannel in = is.getChannel();
             FileOutputStream os = new FileOutputStream(target, true);
             FileChannel out = os.getChannel()) {

            long position = 0;
            long size = in.size() + position;
            while (0 < size) {
                long count = in.transferTo(position, size, out);
                if (count > 0) {
                    position += count;
                    size -= count;
                }
            }
            return true;
        }catch(Exception e){
            return false;
        }
    }

    public static String getNoSuffix(String input){
        Path file = Paths.get(input);
        String inputName = file.getFileName().toString();
        if(inputName.lastIndexOf(".") == -1){
            return inputName;
        }else{
            return inputName.substring(0, inputName.lastIndexOf("."));
        }
    }

    public static String completeZero (int dest_digit, int num){
        String format = "%0" + dest_digit + "d";
        return String.format(format, num);
    }

    public static int findFirstCharPos(String str){
        Matcher matcher = Pattern.compile("[A-Za-z]").matcher(str);
        if(matcher.find()) {
            return matcher.start();
        }
        return -1;
    }

    public static int lineCount(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        byte[] c = new byte[1024];
        int count = 0;
        int readChars = 0;
        while ((readChars = is.read(c)) != -1) {
            for (int i = 0; i < readChars; ++i) {
                if (c[i] == '\n')
                    ++count;
            }
        }
        is.close();
        return count;
    }

    public static String ArrayToString(String[] resArray, String sepChar){
        String resStr = "";

        for(int i=0; i<resArray.length; i++){
            String temp = "";
            if(resArray[i]==null){
                temp = "";
            }else{
                temp = resArray[i];
            }

            if(i<resArray.length-1){
                resStr = resStr + temp + sepChar;
            }else{
                resStr = resStr + temp;
            }
        }

        return  resStr;
    }

    public static String getFileEncode(File file) throws IOException {
        CharsetDetector detector = new CharsetDetector();
        detector.setText(new BufferedInputStream(new FileInputStream(file)));
        CharsetMatch match = detector.detect();
        String encoding = match.getName();

        return encoding;
    }
}
