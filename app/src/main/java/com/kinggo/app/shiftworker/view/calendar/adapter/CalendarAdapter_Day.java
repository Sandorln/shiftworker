package com.kinggo.app.shiftworker.view.calendar.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kinggo.app.shiftworker.R;
import com.kinggo.app.shiftworker.view.calendar.entity.MyCalendar;
import com.kinggo.app.shiftworker.view.calendar.entity.WorkDay;
import com.kinggo.app.shiftworker.view.calendar.entity.WorkInfo;
import com.kinggo.app.shiftworker.view.main.MainActivity;
import com.kinggo.app.shiftworker.view.main.OnContentConnect;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class CalendarAdapter_Day extends RecyclerView.Adapter<CalendarAdapter_Day.DayViewHolder> {

    private final static int DAY_MAX = 42;
    private static int viewHeight = 0;
    private static int viewWidth = 0;

    private Context context;

    // MainActivity 와 연결 리스너
    private static OnContentConnect onContentConnect;

    private String[] workInfo;
    private int startDaySubToDay;
    private OnClickCalendarDay onClickCalendarDay;

    private MyCalendar myCalendar;

    public CalendarAdapter_Day(MyCalendar myCalendar, Context context, OnClickCalendarDay onClickCalendarDay) {
        this.myCalendar = myCalendar;
        this.context = context;

        if (onContentConnect == null)
            onContentConnect = ((MainActivity) context);

        this.onClickCalendarDay = onClickCalendarDay;

        workInfo = onContentConnect.findUser().getWorkSplit();
        startDaySubToDay = myCalendar.getWorkStartSubMonthFirst();
    }

    @SuppressLint("CutPasteId")
    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        if (viewHeight <= 0) {
            viewHeight = viewGroup.getHeight() / 6;
        }
        if (viewWidth <= 0) {
            viewWidth = viewGroup.getWidth() / 7;
        }


        return new DayViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_calendar, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder dayViewHolder, int position) {
        dayViewHolder.thisView.setOnClickListener(view -> onClickCalendarDay.OnClickDay(position));
        settingDays(position, dayViewHolder);
    }

    @Override
    public int getItemCount() {
        return DAY_MAX;
    }

    // 해당 월 첫 날 ~ 마지막 날 안의 날만 체크
    private Boolean settingDays(int position, DayViewHolder dayViewHolder) {
        int dayCount = myCalendar.getDay(position);

        if (dayCount > 0 && dayCount <= myCalendar.getLastDay()) {

            String selectDay = myCalendar.getYyyyMMdd(dayCount);
            dayViewHolder.day_tx.setText(String.valueOf(dayCount));

            if (position % 7 == 0) dayViewHolder.day_tx.setTextColor(Color.parseColor("#FE2E64"));
            else if (position % 7 == 6)
                dayViewHolder.day_tx.setTextColor(Color.parseColor("#ff1f3a93"));

            checkHoliday(selectDay, dayViewHolder);
            checkMemoOverTime(selectDay, dayViewHolder);
            checkWorkInfo(selectDay, dayCount, dayViewHolder);

            return true;
        } else {
            dayViewHolder.day_tx.setText("");
            return false;
        }
    }

    private void checkWorkInfo(String selectDay, int dayCount, DayViewHolder dayViewHolder) {
        WorkDay today = onContentConnect.findWorkDay(selectDay);
        WorkDay preDay;
        WorkDay nextDay;

        // 이전 달 마지막 날과 다음 달 첫 날에 변경사항이 있는지
        if (dayCount == 1 || dayCount == myCalendar.getLastDay()) {

            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Calendar calendar = new GregorianCalendar();
            calendar.set(Calendar.YEAR, myCalendar.getSelectY());
            calendar.set(Calendar.MONTH, myCalendar.getSelectM() - 1);
            calendar.set(Calendar.DATE, dayCount);

            // 이전 달 마지막 날과 다음 달 첫 날에 근무 형태가 변형 되었는지 체크
            if (dayCount == 1) {
                // 이전 달 마지막 근무 형태가 변형 되었는지
                calendar.add(Calendar.DATE, -1);
                preDay = onContentConnect.findWorkDay(sdf.format(calendar.getTime()));
                nextDay = onContentConnect.findWorkDay(myCalendar.getYyyyMMdd(dayCount + 1));
            } else {
                calendar.add(Calendar.DATE, 1);
                preDay = onContentConnect.findWorkDay(myCalendar.getYyyyMMdd(dayCount - 1));
                nextDay = onContentConnect.findWorkDay(sdf.format(calendar.getTime()));
            }

        } else {
            preDay = onContentConnect.findWorkDay(myCalendar.getYyyyMMdd(dayCount - 1));
            nextDay = onContentConnect.findWorkDay(myCalendar.getYyyyMMdd(dayCount + 1));
        }

        // 여기까지 today / pre / next 값이 null 값일 경우
        // 여기서부터는 원래 근무 패턴에 값을 적용
        int todayOriginWorkCount = (startDaySubToDay + dayCount - 1) % workInfo.length;
        todayOriginWorkCount = todayOriginWorkCount >= 0 ? todayOriginWorkCount : todayOriginWorkCount + workInfo.length;

        if (today == null) {
            today = new WorkDay(Integer.parseInt(workInfo[todayOriginWorkCount]), null);
        } else { // 근무 형태가 변경되었다는 것 알려주는 표시
            if (dayViewHolder.changeWork.getParent() != null)
                ((ViewGroup) dayViewHolder.changeWork.getParent()).removeView(dayViewHolder.changeWork);

            dayViewHolder.content.addView(dayViewHolder.changeWork);
        }

        if (preDay == null) {
            preDay = new WorkDay(Integer.parseInt(workInfo[todayOriginWorkCount > 0 ? todayOriginWorkCount - 1 : workInfo.length - 1]), null);
        }

        if (nextDay == null) {
            nextDay = new WorkDay(Integer.parseInt(workInfo[todayOriginWorkCount < workInfo.length - 1 ? todayOriginWorkCount + 1 : 0]), null);
        }

        // 해당하는 날의 WorkInfo (근무형태 찾기)
        WorkInfo toDayWorkInfo = findWorkInfo(today);

        if (today.getWorkInfoId() >= 0) {
            if (today.getWorkInfoId() == preDay.getWorkInfoId()) {

                if (dayViewHolder.leftBar.getParent() != null)
                    ((ViewGroup) dayViewHolder.leftBar.getParent()).removeView(dayViewHolder.leftBar);

                dayViewHolder.leftBar.setBackgroundColor(Color.parseColor(toDayWorkInfo.getColor()));
                dayViewHolder.content.addView(dayViewHolder.leftBar);
            }

            if (today.getWorkInfoId() == nextDay.getWorkInfoId()) {

                if (dayViewHolder.rightBar.getParent() != null)
                    ((ViewGroup) dayViewHolder.rightBar.getParent()).removeView(dayViewHolder.rightBar);

                dayViewHolder.rightBar.setBackgroundColor(Color.parseColor(toDayWorkInfo.getColor()));
                dayViewHolder.content.addView(dayViewHolder.rightBar);
            }
        }

        if (today.getWorkInfoId() != preDay.getWorkInfoId() || today.getWorkInfoId() != nextDay.getWorkInfoId()) {
            dayViewHolder.workImg.setColorFilter(Color.parseColor(toDayWorkInfo.getColor()));

            if (today.getWorkInfoId() >= 0) {
                dayViewHolder.workTx.setTextColor(Color.parseColor("#FFFFFF"));
            } else
                dayViewHolder.workTx.setTextColor(Color.parseColor("#000000"));


            // 최대 3글자 표현
            if (toDayWorkInfo.getName().length() > 3) {
                dayViewHolder.workTx.setText(toDayWorkInfo.getName().substring(0, 3));
            } else
                dayViewHolder.workTx.setText(toDayWorkInfo.getName());

            if (dayViewHolder.workImg.getParent() != null)
                ((ViewGroup) dayViewHolder.workImg.getParent()).removeView(dayViewHolder.workImg);

            if (dayViewHolder.workTx.getParent() != null)
                ((ViewGroup) dayViewHolder.workTx.getParent()).removeView(dayViewHolder.workTx);

            dayViewHolder.content.addView(dayViewHolder.workImg);
            dayViewHolder.content.addView(dayViewHolder.workTx);
        }
    }

    // 오늘의 Work
    private WorkInfo findWorkInfo(WorkDay today) {
        WorkInfo workInfo = onContentConnect.findWorkInfoById(today.getWorkInfoId());
        if (workInfo != null)
            return workInfo;
        else
            return new WorkInfo(-1, today.getWorkEtc(), "#FFFFFF");
    }

    private void checkMemoOverTime(String selectDay, DayViewHolder dayViewHolder) {
        int overTime = onContentConnect.findOverTime(selectDay);
        String memo = onContentConnect.findMemo(selectDay);

        if (dayViewHolder.memo_layout.getParent() != null)
            ((ViewGroup) dayViewHolder.memo_layout.getParent()).removeView(dayViewHolder.memo_layout);

        if (dayViewHolder.overTime_layout.getParent() != null)
            ((ViewGroup) dayViewHolder.overTime_layout.getParent()).removeView(dayViewHolder.overTime_layout);

        if (overTime != 0) {
            dayViewHolder.overTime_tx.setText(String.format("%02d : %02d", overTime / 60, overTime % 60));
            dayViewHolder.content.addView(dayViewHolder.overTime_layout);
        }

        if (memo != null && memo.length() > 0) {
            dayViewHolder.content.removeView(dayViewHolder.memo_layout);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.bottomMargin = 5;

            if (overTime != 0)
                layoutParams.addRule(RelativeLayout.ABOVE, dayViewHolder.overTime_layout.getId());
            else
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

            dayViewHolder.memo_layout.setLayoutParams(layoutParams);
            dayViewHolder.memo_tx.setText(memo);
            dayViewHolder.content.addView(dayViewHolder.memo_layout);
        }
    }

    // 공휴일 표시
    private void checkHoliday(String selectDay, DayViewHolder dayViewHolder) {
        if (dayViewHolder.holiday.getParent() != null)
            ((ViewGroup) dayViewHolder.holiday.getParent()).removeView(dayViewHolder.holiday);

        ArrayList<String> holidays = (ArrayList<String>) onContentConnect.findHoliday(selectDay);
        if (holidays != null) {
            dayViewHolder.holiday.setText(holidays.get(0));
            dayViewHolder.content.addView(dayViewHolder.holiday);
        }
    }

    class DayViewHolder extends RecyclerView.ViewHolder {
        View thisView;
        TextView day_tx;

        RelativeLayout back;
        RelativeLayout content;

        TextView holiday;

        LinearLayout overTime_layout;
        TextView overTime_tx;

        LinearLayout memo_layout;
        TextView memo_tx;

        View rightBar;
        View leftBar;

        ImageView workImg;
        TextView workTx;

        View changeWork;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            thisView = itemView;
            day_tx = itemView.findViewById(R.id.item_calendar_day_tx);

            back = itemView.findViewById(R.id.item_calendar_back);
            content = itemView.findViewById(R.id.item_calendar_content);

            back.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, viewHeight));

            createContentView();
        }


        // 동적 할당으로 View 생성
        private void createContentView() {
            // 공휴일 텍스트 생성
            holiday = new TextView(context);
            holiday.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
            holiday.setTextColor(Color.parseColor("#ff3535"));

            RelativeLayout.LayoutParams holiday_p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            holiday_p.addRule(RelativeLayout.ALIGN_TOP, R.id.item_calendar_day_tx);
            holiday_p.addRule(RelativeLayout.ALIGN_PARENT_END);
            holiday_p.topMargin = 5;
            holiday_p.rightMargin = 10;
            holiday.setLayoutParams(holiday_p);


            // 오버 타임 레이아웃 생성
            int overTime_id = View.generateViewId();

            overTime_layout = new LinearLayout(context);
            overTime_layout.setOrientation(LinearLayout.HORIZONTAL);
            overTime_layout.setWeightSum(1);
            overTime_layout.setId(overTime_id);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.bottomMargin = 5;
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            overTime_layout.setLayoutParams(layoutParams);

            // 오버 타임 이미지
            ImageView overTime_img = new ImageView(context);
            overTime_img.setImageResource(R.drawable.calendar_over_icon);

            LinearLayout.LayoutParams overTime_p_img = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            overTime_p_img.weight = 0.30f;
            overTime_p_img.gravity = Gravity.CENTER_HORIZONTAL;

            overTime_img.setLayoutParams(overTime_p_img);
            overTime_layout.addView(overTime_img);

            // 오버 타임 텍스트
            overTime_tx = new TextView(context);

            overTime_tx.setTextSize(TypedValue.COMPLEX_UNIT_SP, 7);
            overTime_tx.setGravity(Gravity.CENTER_HORIZONTAL);

            LinearLayout.LayoutParams overTime_tx_p = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            overTime_tx_p.weight = 0.70f;

            overTime_tx.setLayoutParams(overTime_tx_p);
            overTime_layout.addView(overTime_tx);


            // 메모 레이아웃 생성
            memo_layout = new LinearLayout(context);
            memo_layout.setOrientation(LinearLayout.HORIZONTAL);
            memo_layout.setWeightSum(1);

            // 메모 이미지 생성
            ImageView memo_img = new ImageView(context);
            memo_img.setImageResource(R.drawable.calendar_memo_icon);

            LinearLayout.LayoutParams memo_p_img = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            memo_p_img.weight = 0.30f;
            memo_p_img.gravity = Gravity.CENTER_HORIZONTAL;

            memo_img.setLayoutParams(memo_p_img);
            memo_layout.addView(memo_img);

            // 메모 텍스트 생성
            memo_tx = new TextView(context);
            memo_tx.setMaxLines(1);

            memo_tx.setTextSize(TypedValue.COMPLEX_UNIT_SP, 7);
            memo_tx.setGravity(Gravity.CENTER_HORIZONTAL);

            LinearLayout.LayoutParams memo_tx_p = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            memo_tx_p.weight = 0.70f;

            memo_tx.setLayoutParams(memo_tx_p);
            memo_layout.addView(memo_tx);

            // 연결 선 생성 < 왼쪽 / 오른 쪽 >
            leftBar = new View(context);

            RelativeLayout.LayoutParams leftBar_p = new RelativeLayout.LayoutParams(viewWidth / 2 + 1, (int) (viewHeight / 3.5));
            leftBar_p.addRule(RelativeLayout.CENTER_VERTICAL);
            leftBar_p.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

            leftBar.setAlpha(0.33f);
            leftBar.setLayoutParams(leftBar_p);

            rightBar = new View(context);

            RelativeLayout.LayoutParams rightBar_p = new RelativeLayout.LayoutParams(viewWidth / 2, (int) (viewHeight / 3.5));
            rightBar_p.addRule(RelativeLayout.CENTER_VERTICAL);
            rightBar_p.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

            rightBar.setAlpha(0.33f);
            rightBar.setLayoutParams(rightBar_p);


            // 중앙 근무 형태 이미지 표시
            workImg = new ImageView(context);
            workImg.setImageResource(R.drawable.work_icon_circle);

            RelativeLayout.LayoutParams layoutParams_img = new RelativeLayout.LayoutParams((viewWidth / 2), (int) (viewHeight / 3.5));
            layoutParams_img.addRule(RelativeLayout.CENTER_IN_PARENT);

            workImg.setLayoutParams(layoutParams_img);


            // 중앙 근무 형태 글씨 표시
            workTx = new TextView(context);
            workTx.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            workTx.setGravity(Gravity.CENTER);
            workTx.setMaxLines(1);

            RelativeLayout.LayoutParams layoutParams_tx = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (viewHeight / 3.5));
            layoutParams_tx.addRule(RelativeLayout.CENTER_IN_PARENT);
            workTx.setLayoutParams(layoutParams_tx);

            // 해당 날이 변경 되었을 시
            changeWork = new View(context);
            changeWork.setBackgroundColor(Color.parseColor("#FFf03434"));

            RelativeLayout.LayoutParams changeWork_p = new RelativeLayout.LayoutParams(10, 10);
            changeWork_p.addRule(RelativeLayout.BELOW, R.id.item_calendar_day_tx);
            changeWork_p.addRule(RelativeLayout.ALIGN_START, R.id.item_calendar_day_tx);
            changeWork_p.leftMargin = 1;
            changeWork_p.topMargin = 1;

            changeWork.setLayoutParams(changeWork_p);
        }
    }

    public interface OnClickCalendarDay {
        void OnClickDay(int position);
    }
}