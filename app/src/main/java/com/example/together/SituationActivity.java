package com.example.together;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.text.DecimalFormat;

public class SituationActivity extends AppCompatActivity {

    //블루투스
    static final int REQUEST_ENABLE_BT = 10;
    int mPairedDeviceCount = 0;
    Set<BluetoothDevice> mDevices;

    // 블루투스 모듈을 사용하기 위한 오브젝트트
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mRemoteDevice;
    BluetoothSocket mSocket = null;

    OutputStream mOutputStream = null; //아웃풋스트림
    InputStream mInputStream = null; //인풋 스트림

    String mStrDelimiter = "\n";
    char mCharDelimiter = '\n';

    Thread mWorkerThread = null;
    byte[] readBuffer;
    int readBufferPosition;

    TextView mTextReceive;
    EditText mEditSend;
    Button mButtonSend;

    //시간
    TextView nosmokingText, savetimeText, savemoneyText;

    static String year, month, day, hour, minute;
    static String smokingtime, smokingnumber, smokingvalue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_situation);

        TextView exampleText = (TextView)findViewById(R.id.exampleText);

        SharedPreferences yearpref = getSharedPreferences("Time", Activity.MODE_PRIVATE);
        year = yearpref.getString("year", "error");

        SharedPreferences monthpref = getSharedPreferences("Time", Activity.MODE_PRIVATE);
        month = monthpref.getString("month", "error");

        SharedPreferences daypref = getSharedPreferences("Time", Activity.MODE_PRIVATE);
        day = daypref.getString("day", "error");

        SharedPreferences hourpref = getSharedPreferences("Time", Activity.MODE_PRIVATE);
        hour = daypref.getString("hour", "error");

        SharedPreferences minutepref = getSharedPreferences("Time", Activity.MODE_PRIVATE);
        minute = daypref.getString("minute", "error");

        SharedPreferences smokingtimepref = getSharedPreferences("Time", Activity.MODE_PRIVATE);
        smokingtime = smokingtimepref.getString("smokingtime", "error");

        SharedPreferences smokingnumberpref = getSharedPreferences("Time", Activity.MODE_PRIVATE);
        smokingnumber = smokingnumberpref.getString("smokingnumber", "error");

        SharedPreferences smokingvaluepref = getSharedPreferences("Time", Activity.MODE_PRIVATE);
        smokingvalue = smokingvaluepref.getString("smokingvalue", "error");


        mTextReceive = (TextView)findViewById(R.id.receiveString);
        checkBluetooth();

        nosmokingText = (TextView)findViewById(R.id.smokingstopTime);
        savetimeText = (TextView)findViewById(R.id.saveTimeText);
        savemoneyText = (TextView)findViewById(R.id.savemoneyText);

        final Button situationButton = (Button)findViewById(R.id.situationButton);
        final Button rankingButton = (Button)findViewById(R.id.rankingButton);
        final Button diaryButton = (Button)findViewById(R.id.diaryButton);
        final Button settingButton = (Button)findViewById(R.id.settingButton);
        final LinearLayout notice = (LinearLayout) findViewById(R.id.notice);

        situationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SituationActivity.this, SituationActivity.class);
                SituationActivity.this.startActivity(intent);
                finish();
            }
        });

        rankingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SituationActivity.this, RankingActivity.class);
                SituationActivity.this.startActivity(intent);
                finish();
            }
        });

        diaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SituationActivity.this, DiaryActivity.class);
                SituationActivity.this.startActivity(intent);
                finish();

            }
        });

        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SituationActivity.this, SettingActivity.class);
                SituationActivity.this.startActivity(intent);
                finish();
            }
        });

    }

    public static String Comma_won(String junsu) {
        int inValues = Integer.parseInt(junsu);
        DecimalFormat Commas = new DecimalFormat("#,###");
        String result_int = (String)Commas.format(inValues);
        return result_int;
    }


    // 금연한 시간 구하기
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            updateThread();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Thread myThread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        handler.sendMessage(handler.obtainMessage());
                        Thread.sleep(1000);
                    } catch (Throwable t) {
                    }
                }
            }
        });
        myThread.start();
    }

    private void updateThread() {
        // 현재 날짜&시간 구하기
        Calendar today = Calendar.getInstance();
        // 시작 날짜&시간
        Calendar startday = Calendar.getInstance();
        startday.set(Integer.parseInt(year), Integer.parseInt(month)-1, Integer.parseInt(day), Integer.parseInt(hour), Integer.parseInt(minute)+1, 0);

        // 시간 차이 값을 초단위로 계산
        long gapSec = (today.getTimeInMillis() - startday.getTimeInMillis()) / 1000;

        // 지나간 Day 값 계산
        long gapDay = gapSec / (60 * 60 * 24);
        // 지나간 Hour 값 계산
        gapSec -= gapDay * (60 * 60 * 24);
        long gapHour = gapSec / (60 * 60);
        // 지나간 Min 값 계산
        gapSec -= gapHour * (60 * 60);
        long gapMin = gapSec / 60;
        // 지나간 Sec 값 계산
        gapSec -= gapMin * 60;



        String strResult = (gapDay + "일 " + gapHour + "시 " + gapMin + "분 " + gapSec + "초");
        nosmokingText.setText(strResult);

        //절약시간 날짜
        long savesec = gapDay * (Integer.parseInt(smokingtime) * 60 * Integer.parseInt(smokingnumber));

        //절약시간 구하기

        long savehour = savesec / (60 * 60);
        savesec -= savehour * (60 * 60);
        long savemin = savesec / (60);

        savetimeText.setText(savehour + "시간 " + savemin + "분 ");

        //절약금액
        long savemoney = gapDay * ((Integer.parseInt(smokingvalue) / 20) * Integer.parseInt(smokingnumber));

        String koreawon = Comma_won(String.valueOf(savemoney));

        savemoneyText.setText("￦" + koreawon);

        long manwon = savemoney / 10000;
        savemoney -= manwon * 10000;




/*
        GregorianCalendar calendar = new GregorianCalendar();

        int year = calendar.get(GregorianCalendar.YEAR)-1;
        int month=calendar.get(GregorianCalendar.MONTH)+1;
        int today=calendar.get(GregorianCalendar.DAY_OF_MONTH);
        int hour = calendar.get(GregorianCalendar.HOUR)-11;
        int min=calendar.get(GregorianCalendar.MINUTE)-48;
        int sec=calendar.get(GregorianCalendar.SECOND);

        int totalsec = year
*/

      //  nosmokingText.setText(year +"년 "+ month +"월 "+today +"일 "+ hour +"시 "+ min +"분 "+ sec + "초");

    }

    // 블루투스 연결할 수 있는 디바이스 리스트
    BluetoothDevice getDeviceFromBondedList(String name){
        BluetoothDevice selectDevice = null;

        for(BluetoothDevice device : mDevices){
            if(name.equals(device.getName())){
                selectDevice = device;
                break;
            }
        }
        return selectDevice;
    }

    void sendData(String msg) {
        msg += mStrDelimiter;  // 문자열 종료표시 (\n)
        try{
            // getBytes() : String을 byte로 변환
            // OutputStream.write : 데이터를 쓸때는 write(byte[]) 메소드를 사용함. byte[] 안에 있는 데이터를 한번에 기록해 준다.
            mOutputStream.write(msg.getBytes());  // 문자열 전송.
        }catch(Exception e) {  // 문자열 전송 도중 오류가 발생한 경우
            Toast.makeText(getApplicationContext(), "데이터 전송중 오류가 발생", Toast.LENGTH_LONG).show();
            finish();  // App 종료
        }
    }

    // 디바이스랑 연결했을 때의 메소드..
    void connectToSelectedDevices(String selectDeviceName){
        mRemoteDevice = getDeviceFromBondedList(selectDeviceName);

        UUID uuid = java.util.UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        try{

            mSocket = mRemoteDevice.createRfcommSocketToServiceRecord(uuid);
            mSocket.connect();

            mOutputStream = mSocket.getOutputStream();
            mInputStream = mSocket.getInputStream();

            beginListenForData();

        }catch(Exception e){
            Toast.makeText(getApplicationContext(), "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    void beginListenForData(){

        final Handler handler = new Handler();

        readBufferPosition = 0;
        readBuffer = new byte[1024];

        mWorkerThread = new Thread(new Runnable(){
            @Override
            public void run(){
                while(!Thread.currentThread().isInterrupted()){
                    try{
                        int byteAvailable = mInputStream.available();
                        if(byteAvailable > 0){
                            byte[] packetBytes = new byte[byteAvailable];
                            mInputStream.read(packetBytes);

                            for(int i=0; i<byteAvailable; i++){
                                byte b = packetBytes[i];
                                if(b == mCharDelimiter){
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0 , encodedBytes.length);

                                    final String data = new String(encodedBytes, "UTF-8"); //UTF-8은 한글을 만들기 위해서 하였다.
                                    readBufferPosition = 0;

                                    handler.post(new Runnable(){
                                        @Override
                                        public void run(){
                                            mTextReceive.setText(data);
                                            //mEditReceive.setText(mEditReceive.getText().toString() + data + mStrDelimiter);
                                        }
                                    });


                                }
                                else{
                                    readBuffer[readBufferPosition++] = b;

                                }
                            }
                        }
                    }catch(Exception e){
                        Toast.makeText(getApplicationContext(), "데이터 수신 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            }
        });
        mWorkerThread.start();
    }

    void selectDevice() {
        mDevices = mBluetoothAdapter.getBondedDevices();
        mPairedDeviceCount = mDevices.size();

        if (mPairedDeviceCount == 0) {
            //  페어링 된 장치가 없는 경우
            Toast.makeText(getApplicationContext(), "페어링된 장치가 없습니다.", Toast.LENGTH_LONG).show();
            finish();    // 어플리케이션 종료
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("블루투스 장치 선택");


        // 페어링 된 블루투스 장치의 이름 목록 작성
        List<String> listItems = new ArrayList<String>();
        for (BluetoothDevice device : mDevices) {
            listItems.add(device.getName());
        }
        listItems.add("취소");    // 취소 항목 추가

        final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);

        listItems.toArray(new CharSequence[listItems.size()]);

        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if (item == mPairedDeviceCount) {
                    // 연결할 장치를 선택하지 않고 '취소'를 누른 경우
                    finish();
                } else {
                    // 연결할 장치를 선택한 경우
                    // 선택한 장치와 연결을 시도함
                    connectToSelectedDevices(items[item].toString());
                }
            }
        });


        builder.setCancelable(false);    // 뒤로 가기 버튼 사용 금지
        AlertDialog alert = builder.create();
        alert.show();
    }


    void checkBluetooth(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // 블루투스를 지원하지 않는 단말 표시
        if(mBluetoothAdapter == null){
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();

        }
        else{
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                // Otherwise, setup the chat session
            }
            else {
                selectDevice();
            }
        }
    }

    @Override
    protected void onDestroy(){
        try{
            mWorkerThread.interrupt();
            mInputStream.close();
            mSocket.close();
        }catch(Exception e){}
        super.onDestroy();
    }

}
