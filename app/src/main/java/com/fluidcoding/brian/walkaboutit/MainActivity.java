package com.fluidcoding.brian.walkaboutit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button btn_startWalking;
    Intent locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = new Intent(this.getActivity(), LocationSerive.class);

        getActivity().startService();

        btn_startWalking = (Button)findViewById(R.id.btn_startWalking);
        btn_startWalking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
