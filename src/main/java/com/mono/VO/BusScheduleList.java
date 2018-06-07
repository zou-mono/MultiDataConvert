package com.mono.VO;

import java.util.List;

/**
 * Created by mono-office on 2017/4/17.
 */
public class BusScheduleList {
    private List<BusSchedule> _bsList;
    private int _maxOrder;

    public List<BusSchedule> get_bsList() {
        return _bsList;
    }

    public void set_bsList(List<BusSchedule> _bsList) {
        this._bsList = _bsList;
    }

    public int get_maxOrder() {
        return _maxOrder;
    }

    public void set_maxOrder(int _maxOrder) {
        this._maxOrder = _maxOrder;
    }
}
