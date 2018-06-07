package com.mono.VO;

import org.joda.time.format.DateTimeFormat;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.joda.ParseDateTime;

/**
 * Created by mono-office on 2017/3/2.
 */
public class BusStop {
    private String stopID;
    public String stopName;
    private double X;
    private double Y;

    public String getStopID() {
        return stopID;
    }

    public void setStopID(String stopID) {
        this.stopID = stopID;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public double getX() {
        return X;
    }

    public void setX(double x) {
        X = x;
    }

    public double getY() {
        return Y;
    }

    public void setY(double y) {
        Y = y;
    }

    public static CellProcessor[] getReadProcessors() {
        final CellProcessor[] processors = new CellProcessor[] {
                new NotNull(), // StopID
                new Optional(), // StopName
                new ParseDouble(), // longitude
                new ParseDouble(), // latitude
        };

        return processors;
    }
}
