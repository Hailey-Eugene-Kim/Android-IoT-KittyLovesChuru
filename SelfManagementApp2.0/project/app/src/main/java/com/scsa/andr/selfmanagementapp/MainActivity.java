package com.scsa.andr.selfmanagementapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv = findViewById(R.id.textView4); //토글
        tv.setOnClickListener(v -> {
            String str = tv.getText().toString();
            if(str.equals(R.string.play)){
                tv.setText(R.string.hello);
            }else{
                tv.setText(R.string.play);
            }
        });

        Button button1 = (Button) findViewById(R.id.todo);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Toast.makeText(MainActivity.this, "to-do list로 이동",  Toast.LENGTH_SHORT).show();

                Intent i = new Intent(MainActivity.this,MainToDo.class);
                startActivity(i);
            }
        });

        Button button3 = (Button) findViewById(R.id.workout);
        button3.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onClick(View v) {

                Toast.makeText(MainActivity.this, "캣타워로 이동",  Toast.LENGTH_SHORT).show();

                Intent i = new Intent(MainActivity.this,MainFitness.class);
                startActivity(i);
            }
        });

        Button button4 = (Button) findViewById(R.id.game);
        button4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Toast.makeText(MainActivity.this, "츄르 게임으로 이동",  Toast.LENGTH_SHORT).show();

                Intent i = new Intent(MainActivity.this,MainGame.class);
                startActivity(i);
            }
        });
    }
}