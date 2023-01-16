package com.dani44.BlueToothBleHelpers;

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
import java.util.Observable;

@SuppressLint("MissingPermission")
public class ElsBluetoothLeService extends Service {

    boolean mIsConnected = false ;

    public ElsDataModel elsData = new ElsDataModel() ;




    final String TAG = "ElsBluetoothLeService" ;
    BluetoothManager bluetoothManager ;
    BluetoothAdapter blueToothAdapter ;
    BluetoothDevice bluetoothDevice ;
    BluetoothGatt bluetoothGatt ;

    BluetoothGattService els_service = null ;

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public ElsBluetoothLeService getService() {
           return ElsBluetoothLeService.this;
        }
    }

    public void executeGCode( String command) {

        // BluetoothGattService service = bluetoothGatt.getService(ESP32ElsServiceDescriptor.SERVICE_UUID) ;
        // BluetoothGattService service = bluetoothGatt.getServices().get(2);
        if ( els_service != null) {
            BluetoothGattCharacteristic character = els_service.getCharacteristic(ESP32ElsServiceDescriptor.CHARACTER_GCODE_COMMAND_UUID) ;
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
        } else {
            bluetoothDevice = null ;

            for( BluetoothDevice device : blueToothAdapter.getBondedDevices()){
                if( device.getName().equals(ESP32ElsServiceDescriptor.BT_DEVICE_NAME)){
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
                        els_service = null ;
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

               els_service = bluetoothGatt.getService(ESP32ElsServiceDescriptor.SERVICE_UUID) ;
               if (els_service != null) {

                   BluetoothGattCharacteristic character_notifier = els_service.getCharacteristic(ESP32ElsServiceDescriptor.CHARACTER_NOTIFIER_UUID) ;
                   if( character_notifier != null){
                       gatt.setCharacteristicNotification(character_notifier,true ) ;
                       Log.i(TAG, "Gatt Notify enabled done");
                   }

               }

           }

           @Override
           public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
               super.onCharacteristicChanged(gatt, characteristic);
               String data = "NotFound" ;
               BluetoothGattCharacteristic character_jsondata = els_service.getCharacteristic(ESP32ElsServiceDescriptor.CHARACTER_ELSDATA_UUID) ;
               if( character_jsondata != null){
                   bluetoothGatt.readCharacteristic(character_jsondata) ;
                   data = character_jsondata.getStringValue(0) ;
                   Log.i(TAG, "Gatt readCharacteristic done:" + data);
               }
              elsData.setElsDataJson( data );


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


    public class ElsDataModel extends Observable {
        String elsDataJson  = ""  ;


        public String getElsDataJson() {
            return elsDataJson;
        }

        public void setElsDataJson(String elsDataJson) {
            this.elsDataJson = elsDataJson;
            setChanged();
            notifyObservers(this);
        }

    }






}