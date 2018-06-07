package com.mono.VO;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.joda.FmtDateTime;
import org.supercsv.cellprocessor.joda.ParseDateTime;

/**
 * Created by mono-office on 2017/1/20.
 */
public class VIData {
    private String carID;
    private DateTime PassTime;
    private String DetectorID;
    private int IOType;
    private int LaneID;
    private int vType;
    private int filter;

    public int getFilter() {
        return filter;
    }

    public void setFilter(int filter) {
        this.filter = filter;
    }

    public int getvType() {
        return vType;
    }

    public void setvType(int vType) {
        this.vType = vType;
    }

    public String getDetectorID() {
        return DetectorID;
    }

    public void setDetectorID(String detectorID) {
        DetectorID = detectorID;
    }

    public int getIOType() {
        return IOType;
    }

    public void setIOType(int IOType) {
        this.IOType = IOType;
    }

    public int getLaneID() {
        return LaneID;
    }

    public void setLaneID(int laneID) {
        LaneID = laneID;
    }

    public DateTime getPassTime() {
        return PassTime;
    }

    public void setPassTime(DateTime passTime) {
        PassTime = passTime;
    }

    public String getCarID() {
        return carID;
    }

    public void setCarID(String carID) {
        this.carID = carID;
    }

    public static CellProcessor[] getWriteProcessors() {
        final CellProcessor[] processors = new CellProcessor[] {
                new NotNull(), // CarID
                new NotNull(), // DetectorID
                new ParseInt(), // LaneID
                new FmtDateTime(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")), // PassTime
                new ParseInt(), // vType
                new ParseInt(), // filter
//                new ParseInt(), // IOType
        };

        return processors;
    }
}
