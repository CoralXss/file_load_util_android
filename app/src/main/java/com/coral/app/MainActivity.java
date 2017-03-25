package com.coral.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.coral.load.activity.FileLoadActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.tv_click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 此处只是为了方便，具体用法查看 FileLoadActivity
                Intent intent = new Intent(MainActivity.this, FileLoadActivity.class);
                startActivity(intent);
            }
        });
    }
}
