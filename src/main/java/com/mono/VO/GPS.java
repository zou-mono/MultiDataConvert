package com.mono.VO;

import com.google.gson.annotations.SerializedName;
import com.mono.common.ParseShort;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.joda.FmtDateTime;
import org.supercsv.cellprocessor.joda.ParseDateTime;

/**
 * Created by mono-office on 2017/3/9.
 */
public class GPS {
    @SerializedName("id")
    private String ID;

    @SerializedName("x")
    private float Longitude;

    @SerializedName("y")
    private float Latitude;

    private DateTime Time;
    private short State;
    private int Speed;
    private short Direction;
    private short valid;

    private String source;
    private String year;
    private String month;
    private String day;
    private String hour;
    private String minute;
    private String second;

    //    public float KM;
//    public short TEMP;
//    public short Oil;
    private int Stay;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public float getLatitude() {
        return Latitude;
    }

    public void setLatitude(float latitude) {
        Latitude = latitude;
    }

    public float getLongitude() {
        return Longitude;
    }

    public void setLongitude(float longitude) {
        Longitude = longitude;
    }

    public DateTime getTime() {
        return Time;
    }

    public void setTime(DateTime time) {
        Time = time;
    }

    public short getState() {
        return State;
    }

    public void setState(short state) {
        State = state;
    }

    public int getSpeed() {
        return Speed;
    }

    public void setSpeed(int speed) {
        Speed = speed;
    }

    public short getDirection() {
        return Direction;
    }

    public void setDirection(short direction) {
        Direction = direction;
    }

    public short getValid() {
        return valid;
    }

    public void setValid(short valid) {
        this.valid = valid;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getMinute() {
        return minute;
    }

    public void setMinute(String minute) {
        this.minute = minute;
    }

    public String getSecond() {
        return second;
    }

    public void setSecond(String second) {
        this.second = second;
    }

    //    public float getKM() {
//        return KM;
//    }
//
//    public void setKM(float KM) {
//        this.KM = KM;
//    }
//
//    public short getTEMP() {
//        return TEMP;
//    }
//
//    public void setTEMP(short TEMP) {
//        this.TEMP = TEMP;
//    }
//
//    public short getOil() {
//        return Oil;
//    }
//
//    public void setOil(short oil) {
//        Oil = oil;
//    }

    public int getStay() {
        return Stay;
    }

    public void setStay(int stay) {
        Stay = stay;
    }

    public static DateTime ConvertIntDatetime(long utc) {
        DateTime dt = new DateTime(utc, DateTimeZone.UTC);
        return dt.withZone(DateTimeZone.forID("Asia/Shanghai"));
    }

    public static CellProcessor[] getWriteProcessors() {
        final CellProcessor[] processors = new CellProcessor[] {
                new Optional(), // ID
                new NotNull(), // longitude
                new NotNull(), // latitude
                //new FmtDate("yyyy-MM-dd HH:mm:ss"), // DateTime
                new FmtDateTime(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")), // DateTime
                new NotNull(), // state
                new NotNull(), // speed
                new NotNull(), // direction
                new Optional(), // stayflag
        };

        return processors;
    }

    public static CellProcessor[] getWriteProcessors2() {
        final CellProcessor[] processors = new CellProcessor[] {
                new NotNull(), // source
                new NotNull(), // ID
                new NotNull(), // year
                new NotNull(), // month
                new NotNull(), // day
                new NotNull(), // hour
                new NotNull(), // minute
                new NotNull(), // second
                new NotNull(), // longitude
                new NotNull(), // latitude
                new NotNull(), // direction
                new NotNull(), // speed
                new NotNull(), // state
                new NotNull(), // valid
        };

        return processors;
    }

    public static CellProcessor[] getReadProcessors() {
        final CellProcessor[] processors = new CellProcessor[] {
                new Optional(), // ID
                new ParseDouble(), // longitude
                new ParseDouble(), // latitude
                new ParseDateTime(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")), // DateTime
                new ParseShort(), // state
                new ParseInt(), // speed
                new ParseShort(), // direction
                //new IsIncludedIn(new Object[]{"0","1"})  // stayflag
                //new Optional(new LMinMax(0L,1L))
                //new LMinMax(0L,1L)
                new ParseInt() // stayflag
        };

        return processors;
    }
}
