package com.mono.VO;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.joda.FmtDateTime;
import org.supercsv.cellprocessor.joda.ParseDateTime;

public class MCData {
    private String cardID;   //卡号
    private int tradeType;  //交易类型
    private String inPos;  //进站地点
    private DateTime inTime;  //进站时间
    private String inTerminal;  //进站终端
    private String outPos;  //出站地点
    private DateTime outTime;  //出站时间
    private String outTerminal;  //出站终端
    private DateTime tradeDate;  //交易时间
    private String terminalID; //交易终端

    private static DateTimeParser[] parsers = {
            DateTimeFormat.forPattern( "yyyy/M/d H:m:s" ).getParser(),
            DateTimeFormat.forPattern( "yyyy/M/d" ).getParser(),
            DateTimeFormat.forPattern( "yyyy/M/d H:mm:s" ).getParser(),
            DateTimeFormat.forPattern( "yyyy/M/d H:mm:ss" ).getParser(),
            DateTimeFormat.forPattern( "yyyy/M/d HH:mm:ss" ).getParser()
    };

    public String getTerminalID() {
        return terminalID;
    }

    public void setTerminalID(String terminalID) {
        this.terminalID = terminalID;
    }

    public DateTime getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(DateTime tradeDate) {
        this.tradeDate = tradeDate;
    }

    public String getCardID() {
        return cardID;
    }

    public void setCardID(String cardID) {
        this.cardID = cardID;
    }

    public int getTradeType() {
        return tradeType;
    }

    public void setTradeType(int tradeType) {
        this.tradeType = tradeType;
    }

    public String getInPos() {
        return inPos;
    }

    public void setInPos(String inPos) {
        this.inPos = inPos;
    }

    public DateTime getInTime() {
        return inTime;
    }

    public void setInTime(DateTime inTime) {
        this.inTime = inTime;
    }

    public String getInTerminal() {
        return inTerminal;
    }

    public void setInTerminal(String inTerminal) {
        this.inTerminal = inTerminal;
    }

    public String getOutPos() {
        return outPos;
    }

    public void setOutPos(String outPos) {
        this.outPos = outPos;
    }

    public DateTime getOutTime() {
        return outTime;
    }

    public void setOutTime(DateTime outTime) {
        this.outTime = outTime;
    }

    public String getOutTerminal() {
        return outTerminal;
    }

    public void setOutTerminal(String outTerminal) {
        this.outTerminal = outTerminal;
    }

    public static CellProcessor[] getReadProcessors() {
        final CellProcessor[] processors = new CellProcessor[] {
                new Optional(), // CARDID
                new ParseInt(), // TRADETYPE
                new ParseDateTime(new DateTimeFormatterBuilder()
                        .append( null,parsers ).toFormatter()), // TRADEDATE
                new Optional()// TERMINALID
        };

        return processors;
    }

    public static CellProcessor[] getReadProcessors2() {
        final CellProcessor[] processors = new CellProcessor[] {
                new Optional(), // CARDID
                new ParseInt(), // TRADETYPE
                new Optional(), // inPos
                new ParseDateTime(new DateTimeFormatterBuilder()
                        .append( null,parsers ).toFormatter()), // inTime
                new Optional(), // inTerminal
                new Optional(), // outPos
                new ParseDateTime(new DateTimeFormatterBuilder()
                        .append( null,parsers ).toFormatter()), // outTime
                new Optional()// outTerminal
        };

        return processors;
    }

    public static CellProcessor[] getWriteProcessors() {
        final CellProcessor[] processors = new CellProcessor[] {
                new NotNull(), // CARDID
                new NotNull(), // TRADETYPE
                new FmtDateTime(DateTimeFormat.forPattern("yyyy/M/d H:mm:ss")), // TRADEDATE
                new NotNull()// TERMINALID
        };

        return processors;
    }
}
