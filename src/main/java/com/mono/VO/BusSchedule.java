package com.mono.VO;

import com.mono.common.FileUtils;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Properties;

/**
 * Created by mono-office on 2017/4/13.
 */
public class BusSchedule {
    private String _CarID;
    private List<BusArrive> _StopLst;
    private int _maxStopOrder;

    public String get_CarID() {
        return _CarID;
    }

    public void set_CarID(String _CarID) {
        this._CarID = _CarID;
    }

    public List<BusArrive> get_StopLst() {
        return _StopLst;
    }

    public void set_StopLst(List<BusArrive> _StopLst) {
        this._StopLst = _StopLst;
    }

    public int get_maxStopOrder() {
        int _maxStopOrder=0;
        for(BusArrive ba : _StopLst){
            if(ba.get_StopOrder()> _maxStopOrder)
                _maxStopOrder = ba.get_StopOrder();
        }
        return _maxStopOrder;
    }

    public void set_maxStopOrder(int _maxStopOrder) {
        this._maxStopOrder = _maxStopOrder;
//        int CurrentMax=0;
//        for(BusArrive ba : _StopLst){
//            if(ba.get_StopOrder()> CurrentMax)
//                CurrentMax = ba.get_StopOrder();
//        }
//        this._maxStopOrder = CurrentMax;
    }

//    public String toCSVString(int max, String type, String sepChar){
//        if(_StopLst==null) return "";
//        if(_StopLst.size()==0) return "";
//
//        String resArray[]=new String[max+1]; //多出的一列是用来显示车牌的
//        resArray[0] = "";
//        for (BusArrive a_StopLst : _StopLst) {
//            int order = a_StopLst.get_StopOrder();
//            resArray[order] = a_StopLst.getStopName();
//        }
//
//        return FileUtils.ArrayToString(resArray, sepChar);
//    }
    public String toCSVString(int max, String sepChar){
        //int max = _maxStopOrder;

        if(_StopLst==null) return "";
        if(_StopLst.size()==0) return "";

        String resArray[]=new String[max+1]; //多出的一列是用来显示车牌的
        resArray[0] = _StopLst.get(0).get_carID();
        for (BusArrive a_StopLst : _StopLst) {
            int order = a_StopLst.get_StopOrder();
            resArray[order] = a_StopLst.get_ArriveTime().toString("HH:mm:ss");
        }

        return FileUtils.ArrayToString(resArray, sepChar);
    }
}
