package com.kinggo.app.shiftworker.view.calendar;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kinggo.app.shiftworker.R;
import com.kinggo.app.shiftworker.view.calendar.entity.WorkDay;
import com.kinggo.app.shiftworker.view.calendar.entity.WorkInfo;
import com.kinggo.app.shiftworker.view.main.MainActivity;
import com.kinggo.app.shiftworker.view.main.OnContentConnect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CalendarContentFragment extends Fragment {

    // CalendarFragment 와 연결 리스너
    private static OnResetDayListener onResetDayListener;
    private InputMethodManager imm;

    // MainActivity 와 연결 리스너
    private static OnContentConnect onContentConnect;
    private String selectDay;

    // DataChange 시 RecyclerView 아이템 중 변경될 Position 값
    private int position;

    // DB 에 저장 될 값
    private String workEtcInput;

    // 근무 형태 관련
    private static LayoutParams layoutParamsBtn;

    private Map<Integer, RadioButton> mapViewIdBtn;
    private Map<Integer, WorkInfo> mapBtnViewIdWork;

    private ArrayList<WorkInfo> workInfoList;

    private RadioGroup.OnCheckedChangeListener onCheckedChangeListener = (radioGroup, radioId) -> {
        inputWorkTread(mapBtnViewIdWork.get(radioId).getId());
        checkWorkInfo(mapBtnViewIdWork.get(radioId).getId());
    };

    private int originWorkId;                       // 기본 근무 형태 ID
    private WorkDay work;                               // 사용자 변경 근무 형태 ID

    private ArrayList<String> holidays;
    private String memo;
    private int overTime; // 분 단위

    @BindView(R.id.tx_calContent_select)
    TextView selectDay_tx;
    @BindView(R.id.tx_calContent_holiday)
    TextView holiday_tx;
    @BindView(R.id.edi_calContent_memo)
    EditText memo_tx;
    @BindView(R.id.rd_calContent_work)
    RadioGroup work_rd;
    @BindView(R.id.f_calContent_back)
    RelativeLayout content_rl;
    @BindView(R.id.tx_calContent_work_reset)
    TextView workReset_tx;
    @BindView(R.id.tx_calContent_work_etc)
    TextView workEtc_tx;
    @BindView(R.id.tx_calContent_overTime)
    TextView overTime_tx;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_calendar_content, null);
        ButterKnife.bind(this, view);

        resetObject();
        resetView();

        return view;
    }

    private void resetObject() {
        imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        if (onResetDayListener == null)
            onResetDayListener = (CalendarFragment) getActivity().getSupportFragmentManager().findFragmentByTag("f_cal");

        if (onContentConnect == null)
            onContentConnect = (MainActivity) getActivity();

        assert getArguments() != null;
        selectDay = getArguments().getString("selectDay");
        position = getArguments().getInt("position");
        originWorkId = getArguments().getInt("originWorkId");

        workInfoList = onContentConnect.getArrayWorkInfo();

        mapBtnViewIdWork = new HashMap<>();
        mapViewIdBtn = new HashMap<>();

        if (layoutParamsBtn == null) {
            layoutParamsBtn = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
            layoutParamsBtn.setMarginStart(25);
        }

        if (onContentConnect != null) {
            holidays = (ArrayList<String>) onContentConnect.findHoliday(selectDay);
            memo = onContentConnect.findMemo(selectDay);
            work = onContentConnect.findWorkDay(selectDay);
            overTime = onContentConnect.findOverTime(selectDay);
        }
    }

    private void resetView() {
        StringBuffer buffer = new StringBuffer(selectDay);
        buffer.insert(4, ".");
        buffer.insert(7, ".");

        // 선택된 날짜 입력
        selectDay_tx.setText(buffer.toString());

        settingHoliday();
        settingMemo();
        settingWork();
        settingOverTime();
    }

    private void settingOverTime() {
        if (overTime != 0) {
            overTime_tx.setText(String.format("%02d : %02d", overTime / 60, overTime % 60));
        }
    }

    private void settingWork() {
        for (WorkInfo workInfo : workInfoList) {
            // 사용자가 지정한 근무 형태
            RadioButton btn = new RadioButton(getContext());

            int btnId = View.generateViewId();

            btn.setId(btnId);
            btn.setLayoutParams(layoutParamsBtn);
            btn.setText(" " + workInfo.getName());
            btn.setTextSize(10);
            work_rd.addView(btn);
            mapBtnViewIdWork.put(btnId, workInfo);
            mapViewIdBtn.put(btnId, btn);
        }

        if (work != null)
            checkWorkInfo(work.getWorkInfoId());
        else
            checkWorkInfo(originWorkId);

        work_rd.setOnCheckedChangeListener(onCheckedChangeListener);
    }

    private void checkWorkInfo(int selectId) {
        selectId = selectId == 0 ? originWorkId : selectId;

        for (Map.Entry<Integer, WorkInfo> entry : mapBtnViewIdWork.entrySet()) {
            RadioButton radioButton = mapViewIdBtn.get(entry.getKey());

            Drawable drawableBtn = getResources().getDrawable(R.drawable.work_icon_circle, getActivity().getTheme());

            if (entry.getValue().getId() == selectId) {
                drawableBtn.setColorFilter(Color.parseColor(entry.getValue().getColor()), PorterDuff.Mode.SRC);
                work_rd.check(entry.getKey());
            } else {
                drawableBtn.setColorFilter(Color.parseColor("#FFD7D7D7"), PorterDuff.Mode.SRC);
            }
            radioButton.setButtonDrawable(drawableBtn);
        }

        if (selectId == -1 && work != null) {
            workEtc_tx.setText("다른 일정 설정 : " + work.getWorkEtc());
        } else
            workEtc_tx.setText("다른 일정 설정");
    }

    private void inputWorkTread(int value) {
        Message message = new Message();
        message.what = 1;

        new Thread(() -> {
            int workInput;

            if (originWorkId == value) {
                workInput = 0;
            } else
                workInput = value;

            onContentConnect.inputWorkDay(selectDay, new WorkDay(workInput, workEtcInput));

            handler.sendMessage(message);
        }).start();
    }

    private void settingMemo() {
        // 메모 관련 view 넣기
        if (memo != null) {
            memo_tx.setText(memo);
        }

        assert memo_tx != null;
        memo_tx.setOnFocusChangeListener((view, b) -> {
            if (!b) {
                Message message = new Message();
                message.what = 0;

                new Thread(() -> {
                    onContentConnect.inputMemo(selectDay, memo_tx.getText().toString());
                    handler.sendMessage(message);
                }).start();
            }
        });
    }

    private void settingHoliday() {
        // 공휴일이 존재한다면 입력
        if (holidays != null) {
            StringBuilder resultHoliday = new StringBuilder();
            resultHoliday.append(" | ");
            for (String holiday : holidays) {
                resultHoliday.append(holiday).append(" | ");
            }
            holiday_tx.setText(resultHoliday);
        } else
            holiday_tx.setVisibility(View.GONE);
    }

    @OnClick({R.id.f_calContent_back, R.id.img_calContent_back, R.id.tx_calContent_overTime})
    public void backClickNo(View v) {
        switch (v.getId()) {
            case R.id.img_calContent_back:
                assert getFragmentManager() != null;

                // 키보드 숨기기
                imm.hideSoftInputFromWindow(memo_tx.getWindowToken(), 0);

                // 해당 Fragment 삭제 애니메이션
                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.right_out);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        // 해당 Fragment 삭제
                        getFragmentManager().beginTransaction()
                                .remove(CalendarContentFragment.this)
                                .commit();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                // 애니메이션 시작
                Objects.requireNonNull(getView()).startAnimation(animation);

                break;
            case R.id.tx_calContent_overTime:
                createOverTimeDialog();
                break;
            case R.id.f_calContent_back:
                break;
        }
    }

    private void createOverTimeDialog() {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = (timePicker, hour, minute) -> {
            inputOverTime(hour * 60 + minute);
        };

        new TimePickerDialog(getContext(), onTimeSetListener, 12, 0, true).show();
    }

    private void inputOverTime(int overTime) {
        overTime_tx.setText(String.format("%02d : %02d", overTime / 60, overTime % 60));
        Message message = new Message();
        message.what = 2;
        new Thread(() -> {
            onContentConnect.inputOverTime(selectDay, overTime);
            handler.sendMessage(message);
        }).start();
    }


    @OnClick({R.id.tx_calContent_work_etc, R.id.tx_calContent_work_reset, R.id.tx_calContent_over_reset})
    public void clickEvent(View v) {
        switch (v.getId()) {
            case R.id.tx_calContent_work_reset:
                inputWorkTread(0);
                checkWorkInfo(0);
                break;
            case R.id.tx_calContent_work_etc:
                createWorkEtcDialog();
                break;
            case R.id.tx_calContent_over_reset:
                inputOverTime(0);
                break;
        }
    }

    // 기타 버튼 눌렀을 시
    private void createWorkEtcDialog() {
        AlertDialog.Builder ad = new AlertDialog.Builder(getContext());

        ad.setTitle("새로운 일정");                     // 제목 설정
        ad.setMessage("새로운 일정을 입력해주세요");       // 내용 설정

        final EditText et = new EditText(getContext());
        ad.setView(et);

        ad.setPositiveButton("변경", (dialog, which) -> {

            String value = et.getText().toString();
            workEtcInput = value;
            inputWorkTread(-1);
            checkWorkInfo(-1);
            workEtc_tx.setText("기타 : " + workEtcInput);

            dialog.dismiss();     //닫기
            // Event
        });

        ad.setNegativeButton("취소", (dialog, which) -> {
            dialog.dismiss();     //닫기
            // Event
        });

        ad.show();
    }

    // 0 : 메모 / 1 : 근무 형태 / 2 : 오버타임
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    try {
                        onResetDayListener.resetDayView(position);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 1:
                    try {
                        onResetDayListener.resetPreOrNextMonthView(position);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    try {
                        onResetDayListener.resetDayView(position);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    public interface OnResetDayListener {
        void resetDayView(int dayPosition);
        void resetPreOrNextMonthView(int dayPosition);
    }
}