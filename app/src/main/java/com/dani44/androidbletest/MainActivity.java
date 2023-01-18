package com.dani44.androidbletest;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dani44.BlueToothBleHelpers.ElsBluetoothLeService;


@SuppressLint("MissingPermission")
public class MainActivity extends AppCompatActivity {

    Button btn_send ;
    Button btn_recieve ;

    TextView txtViewPosZ ;
    TextView txtViewConnectStatus ;

    private final String TAG = "MainActivity" ;

    boolean mBound = false ;
    boolean mConnected = false ;

    ElsBluetoothLeService mElsBluetoothLeService ;
    // BleObserver mBleobserver ;

    MainActivity mainActivity = null ;


    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (ElsBluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                txtViewConnectStatus.setText("Connected");
            } else if (ElsBluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                txtViewConnectStatus.setText("OffLine");
            } else if (ElsBluetoothLeService.ACTION_GATT_DATA_CCHANGED.equals(action)) {
                txtViewPosZ.setText( mElsBluetoothLeService.getElsMessage().toString() ) ;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());

        if (mElsBluetoothLeService != null) {
            mElsBluetoothLeService.setNotification(true);
            mBound = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ElsBluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(ElsBluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(ElsBluetoothLeService.ACTION_GATT_DATA_CCHANGED);
        return intentFilter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this ;

        // Quelle
        // https://developer.android.com/guide/topics/connectivity/bluetooth/connect-gatt-server
        Intent gattServiceIntent = new Intent( this, ElsBluetoothLeService.class );
        bindService(gattServiceIntent, connection, getApplicationContext().BIND_AUTO_CREATE);
        // Ende Quelle

        startService(gattServiceIntent);


        txtViewPosZ = (TextView) findViewById(R.id.txtViewPosZ );
        txtViewConnectStatus = (TextView) findViewById(R.id.textViewConnectStatus );

        btn_send = (Button) findViewById(R.id.buttonSend);
        btn_send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mConnected) {
                    mElsBluetoothLeService.executeGCode("G01 X111.342 F404");
                }
            }
        });

        btn_recieve = (Button) findViewById(R.id.buttonRecieve);
        btn_recieve.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mConnected){
                    mElsBluetoothLeService.requestElsDataFromDevice();
                }
            }
        });



    }


    @Override
    protected void onStart() {

        super.onStart();
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent( this, ElsBluetoothLeService.class ) );
        super.onDestroy();
    }





    private ServiceConnection connection = new ServiceConnection() {

    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
        mElsBluetoothLeService = ((ElsBluetoothLeService.LocalBinder) service).getService();

        if (mElsBluetoothLeService != null) {
            mBound = true;
        }
    }



    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        mBound = false;
    }
    };


}