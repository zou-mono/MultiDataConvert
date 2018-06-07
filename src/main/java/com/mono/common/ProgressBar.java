package com.mono.common;

import org.apache.commons.lang3.StringUtils;
import sun.util.calendar.LocalGregorianCalendar;

import java.util.Date;
import java.util.Timer;
import java.util.Collections;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by mono-office on 11/10/2016.
 */
public class ProgressBar {
    private int _blockCount;
    private long _StartTime;
    private static Timer _timer;

    private AtomicLong _currentProgress = new AtomicLong(0);
    private long _total;

    public ProgressBar(){

    }

    public void Start(long total, int blockCount){
        _total = total;
        _blockCount = blockCount;
        Date dt = new Date();
        //_StartTime = System.currentTimeMillis();
        _StartTime = dt.getTime();

        _timer = new Timer();
        _timer.schedule (new TimerTask() {
            @Override
            public void run() {
                printProgress(_currentProgress.get());
            }
        }, dt, 300);
    }

    public void Report(long value){
        _currentProgress.compareAndSet(_currentProgress.get(), value);
    }

    private void printProgress(long current) {
        long eta = current == 0 ? 0 :
                (_total - current) * (System.currentTimeMillis() - _StartTime) / current;

        String etaHms = current == 0 ? "N/A" :
                String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(eta),
                        TimeUnit.MILLISECONDS.toMinutes(eta) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(eta) % TimeUnit.MINUTES.toSeconds(1));

        StringBuilder string = new StringBuilder();
        int percent = (int) (current * 100 / _total);
        string
                .append('\r')
                .append(String.join("", Collections.nCopies(percent == 0 ? 2 : 2 - (int) (Math.log10(percent)), " ")))
                .append(String.format(" %d%% [", percent))
                .append(String.join("", Collections.nCopies(percent*_blockCount/100, "=")))
                .append('>')
                .append(String.join("", Collections.nCopies(_blockCount - percent*_blockCount/100, " "))) //100 - percent
                .append(']')
                //.append(String.join("", Collections.nCopies((int) (Math.log10(_total)) - (int) (Math.log10(current)), " ")))
                .append(String.format(" %d/%d, ETA: %s", current, _total, etaHms));

        System.out.print(string);
    }

    public void Complete(boolean bStay){
//        _timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                _timer.cancel();
//
//                if(!bStay){
//                    System.out.print("\r");
//                }
//            }
//        }, 300);
        if(!bStay){
            System.out.print("\r");
        }
        _timer.cancel();
    }
}
