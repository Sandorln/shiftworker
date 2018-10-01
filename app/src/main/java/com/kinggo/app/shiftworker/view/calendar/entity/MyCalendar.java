package com.kinggo.app.shiftworker.view.calendar.entity;

import java.io.Serializable;

public class MyCalendar implements Serializable {
    int firstWeek;
    int lastDay;
    int selectY;
    int selectM;
    int workStartSubMonthFirst;                 // 달 처음의 근무형태가 몇번째인지 카운트

    public int getWorkStartSubMonthFirst() {
        return workStartSubMonthFirst;
    }

    public void setWorkStartSubMonthFirst(int workStartSubMonthFirst) {
        this.workStartSubMonthFirst = workStartSubMonthFirst;
    }

    public int getFirstWeek() {
        return firstWeek;
    }

    public void setFirstWeek(int firstWeek) {
        this.firstWeek = firstWeek;
    }

    public int getLastDay() {
        return lastDay;
    }

    public void setLastDay(int lastDay) {
        this.lastDay = lastDay;
    }

    public int getSelectY() {
        return selectY;
    }

    public void setSelectY(int selectY) {
        this.selectY = selectY;
    }

    public int getSelectM() {
        return selectM;
    }

    public void setSelectM(int selectM) {
        this.selectM = selectM;
    }

    public String getYyyyMMdd(int day) {
        return String.format("%04d%02d%02d", selectY, selectM, day);
    }

    public int getDay(int position){
        return (position + 2) - firstWeek;
    }
}
