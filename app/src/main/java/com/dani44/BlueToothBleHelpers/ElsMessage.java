package com.dani44.BlueToothBleHelpers;

import java.nio.ByteBuffer;

public class ElsMessage {

    private byte state = 0 ;
    private byte error = 0 ;
    private short rpm  = 0 ;
    private float posZ = 0.0f ;


    public byte getError() { return error; }
    public byte getState() { return state; }
    public float getPosZ() { return posZ;  }
    public short getRpm()  { return rpm;   }

    // ---------------------------------------------------------------------------
    // GATT Characteristic.getValue() --> byte[]
    // ---------------------------------------------------------------------------
    // private byte[] array = {0,          // Byte  State
    //                         1,          // Byte  Error
    //                         2,3,        // Short Rpm
    //                         4,5,6,7     // float PosZ
    //                         };
    // ---------------------------------------------------------------------------
    public boolean assign( byte[] buffer ){
        if( buffer.length >= 8 ){
            this.state = buffer[0] ;
            this.error = buffer[1] ;

            byte[] rpmBuffer= { buffer[2] , buffer[3]} ;
            this.rpm  = ByteBuffer.wrap( rpmBuffer ).getShort() ;

            byte[] posZBuffer= { buffer[4] , buffer[5], buffer[6], buffer[7] } ;
            this.posZ = ByteBuffer.wrap( posZBuffer ).getFloat() ;
            return true ;
        } else {
            return false ;
        }
    }

}
