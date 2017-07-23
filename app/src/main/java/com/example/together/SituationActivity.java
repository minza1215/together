package com.example.together;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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

    // 블루투스 모듈을 사용하기 위한 오브젝트
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

    //담배 시간 및 절약금액
    TextView nosmokingText, savetimeText, savemoneyText;

    static String year, month, day, hour, minute;
    static String smokingtime, smokingnumber, smokingvalue;

    static String dataT; // 센서 값 받아들이는 변수
    static float dataA;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_situation);


        TextView exampleText = (TextView) findViewById(R.id.exampleText);

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


        mTextReceive = (TextView) findViewById(R.id.receiveString);
        checkBluetooth();

        nosmokingText = (TextView) findViewById(R.id.smokingstopTime);
        savetimeText = (TextView) findViewById(R.id.saveTimeText);
        savemoneyText = (TextView) findViewById(R.id.savemoneyText);

        final Button situationButton = (Button) findViewById(R.id.situationButton);
        final Button rankingButton = (Button) findViewById(R.id.rankingButton);
        final Button diaryButton = (Button) findViewById(R.id.diaryButton);
        final Button settingButton = (Button) findViewById(R.id.settingButton);
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

        checkDangerousPermissions();
    }

    //절약금액 한국화폐 단위로 바꾸는 메소드
    public static String Comma_won(String junsu) {
        int inValues = Integer.parseInt(junsu);
        DecimalFormat Commas = new DecimalFormat("#,###");
        String result_int = (String) Commas.format(inValues);
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
                        Thread.sleep(1000);     //센서값 바뀌는 속도 조절 값.
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
        startday.set(Integer.parseInt(year), Integer.parseInt(month) - 1, Integer.parseInt(day), Integer.parseInt(hour), Integer.parseInt(minute) + 1, 0);

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

    }

    // 블루투스 연결할 수 있는 디바이스 리스트
    BluetoothDevice getDeviceFromBondedList(String name) {
        BluetoothDevice selectDevice = null;

        for (BluetoothDevice device : mDevices) {
            if (name.equals(device.getName())) {
                selectDevice = device;
                break;
            }
        }
        return selectDevice;
    }

    void sendData(String msg) {
        msg += mStrDelimiter;  // 문자열 종료표시 (\n)
        try {
            // getBytes() : String을 byte로 변환
            // OutputStream.write : 데이터를 쓸때는 write(byte[]) 메소드를 사용함. byte[] 안에 있는 데이터를 한번에 기록해 준다.
            mOutputStream.write(msg.getBytes());  // 문자열 전송.
        } catch (Exception e) {  // 문자열 전송 도중 오류가 발생한 경우
            Toast.makeText(getApplicationContext(), "데이터 전송중 오류가 발생", Toast.LENGTH_LONG).show();
            finish();  // App 종료
        }
    } //아두이노 한테 보내는 메소드인데 노 필요 일단은 주석처리..

    // 디바이스랑 연결했을 때의 메소드..
    void connectToSelectedDevices(String selectDeviceName) {
        mRemoteDevice = getDeviceFromBondedList(selectDeviceName);

        UUID uuid = java.util.UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        try {

            mSocket = mRemoteDevice.createRfcommSocketToServiceRecord(uuid);
            mSocket.connect();

            mOutputStream = mSocket.getOutputStream();
            mInputStream = mSocket.getInputStream();

            beginListenForData();

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    void beginListenForData() {

        final Handler handler = new Handler();

        readBufferPosition = 0;
        readBuffer = new byte[1024];

        mWorkerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        int byteAvailable = mInputStream.available();
                        if (byteAvailable > 0) {
                            byte[] packetBytes = new byte[byteAvailable];
                            mInputStream.read(packetBytes);

                            for (int i = 0; i < byteAvailable; i++) {
                                byte b = packetBytes[i];
                                if (b == mCharDelimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);

                                    dataT = new String(encodedBytes , "UTF-8"); //UTF-8은 한글을 만들기 위해서 하였다.
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mTextReceive.setText(dataT);

                                            // 센서 값을 통한 푸쉬알람
                                            if (Float.parseFloat(dataT) > 50) {

                                                String ContentTitle = "Together Push Alarm";
                                                String ContentText = "푸시알람";

                                                //------------------------------------------------------------------
                                                // Create Notification object.
                                                Intent intent = new Intent(SituationActivity.this, SituationActivity.class);
                                                PendingIntent pendingIntent = PendingIntent.getActivity(SituationActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


                                                Notification.Builder notiBuilder = new Notification.Builder(getApplicationContext());
                                                notiBuilder.setSmallIcon(R.drawable.logo);
                                                notiBuilder.setContentTitle(ContentTitle);
                                                notiBuilder.setContentText(ContentText);
                                                notiBuilder.setContentIntent(pendingIntent);

                                                Notification noti = notiBuilder.build();
                                                noti.defaults = Notification.DEFAULT_SOUND;

                                                //알림 소리를 한번만 내도록
                                                noti.flags = Notification.FLAG_ONLY_ALERT_ONCE;

                                                //확인하면 자동으로 알림이 제거 되도록
                                                noti.flags = Notification.FLAG_AUTO_CANCEL;


                                                //------------------------------------------------------------------
                                                // Notify

                                                NotificationManager notiManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                                notiManager.notify(0, noti);

                                                Toast.makeText(SituationActivity.this, "담배 나빠!", Toast.LENGTH_LONG).show();

                                                startLocationService(dataT); //

                                            }
                                            //mEditReceive.setText(mEditReceive.getText().toString() + data + mStrDelimiter);
                                        }
                                    });


                                } else {
                                    readBuffer[readBufferPosition++] = b;

                                }
                            }
                        }
                    } catch (Exception e) {
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


    void checkBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // 블루투스를 지원하지 않는 단말 표시
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();

        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                // Otherwise, setup the chat session
            } else {
                selectDevice();
            }
        }
    }

    @Override
    protected void onDestroy() {
        try {
            mWorkerThread.interrupt();
            mInputStream.close();
            mSocket.close();
        } catch (Exception e) {
        }
        super.onDestroy();
    }

    private void checkDangerousPermissions() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (int i = 0; i < permissions.length; i++) {
            permissionCheck = ContextCompat.checkSelfPermission(this, permissions[i]);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                break;
            }
        }

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "권한 있음", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "권한 없음", Toast.LENGTH_LONG).show();

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                Toast.makeText(this, "권한 설명 필요함.", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, permissions[i] + " 권한이 승인됨.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, permissions[i] + " 권한이 승인되지 않음.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    /**
     * 위치 정보 확인을 위해 정의한 메소드
     */
    private void startLocationService(String dataT) {
        // 위치 관리자 객체 참조
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // 위치 정보를 받을 리스너 생성
        GPSListener gpsListener = new GPSListener();

        long minTime = 10000;
        float minDistance = 0;
        dataA = Float.parseFloat(dataT);

            try {
                    // GPS를 이용한 위치 요청
                    manager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            minTime,
                            minDistance,
                            gpsListener);

                    // 네트워크를 이용한 위치 요청
                    manager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            minTime,
                            minDistance,
                            gpsListener);

                // 위치 확인이 안되는 경우에도 최근에 확인된 위치 정보 먼저 확인
                /*Location lastLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocation != null) {
                    Double latitude = lastLocation.getLatitude();
                    Double longitude = lastLocation.getLongitude();

                    //Toast.makeText(getApplicationContext(), "Last Known Location : " + "Latitude : " + latitude + "\nLongitude:" + longitude, Toast.LENGTH_LONG).show();
                }*/
            } catch(SecurityException ex) {
                ex.printStackTrace();
            }

        //Toast.makeText(getApplicationContext(), "위치 확인이 시작되었습니다. 로그를 확인하세요.", Toast.LENGTH_SHORT).show();

    }

    /**
     * 리스너 클래스 정의
     */
    public class GPSListener extends SituationActivity implements LocationListener {
        /**
         * 위치 정보가 확인될 때 자동 호출되는 메소드
         */

        float data = SituationActivity.dataA; //센서값 받아오는 변수..

        //위치 정보 판별해서 DB로 전송해주는 메소드
        public void onLocationChanged(Location location) {

                Integer LocationRange = 0;
                //String NoRange = "8989";
                Double Latitude = location.getLatitude();
                Double Longitude = location.getLongitude();
            /*if(Float.parseFloat(dataT) > 70) {
                String msg = "Latitude : " + Latitude + "\nLongitude:" + Longitude;
                Log.i("GPSListener", msg);

                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }*/

                Response.Listener<String> responseListener = new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            if(success){
                                AlertDialog.Builder builder = new AlertDialog.Builder(SituationActivity.this);
                            /*dialog = builder.setMessage("위치를 DB에 보냅니다. ")
                                    .setPositiveButton("확인", null)
                                    .create();
                            dialog.show();*/
                            }else{
                                AlertDialog.Builder builder = new AlertDialog.Builder(SituationActivity.this);
                            /*dialog = builder.setMessage("위치를 DB에 보낼 수 없습니다. ")
                                    .setNegativeButton("확인", null)
                                    .create();
                            dialog.show();*/
                            }
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                };

                // 한경대학교 위도 64등분
                double [] wi = {37.009617,37.00968361,37.00975022,37.00981683,
                        37.00988344,37.00995005,37.01001666,37.01008327,
                        37.01014988,37.01021648,37.01028309,37.0103497,
                        37.01041631,37.01048292,37.01054953,37.01061614,
                        37.01068275,37.01074936,37.01081597,37.01088258,
                        37.01094919,37.0110158,37.01108241,	37.01114902,
                        37.01121562,37.01128223,37.01134884,37.01141545,
                        37.01148206,37.01154867,37.01161528,37.01168189,
                        37.0117485,37.01181511,	37.01188172,37.01194833,
                        37.01201494,37.01208155,37.01214816,37.01221477,
                        37.01228137,37.01234798,37.01241459,37.0124812,
                        37.01254781,37.01261442,37.01268103,37.01274764,
                        37.01281425,37.01288086,37.01294747,37.01301408,
                        37.01308069,37.0131473,	37.01321391,37.01328052,
                        37.01334712,37.01341373,37.01348034,37.01354695,
                        37.01361356,37.01368017,37.01374678,37.01381339,
                        37.01388
                };

                // 한경대학교 경도 64등분
                double [] gy = {127.260058,	127.2601549, 127.2602517, 127.2603486,
                        127.2604454,127.2605423,127.2606392,127.260736,
                        127.2608329,127.2609297,127.2610266,127.2611235,
                        127.2612203,127.2613172,127.261414,	127.2615109,
                        127.2616078,127.2617046,127.2618015,127.2618983,
                        127.2619952,127.262092,127.2621889,	127.2622858,
                        127.2623826,127.2624795,127.2625763,127.2626732,
                        127.2627701,127.2628669,127.2629638,127.2630606,
                        127.2631575,127.2632544,127.2633512,127.2634481,
                        127.2635449,127.2636418,127.2637387,127.2638355,
                        127.2639324,127.2640292,127.2641261,127.264223,
                        127.2643198,127.2644167,127.2645135,127.2646104,
                        127.2647073,127.2648041,127.264901,	127.2649978,
                        127.2650947,127.2651915,127.2652884,127.2653853,
                        127.2654821,127.265579,	127.2656758,127.2657727,
                        127.2658696,127.2659664,127.2660633,127.2661601,
                        127.266257
                };

                for(int i = 0; i< wi.length; i++){
                    for(int j = 0; j< gy.length; j++){
                        if(Latitude > wi[i] && Latitude < (wi[i+1]) && Longitude > gy[j] && Longitude < (gy[j+1])){
                            //System.out.println("구역은 " + LocationRange + " 입니다.");
                            if(data > 70) {
                                MapRequest mapRequest = new MapRequest(LocationRange + "", Latitude + "", Longitude + "", responseListener);
                                RequestQueue queue = Volley.newRequestQueue(SituationActivity.this);
                                queue.add(mapRequest);
                                data = 0; // 이걸 해줘야 센서의 안정수치로 들어가면 DB에 전송이 안된다.
                            }
                        }
                        LocationRange++;
                    }
                }



            /*MapRequest mapRequest = new MapRequest(NoRange ,Latitude+"", Longitude+"", responseListener);
            RequestQueue queue = Volley.newRequestQueue(SituationActivity.this);
            queue.add(mapRequest);*/
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }

}


