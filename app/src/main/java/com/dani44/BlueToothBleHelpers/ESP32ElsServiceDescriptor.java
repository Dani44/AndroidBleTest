package com.dani44.BlueToothBleHelpers;

import java.util.UUID;


// C++ Headerfile
/*
    #define BT_DEVICE_NAME "TEST_ESP32_BLE_101"
    #define SERVICE_UUID "421279a9-2f85-4385-90ff-32132a18a846"
    #define CHARACTER_NOTIFIER_UUID "e673fec2-0237-4e48-8cb3-9e7a65fd2381"
    #define CHARACTER_GCODE_COMMAND_UUID "d4478245-921f-4ae7-91c9-319d14fdc6f0"
    #define CHARACTER_ELSDATA_UUID  "b528fea8-a79d-4c79-b5c9-76036a44fb94"
*/

/*
Erstellt am 16.01.2023
 */


public class ESP32ElsServiceDescriptor {


    public static final String BT_DEVICE_NAME                   = "TEST_ESP32_BLE_101" ;
    public static final String SERVICE_UUID_STR                 = "421279a9-2f85-4385-90ff-32132a18a846" ;
    public static final String CHARACTER_NOTIFIER_UUID_STR      = "e673fec2-0237-4e48-8cb3-9e7a65fd2381" ;
    public static final String CHARACTER_GCODE_COMMAND_UUID_STR = "d4478245-921f-4ae7-91c9-319d14fdc6f0" ;
    public static final String CHARACTER_ELSDATA_UUID_STR       = "b528fea8-a79d-4c79-b5c9-76036a44fb94" ;



    public static final UUID SERVICE_UUID = UUID.fromString(SERVICE_UUID_STR) ;
    public static final UUID CHARACTER_NOTIFIER_UUID = UUID.fromString(CHARACTER_NOTIFIER_UUID_STR) ;
    public static final UUID CHARACTER_GCODE_COMMAND_UUID = UUID.fromString(CHARACTER_GCODE_COMMAND_UUID_STR) ;
    public static final UUID CHARACTER_ELSDATA_UUID = UUID.fromString(CHARACTER_ELSDATA_UUID_STR) ;






}

