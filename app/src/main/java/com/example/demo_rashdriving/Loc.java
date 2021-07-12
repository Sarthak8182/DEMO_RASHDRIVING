package com.example.demo_rashdriving;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;


public class Loc extends Activity implements EasyPermissions.PermissionCallbacks {
    private LocationManager lm;

    public String No1, No2;
    private String[] locationPermission;private FusedLocationProviderClient client;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private boolean haslocpermission;
    public static double latitude1;
    private double longitude1;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_certainity);


        lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);


        locationPermission = new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
        //permission are not given by user
        if (!EasyPermissions.hasPermissions(this, locationPermission)) {
            showPermission();
        }
    }

    private void getLocationData() {

        client = LocationServices.getFusedLocationProviderClient(this);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location!=null){
                    updateUI(location);

                }
                else{
                    Toast.makeText(Loc.this, "Sorry your phone sucks", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message = e.getMessage();

            }
        });

    }

    private void updateUI(Location location) {
        if (location != null) {

            latitude1 = location.getLatitude();
            longitude1 = location.getLongitude();


            Intent i =new Intent(this,MainActivity.class);
            i.putExtra("Lat",latitude1);
            i.putExtra("Longi",longitude1);
            Intent in =new Intent(this,ShakeService.class);
            in.putExtra("Lat",latitude1);
            in.putExtra("Longi",longitude1);


        }
    }

    private void showPermission(){
        EasyPermissions.requestPermissions(this,"provide location permission",10,locationPermission);
    }




    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        getLocationData();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

}