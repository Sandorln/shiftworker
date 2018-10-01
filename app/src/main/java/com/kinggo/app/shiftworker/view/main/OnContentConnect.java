package com.kinggo.app.shiftworker.view.main;

import com.kinggo.app.shiftworker.view.calendar.entity.User;
import com.kinggo.app.shiftworker.view.calendar.entity.WorkDay;
import com.kinggo.app.shiftworker.view.calendar.entity.WorkInfo;
import com.kinggo.app.shiftworker.view.community.domain.UserInfo;

import java.util.ArrayList;
import java.util.List;

public interface OnContentConnect {

    // 해당 값 찾기
    List<String> findHoliday(String selectDay);
    String findMemo(String selectDay);
    WorkDay findWorkDay(String selectDay);
    int findOverTime(String selectDay);
    User findUser();
    WorkInfo findWorkInfoById(Integer workId);
    ArrayList<WorkInfo> getArrayWorkInfo();

    // 값 저장 ( DB )
    void inputMemo(String day, String content);
    void inputWorkDay(String selectDay, WorkDay workDay);
    void updateUserWork(ArrayList<WorkInfo> workPatternList, ArrayList<WorkInfo> workInfoList, String selectFirstDay);
    void inputOverTime(String selectDay, int overTimeInput);
}