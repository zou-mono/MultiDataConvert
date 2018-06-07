package com.mono;

import com.mono.VO.GPS;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by mono-office on 11/2/2016.
 */
public interface IDataPreprocessing {
    boolean init(Namespace ns);
    void DataPreprocessing();
    List<?> CsvReader(File file) throws IOException;
    //void CsvWriter(String file, List<VIData> Array);
    void CsvWriter(String file);
}
