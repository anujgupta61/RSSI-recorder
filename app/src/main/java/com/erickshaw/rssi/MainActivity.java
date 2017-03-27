package com.erickshaw.rssi;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks , GoogleApiClient.OnConnectionFailedListener , LocationListener{

    private  GoogleApiClient mGoogleApiClient; // creating google API client

    private LocationRequest mLocationRequest; // location object that holds location values fetched

    long startTime = 0;

    String latitude = "";
    String longitute = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // creating Google API Client
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
        ShowGPSSettings(this);
        final Button jsonBtn = (Button) findViewById(R.id.json_button) ;
        jsonBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRssiValues(jsonBtn) ;
            }
        });
        final Button normalBtn = (Button) findViewById(R.id.normal_button) ;
        normalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRssiValues(normalBtn) ;
            }
        });
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        mGoogleApiClient.connect();

        super.onStart();
    }

    public void ShowGPSSettings(Activity activity) {
        String provider = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if(!provider.contains("gps")){ //if gps is disabled
            try {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("NO GPS")
                        .setMessage("Please select High Accuracy Location Mode")
                        .setCancelable(true)
                        .setPositiveButton("Cancel",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog . cancel() ;
                            }
                        })
                        .setNegativeButton("GPS Settings",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) ;
                            }
                        });


                AlertDialog alert = builder.create();
                alert.show();

            }
            catch(Exception e)
            {

            }
        }
    }


    // function to get wifiInfo object 
    public static WifiInfo getWiInfo(Context context) {
        WifiManager myWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
        return myWifiInfo;
    }
    @Override
    protected void onStop() {
        // TODO Auto-generated method stub

        super.onStop();
    }

    // write data to textview in xml
    void write(String str)
    {
        TextView textView = (TextView)findViewById(R.id.textView);
        textView.setText(str);
    }


    // return rssi value
    int getRSSI()
    {
        int data = 1 ; // 1 refers null RSSI value
        try
        {
            data = getWiInfo(getApplicationContext()).getRssi() ;
        }
        catch (Exception e)
        {
        }
        return data;
    }

    // get ssid  ie PU@CAMPUS name
    String getSSID()
    {
        String data;
        try
        {
            data = getWiInfo(getApplicationContext()).getSSID();
        }
        catch (Exception e)
        {
            data = "No wifi";
        }
        return data;
    }

    /*

    Google location code starts here
     */

    @Override
    public void onDestroy() {
        // Disconected Google Api Client
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(500) ;
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest , this);
        }
        catch (SecurityException e) {
            Toast.makeText(getApplicationContext(), "Connection request not completed ...", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Toast.makeText(this, "Check Your Internet Connection", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(getApplicationContext(), "Connection is Slow", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLocationChanged(Location location) {

        RssiDbHelper resDbHelper = new RssiDbHelper(getApplicationContext());
        SQLiteDatabase mydb = resDbHelper.getWritableDatabase();
        SQLiteDatabase mydb1 = resDbHelper.getReadableDatabase();
        
        String lng= Double.toString(location.getLongitude());
        String lat = Double.toString(location.getLatitude());
        long finalTime= System.currentTimeMillis();
        if((finalTime - startTime)>=200) {

            // error handling
            if(checkConnection(this))
            {
                latitude = lat;
                longitute = lng;
                int rssi = getRSSI() ;
                String ssid = getSSID() ;
                String data = "Latitude : "+ latitude + "\nLongitude : "+longitute + "\nRSSI : "+ rssi+"\nSSID : "+ ssid ;
                write(data);

                Cursor c = mydb1.rawQuery("SELECT * FROM " + RssiDBContract.RssiEntry1.TABLE_NAME + " WHERE " + RssiDBContract.RssiEntry1.COLUMN_NAME_LAT + " = " + latitude + " AND " + RssiDBContract.RssiEntry1.COLUMN_NAME_LNG + " = " + longitute + " ;" , null);
                if(c.getCount() != 0) {
                    if (ssid.equals("\"PU@CAMPUS\"")) {
                        Toast.makeText(this, "Entry updated .", Toast.LENGTH_SHORT).show();
                        mydb1.execSQL("UPDATE " + RssiDBContract.RssiEntry1.TABLE_NAME + " SET " + RssiDBContract.RssiEntry1.COLUMN_NAME_RSSI + " = " + rssi + " WHERE " + RssiDBContract.RssiEntry1.COLUMN_NAME_LAT + " = " + latitude + " AND " + RssiDBContract.RssiEntry1.COLUMN_NAME_LNG + " = " + longitute + ";");
                    } else {
                        Toast.makeText(this, "No WiFi . Entry Updated .", Toast.LENGTH_SHORT).show();
                        mydb1.execSQL("UPDATE " + RssiDBContract.RssiEntry1.TABLE_NAME + " SET " + RssiDBContract.RssiEntry1.COLUMN_NAME_RSSI + " = 1 " + " WHERE " + RssiDBContract.RssiEntry1.COLUMN_NAME_LAT + " = " + latitude + " AND " + RssiDBContract.RssiEntry1.COLUMN_NAME_LNG + " = " + longitute + ";");
                    }
                } else {
                    if (ssid.equals("\"PU@CAMPUS\"")) {
                        // Creating a new map
                        ContentValues values = new ContentValues();
                        values.put(RssiDBContract.RssiEntry1.COLUMN_NAME_LAT, latitude);
                        values.put(RssiDBContract.RssiEntry1.COLUMN_NAME_LNG, longitute);
                        values.put(RssiDBContract.RssiEntry1.COLUMN_NAME_RSSI, rssi);

                        //Inserting a new row
                        long newRowID = mydb.insert(
                                RssiDBContract.RssiEntry1.TABLE_NAME,
                                null,
                                values
                        );
                        Toast.makeText(this, "Entry added .", Toast.LENGTH_SHORT).show();
                    } else {
                        // Creating a new map
                        ContentValues values = new ContentValues();
                        values.put(RssiDBContract.RssiEntry1.COLUMN_NAME_LAT, latitude);
                        values.put(RssiDBContract.RssiEntry1.COLUMN_NAME_LNG, longitute);
                        values.put(RssiDBContract.RssiEntry1.COLUMN_NAME_RSSI, 1);

                        //Inserting a new row
                        long newRowID = mydb.insert(
                                RssiDBContract.RssiEntry1.TABLE_NAME,
                                null,
                                values
                        );
                        Toast.makeText(this, "No WiFi . Entry added .", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            else
            {
                //Toast.makeText(this, "Please check your internet conection ( E-rickshaw : Driver Mode )", Toast.LENGTH_LONG).show();
            }
            startTime = finalTime;
        }
    }

    boolean checkConnection(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected ;
    }

    void downloadFile(String str , boolean is_json) {
        String filename ;
        if(is_json)
            filename = "rssi.json" ;
        else
            filename = "rssi.txt";
        String content = str ;
        File file ;
        FileOutputStream outputStream ;
        try {
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) , filename);
            outputStream = new FileOutputStream(file);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "File saved .", Toast.LENGTH_SHORT).show();
    }

    void getRssiValues(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }
        RssiDbHelper resDbHelper = new RssiDbHelper(getApplicationContext());
        SQLiteDatabase mydb = resDbHelper.getReadableDatabase();
        Cursor c = mydb.rawQuery("SELECT * FROM " + RssiDBContract.RssiEntry1.TABLE_NAME + " ;" , null);
        String str = "" ;
        String str1 = "{\n" + "\"rssi_values\": [\n" ;
        boolean flag = false ;
        while(c.moveToNext()) {
            double lat = c.getDouble(c.getColumnIndexOrThrow(RssiDBContract.RssiEntry1.COLUMN_NAME_LAT));
            double lng = c.getDouble(c.getColumnIndexOrThrow(RssiDBContract.RssiEntry1.COLUMN_NAME_LNG));
            int rssi = c.getInt(c.getColumnIndexOrThrow(RssiDBContract.RssiEntry1.COLUMN_NAME_RSSI));
            if(view . getId() == R.id.normal_button)
                str = str + "Lat : " + lat + " , Lng : " + lng + " , RSSI : " + rssi + "\n" ;
            if(view . getId() == R.id.json_button) {
                if(! flag) {
                    flag = true ;
                } else {
                    str1 = str1 + ",\n" ;
                }
                str1 = str1 + "{ \"latitude\": \"" + lat + "\", \"longitude\": \"" + lng + "\" , \"rssi_val\": \"" + rssi + "\" }";
            }
        }
        str1 = str1 + "\n]\n}" ;
        TextView txt = (TextView) findViewById(R.id.rssi_vals) ;
        if(view . getId() == R.id.normal_button) {
            txt.setText(str);
            downloadFile(str , false);
        }
        if(view . getId() == R.id.json_button) {
            txt.setText(str1);
            downloadFile(str1 , true);
        }
    }
}
