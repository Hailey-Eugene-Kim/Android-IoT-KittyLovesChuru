package com.scsa.andr.selfmanagementapp;

import static com.scsa.andr.selfmanagementapp.MainToDo.churucnt;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainGame extends AppCompatActivity {

    private static final String TAG = "MainGame_SCSA";

    FrameLayout f;
    FrameLayout.LayoutParams params;
    int count = 0;   //잡은 쥐 개수를 저장할 변수
    int delay = 1200;  // 게임 속도 조절
    static boolean threadEndFlag = true; // 쓰레드 끄기
    MouseTask mt;                // 쓰레드 구현

    int myWidth;  // 내 폰의 너비
    int myHeight; // 내 폰의 높이
    int imgWidth = 300;  //그림 크기
    int imgHeight = 300;//그림 크기
    Random r = new Random();  // 이미지 위치를 랜덤하게 발생시킬 객체

    SoundPool pool;   // 소리
    int liveMouse;    // 소리
    MediaPlayer mp;   // 소리

    int x = 200;        //시작위치
    int y = 200;        //시작위치
    ImageView[] imgs; // 이미지들을 담아 놓을 배열

    int level = 1;      // 게임 레벨
    int nums = 0;

    //NFC
    //private static final String TAG2 = "NFC_SCSA";
    NfcAdapter nAdapter;
    PendingIntent pIntent;
    IntentFilter[] filters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_main);
        f = (FrameLayout) findViewById(R.id.frame);
        params = new FrameLayout.LayoutParams(1, 1);

        //디스플레이 크기 체크
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        myWidth = metrics.widthPixels;
        myHeight = metrics.heightPixels;
        Log.d(TAG, "My Window " + myWidth + " : " + myHeight);

        //NFC
        nAdapter = NfcAdapter.getDefaultAdapter(this);

        Intent i = new Intent(this, MainGame.class);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        pIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_MUTABLE);

        IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED); //TAG가 가장 마지막에 받아서 다 걸리게 됨

//        try {
//            filter.addDataType("text/*");
//        } catch (IntentFilter.MalformedMimeTypeException e) {
//            e.printStackTrace();
//        }
        filters = new IntentFilter[]{filter,};

        //todo에서 체크된 개수만큼 츄르 플레이
        init(churucnt);
        Toast.makeText(MainGame.this, getString(R.string.churucnt) + churucnt, Toast.LENGTH_SHORT).show();
    }

    public void init(int nums) {
        //초기화
        count = 0;
        threadEndFlag = true;
        this.nums = nums;
        delay = (int) (delay * (10 - level) / 10.);

        f.removeAllViews();

        //사운드 셋팅
//        pool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .build();

        pool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(attributes)
                .build();
        liveMouse = pool.load(this, R.raw.meow, 1);
        mp = MediaPlayer.create(this, R.raw.gamebgm);
        mp.setLooping(true);

        //이미지 담을 배열 생성과 이미지 담기
        imgs = new ImageView[nums];
        for (int i = 0; i < nums; i++) {
            ImageView iv = new ImageView(this);
            iv.setImageResource(R.drawable.churu);  // 이미지 소스 설정
            f.addView(iv, params);  // 화면에 표시
            imgs[i] = iv;     // 배열에 담기
            iv.setOnClickListener(h);  // 이벤트 등록
        }

        mt = new MouseTask();  //일정 간격으로 이미지 위치를 바꿀 쓰레드 실행
        mt.execute();
    }

    protected void onResume() {
        super.onResume();
        nAdapter.enableForegroundDispatch(this, pIntent, filters, null);
        mp.start();
    }

    protected void onPause() {
        super.onPause();
        nAdapter.disableForegroundDispatch(this);
        mp.stop();
    }

    //NFC 인식
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String action = intent.getAction();
        Log.d(TAG, "New Intent action : " + action);

        churucnt++;
        Toast.makeText(this, "집사의 츄르 선물이 도착했습니다!", Toast.LENGTH_SHORT).show();
        init(churucnt);
    }

    protected void onDestroy() {
        super.onDestroy();
        mp.release();
        mt.cancel(true);
        threadEndFlag = false;

    }

    View.OnClickListener h = new View.OnClickListener() {
        public void onClick(View v) {   // 쥐를 잡았을 때
            count++;
            churucnt--;
            ImageView iv = (ImageView) v;
            pool.play(liveMouse, 1, 1, 0, 0, 1);  // 소리 내기
            iv.setVisibility(View.INVISIBLE);          // 이미지(쥐) 제거

            Toast.makeText(MainGame.this, count + " 냠", Toast.LENGTH_SHORT).show();
            if (count == nums) {   // 쥐를 다 잡았을때
                threadEndFlag = false;
                mt.cancel(true);

                AlertDialog.Builder dia = new AlertDialog.Builder(MainGame.this);
                dia.setMessage("오늘의 츄르를 모두 먹었습니다");
                dia.setPositiveButton("진짜 츄르를 먹을래요", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        //츄르 스토어로 이동
                        Toast.makeText(MainGame.this, "churu store로 이동",  Toast.LENGTH_SHORT).show();

                        Intent i = new Intent(MainGame.this,GameShop.class);
                        startActivity(i);
                    }
                });
                dia.setNegativeButton("그만 먹을래요", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                dia.show();
            }

        }
    };

    // 쥐 위치 이동하여 다시 그리기
    public void update() {
        if (!threadEndFlag) return;
        Log.d(TAG, "update:");
        for (ImageView img : imgs) {
            x = r.nextInt(myWidth - imgWidth);
            y = r.nextInt(myHeight - imgHeight);

            img.layout(x, y, x + imgWidth, y + imgHeight);
            img.invalidate();
        }

    }

    // 일정 시간 간격으로 쥐를 다시 그리도록 update()를 호출하는 쓰레드
    class MouseTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {// 다른 쓰레드
            while (threadEndFlag) {
                //다른 쓰레드에서는 UI를 접근할 수 없으므로
                publishProgress();    //자동으로 onProgressUpdate() 가 호출된다.
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            update();
        }
    }

    ;//end MouseTask
}