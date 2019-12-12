package com.example.mjrlo.uberbestshot;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button riderButton= (Button)findViewById(R.id.RiderButton);

Button driverButton= (Button)findViewById(R.id.DriverButton);

        driverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,DriverLoginPage.class);
                startActivity(intent);



            }
        });
       //  riderButton= (Button)findViewById(R.id.RiderButton);

        riderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,RiderLoginPage.class);
                startActivity(intent);



            }
        });

    }

}
