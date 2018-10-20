package com.example.mjrlo.uberbestshot;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button driverButton = (Button) findViewById(R.id.DriverButton);
        Button riderButton= (Button)findViewById(R.id.RiderButton);


        driverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,DriverLoginPage.class);
                startActivity(intent);


            }
        });


    }

}
