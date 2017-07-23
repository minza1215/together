package com.example.together;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RankingActivity extends AppCompatActivity {

    private ListView rankingListView;
    private RankingListAdapter adapter;
    private List<Ranking> rankingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        rankingListView = (ListView)findViewById(R.id.rankingListView);
        rankingList = new ArrayList<Ranking>();
        adapter = new RankingListAdapter(getApplicationContext(), rankingList);
        rankingListView.setAdapter(adapter);

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

        new BackgroundTask().execute();

    }

    class BackgroundTask extends AsyncTask<Void, Void, String>
    {
        String target;

        @Override
        protected void onPreExecute(){
            target = "http://13.124.142.75/RankingList.php";
        }

        @Override
        protected String doInBackground(Void... voids) {
            try{
                URL url = new URL(target);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String temp;
                StringBuilder stringBuilder = new StringBuilder();
                while((temp = bufferedReader.readLine()) != null){
                    stringBuilder.append(temp +"\n");
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return stringBuilder.toString().trim();
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onProgressUpdate(Void... values){
            super.onProgressUpdate();
        }

        @Override
        public void onPostExecute(String result){
            try{
                JSONObject jsonObject = new JSONObject(result);
                JSONArray jsonArray = jsonObject.getJSONArray("response");
                int count = 0;
                String userId, userName, userAlarmValue;
                while(count < jsonArray.length()){
                    JSONObject object = jsonArray.getJSONObject(count);
                    userId = object.getString("userID");
                    userName = object.getString("userName");
                    userAlarmValue = object.getString("userAlarmValue");
                    Ranking ranking = new Ranking(String.valueOf(count+1), userId , userName, userAlarmValue);
                    rankingList.add(ranking);
                    count++;
                }
                rankingListView.setAdapter(adapter);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


}
