package com.kinggo.app.shiftworker.view.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.kinggo.app.shiftworker.R;
import com.kinggo.app.shiftworker.view.calendar.adapter.CalendarAdapter_Month;
import com.kinggo.app.shiftworker.view.calendar.dialog.CalendarDialogFragment;
import com.kinggo.app.shiftworker.view.calendar.entity.MyCalendar;
import com.kinggo.app.shiftworker.view.calendar.entity.User;
import com.kinggo.app.shiftworker.view.main.MainActivity;
import com.kinggo.app.shiftworker.view.main.OnContentConnect;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnPageChange;

public class CalendarFragment extends Fragment implements CalendarContentFragment.OnResetDayListener {

    // MainActivity 와 연결 리스너
    private static OnContentConnect onContentConnect;

    private User userInfo;

    private Animation alpha_in;

    private CalendarDialogFragment cd_fg;

    private Calendar calendar;
    private List<MyCalendar> myCalendars;
    private CalendarAdapter_Month adapter_month;

    private int[] today;

    private final int FRAGMENT_PAGE_MAX = 25;
    private final int FRAGMENT_PAGE_START = FRAGMENT_PAGE_MAX / 2 - 1;

    private int selectPosition;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    Date beginDate;
    Date endDate;

    @BindView(R.id.tx_calendar_select)
    TextView calendarSelect_tx;

    @BindView(R.id.vp_calendar_list)
    ViewPager calendar_vp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_calendar_main, null);
        ButterKnife.bind(this, view);

        ResetObject();
        ResetPage();

        view.startAnimation(alpha_in);
        return view;
    }

    @OnPageChange({R.id.vp_calendar_list})
    public void viewPagerEvent(int position) {
        String selectDay = String.format("%04d . %02d", myCalendars.get(position).getSelectY(), myCalendars.get(position).getSelectM());
        calendarSelect_tx.setText(selectDay);
        if (position <= 0) {
            ResetPage();
        } else if (position >= myCalendars.size() - 1) {
            selectPosition += 1;
            addPage();
        } else
            selectPosition = position;
    }


    @OnClick({R.id.tx_calendar_select, R.id.btn_calendar_userSetting})
    public void btnEvent(View view) {
        switch (view.getId()) {
            case R.id.tx_calendar_select:
                createDialogFragment();
                break;
            case R.id.btn_calendar_userSetting:
                createUserSettingFragment();
                break;
        }

        calendar_vp.setCurrentItem(selectPosition);
    }

    private void createUserSettingFragment() {
        UserSettingFragment userSettingFragment = new UserSettingFragment();

        Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction()
                .addToBackStack("f_cal")
                .setCustomAnimations(R.anim.left_in, R.anim.right_out, R.anim.left_in, R.anim.right_out)
                .add(R.id.fTab_main_calendar, userSettingFragment, "f_calendar_usersetting")
                .commit();
    }

    private void ResetObject() {
        if (onContentConnect == null)
            onContentConnect = (MainActivity) getActivity();

        assert onContentConnect != null;
        userInfo = onContentConnect.findUser();

        alpha_in = AnimationUtils.loadAnimation(getContext(), R.anim.alpha_in);
        calendar = new GregorianCalendar(Locale.KOREA);
        calendar.setTime(new Date());
        today = new int[3];

        today[0] = calendar.get(Calendar.YEAR);
        today[1] = calendar.get(Calendar.MONTH) + 1;
        today[2] = calendar.get(Calendar.DATE);

        myCalendars = new ArrayList<>();
        adapter_month = new CalendarAdapter_Month(getChildFragmentManager(), myCalendars);
        calendar_vp.setAdapter(adapter_month);
    }

    private void ResetPage() {
        try {
            beginDate = sdf.parse(userInfo.getWorkfirstday());

            if (myCalendars.size() > 0) {

                calendar.set(Calendar.YEAR, myCalendars.get(0).getSelectY());
                calendar.set(Calendar.MONTH, myCalendars.get(0).getSelectM());
                myCalendars.clear();

                selectPosition = FRAGMENT_PAGE_START - 1;

            } else selectPosition = FRAGMENT_PAGE_START;

            calendar.add(Calendar.MONTH, -FRAGMENT_PAGE_START);
            calendar.set(Calendar.DATE, 1);

            for (int value = 0; value < FRAGMENT_PAGE_MAX; value++) {
                MyCalendar myCalendar = new MyCalendar();

                endDate = sdf.parse(sdf.format(calendar.getTime()));
                myCalendar.setSelectY(calendar.get(Calendar.YEAR));
                myCalendar.setSelectM(calendar.get(Calendar.MONTH) + 1);
                myCalendar.setLastDay(calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                myCalendar.setFirstWeek(calendar.get(Calendar.DAY_OF_WEEK));

                Long subDay = endDate.getTime() - beginDate.getTime();

                myCalendar.setWorkStartSubMonthFirst((int) (subDay / (24 * 60 * 60 * 1000)));
                myCalendars.add(myCalendar);
                calendar.add(Calendar.MONTH, 1);
            }

            if (adapter_month != null) {
                adapter_month.notifyDataSetChanged();
                calendar_vp.setCurrentItem(selectPosition, false);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void addPage() {
        MyCalendar myCalendar = new MyCalendar();

        myCalendar.setSelectY(calendar.get(Calendar.YEAR));
        myCalendar.setSelectM(calendar.get(Calendar.MONTH) + 1);
        myCalendar.setLastDay(calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        myCalendar.setFirstWeek(calendar.get(Calendar.DAY_OF_WEEK));

        myCalendars.add(myCalendar);
        calendar.add(Calendar.MONTH, 1);

        adapter_month.notifyDataSetChanged();
    }

    // Custom DialogFragment (calendar)
    private void createDialogFragment() {

        assert getFragmentManager() != null;
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        cd_fg = new CalendarDialogFragment();
        Bundle bundle = new Bundle();

        bundle.putSerializable("myCalendar", myCalendars.get(selectPosition));
        cd_fg.setArguments(bundle);

        cd_fg.setTargetFragment(CalendarFragment.this, 1001);
        cd_fg.show(ft, "CalendarDialogFragment");

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001) {
            MyCalendar value = (MyCalendar) data.getSerializableExtra("myCalendar");

            if (cd_fg != null)
                getFragmentManager().beginTransaction().remove(cd_fg);

            if (value.getSelectM() != -1)
                myCalendars.set(0, value);
            else {
                myCalendars.get(0).setSelectY(today[0]);
                myCalendars.get(0).setSelectM(today[1]);
            }

            ResetPage();

            adapter_month = new CalendarAdapter_Month(getChildFragmentManager(), myCalendars);
            calendar_vp.setAdapter(adapter_month);
            calendar_vp.setCurrentItem(selectPosition, false);
            calendar_vp.startAnimation(alpha_in);
        }
    }

    @Override
    public void resetDayView(int dayPosition) {
        ((CalendarListFragment) adapter_month.getItem(selectPosition)).resetSelectDayView(dayPosition);
    }

    @Override
    public void resetPreOrNextMonthView(int dayPosition) {
        CalendarListFragment selectCalendar = (CalendarListFragment) adapter_month.getItem(selectPosition);
        CalendarListFragment preCalendar = (CalendarListFragment) adapter_month.getItem(selectPosition - 1);
        CalendarListFragment nextCalendar = (CalendarListFragment) adapter_month.getItem(selectPosition + 1);

        int selectDay = myCalendars.get(selectPosition).getDay(dayPosition);
        int selectMonthLastDay = myCalendars.get(selectPosition).getLastDay();

        // 해당 날이 첫번째 날일 경우
        if (selectDay == 1) {
            selectCalendar.resetRangeDayView(dayPosition, dayPosition + 1);

            // 이전 날의 마지막 날 변경
            int preLastDayPosition = myCalendars.get(selectPosition - 1).getLastDay() + myCalendars.get(selectPosition - 1).getFirstWeek();
            preCalendar.resetSelectDayView(preLastDayPosition);
        } else if (selectDay == selectMonthLastDay) { // 해당 날이 마지막 날일 경우
            selectCalendar.resetRangeDayView(dayPosition - 1, dayPosition);

            // 다음 날의 첫 번째 날 변경
            int nextFirstDayPosition = myCalendars.get(selectPosition + 1).getFirstWeek();
            nextCalendar.resetSelectDayView(nextFirstDayPosition);
        } else {
            selectCalendar.resetRangeDayView(dayPosition - 1, dayPosition + 1);
        }
    }
}