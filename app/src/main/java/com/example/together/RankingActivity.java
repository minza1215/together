package com.example.together;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class RankingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        final Button situationButton = (Button)findViewById(R.id.situationButton);
        final Button rankingButton = (Button)findViewById(R.id.rankingButton);
        final Button diaryButton = (Button)findViewById(R.id.diaryButton);
        final Button settingButton = (Button)findViewById(R.id.settingButton);
        final LinearLayout notice = (LinearLayout) findViewById(R.id.notice);

        situationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RankingActivity.this, SituationActivity.class);
                RankingActivity.this.startActivity(intent);
                finish();
            }
        });

        rankingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RankingActivity.this, RankingActivity.class);
                RankingActivity.this.startActivity(intent);
                finish();
            }
        });

        diaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RankingActivity.this, DiaryActivity.class);
                RankingActivity.this.startActivity(intent);
                finish();

            }
        });

        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RankingActivity.this, SettingActivity.class);
                RankingActivity.this.startActivity(intent);
                finish();
            }
        });
    }
}
