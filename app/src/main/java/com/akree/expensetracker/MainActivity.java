package com.akree.expensetracker;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Thread timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timer = new Thread() {

            @Override
            public void run() {
                try {
                    synchronized (this) {

                        wait(5000);

                    }
                } catch (InterruptedException e) {

                    e.printStackTrace();

                } finally {

                    Intent intent = new Intent(MainActivity.this, AuthorizationActivity.class);
                    startActivity(intent);
                    finish();

                }
            }
        };

        timer.start();

    }
}