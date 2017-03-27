package com.erickshaw.rssi;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RssiDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1 ;
    public static final String DATABASE_NAME = "rssi.db" ;
    public static final String SQL_CREATE_ENTRIES1 =
            "CREATE TABLE IF NOT EXISTS " + RssiDBContract.RssiEntry1.TABLE_NAME + " (" +
                    RssiDBContract.RssiEntry1.COLUMN_NAME_LAT + " DOUBLE ," +
                    RssiDBContract.RssiEntry1.COLUMN_NAME_LNG + " DOUBLE ," +
                    RssiDBContract.RssiEntry1.COLUMN_NAME_RSSI + " INTEGER " + " ) ;" ;

    private static final String SQL_DELETE_ENTRIES1 =
            "DROP TABLE IF EXISTS " + RssiDBContract.RssiEntry1.TABLE_NAME ;

    public RssiDbHelper(Context context) {
        super(context , DATABASE_NAME , null , DATABASE_VERSION) ;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES1) ;
    }

    public void onUpgrade(SQLiteDatabase db , int oldVersion , int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES1) ;
        onCreate(db) ;
    }

    public void onDowngrade(SQLiteDatabase db , int oldVersion , int newVersion) {
        onUpgrade(db , oldVersion , newVersion) ;
    }
}
