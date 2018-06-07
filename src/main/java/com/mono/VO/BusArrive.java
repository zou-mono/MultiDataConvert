package com.mono.VO;

import javafx.scene.paint.Stop;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 * Created by mono-office on 2017/4/12.
 */
public class BusArrive extends BusStop{
    private DateTime _ArriveTime;
    private int _StopOrder;
    private String _carID;
    private int _direction;
    private String _routeID;
    private String _ScheduleID;

    public BusArrive(){}

    public BusArrive(String ScheduleID, String carID, String routeID, String direction, String ArriveTime, String StopName, String StopOrder){
        _ScheduleID = ScheduleID;
        _carID = carID;
        _routeID = routeID;
        _direction = Integer.valueOf(direction);
        //_ArriveTime = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parseDateTime(ArriveTime);
        try {
            _ArriveTime = DateTime.parse(ArriveTime);
        }catch(Exception ex){
            _ArriveTime = DateTimeFormat.forPattern("HH:mm:ss").parseDateTime(ArriveTime);
        }

        stopName = StopName;
        _StopOrder = Integer.valueOf(StopOrder);
    }

    public BusArrive(String ScheduleID, String carID, String ArriveTime, String StopOrder){
        _ScheduleID = ScheduleID;
        _carID = carID;
        //_ArriveTime = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parseDateTime(ArriveTime);
        _ArriveTime = DateTime.parse(ArriveTime);
        _StopOrder = Integer.valueOf(StopOrder);
    }

    public String get_ScheduleID() {
        return _ScheduleID;
    }

    public void set_ScheduleID(String _ScheduleID) {
        this._ScheduleID = _ScheduleID;
    }

    public String get_routeID() {
        return _routeID;
    }

    public void set_routeID(String _routeID) {
        this._routeID = _routeID;
    }

    public int get_direction() {
        return _direction;
    }

    public void set_direction(int _direction) {
        this._direction = _direction;
    }

    public String get_carID() {
        return _carID;
    }

    public void set_carID(String _carID) {
        this._carID = _carID;
    }

    public DateTime get_ArriveTime() {
        return _ArriveTime;
    }

    public void set_ArriveTime(DateTime _ArriveTime) {
        this._ArriveTime = _ArriveTime;
    }

    public int get_StopOrder() {
        return _StopOrder;
    }

    public void set_StopOrder(int _StopOrder) {
        this._StopOrder = _StopOrder;
    }
}
