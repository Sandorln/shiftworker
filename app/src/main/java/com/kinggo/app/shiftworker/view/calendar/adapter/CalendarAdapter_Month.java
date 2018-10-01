package com.kinggo.app.shiftworker.view.calendar.adapter;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.kinggo.app.shiftworker.view.calendar.CalendarListFragment;
import com.kinggo.app.shiftworker.view.calendar.entity.MyCalendar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarAdapter_Month extends FragmentStatePagerAdapter {

    private Map<Integer, CalendarListFragment> frgMap;
    private List<MyCalendar> myCalendars;

    public CalendarAdapter_Month(FragmentManager fm, List<MyCalendar> myCalendars) {
        super(fm);
        frgMap = new HashMap<>();
        this.myCalendars = myCalendars;
    }


    @Override
    public int getCount() {
        return myCalendars.size();
    }

    @Override
    public Fragment getItem(int position) {
        CalendarListFragment calFrg = null;

        if (frgMap.size() > 0) {
            calFrg = frgMap.get(position);
        }

        if (calFrg == null){
            calFrg = new CalendarListFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("myCalendar", myCalendars.get(position));
            calFrg.setArguments(bundle);
            frgMap.put(position, calFrg);
        }

        return calFrg;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public Parcelable saveState() {
        return null;
    }
}
