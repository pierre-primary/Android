package anuode.apitest;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CalendarView;

import com.anuode.util.Logs;

/**
 * Created by xudong on 2015/4/22.
 */
public class CalenderAc extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calender);
        CalendarView calendarView =(CalendarView) findViewById(R.id.calender);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                Logs.w(" >> ","day:" +dayOfMonth + " month:" + month + " year:" + year);
            }
        });
    }
}
