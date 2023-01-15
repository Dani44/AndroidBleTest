package com.dani44.androidbletest;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.view.View;
import android.widget.Button;


@SuppressLint("MissingPermission")
public class MainActivity extends AppCompatActivity {

    Button btn_read ;

    boolean mBound = false ;
    ElsBluetoothLeService mElsBluetoothLeService ;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btn_read = (Button) findViewById(R.id.button_read);
        btn_read.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { /*
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Hello world!");
                AlertDialog dialog = builder.create();
                dialog.show();
                */

                mElsBluetoothLeService.executeGCode("G01 X111.342 F404");
            }
        });




    }





    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent( this, ElsBluetoothLeService.class );
        startService(intent);
        bindService(intent, connection, getApplicationContext().BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent( this, ElsBluetoothLeService.class ) );
        super.onDestroy();
    }





    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ElsBluetoothLeService.LocalBinder binder = (ElsBluetoothLeService.LocalBinder) service;
            mElsBluetoothLeService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


}