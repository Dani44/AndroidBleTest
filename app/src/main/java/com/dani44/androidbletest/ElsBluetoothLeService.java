package com.dani44.androidbletest;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;
import java.util.UUID;

@SuppressLint("MissingPermission")
public class ElsBluetoothLeService extends Service {

    boolean mIsConnected = false ;

    final String BLE_SERVER_NAME = "TEST_ESP32_BLE_100" ;

    final String SERVICE_UUID = "91bad492-b950-4226-aa2b-4ede9fa42f59" ;
    final String BME_CHARACTERISTIC_UUID = "cba1d466-344c-4be3-ab3f-189f80dd7518" ;
    final String GCODE_CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8" ;



    final String TAG = "ElsBluetoothLeService" ;
    BluetoothManager bluetoothManager ;
    BluetoothAdapter blueToothAdapter ;
    BluetoothDevice bluetoothDevice ;
    BluetoothGatt bluetoothGatt ;


    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        ElsBluetoothLeService getService() {
           return ElsBluetoothLeService.this;
        }
    }

    public void executeGCode( String command) {

        BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(SERVICE_UUID)) ;
        // BluetoothGattService service = bluetoothGatt.getServices().get(2);
        if (service != null) {
            BluetoothGattCharacteristic character = service.getCharacteristic(UUID.fromString(GCODE_CHARACTERISTIC_UUID)) ;
            if( character != null){
                character.setValue(command) ;
                bluetoothGatt.writeCharacteristic(character);
                Log.i(TAG, "Gatt writeCharacteristic done");
            }
        }

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
       // blueToothAdapter  = bluetoothManager.getAdapter();
        Log.i(TAG, "onCreate: on Create Startet");
        bluetoothManager = (BluetoothManager) getSystemService(getApplicationContext().BLUETOOTH_SERVICE);
        blueToothAdapter = bluetoothManager.getAdapter();



        if (blueToothAdapter == null || !blueToothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            bluetoothDevice = null ;

            for( BluetoothDevice device : blueToothAdapter.getBondedDevices()){
                if( device.getName().startsWith("TEST_ESP32_BLE")){
                    bluetoothDevice = device ;
                    break ;
                }
            }


        }

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: " + blueToothAdapter.getName());
        if( bluetoothDevice ==  null ){
            Log.e(TAG, "onStartCommand: BlueToothDevice NOT FOUND" );
        } else {
            Log.i(TAG, "onStartCommand: BlueToothDevice=" + bluetoothDevice.getName());
        }

       bluetoothGatt = bluetoothDevice.connectGatt(this, true, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

                super.onConnectionStateChange(gatt, status, newState);

                switch( newState ){
                    case BluetoothProfile.STATE_CONNECTED:{
                        mIsConnected = true ;
                        Log.i( TAG, "BluetoothGatt has connected: Start scann Services .... "   ) ;
                        bluetoothGatt.discoverServices();
                        break ;
                    }
                    case BluetoothProfile.STATE_DISCONNECTED:{
                        Log.i( TAG, "BluetoothGatt has disconnected."   ) ;
                        mIsConnected = false ;
                        break;
                    }
                }

            }

           @Override
           public void onServicesDiscovered(BluetoothGatt gatt, int status) {
               super.onServicesDiscovered(gatt, status);
               Log.i( TAG, "onServicesDiscovered" ) ;
               for(BluetoothGattService bleService : gatt.getServices()){
                   Log.i( TAG, "--> Service:" + bleService.getUuid() ) ;

               }

               BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(SERVICE_UUID)) ;
               if (service != null) {
                   BluetoothGattCharacteristic character_bme = service.getCharacteristic(UUID.fromString(BME_CHARACTERISTIC_UUID)) ;
                   if( character_bme != null){
                       gatt.setCharacteristicNotification(character_bme,true ) ;
                       Log.i(TAG, "Gatt Notify enabled done");
                   }
               }

           }

           @Override
           public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
               super.onCharacteristicChanged(gatt, characteristic);
               Log.d(TAG, "Gatt Notify BME:" + characteristic.getStringValue(0));

           }
       });








        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        for(BluetoothGattService bleService : bluetoothGatt.getServices()){
            Log.i( TAG, "-->> Service:" + bleService.getUuid() ) ;

        }



        Log.i(TAG, "onDestroy: killed");
        super.onDestroy();
    }
}