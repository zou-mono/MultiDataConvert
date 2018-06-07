package com.mono.options;

import com.mono.dataConvert.BusSched;
import com.mono.dataConvert.BusTable;
import com.mono.dataConvert.FCD;
import com.mono.dataConvert.VI;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

/**
 * Created by mono-office on 2017/1/20.
 */
public class Verb {
    private static Subparsers _subparsers;

    public static Subparsers Set(Subparsers subparsers){
        _subparsers = subparsers;
        setVerbConvertVI();
        setVerbConvertBustable();
        setVerbConvertFCD();
        setVerbConvertBusSchedule();
        return _subparsers;
    }

    private static void setVerbConvertVI(){
        Subparser verbConvert = _subparsers.addParser("vi")
                .setDefault("func", new VI())
                .help("将交警提供的原始车牌识别数据转换为仿真二期车牌识别数据输入格式");
        verbConvert.addArgument("-i","--input")
                .help("Input data are about to be preprocessed. If it is file, should contain suffix name. Required.")
                .required(true);
        verbConvert.addArgument("-e","--encoding")
                .help("Output data encoding. 1 - UTF8, 2 - gb2312. The default is UTF8.")
                .type(int.class)
                .setDefault(1)
                .required(false);
        verbConvert.addArgument("-n","--maxThread")
                .help("Set the maximum count of running thread.")
                .type(int.class)
                .setDefault(1);
        verbConvert.addArgument("-o","--output")
                .help("Output directory to be stored result files. The default is in current directory and the name is vi_convert_result.")
                .setDefault("vi_convert_result");
    }

    private static void setVerbConvertBustable(){
        Subparser verbConvert = _subparsers.addParser("bustable")
                .setDefault("func", new BusTable())
                .help("将先进院提供的静态公交线路和站点表转换为仿真二期系统所需的三张公交静态信息表");
        verbConvert.addArgument("-s", "--stoptable")
                .help("输入匹配后的仿真二期公交站点表.要包含后缀名，坐标为深圳坐标");
        verbConvert.addArgument("-r", "--routetable")
                .help("输入先进院公交线路表.要包含后缀名，坐标为WGS84坐标");
        verbConvert.addArgument("-n", "--nametable")
                .help("输入先进院ID和公交线路名称对应关系表.");
        verbConvert.addArgument("-c", "--IDtable")
                .help("输入先进院和仿真二期公交站点ID对应关系表.");
        verbConvert.addArgument("-e","--encoding")
                .help("Output data encoding. 1 - UTF8, 2 - gb2312. The default is UTF8.")
                .type(int.class)
                .setDefault(1)
                .required(false);
        verbConvert.addArgument("-o","--output")
                .help("Output directory to be stored results. The default is in current directory and the name is bustable_convert_result.")
                .setDefault("bustable_convert_result");
    }

    private static void setVerbConvertFCD(){
        Subparser verbConvert = _subparsers.addParser("FCD")
                .setDefault("func", new FCD())
                .help("将原始GPS数据转换为仿真二期车速计算输入格式.");
        verbConvert.addArgument("-i","--input")
                .help("Input data are about to be preprocessed. It can be file or directory.")
                .required(true);
        verbConvert.addArgument("-n","--maxThread")
                .help("Set the maximum count of running thread.")
                .type(int.class)
                .setDefault(1);
        verbConvert.addArgument("-l","--lpFile")
                .help("License plate file to be filtered input files. The default is empty, means not to filter.");
        verbConvert.addArgument("-o","--output")
                .help("Output directory to be stored result. The default is in current directory and the name must be " +
                        "the date of the preprocessed data, such as 20160101. Required.")
                .required(true);
    }

    private static void setVerbConvertBusSchedule(){
        Subparser verbConvert = _subparsers.addParser("busSchedule")
                .setDefault("func", new BusSched())
                .help("将先进院提供的时刻表转换为矩阵形式.");
        verbConvert.addArgument("-i","--input")
                .help("Input data are about to be preprocessed. If it is file, should contain suffix name. Required.")
                .required(true);
        verbConvert.addArgument("-n","--maxThread")
                .help("Set the maximum count of running thread.")
                .type(int.class)
                .setDefault(1);
        verbConvert.addArgument("-o","--output")
                .help("Output directory to be stored result files. The default is in current directory and the name is busSchedule_convert_result.")
                .setDefault("busSchedule_convert_result");
    }
}
