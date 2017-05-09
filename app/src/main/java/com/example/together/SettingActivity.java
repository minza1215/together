package com.example.together;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class SettingActivity extends AppCompatActivity {

    String setyear, setmonth, setday;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        final Button situationButton = (Button)findViewById(R.id.situationButton);
        final Button rankingButton = (Button)findViewById(R.id.rankingButton);
        final Button diaryButton = (Button)findViewById(R.id.diaryButton);
        final Button settingButton = (Button)findViewById(R.id.settingButton);
        final LinearLayout notice = (LinearLayout) findViewById(R.id.notice);

        //Save Button
        final Button saveButton = (Button)findViewById(R.id.saveButton);

        final EditText yearText = (EditText)findViewById(R.id.yearText);
        final EditText monthText = (EditText)findViewById(R.id.monthText);
        final EditText dayText = (EditText)findViewById(R.id.dayText);
        final EditText hourText = (EditText)findViewById(R.id.hourText);
        final EditText minuteText = (EditText)findViewById(R.id.minuteText);

        final EditText smokingtimeText = (EditText)findViewById(R.id.smokingtimeText);
        final EditText smokingnumberText = (EditText)findViewById(R.id.smokingnumberText);
        final EditText smokingvalueText = (EditText)findViewById(R.id.smokingvalueText);

        situationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, SituationActivity.class);
                SettingActivity.this.startActivity(intent);
                finish();
            }
        });

        rankingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, RankingActivity.class);
                SettingActivity.this.startActivity(intent);
                finish();
            }
        });

        diaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, DiaryActivity.class);
                SettingActivity.this.startActivity(intent);
                finish();

            }
        });

        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, SettingActivity.class);
                SettingActivity.this.startActivity(intent);
                finish();
            }
        });


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //year
                Editable year = yearText.getText();
                SharedPreferences yearpref = getSharedPreferences("Time", Activity.MODE_PRIVATE);
                SharedPreferences.Editor yeareditor = yearpref.edit();
                yeareditor.putString("year", String.valueOf(year));
                yeareditor.commit();

                //month
                Editable month = monthText.getText();
                SharedPreferences monthpref = getSharedPreferences("Time", Activity.MODE_PRIVATE);
                SharedPreferences.Editor montheditor = monthpref.edit();
                montheditor.putString("month", String.valueOf(month));
                montheditor.commit();

                //day
                Editable day = dayText.getText();
                SharedPreferences daypref = getSharedPreferences("Time", Activity.MODE_PRIVATE);
                SharedPreferences.Editor dayeditor = daypref.edit();
                dayeditor.putString("day", String.valueOf(day));
                dayeditor.commit();

                //hour
                Editable hour = hourText.getText();
                SharedPreferences hourpref = getSharedPreferences("Time", Activity.MODE_PRIVATE);
                SharedPreferences.Editor houreditor = hourpref.edit();
                houreditor.putString("hour", String.valueOf(hour));
                houreditor.commit();

                //minute
                Editable minute = minuteText.getText();
                SharedPreferences minutepref = getSharedPreferences("Time", Activity.MODE_PRIVATE);
                SharedPreferences.Editor minuteeditor = daypref.edit();
                minuteeditor.putString("minute", String.valueOf(minute));
                minuteeditor.commit();

                //smokingtimetText
                Editable smokingtime = smokingtimeText.getText();
                SharedPreferences smokingtimepref = getSharedPreferences("Time", Activity.MODE_PRIVATE);
                SharedPreferences.Editor smokingtimeeditor = smokingtimepref.edit();
                smokingtimeeditor.putString("smokingtime", String.valueOf(smokingtime));
                smokingtimeeditor.commit();

                //smokingnumberText
                Editable smokingnumber = smokingnumberText.getText();
                SharedPreferences smokingnumberpref = getSharedPreferences("Time", Activity.MODE_PRIVATE);
                SharedPreferences.Editor smokingnumbereditor = smokingnumberpref.edit();
                smokingnumbereditor.putString("smokingnumber", String.valueOf(smokingnumber));
                smokingnumbereditor.commit();

                //smokingnumberText
                Editable smokingvalue = smokingvalueText.getText();
                SharedPreferences smokingvaluepref = getSharedPreferences("Time", Activity.MODE_PRIVATE);
                SharedPreferences.Editor smokingvalueeditor = smokingvaluepref.edit();
                smokingvalueeditor.putString("smokingvalue", String.valueOf(smokingvalue));
                smokingvalueeditor.commit();


            }
        });

    }

}
