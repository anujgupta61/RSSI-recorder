package com.erickshaw.rssi;

import android.provider.BaseColumns;


public final class RssiDBContract {
    public RssiDBContract() {}

    public static abstract class RssiEntry1 implements BaseColumns {
        public static final String TABLE_NAME = "rssi_values" ;
        public static final String COLUMN_NAME_LAT = "latitude" ;
        public static final String COLUMN_NAME_LNG = "longitude" ;
        public static final String COLUMN_NAME_RSSI = "rssi_val" ;
    }
}
