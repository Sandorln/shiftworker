package com.kinggo.app.shiftworker.view.calendar;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.kinggo.app.shiftworker.R;
import com.kinggo.app.shiftworker.view.calendar.adapter.WorkInfoGridAdapter;
import com.kinggo.app.shiftworker.view.calendar.adapter.WorkInfoRecyclerAdapter;
import com.kinggo.app.shiftworker.view.calendar.entity.User;
import com.kinggo.app.shiftworker.view.calendar.entity.WorkInfo;
import com.kinggo.app.shiftworker.view.main.MainActivity;
import com.kinggo.app.shiftworker.view.main.OnContentConnect;
import com.kinggo.app.shiftworker.view.main.OnResetMainActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class UserSettingFragment extends Fragment implements WorkInfoRecyclerAdapter.OnWorkInfoListClickEvent {

    // MainActivity 와 연결
    private static OnContentConnect onContentConnect;
    private static OnResetMainActivity onResetMainActivity;

    private ArrayList<WorkInfo> workPatternList;     // 현재 설정된 근무 패턴 / 이후 변경될 근무 패턴
    private ArrayList<WorkInfo> workInfoList;

    private User user;

    // 근무 첫 날
    private int year;
    private int month;
    private int day;

    private WorkInfoRecyclerAdapter workInfoAdapter;

    private String[] colorSelector = {"#00b16a", "#663399", "#7d7d7d", "#f9690e", "#1f3a93"};
    private static int colorCount = 3;

    private WorkInfoGridAdapter workPatternAdapter;

    @BindView(R.id.setting_selectDay_tx)
    TextView selectDay_tx;

    @BindView(R.id.setting_workPattern_grid)
    GridView pattern_grid;

    @BindView(R.id.setting_workInfo_rc)
    RecyclerView workInfo_rc;

    @BindView(R.id.setting_workInfo_deleteCk)
    CheckBox delete_ck;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_calendar_usersetting, container, false);
        ButterKnife.bind(this, view);

        resetObject();
        resetView();

        return view;
    }

    private void resetObject() {
        if (onResetMainActivity == null)
            onResetMainActivity = (MainActivity) getActivity();

        if (onContentConnect == null) {
            onContentConnect = (MainActivity) getActivity();
        }

        assert onContentConnect != null;
        user = onContentConnect.findUser();
        workInfoList = new ArrayList<>(onContentConnect.getArrayWorkInfo());
        List<String> values = new ArrayList<>(Arrays.asList(onContentConnect.findUser().getWorkSplit()));

        year = Integer.parseInt(user.getWorkfirstday().substring(0, 4));
        month = Integer.parseInt(user.getWorkfirstday().substring(4, 6));
        day = Integer.parseInt(user.getWorkfirstday().substring(6, 8));

        workPatternList = new ArrayList<>();

        for (String workId : values) {
            workPatternList.add(onContentConnect.findWorkInfoById(Integer.parseInt(workId)));
        }
    }

    // 화면 초기화
    private void resetView() {
        selectDay_tx.setText(year + " . " + month + " . " + day);

        // RecyclerView 설정
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        workInfo_rc.setLayoutManager(layoutManager);

        workInfoAdapter = new WorkInfoRecyclerAdapter(workInfoList);
        workInfoAdapter.setOnWorkInfoListClickEvent(this);
        workInfo_rc.setAdapter(workInfoAdapter);

        workPatternAdapter = new WorkInfoGridAdapter(workPatternList, getContext());
        pattern_grid.setAdapter(workPatternAdapter);
    }

    @OnClick({R.id.setting_total_view, R.id.setting_back_btn, R.id.setting_okayBtn, R.id.setting_resetBtn,
            R.id.setting_addWorkInfo_btn, R.id.setting_selectDay_tx})
    public void clickEvent(View v) {
        switch (v.getId()) {
            case R.id.setting_total_view:
                break;
            case R.id.setting_back_btn:
                assert getFragmentManager() != null;
                getFragmentManager().beginTransaction()
                        .remove(Objects.requireNonNull(getFragmentManager().findFragmentByTag("f_calendar_usersetting")))
                        .commit();
                break;
            case R.id.setting_okayBtn:
                inputData();
                break;
            case R.id.setting_resetBtn:
                resetCheck();
                break;
            case R.id.setting_addWorkInfo_btn:
                createTextDialog();
                break;
            case R.id.setting_selectDay_tx:
                selectFirstDay();
                break;
        }
    }

    // 근무 첫 날 고르기
    private void selectFirstDay() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                android.R.style.Theme_Holo_Dialog, onDateSetListener, year, month - 1, day);
        datePickerDialog.getDatePicker().setSpinnersShown(true);
        datePickerDialog.show();
    }

    private DatePickerDialog.OnDateSetListener onDateSetListener = (datePicker, selectYear, selectMonth, selectDay) -> {
        year = selectYear;
        month = selectMonth + 1;
        day = selectDay;
        selectDay_tx.setText(year + " . " + month + " . " + day);
    };

    // 되돌리기 버튼 눌렀을 시
    private void resetCheck() {
        new AlertDialog.Builder(getContext())
                .setTitle("초기화")
                .setMessage("현재까지 입력된 정보가 초기화됩니다")
                .setPositiveButton("확인", (dialogInterface, i) -> {
                    resetObject();
                    resetView();
                })
                .setNegativeButton("취소", (dialogInterface, i) -> {

                })
                .show();
    }

    @OnItemClick({R.id.setting_workPattern_grid})
    public void clickItemEvent(int position) {
        workPatternList.remove(position);
        workPatternAdapter.notifyDataSetChanged();
    }

    private void createTextDialog() {
        AlertDialog.Builder ad = new AlertDialog.Builder(getContext());
        delete_ck.setChecked(false);

        ad.setTitle("근무 형태 추가");                          // 제목 설정
        ad.setMessage("추가할 근무 형태 이름을 입력하세요");       // 내용 설정

        final EditText et = new EditText(getContext());
        ad.setView(et);

        ad.setPositiveButton("추가", (dialog, which) -> {

            String value = et.getText().toString();
            checkOverlapWorkName(value);

            dialog.dismiss();     //닫기
            // Event
        });

        ad.setNegativeButton("취소", (dialog, which) -> {
            dialog.dismiss();     //닫기
            // Event
        });

        ad.show();
    }


    // 중복되는 이름인지 체크
    private void checkOverlapWorkName(String name) {
        boolean checkName = true;

        for (WorkInfo workInfo : workInfoList) {
            if (workInfo.getName().equals(name)) {
                checkName = false;
            }
        }

        if (checkName) {
            String color = colorSelector[colorCount];
            colorCount = colorCount < colorSelector.length - 1 ? colorCount + 1 : 0;
            workInfoList.add(new WorkInfo(0, name, color));
            workInfoAdapter.notifyDataSetChanged();
        }
    }

    // 정보 입력 및 화면 갱신
    private void inputData() {
        try {
            if (workPatternList.size() <= 0) {
                throw new NoPatternException("근무 패턴이 존재하지 않습니다");
            }
            String selectFirstDay = String.format("%04d%02d%02d", year, month, day);


            onContentConnect.updateUserWork(workPatternList, workInfoList, selectFirstDay);
            onResetMainActivity.Restart_App();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void clickWorkInfoEvent(int position) {
        if (delete_ck.isChecked()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                List<WorkInfo> temp = workPatternList.stream()
                        .filter(WorkInfo -> !WorkInfo.getName().equals(workInfoList.get(position).getName()))
                        .collect(Collectors.toList());

                workPatternList = (ArrayList<WorkInfo>) temp;
            }

            workInfoList.remove(position);
            workInfoAdapter.notifyDataSetChanged();
            workPatternAdapter = new WorkInfoGridAdapter(workPatternList, getContext());
            pattern_grid.setAdapter(workPatternAdapter);
        } else {
            workPatternList.add(workInfoList.get(position));
            workPatternAdapter.notifyDataSetChanged();
        }
    }

    private class NoPatternException extends Exception {
        NoPatternException(String message) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }


}
