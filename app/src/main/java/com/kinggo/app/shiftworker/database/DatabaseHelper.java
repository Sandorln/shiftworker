package com.kinggo.app.shiftworker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ibm.icu.util.ChineseCalendar;
import com.kinggo.app.shiftworker.view.calendar.entity.User;
import com.kinggo.app.shiftworker.view.calendar.entity.WorkDay;
import com.kinggo.app.shiftworker.view.calendar.entity.WorkInfo;
import com.kinggo.app.shiftworker.view.community.domain.UserInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "ShiftWorker";

    // 테이블 이름
    private final String TABLE_HOLIDAY = "holiday";
    private final String TABLE_MEMO = "memo";
    private final String TABLE_WORKDAY = "work";
    private final String TABLE_OVERTIME = "overtime";
    private final String TABLE_USER = "user";
    private final String TABLE_WORKINFO = "workInfo";

    // 테이블 내 컬럼 ( 공용 / user 제외 )
    private final String COLUMN_KEY = "day";                 // Primary key
    private final String COLUMN_CONTENT = "content";

    // User 테이블 내 컬럼
    private final String USER_ID = "id";                     // primary key
    private final String USER_WORKFIRSTDAY = "workfirstday"; // 근무 형태의 첫 시작일
    private final String USER_WORPATTERN = "workpattern";         // 근무 형태
    private final String USER_NAME = "username";
    private final String USER_PASSWORD = "password";
    private final String USER_TOKEN = "token";

    // WorkInfo 테이블 내 컬럼
    private final String WORKINFO_ID = "id";
    private final String WORKINFO_NAME = "name";
    private final String WORKINFO_COLOR = "color";

    // WorkDay 테이블 내 컬럼
    private final String WORKDAY_ID = "day";
    private final String WORKDAY_WORKINFOID = "workInfoId";
    private final String WORKDAY_WORKETC = "workETC";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createHolidayTable(db);
        createMemoTable(db);
        createWorkDayTable(db);
        createOverTimeTable(db);
        createUserTable(db);
        createWorkInfo(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVr, int newVr) {

        String DROP_TABLE_HOLIDAY =
                "DROP TABLE IF EXISTS " + TABLE_HOLIDAY;
        db.execSQL(DROP_TABLE_HOLIDAY);

        String DROP_TABLE_MEMO =
                "DROP TABLE IF EXISTS " + TABLE_MEMO;
        db.execSQL(DROP_TABLE_MEMO);

        String DROP_TABLE_WORK =
                "DROP TABLE IF EXISTS " + TABLE_WORKDAY;
        db.execSQL(DROP_TABLE_WORK);

        String DROP_TABLE_OVERTIME =
                "DROP TABLE IF EXISTS " + TABLE_OVERTIME;
        db.execSQL(DROP_TABLE_OVERTIME);

        onCreate(db);
    }

    // 테이블
    private void createHolidayTable(SQLiteDatabase db) {
        // 테이블 생성
        String CREATE_TABLE_HOLIDAY =
                "CREATE TABLE " + TABLE_HOLIDAY + "(" +
                        COLUMN_KEY + " TEXT NOT NULL, " +
                        COLUMN_CONTENT + " TEXT NOT NULL" +
                        ");";
        db.execSQL(CREATE_TABLE_HOLIDAY);

        // 내용 입력
        addHolidayContent(db);
    }

    private void createWorkInfo(SQLiteDatabase db) {
        // 테이블 생성
        String CREATE_TABLE_HOLIDAY =
                "CREATE TABLE " + TABLE_WORKINFO + "(" +
                        WORKINFO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        WORKINFO_NAME + " TEXT , " +
                        WORKINFO_COLOR + " TEXT " +
                        ");";
        db.execSQL(CREATE_TABLE_HOLIDAY);
        // 내용 입력
        addWorkCategory(db);
    }

    private void createMemoTable(SQLiteDatabase db) {
        String CREATE_TABLE_MEMO =
                "CREATE TABLE " + TABLE_MEMO + "(" +
                        COLUMN_KEY + " TEXT PRIMARY KEY, " +
                        COLUMN_CONTENT + " TEXT " +
                        ");";
        db.execSQL(CREATE_TABLE_MEMO);
    }

    private void createWorkDayTable(SQLiteDatabase db) {
        String CREATE_TABLE_WORK =
                "CREATE TABLE " + TABLE_WORKDAY + "(" +
                        WORKDAY_ID + " TEXT PRIMARY KEY, " +
                        WORKDAY_WORKINFOID + " INTEGER, " +
                        WORKDAY_WORKETC + " TEXT " +
                        ");";
        db.execSQL(CREATE_TABLE_WORK);
    }

    private void createOverTimeTable(SQLiteDatabase db) {
        String CREATE_TABLE_OVERTIME =
                "CREATE TABLE " + TABLE_OVERTIME + "(" +
                        COLUMN_KEY + " TEXT PRIMARY KEY, " +
                        COLUMN_CONTENT + " INTEGER " +
                        ");";
        db.execSQL(CREATE_TABLE_OVERTIME);
    }

    private void createUserTable(SQLiteDatabase db) {
        String CREATE_TABLE_USER =
                "CREATE TABLE " + TABLE_USER + "(" +
                        USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        USER_WORKFIRSTDAY + " TEXT ," +
                        USER_WORPATTERN + " TEXT ," +
                        USER_NAME + " TEXT ," +
                        USER_PASSWORD + " TEXT ," +
                        USER_TOKEN + " TEXT " +
                        ");";
        db.execSQL(CREATE_TABLE_USER);

        // 테스트용
        ContentValues values = new ContentValues();

        values.put(USER_ID, 0);
        values.put(USER_WORKFIRSTDAY, "20180101");
        values.put(USER_WORPATTERN, "1;1;1;1;3;3;2;2;2;2;3;3");

        db.insert(TABLE_USER, null, values);
    }

    // 공휴일 테이블에 값 입력
    private void addHolidayContent(SQLiteDatabase db) {

        final int FIRST_YEAR = 1950;
        final int LAST_YEAR = 2050;
        final int MAX_DAY = (LAST_YEAR - FIRST_YEAR) * 365;

        final String FIRST_DAY = "0101"; // 설날(음력) 첫 시작을 위해

        final Map<String, String> solarHolidays = makingSolarHoliday();
        final Map<String, String> lunarHolidays = makingLunarHoliday();

        ChineseCalendar cc = new ChineseCalendar();
        Calendar cal = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

        cal.set(Calendar.YEAR, FIRST_YEAR);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        cc.setTimeInMillis(cal.getTimeInMillis());

        for (int value = 0; value <= MAX_DAY; value++) {
            List<String> holidays = new ArrayList<>();

            String solarDay = sdf.format(cal.getTime());
            StringBuffer lunarDay = new StringBuffer();

            lunarDay.append(String.format("%04d", cc.get(ChineseCalendar.EXTENDED_YEAR) - 2637))
                    .append(String.format("%02d", cc.get(ChineseCalendar.MONTH) + 1))
                    .append(String.format("%02d", cc.get(ChineseCalendar.DAY_OF_MONTH)));

            String checkSolarHoliday = solarHolidays.get(solarDay.substring(4, 8));
            String checkLunarHoliday = lunarHolidays.get(lunarDay.substring(4, 8));


            if (checkSolarHoliday != null)
                holidays.add(checkSolarHoliday);

            if (checkLunarHoliday != null)
                holidays.add(checkLunarHoliday);

            // 만일 해당일에 공휴일이 있을 경우
            // holiday table 에 값 입력
            if (holidays.size() > 0) {
                for (String holiday : holidays) {
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_KEY, solarDay);
                    values.put(COLUMN_CONTENT, holiday);
                    db.insert(TABLE_HOLIDAY, null, values);
                }
            }

            // 설날 첫 시작 판별
            if (lunarDay.substring(4, 8).equals(FIRST_DAY)) {
                cal.add(Calendar.DATE, -1);

                // 음력 마지막 날을 양력으로 표시
                String lunaLastDay = sdf.format(cal.getTime());

                ContentValues values = new ContentValues();
                values.put(COLUMN_KEY, lunaLastDay);
                values.put(COLUMN_CONTENT, "설날");
                db.insert(TABLE_HOLIDAY, null, values);

                cal.add(Calendar.DATE, 1);
            }

            cal.add(Calendar.DATE, 1);
            cc.add(ChineseCalendar.DATE, 1);
        }
    }

    // 기본 근무 형태 값 입력
    private void addWorkCategory(SQLiteDatabase db) {

        ContentValues values = new ContentValues();
        try {
            values.put(WORKINFO_ID, 1);
            values.put(WORKINFO_NAME, "주간");
            values.put(WORKINFO_COLOR, "#FFf9bf3b");
            db.insert(TABLE_WORKINFO, null, values);
            values.clear();

            values.put(WORKINFO_ID, 2);
            values.put(WORKINFO_NAME, "야간");
            values.put(WORKINFO_COLOR, "#FF67809f");
            db.insert(TABLE_WORKINFO, null, values);
            values.clear();

            values.put(WORKINFO_ID, 3);
            values.put(WORKINFO_NAME, "휴무");
            values.put(WORKINFO_COLOR, "#FFf03434");
            db.insert(TABLE_WORKINFO, null, values);
            values.clear();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 각 테이블 리스트 가져오기
    public Map<String, List<String>> getAllHoliday() {
        Map<String, List<String>> mapHoliday = new HashMap<>();

        String SELECT_ALL = "SELECT * FROM " + TABLE_HOLIDAY;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_ALL, null);
        try {

            if (cursor.moveToFirst()) {
                do {

                    String day = cursor.getString(0);
                    String content = cursor.getString(1);
                    List<String> holidays = mapHoliday.get(day);

                    if (holidays == null)
                        holidays = new ArrayList<>();

                    holidays.add(content);
                    mapHoliday.put(day, holidays);

                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }

        return mapHoliday;
    }

    public Map<String, String> getAllMemo() {
        Map<String, String> mapMemo = new HashMap<>();

        String SELECT_ALL = "SELECT * FROM " + TABLE_MEMO;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_ALL, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    String day = cursor.getString(0);
                    String content = cursor.getString(1);

                    mapMemo.put(day, content);
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }

        return mapMemo;
    }

    public Map<String, WorkDay> getAllWorkDay() {
        Map<String, WorkDay> mapWork = new HashMap<>();

        String SELECT_ALL = "SELECT * FROM " + TABLE_WORKDAY;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_ALL, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    String day = cursor.getString(0);
                    mapWork.put(day, new WorkDay(cursor.getInt(1), cursor.getString(2)));
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }

        return mapWork;
    }

    public Map<String, Integer> getAllOverTime() {
        Map<String, Integer> mapOverTime = new HashMap<>();

        String SELECT_ALL = "SELECT * FROM " + TABLE_OVERTIME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_ALL, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    String day = cursor.getString(0);
                    int content = cursor.getInt(1);

                    mapOverTime.put(day, content);
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }

        return mapOverTime;
    }

    public Map<Integer, WorkInfo> getAllWorkCategory() {
        Map<Integer, WorkInfo> mapWorkCategory = new HashMap<>();
        String SELECT_ALL = "SELECT * FROM " + TABLE_WORKINFO;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_ALL, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    WorkInfo workInfo = new WorkInfo(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
                    mapWorkCategory.put(cursor.getInt(0), workInfo);
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return mapWorkCategory;
    }

    public User getUser() {
        User user = new User();
        String SELECT_ALL = "SELECT * FROM " + TABLE_USER + " WHERE id = 0";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_ALL, null);
        try {
            if (cursor.moveToFirst()) {
                user.setId(cursor.getInt(0));
                user.setWorkfirstday(cursor.getString(1));
                user.setWorkinfo(cursor.getString(2));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return user;
    }

    public UserInfo getUserInfo() {
        UserInfo userInfo = null;
        String SELECT_ALL = "SELECT username,password,token FROM " + TABLE_USER + " WHERE id = 0";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_ALL, null);
        try {
            if (cursor.moveToFirst()) {
                if (cursor.getString(0) != null && cursor.getString(1) != null)
                    userInfo = new UserInfo(cursor.getString(0), cursor.getString(1), cursor.getString(2));
                else
                    userInfo = new UserInfo("", "");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }

        return userInfo;
    }

    // insert | delete | update 관련 기능
    public void addData(String day, Object content, int tableNum) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            String tableName = null;
            values.put(COLUMN_KEY, day);

            switch (tableNum) {
                case 0:
                    values.put(COLUMN_CONTENT, (String) content);
                    tableName = TABLE_MEMO;
                    break;
                case 1:
                    values.put(COLUMN_CONTENT, (Integer) content);
                    tableName = TABLE_OVERTIME;
                    break;
            }
            db.insert(tableName, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public void updateData(String day, Object content, int tableNum) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            String tableName = null;

            switch (tableNum) {
                case 0:
                    values.put(COLUMN_CONTENT, (String) content);
                    tableName = TABLE_MEMO;
                    break;
                case 1:
                    values.put(COLUMN_CONTENT, (Integer) content);
                    tableName = TABLE_OVERTIME;
                    break;
                case 2:
                    values.put(COLUMN_CONTENT, (Integer) content);
                    tableName = TABLE_WORKDAY;
                    break;
            }

            if (tableName != null)
                db.update(tableName, values, "day=?", new String[]{day});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public void deleteData(String day, int tableNum) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            switch (tableNum) {
                case 0:
                    db.delete(TABLE_MEMO, "day=?", new String[]{day});
                    break;
                case 1:
                    db.delete(TABLE_OVERTIME, "day=?", new String[]{day});
                    break;
                case 2:
                    db.delete(TABLE_WORKDAY, "day=?", new String[]{day});
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public void addWorkInfo(Map<String, String> mapWorkInfo) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.delete(TABLE_WORKINFO, "", new String[]{});
            db.delete(TABLE_WORKDAY, "", new String[]{});

            for (Map.Entry<String, String> entry : mapWorkInfo.entrySet()) {
                ContentValues values = new ContentValues();
                values.put(WORKINFO_NAME, entry.getKey());
                values.put(WORKINFO_COLOR, entry.getValue());
                db.insert(TABLE_WORKINFO, null, values);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    // 고정 공휴일 목록
    private Map<String, String> makingSolarHoliday() {
        Map<String, String> solarHolidayMap = new HashMap<>();

        solarHolidayMap.put("0101", "신정");
        solarHolidayMap.put("0301", "삼일절");
        solarHolidayMap.put("0505", "어린이날");
        solarHolidayMap.put("0606", "현충일");
        solarHolidayMap.put("0815", "광복절");
        solarHolidayMap.put("1003", "개천절");
        solarHolidayMap.put("1009", "한글날");
        solarHolidayMap.put("1225", "성탄절");

        return solarHolidayMap;
    }

    private Map<String, String> makingLunarHoliday() {
        Map<String, String> lunarHolidayMap = new HashMap<>();

        lunarHolidayMap.put("0101", "설날");
        lunarHolidayMap.put("0102", "설날");
        lunarHolidayMap.put("0408", "석가탄신일");
        lunarHolidayMap.put("0814", "추석");
        lunarHolidayMap.put("0815", "추석");
        lunarHolidayMap.put("0816", "추석");

        return lunarHolidayMap;
    }

    public void addWorkDay(String day, WorkDay workDay) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(WORKDAY_ID, day);
            values.put(WORKDAY_WORKINFOID, workDay.getWorkInfoId());

            if (workDay.getWorkEtc() != null)
                values.put(WORKDAY_WORKETC, workDay.getWorkEtc());

            db.insert(TABLE_WORKDAY, null, values);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public void updateWorkDay(String day, WorkDay workDay) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(WORKDAY_WORKINFOID, workDay.getWorkInfoId());

            if (workDay.getWorkEtc() != null)
                values.put(WORKDAY_WORKETC, workDay.getWorkEtc());

            db.update(TABLE_WORKDAY, values, "day=?", new String[]{day});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public void updateUser_Login(UserInfo userInfo) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues user = new ContentValues();
            user.put(USER_NAME, userInfo.getUsername());
            user.put(USER_PASSWORD, userInfo.getPassword());
            user.put(USER_TOKEN, userInfo.getToken());
            db.update(TABLE_USER, user, "id = ?", new String[]{"0"});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateUser_Work(String userWorkPattern, ArrayList<WorkInfo> workInfoList, String selectFirstDay) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            // 이전에 있었던 WorkInfo / WorkDay 테이블 내 값 제거
            db.delete(TABLE_WORKINFO, "", new String[]{});
            db.delete(TABLE_WORKDAY, "workInfoId>?", new String[]{"0"});

            ContentValues user = new ContentValues();
            user.put(USER_WORPATTERN, userWorkPattern);
            user.put(USER_WORKFIRSTDAY, selectFirstDay);
            db.update(TABLE_USER, user, "id=?", new String[]{"0"});

            for (WorkInfo workInfo : workInfoList) {
                ContentValues values = new ContentValues();
                values.put(WORKINFO_ID, workInfo.getId());
                values.put(WORKINFO_NAME, workInfo.getName());
                values.put(WORKINFO_COLOR, workInfo.getColor());

                db.insert(TABLE_WORKINFO, null, values);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }
}