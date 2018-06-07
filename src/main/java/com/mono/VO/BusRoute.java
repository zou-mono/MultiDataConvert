package com.mono.VO;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseChar;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import com.mono.common.ParseShort;

/**
 * Created by mono-office on 2017/3/2.
 */
public class BusRoute {
    private String routeID;
    private Short routeDir;
    private String stopID;
    private String stopName;
    private Short stopOrder;
    private double longitude;
    private double latitude;

    public String getRouteID() {
        return routeID;
    }

    public void setRouteID(String routeID) {
        this.routeID = routeID;
    }

    public Short getRouteDir() {
        return routeDir;
    }

    public void setRouteDir(Short routeDir) {
        this.routeDir = routeDir;
    }

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

    public Short getStopOrder() {
        return stopOrder;
    }

    public void setStopOrder(Short stopOrder) {
        this.stopOrder = stopOrder;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public static CellProcessor[] getReadProcessors() {
        final CellProcessor[] processors = new CellProcessor[] {
                new NotNull(), // routeID
                new ParseShort(), // routeDir
                new NotNull(), // stopID
                new Optional(), // StopName
                new ParseShort(), // stopOrder
                new Optional(), // longitude
                new Optional(), // latitude
        };

        return processors;
    }
}
