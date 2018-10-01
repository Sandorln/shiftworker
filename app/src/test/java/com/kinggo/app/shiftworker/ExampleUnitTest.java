package com.kinggo.app.shiftworker;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void 달력테스트() {

        Calendar calendar1 = new GregorianCalendar(Locale.KOREA);
        calendar1.setTime(new Date());
        calendar1.set(Calendar.DATE, 1);
        SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd");

        for (int i = 0; i < 25; i++) {
            System.out.println(fm.format(calendar1.getTime()));
            calendar1.add(Calendar.MONTH, 1);
        }

    }

    @Test
    public void 음력테스트() {
        SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd");

        Calendar calendar1 = new GregorianCalendar(Locale.KOREA);
        calendar1.setTime(new Date());
        calendar1.set(Calendar.DATE, 1);
        System.out.println(fm.format(calendar1.getTime()));

        Calendar calendar2 = Calendar.getInstance();

        calendar1.add(Calendar.DATE, 1);
        System.out.println(fm.format(calendar1.getTime()) + " / " + fm.format(calendar2.getTime()));


    }

    @Test
    public void 공휴일테스트() {
    }
}