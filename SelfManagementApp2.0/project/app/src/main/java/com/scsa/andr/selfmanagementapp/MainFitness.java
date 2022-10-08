package com.scsa.andr.selfmanagementapp;

import static com.scsa.andr.selfmanagementapp.MainToDo.churucnt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.scsa.andr.selfmanagementapp.databinding.FitnessMainBinding;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

@RequiresApi(api = Build.VERSION_CODES.O)
@SuppressLint("MissingPermission")
public class MainFitness extends AppCompatActivity {
    private static final String TAG = "MainActivity_SCSA";

    private BeaconManager beaconManager;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;

    private CheckPermission checkPermission;

    private static final int PERMISSION_REQUEST_CODE = 18;
    private String[] runtimePermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT
    };

    //유효 ID, Major, Minor가 필요
    //beacon basic에서 탐색한 결과 해당 비콘에 맞는 값으로 설정해야 함
    //2022-05-25 10:36:40.496 14830-14830/edu.jaen.android.beacon_basic D/MainActivity_SCSA: processResult: e2c56db5-dffb-48d2-b060-d0f5a71096e0
    //2022-05-25 10:36:40.496 14830-14830/edu.jaen.android.beacon_basic D/MainActivity_SCSA: processResult: 0
    //2022-05-25 10:36:40.497 14830-14830/edu.jaen.android.beacon_basic D/MainActivity_SCSA: processResult: 0
    private static final String BEACON_UUID = "e2c56db5-dffb-48d2-b060-d0f5a71096e0";
    private static final String BEACON_MAJOR = "0";
    private static final String BEACON_MINOR = "0";

    // Beacon의 Region 설정
    // 비교데이터들로, 설치 지역이 어딘지 판단하기 위한 데이터.
    //estimote : apple, eddystone : google
    private Region region = new Region("estimote"
            , Identifier.parse(BEACON_UUID)
            , Identifier.parse(BEACON_MAJOR)
            , Identifier.parse(BEACON_MINOR)
    );

    private static final double BEACON_DISTANCE = 5.0; //꼭대기 반경 5m => 출발
    private static final double BEACON_TOP = 0.5; //꼭대기 반경 50cm => 도착
    private boolean eventPopUpAble = true;

    private FitnessMainBinding binding;

    private Timer timer;
    private int timecnt;
    TimerTask timerTask;
    private int flag = 0;

    private void timerWork() {
        timecnt++;
        Log.d(TAG, "timerWork: " + timecnt);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");

        binding = FitnessMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //BeaconManager 지정
        beaconManager = BeaconManager.getInstanceForApplication(this);

        //교재 p.155
//		estimo 비컨을 분석 하도록 하기 위하여 beacon parser 오프셋, 버전등을 setLayout으로 지정한다.
//		m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24
//		설명: 0 ~ 1 바이트는 제조사를 나타내는 필드로 파싱하지 않는다.
//		2~3 바이트는 0x02, 0x15 이다.
//		4~19 바이트들을 첫번째 ID로 매핑한다.(UUID)
//				20~21 바이트들을 두번째 ID로 매핑한다.(Major)
//				22-23 바이트들을 세번째 ID로 매핑한다.(Minor)
//				24~24 바이트들을 txPower로 매핑한다.
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        checkPermission = new CheckPermission(this);

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "블루투스 기능을 확인해 주세요.", Toast.LENGTH_SHORT).show();

            Intent bleIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(bleIntent, 1);
        }

        if (!checkPermission.runtimeCheckPermission(this, runtimePermissions)) {
            ActivityCompat.requestPermissions(this, runtimePermissions, PERMISSION_REQUEST_CODE);
        } else { //이미 전체 권한이 있는 경우
            startScan();
        }

        timecnt = 0;
        timer = new Timer();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // 권한을 모두 획득했다면.
                    startScan();
                } else {
                    checkPermission.requestPermission();
                }
        }
    }

    //위의 permission 완료되면 스캔 시작
    private void startScan() {
        // 리전에 비컨이 있는지 없는지..정보를 받는 클래스 지정
        beaconManager.addMonitorNotifier(monitorNotifier);
        beaconManager.startMonitoring(region);

        Button buttonstart = (Button) findViewById(R.id.start);
        buttonstart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //detacting되는 해당 region의 beacon정보를 받는 클래스 지정.
                beaconManager.addRangeNotifier(rangeNotifier);
                beaconManager.startRangingBeacons(region); //start를 했으니 이후에 stop도 가능

                //타이머
                Log.d(TAG, "onClick: timer start work!");
                if (timerTask != null) {
                    timerTask.cancel();
                }

                timecnt = 0;
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        timerWork();
                    }
                };
                timer.schedule(timerTask, 0, 1000); //1초에 한 번 호출
            }
        });

    }

    private void initView() {

    }

    //모니터링 결과를 처리할 Notifier를 지정.
    // region에 해당하는 beacon 유무 판단
    MonitorNotifier monitorNotifier = new MonitorNotifier() {
        @Override
        public void didEnterRegion(Region region) { //발견 함.
            Log.d(TAG, "I just saw an beacon for the first time!");
        }

        @Override
        public void didExitRegion(Region region) { //발견 못함.
            Log.d(TAG, "I no longer see an beacon");
        }

        @Override
        public void didDetermineStateForRegion(int state, Region region) { //상태변경
            Log.d(TAG, "I have just switched from seeing/not seeing beacons: " + state);
        }
    };

    //매초마다 해당 리전의 beacon 정보들을 collection으로 제공받아 처리한다.
    RangeNotifier rangeNotifier = new RangeNotifier() {
        @Override
        public void didRangeBeaconsInRegion(Collection beacons, Region region) {
            if (beacons.size() > 0) {
                Iterator iterator = beacons.iterator();
                while (iterator.hasNext()){
                    Beacon beacon = (Beacon)iterator.next();
                    // Major, Minor로 Beacon 구별 (해당 region만 들어오므로 double check.)
                    if(isYourBeacon(beacon)) {
                        double dist = beacon.getDistance();
                        //int flag = 0;

                        // 5m 내에 있을 경우 이벤트 표시 다이얼로그 팝업
                        //if((beacon.getDistance() <= BEACON_DISTANCE)&&(flag == 0)){
                        if(beacon.getDistance() <= BEACON_DISTANCE){
                            Log.d(TAG, "didRangeBeaconsInRegion: distance 이내.");

                            //stopScan => onDestroy
                            runOnUiThread(() -> {
                                String txt = binding.textView.getText().toString();
                                //시간, 속력 출력
                                double diststart = 5;
                                if (flag == 0) {
                                    diststart = beacon.getDistance(); //시작 거리
                                }
                                flag++;
                                double speed = (diststart - dist) / timecnt;
                                TextView timeView = (TextView)findViewById(R.id.time_speed);
                                timeView.setText("시간: " + timecnt + "초   " + "속력: " + String.format("%.2f", speed) + "/초");

                                //10cm에 한 번씩 textview
                                //if (dist % 10 == 0) {
                                    //binding.textView.setText(((new Date()).toString().substring(0, 20) + "남은 거리: " + dist) + "\n\n" + txt);
                                    binding.textView.setText(("운동 시간:" + timecnt + "초" + "  " + "남은 거리: " + String.format("%.2f", dist)) + "m" + "\n\n" + txt);
                                //}

                                Button buttonstop = (Button) findViewById(R.id.stop);
                                buttonstop.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        double diststop = beacon.getDistance();
                                        EndFitness(timecnt, dist);
                                        finish();
                                        //flag++;
                                    }
                                });

                                if (beacon.getDistance() <= BEACON_TOP) {
                                    //Toast.makeText(this, "도착 성공! 츄르를 획득하였습니다", Toast.LENGTH_LONG).show();

                                    MainFitness.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(MainFitness.this, "도착 성공! " + getString(R.string.churucnt) + churucnt, Toast.LENGTH_SHORT).show();
                                            //Toast.makeText(MainFitness.this, getString(R.string.churucnt) + churucnt, Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    });

                                    churucnt++;
                                    EndFitness(timecnt, dist);
                                    //flag++;
                                }

                            });
                        }else{
                            Log.d(TAG, "didRangeBeaconsInRegion: distance 이외.");
                            eventPopUpAble = true;
                        }
                        Log.d(TAG, "distance: " + beacon.getDistance() + " id:" + beacon.getId1() + "/" + beacon.getId2() + "/" + beacon.getId3());
                    }
                }
            }

            if(beacons.isEmpty()){
                Log.d(TAG, "didRangeBeaconsInRegion: 비컨을 찾을 수 없습니다.");
            }
        }
    };

    public void EndFitness(double time, double speed) {

        //타이머 종료
        Log.d(TAG, "onClick: timer stop work!");
        if (timerTask != null) {
            timerTask.cancel();
        }
    }

    // 찾고자 하는 Beacon이 맞는지 확인 //사실 해당되는 것만 들어왔을 것이기 때문에 필요X
    private boolean isYourBeacon(Beacon beacon) {
        return beacon.getId2().toString().equals(BEACON_MAJOR) //id2가 major, id3가 minor이 맞는지 확인
                &&	beacon.getId3().toString().equals(BEACON_MINOR);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    // destroy에서 beacon scan을 중지 시킨다.
    // beacon scan을 중지 하지 않으면 일정 시간 이후 다시 scan이 가능하다.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        beaconManager.stopMonitoring(region);
        beaconManager.stopRangingBeacons(region);
    }
}