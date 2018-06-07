package com.mono.common;

import org.supercsv.io.CsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
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

    public static String GetFileName(File file){
        String str = file.getName();

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


}
