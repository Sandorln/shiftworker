package com.kinggo.app.shiftworker.view.calendar.entity;

import java.io.Serializable;

public class WorkDay implements Serializable {
    int workInfoId; // workInfoId ( workCategory 고유 아이디 )
    String workEtc; // workInfoId 값이 -1 일 경우 사용자가 직접 입력한 값 할당 / 아닐 시 null )

    public int getWorkInfoId() {
        return workInfoId;
    }

    public void setWorkInfoId(int workInfoId) {
        this.workInfoId = workInfoId;
    }

    public String getWorkEtc() {
        return workEtc;
    }

    public void setWorkEtc(String workEtc) {
        this.workEtc = workEtc;
    }

    public WorkDay(int workInfoId, String workEtc) {

        this.workInfoId = workInfoId;
        this.workEtc = workEtc;
    }
}
