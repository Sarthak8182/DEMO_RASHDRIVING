package com.example.demo_rashdriving;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
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


public class CheckCertainty extends Activity implements EasyPermissions.PermissionCallbacks {
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
        try     {
            File myFile = new File("/sdcard/.emergencyNumbers.txt");
            FileInputStream fIn =openFileInput("myfile");
            int c;String temp="";
            while ((c=fIn.read())!=-1){
                temp=temp+Character.toString((char)c);

            }
            BufferedReader myReader = new BufferedReader(
                    new InputStreamReader(fIn));
            No1 = temp.substring(0,'\n');
            No2 = temp.substring('\n'+1);

        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
        final SmsManager sms = SmsManager.getDefault();
        lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_DENIED) {

                Log.d("permission", "permission denied to SEND_SMS - requesting it");
                String[] permissions = {Manifest.permission.SEND_SMS};

                requestPermissions(permissions, PERMISSION_REQUEST_CODE);

            }
        }
        locationPermission = new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
        //permission are not given by user
        if (!EasyPermissions.hasPermissions(this, locationPermission)) {
            showPermission();
        } else {
            //all good

            haslocpermission = true;
            getLocationData();


            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    sms.sendTextMessage(No1, null, "Help! I've met with an accident at http://maps.google.com/?q=" + String.valueOf(latitude1) + "," + String.valueOf(longitude1), null, null);
                    sms.sendTextMessage(No1, null, "Nearby Hospitals http://maps.google.com/maps?q=hospital&mrt=yp&sll=" + String.valueOf(latitude1) + "," + String.valueOf(longitude1) + "&output=kml", null, null);
                    sms.sendTextMessage(No2, null, "Help! I've met with an accident at http://maps.google.com/?q=" + String.valueOf(latitude1) + "," + String.valueOf(longitude1), null, null);
                    sms.sendTextMessage(No2, null, "Nearby Hospitals http://maps.google.com/maps?q=hospital&mrt=yp&sll=" + String.valueOf(latitude1) + "," + String.valueOf(longitude1) + "&output=kml", null, null);
                    System.exit(1);
                }
            }, 1000);

            Button dismiss = findViewById(R.id.dismissB);
            dismiss.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.exit(1);
                }
            });

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
                    Toast.makeText(CheckCertainty.this, "Sorry your phone sucks", Toast.LENGTH_SHORT).show();
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
            long time = location.getTime();

            Intent i =new Intent(this,HomeWidget.class);
            i.putExtra("Latitiude",latitude1);
            i.putExtra("Longitude",longitude1);
            startService(i);

        }
    };

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