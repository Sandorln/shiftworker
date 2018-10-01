package com.kinggo.app.shiftworker.view.calendar.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kinggo.app.shiftworker.R;
import com.kinggo.app.shiftworker.view.calendar.entity.MyCalendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class CalendarDialogFragment extends DialogFragment {

    @BindView(R.id.year_tx)
    TextView year_tx;

    private int year;

    private MyCalendar myCalendar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_calendar_selector, null);
        ButterKnife.bind(this, view);

        resetObject();
        resetView();

        return view;
    }

    private void resetObject() {
        assert getArguments() != null;

        myCalendar = (MyCalendar) getArguments().getSerializable("myCalendar");

        year = myCalendar.getSelectY();

    }

    private void resetView() {
        year_tx.setText(String.valueOf(myCalendar.getSelectY()));
    }


    @OnClick({R.id.month_01, R.id.month_02, R.id.month_03, R.id.month_04, R.id.month_05, R.id.month_06,
            R.id.month_07, R.id.month_08, R.id.month_09, R.id.month_10, R.id.month_11, R.id.month_12,
            R.id.year_pre, R.id.year_next, R.id.move_today})
    public void clickViewEvent(View view) {
        int month = 1;
        switch (view.getId()) {
            case R.id.month_12:
                month++;
            case R.id.month_11:
                month++;
            case R.id.month_10:
                month++;
            case R.id.month_09:
                month++;
            case R.id.month_08:
                month++;
            case R.id.month_07:
                month++;
            case R.id.month_06:
                month++;
            case R.id.month_05:
                month++;
            case R.id.month_04:
                month++;
            case R.id.month_03:
                month++;
            case R.id.month_02:
                month++;
            case R.id.month_01:
                myCalendar.setSelectY(year);
                myCalendar.setSelectM(month);
                finishFragment();
                break;
            case R.id.year_next:
                year += 1;
                myCalendar.setSelectY(year);
                resetView();
                break;
            case R.id.year_pre:
                year -= 1;
                myCalendar.setSelectY(year);
                resetView();
                break;
            case R.id.move_today:
                myCalendar.setSelectM(-1);
                finishFragment();
                break;
        }
    }

    private void finishFragment() {
        Intent intent = new Intent();
        intent.putExtra("myCalendar", myCalendar);

        assert getTargetFragment() != null;
        getTargetFragment().onActivityResult(1001, 0, intent);
        dismiss();
    }

}