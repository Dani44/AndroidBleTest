package com.dani44.BlueToothBleHelpers;
import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;


@SuppressLint("MissingPermission")
public class ElsBluetoothLeService extends Service {

    public final static String ACTION_GATT_DATA_CCHANGED  = "com.dani44.BlueToothBleHelpers.ElsBluetoothLeService.ACTION_GATT_DATA_CCHANGED";
    public final static String ACTION_GATT_CONNECTED = "com.dani44.BlueToothBleHelpers.ElsBluetoothLeService.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.dani44.BlueToothBleHelpers.ElsBluetoothLeService.ACTION_GATT_DISCONNECTED";

    boolean mIsConnected = false ;
    final String TAG = "ElsBluetoothLeService" ;
    BluetoothManager bluetoothManager ;
    BluetoothAdapter blueToothAdapter ;
    BluetoothDevice bluetoothDevice ;
    BluetoothGatt bluetoothGatt ;
    BluetoothGattService els_service = null ;

    ElsMessage mElsMessage = new ElsMessage();

    // Quelle: 1
    // https://developer.android.com/guide/topics/connectivity/bluetooth/connect-gatt-server
    // --------------------------------------------------------------------------------------
    private final Binder binder = new LocalBinder();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    public class LocalBinder extends Binder {
        public ElsBluetoothLeService getService() {
            return ElsBluetoothLeService.this;
        }
    }
    // End Quelle: 1


    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }


    public void setNotification(boolean enabled) {

        if (els_service == null) {
            Log.e(TAG, "els_servise Service not found");
            return;
        }

        BluetoothGattCharacteristic characteristic =  els_service.getCharacteristic(ESP32ElsServiceDescriptor.CHARACTER_NOTIFIER_UUID) ;
        if( characteristic == null ){
            Log.e(TAG, "els_servise characteristics \"" + ESP32ElsServiceDescriptor.CHARACTER_NOTIFIER_UUID_STR + "\" not found");
            return;
        }

        if( characteristic.getDescriptors().size() > 0 ){
            BluetoothGattDescriptor descriptor = characteristic.getDescriptors().get(0);

            if( enabled){
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            } else {
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            }
            bluetoothGatt.writeDescriptor(descriptor);
        } else {
            Log.e(TAG, "els_servise characteristics \"" + ESP32ElsServiceDescriptor.CHARACTER_NOTIFIER_UUID_STR + "\" Notification-Descriptor not found");
            return;
        }



    }

    public void requestElsDataFromDevice(){
        BluetoothGattCharacteristic character_jsondata = els_service.getCharacteristic(ESP32ElsServiceDescriptor.CHARACTER_ELSDATA_UUID) ;
        if( character_jsondata != null){
            bluetoothGatt.readCharacteristic(character_jsondata) ;
            Log.i(TAG, "Gatt readCharacteristic readCharacteristic CHARACTER_ELSDATA_UUID done:");
        }

    }

    public ElsMessage getElsMessage(){

        return mElsMessage ;
    }


    public void executeGCode( String command) {


        // !! ConcurrentLinkedQueue
        // https://stackoverflow.com/questions/70545166/ble-writing-to-a-characteristic-android-studio


        // BluetoothGattService service = bluetoothGatt.getService(ESP32ElsServiceDescriptor.SERVICE_UUID) ;
        // BluetoothGattService service = bluetoothGatt.getServices().get(2);
        if ( els_service != null) {
            BluetoothGattCharacteristic character = els_service.getCharacteristic(ESP32ElsServiceDescriptor.CHARACTER_GCODE_COMMAND_UUID) ;
            if( character != null){

                int counter=1 ;
                character.setValue(command) ;
                while( (! bluetoothGatt.writeCharacteristic(character)  ) && ( counter++ < 3000000 ) ){}
                Log.i(TAG, "Gatt writeCharacteristic done after " + counter + " trials") ;


            }
        }

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
                        broadcastUpdate( ACTION_GATT_CONNECTED ) ;
                        Log.d( TAG, "BluetoothGatt has connected: Start scann Services .... "   ) ;
                        bluetoothGatt.discoverServices();
                        break ;
                    }
                    case BluetoothProfile.STATE_DISCONNECTED:{
                        broadcastUpdate( ACTION_GATT_DISCONNECTED ) ;
                        Log.d( TAG, "BluetoothGatt has disconnected."   ) ;
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

                   setNotification(true);
               }

           }


           @Override
           public void onCharacteristicRead( BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
               super.onCharacteristicRead(gatt,characteristic, status);
           }

           @Override
           public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
               super.onCharacteristicWrite(gatt, characteristic, status);
               if( characteristic.getUuid().equals( ESP32ElsServiceDescriptor.CHARACTER_GCODE_COMMAND_UUID) ){
                   switch( status ){
                       case BluetoothGatt.GATT_SUCCESS:{
                           Log.i(TAG, "OK Wrote to GCode characteristic") ;
                           break ;
                       }
                       case BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH:{
                           Log.e(TAG, "NOK Wrote to GCode characteristic GATT_INVALID_ATTRIBUTE_LENGTH") ;
                           break ;
                       }
                       case BluetoothGatt.GATT_WRITE_NOT_PERMITTED:{
                           Log.e(TAG, "NOK Wrote to GCode characteristic GATT_WRITE_NOT_PERMITTED") ;
                           break ;
                       }

                   }




               }



           }

           @Override
           public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
               super.onCharacteristicChanged(gatt, characteristic);
               mElsMessage.assign( characteristic.getValue()) ;
               broadcastUpdate( ACTION_GATT_DATA_CCHANGED ) ;
           }
       });








        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}