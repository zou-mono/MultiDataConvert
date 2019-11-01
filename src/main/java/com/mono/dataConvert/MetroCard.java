package com.mono.dataConvert;

import com.mono.IDataPreprocessing;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

public class MetroCard implements IDataPreprocessing {
    private static final Logger logger = LogManager.getLogger(MetroCard.class.getName());
    private static String _CurrentDirectory = System.getProperty("user.dir") ; //程序当前运行的路径
    private static String _input = null; //输入数据
    private static String _outputPath;
    private static String _tokenizer; //分隔符

    @Override
    public boolean init(Namespace ns) {
        try{
            Path file;

            _input = ns.get("input");
            _outputPath = ns.get("output");

            file = Paths.get(_input);
            if(!Files.isRegularFile(file)){
                _input = Files.isDirectory(file) ? _input : StringUtils.join(_CurrentDirectory, "\\", _input);
            }
            file = new File(_input).toPath();
            if (!Files.exists(file)) throw new Exception("Error: input data is not exist!");

            String suffix = _input.substring(_input.lastIndexOf("."));
            String inputName = file.getFileName().toString();
            String inputNoSuffix = inputName.substring(0, inputName.lastIndexOf("."));
            if(_outputPath == null) _outputPath = StringUtils.join(_CurrentDirectory, "\\", inputNoSuffix + "_converted", suffix);

            file = Paths.get(_outputPath);
            if(!file.isAbsolute()){
                _outputPath = StringUtils.join(_CurrentDirectory, "\\", _outputPath);
            }

            file = Paths.get(_outputPath);
            if (!Files.exists(file)) Files.createFile(file);

            Properties prop = new Properties();
            prop.load(this.getClass().getResourceAsStream("/fieldOrder.properties"));

            _tokenizer = String.valueOf(prop.getProperty("metroCard.delimiter"));

            logger.info("Initalization completed!");
            return true;
        }catch (Exception ex){
            logger.error("Error: initalization error! " + ex.getMessage());
            return false;
        }
    }

    @Override
    public void DataPreprocessing() {

    }

    @Override
    public List<?> CsvReader(File file) throws IOException {
        return null;
    }

    @Override
    public void CsvWriter(String file) {

    }
}
