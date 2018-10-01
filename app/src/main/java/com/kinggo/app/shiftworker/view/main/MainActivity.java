package com.kinggo.app.shiftworker.view.main;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TabHost;

import com.kinggo.app.shiftworker.R;
import com.kinggo.app.shiftworker.database.DatabaseHelper;
import com.kinggo.app.shiftworker.view.calendar.CalendarContentFragment;
import com.kinggo.app.shiftworker.view.calendar.CalendarFragment;
import com.kinggo.app.shiftworker.view.calendar.UserSettingFragment;
import com.kinggo.app.shiftworker.view.calendar.entity.User;
import com.kinggo.app.shiftworker.view.calendar.entity.WorkDay;
import com.kinggo.app.shiftworker.view.calendar.entity.WorkInfo;
import com.kinggo.app.shiftworker.view.community.CommunityBoardWrite;
import com.kinggo.app.shiftworker.view.community.CommunityContentFragment;
import com.kinggo.app.shiftworker.view.community.CommunityListFragment;
import com.kinggo.app.shiftworker.view.community.CommunityLogin;
import com.kinggo.app.shiftworker.view.community.domain.UserInfo;
import com.kinggo.app.shiftworker.view.more.MoreFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements OnContentConnect, OnResetMainActivity {

    private final int TABLE_MEMO = 0;
    private final int TABLE_OVERTIME = 1;
    private final int TABLE_WORK = 2;

    @BindView(R.id.fTab_main_menu)
    FragmentTabHost fTabHost;

    private DatabaseHelper db;

    private CalendarFragment cal_fg;
    private CommunityListFragment com_fg;
    private MoreFragment more_fg;

    private Map<String, List<String>> mapHolidays;
    private Map<String, String> mapMemos;
    private Map<String, Integer> mapOverTime;
    private Map<String, WorkDay> mapWorkDay;
    private Map<Integer, WorkInfo> mapWorkInfo;
    private ArrayList<WorkInfo> workInfoList;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_main_ftaphost);
        ButterKnife.bind(this);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        new MainAsyncTask().execute();
    }

    private void resetObject() {
        db = new DatabaseHelper(this);

        mapHolidays = db.getAllHoliday();
        mapMemos = db.getAllMemo();
        mapOverTime = db.getAllOverTime();
        mapWorkDay = db.getAllWorkDay();
        mapWorkInfo = db.getAllWorkCategory();
        workInfoList = createWorkArrayList();

        user = db.getUser();

        cal_fg = new CalendarFragment();
        com_fg = new CommunityListFragment();
        more_fg = new MoreFragment();
    }

    private void resetView() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fTab_main_calendar, cal_fg, "f_cal").commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.fTab_main_community, com_fg, "f_com").commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.fTab_main_more, more_fg, "f_more").commit();
    }

    private void createTap() {
        if (fTabHost != null) {
            fTabHost.setup(getApplicationContext(), getSupportFragmentManager(), R.id.fTab_main_menu);
            fTabHost.getTabWidget().setBackgroundColor(Color.parseColor("#52b3d9"));

            TabHost.TabSpec tabSpec1 = fTabHost.newTabSpec("Tab1").setContent(R.id.fTab_main_calendar)
                    .setIndicator(getTabIndicator(fTabHost.getContext(), R.drawable.calendar_menu_icon));
            TabHost.TabSpec tabSpec2 = fTabHost.newTabSpec("Tab2").setContent(R.id.fTab_main_community)
                    .setIndicator(getTabIndicator(fTabHost.getContext(), R.drawable.community_menu_icon));
            TabHost.TabSpec tabSpec3 = fTabHost.newTabSpec("Tab3").setContent(R.id.fTab_main_more)
                    .setIndicator(getTabIndicator(fTabHost.getContext(), R.drawable.more_menu_icon));

            fTabHost.addTab(tabSpec1);
            fTabHost.addTab(tabSpec2);
            fTabHost.addTab(tabSpec3);
        }
    }

    private View getTabIndicator(Context context, int icon) {
        View view = LayoutInflater.from(context).inflate(R.layout.tab_layout, null);
        ImageView iv = view.findViewById(R.id.tab_img);
        iv.setImageResource(icon);
        return view;
    }


    // DB 연결 Interface 구현
    @Override
    public void inputMemo(String day, String content) {
        if (findMemo(day) == null) {
            if (content.length() > 0) {
                db.addData(day, content, TABLE_MEMO);
                mapMemos.put(day, content);
            }
        } else {
            if (content.length() > 0) {
                db.updateData(day, content, TABLE_MEMO);
                mapMemos.put(day, content);
            } else {
                db.deleteData(day, TABLE_MEMO);
                mapMemos.put(day, "");
            }
        }
    }


    @Override
    public List<String> findHoliday(String selectDay) {
        return mapHolidays.get(selectDay);
    }

    @Override
    public String findMemo(String selectDay) {
        return mapMemos.get(selectDay);
    }

    @Override
    public WorkDay findWorkDay(String selectDay) {
        return mapWorkDay.get(selectDay);
    }

    @Override
    public int findOverTime(String selectDay) {
        if (mapOverTime.containsKey(selectDay)) {
            return mapOverTime.get(selectDay);
        } else
            return 0;
    }

    @Override
    public User findUser() {
        return user;
    }

    @Override
    public WorkInfo findWorkInfoById(Integer workId) {
        if (workId == 0) return null;
        else return mapWorkInfo.get(workId);
    }

    @Override
    public void updateUserWork(ArrayList<WorkInfo> workPatternList, ArrayList<WorkInfo> workInfoList, String selectFirstDay) {
        int workId = 1;
        Map<String, Integer> mapWorkInfo = new HashMap<>();
        StringBuffer value = new StringBuffer();

        for (WorkInfo workInfo : workInfoList) {
            workInfo.setId(workId);
            mapWorkInfo.put(workInfo.getName(), workId);
            workId++;
        }

        for (WorkInfo workInfo : workPatternList) {
            value.append(String.valueOf(mapWorkInfo.get(workInfo.getName())));
            value.append(";");
        }

        value.deleteCharAt(value.length() - 1);
        db.updateUser_Work(value.toString(), workInfoList, selectFirstDay);
    }

    @Override
    public void onBackPressed() {
        // 현재 켜져있는 Fragment 들 중 최근에 생성된 Fragment 를 가져옴
        Fragment nowFragment = getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1);

        // 이후에 정말 닫을 것인지 묻는 Dialog 붙을 예정
        if ((nowFragment instanceof CalendarContentFragment) ||
                (nowFragment instanceof UserSettingFragment) ||
                (nowFragment instanceof CommunityContentFragment) ||
                (nowFragment instanceof CommunityBoardWrite) ||
                (nowFragment instanceof CommunityLogin)) {
            getSupportFragmentManager().popBackStack();
        } else {
            createAlertDialog();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        finishAffinity();
        System.runFinalization();
        System.exit(0);
    }

    private void createAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("종료하시겠습니까?");
        builder.setPositiveButton("예", (dialogInterface, i) -> {
            finishAffinity();
            System.runFinalization();
            System.exit(0);
        });
        builder.setNegativeButton("취소", (dialogInterface, i) -> {
        });
        builder.show();
    }

    @Override
    public void Restart_App() {
        PackageManager packageManager = getApplicationContext().getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(getApplicationContext().getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        getApplicationContext().startActivity(mainIntent);
        System.exit(0);
    }

    @Override
    public ArrayList<WorkInfo> getArrayWorkInfo() {
        return this.workInfoList;
    }

    private ArrayList<WorkInfo> createWorkArrayList() {
        ArrayList<WorkInfo> workInfoList = new ArrayList<>();
        for (Map.Entry<Integer, WorkInfo> entry : mapWorkInfo.entrySet()) {
            workInfoList.add(new WorkInfo(entry.getKey(), entry.getValue().getName(), entry.getValue().getColor()));
        }
        return workInfoList;
    }

    @Override
    public void inputOverTime(String selectDay, int overTimeInput) {
        if (findOverTime(selectDay) == 0 && overTimeInput != 0) {
            db.addData(selectDay, overTimeInput, TABLE_OVERTIME);
            mapOverTime.put(selectDay, overTimeInput);
        } else if (overTimeInput != 0) {
            db.updateData(selectDay, overTimeInput, TABLE_OVERTIME);
            mapOverTime.put(selectDay, overTimeInput);
        } else {
            db.deleteData(selectDay, overTimeInput);
            mapOverTime.put(selectDay, 0);
        }
    }

    @Override
    public void inputWorkDay(String day, WorkDay workDay) {
        WorkDay inputWork = findWorkDay(day);
        if (inputWork == null && workDay.getWorkInfoId() != 0) {
            db.addWorkDay(day, workDay);
            mapWorkDay.put(day, workDay);
        } else if (workDay.getWorkInfoId() != 0) {
            db.updateWorkDay(day, workDay);
            mapWorkDay.put(day, workDay);
        } else {
            db.deleteData(day, TABLE_WORK);
            mapWorkDay.put(day, null);
        }
    }

    @SuppressLint("StaticFieldLeak")
    class MainAsyncTask extends AsyncTask<Void, Void, Void> {
        private MainLoadingDialog loadingDialog;

        @Override
        protected Void doInBackground(Void... voids) {
            resetObject();
            resetView();
            return null;
        }

        @Override
        protected void onPreExecute() {
            loadingDialog = new MainLoadingDialog(MainActivity.this);
            loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            loadingDialog.setCanceledOnTouchOutside(false);
            loadingDialog.show();

            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void v) {
            createTap();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            loadingDialog.dismiss();
            super.onPreExecute();
            this.cancel(true);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }
}