package com.kinggo.app.shiftworker.view.calendar;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kinggo.app.shiftworker.R;
import com.kinggo.app.shiftworker.view.calendar.adapter.CalendarAdapter_Day;
import com.kinggo.app.shiftworker.view.calendar.entity.MyCalendar;
import com.kinggo.app.shiftworker.view.calendar.entity.User;
import com.kinggo.app.shiftworker.view.main.MainActivity;
import com.kinggo.app.shiftworker.view.main.OnContentConnect;

import java.util.Objects;

import butterknife.ButterKnife;

public class CalendarListFragment extends Fragment implements CalendarAdapter_Day.OnClickCalendarDay {

    private static OnContentConnect onContentConnect;

    private MyCalendar myCalendar;
    private User userInfo;
    private CalendarAdapter_Day adapterDay;
    private RecyclerView rv_calendar;

    private Long mLastClickTime = 0L;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_calendar_list, null);
        ButterKnife.bind(this, view);

        ResetObject(view);
        ResetView();

        return view;
    }

    private void ResetObject(View view) {
        if (onContentConnect == null)
            onContentConnect = (MainActivity) getActivity();

        assert onContentConnect != null;
        userInfo = onContentConnect.findUser();

        assert getArguments() != null;
        myCalendar = (MyCalendar) getArguments().getSerializable("myCalendar");
        rv_calendar = view.findViewById(R.id.rv_calendar_list);
        rv_calendar.setLayoutManager(new GridLayoutManager(getContext(), 7));

        adapterDay = new CalendarAdapter_Day(myCalendar, getContext(), this);
    }

    private void ResetView() {
        rv_calendar.setAdapter(adapterDay);
    }

    public void resetSelectDayView(int dayPosition) {
        adapterDay.notifyItemChanged(dayPosition);
        rv_calendar.setAdapter(adapterDay);
    }

    public void resetRangeDayView(int startPosition, int endPosition) {
        adapterDay.notifyItemRangeChanged(startPosition, endPosition);
        rv_calendar.setAdapter(adapterDay);
    }


    private void createContentFragment(int day, int position) {
        CalendarContentFragment calendarContentFragment = new CalendarContentFragment();

        Bundle bundle = new Bundle();
        bundle.putString("selectDay", myCalendar.getYyyyMMdd(day));
        bundle.putInt("position", position);

        String[] workInfo = userInfo.getWorkSplit();

        int value = (myCalendar.getWorkStartSubMonthFirst() + day - 1) % workInfo.length;
        value = value >= 0 ? value : value + workInfo.length;
        bundle.putInt("originWorkId", Integer.parseInt(workInfo[value]));
        calendarContentFragment.setArguments(bundle);

        Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction()
                .addToBackStack("f_cal")
                .setCustomAnimations(R.anim.left_in, R.anim.right_out, R.anim.left_in, R.anim.right_out)
                .add(R.id.fTab_main_calendar, calendarContentFragment, "f_calendar_content")
                .commit();
    }

    @Override
    public void OnClickDay(int position) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        } else {
            mLastClickTime = SystemClock.elapsedRealtime();
            int day = myCalendar.getDay(position);
            if (day > 0 && day <= myCalendar.getLastDay()) {
                createContentFragment(day, position);
            }
        }
    }
}
