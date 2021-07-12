package com.example.demo_rashdriving;



import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;

import android.content.Intent;

import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;



import android.util.Log;
import android.widget.Button;

import android.widget.EditText;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    public static String firstN,secondN;
    public int flag;
    public TextView edT1;
    public EditText edT2;
    private String lat;
    private String[] locationPermission;
    private boolean haslocpermission = false;
    private FusedLocationProviderClient client;
    private String mobile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        FloatingActionButton fab=findViewById(R.id.fab);
        ImageView abt =findViewById(R.id.abt);

        abt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i =new Intent(MainActivity.this,About.class);
                startActivity(i);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setAction(Intent.ACTION_VIEW);
                i.setData(Uri.parse("content://contacts/people/"));
                startActivity(i);
            }
        });
        final Button serviceact=findViewById(R.id.serviceact);
        final Button servicede=findViewById(R.id.servicede);
        final Intent intent=getIntent();
        mobile = intent.getStringExtra("mobile");
        serviceact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    Toast.makeText(MainActivity.this, "ACTIVATED!", Toast.LENGTH_LONG).show();
                    startService(new Intent(getApplicationContext(), ShakeService.class));
                    flag = 0;
                }
                 });
        servicede.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                Intent i = new Intent(MainActivity.this, reverify.class);
                i.putExtra("mobiles", mobile);
                startActivity(i);
            }
        });



        Button doneB=findViewById(R.id.doneButton);
        if (doneB != null) {
            doneB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    edT1 =  findViewById(R.id.firstNumber);
                    edT1.setText(mobile);
                    edT2 =  findViewById(R.id.secondNumber);
                    if(edT1.getText()!=null)
                        firstN=edT1.getText().toString();
                    if(edT2.getText()!=null)
                        secondN=edT2.getText().toString();
                    try {
                        String fullpath = String.valueOf(Environment.getExternalStorageDirectory());

                       FileOutputStream fout=openFileOutput("myfile",MODE_PRIVATE);
                       fout.write(firstN.getBytes());
                       fout.write("\n".getBytes());
                       fout.write(secondN.getBytes());



                        Toast.makeText(getApplicationContext(),
                                "The emergency contact numbers have been saved.",
                                Toast.LENGTH_SHORT).show();
                    } catch(FileNotFoundException ex) {
                        Toast.makeText(getApplicationContext(), "NOT FOUND", Toast.LENGTH_SHORT).show();
                    }
                    catch(IOException ex) {
                        Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
                    }
                    Log.d(getPackageName(), "Done! button pressed.");
                }
            });
        }

        locationPermission = new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
        //permission are not given by user
        if (!EasyPermissions.hasPermissions(this, locationPermission)) {
            showPermission();
        } else {

            haslocpermission = true;
            getLocationData();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.aboutM) {
            startActivity(new Intent(MainActivity.this, About.class));
        }
        else if(id == R.id.close){
            System.exit(1);
        }

        return super.onOptionsItemSelected(item);
    }



    public void updateWidget(String data1,String data2){

        Intent intent = new Intent(this, HomeWidget.class);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), HomeWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
        intent.putExtra("info1",data1);
        intent.putExtra("info2",data2);
        intent.putExtra("key",2);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendBroadcast(intent);

    }


    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        getLocationData();

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
                    Toast.makeText(MainActivity.this, "Sorry your phone sucks", Toast.LENGTH_SHORT).show();
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

        if(location!=null){

            double latitude = location.getLatitude();
            double longitude = location.getLongitude();


            //sending data
            updateWidget(Double.toString(latitude),Double.toString(longitude));




        }

    }
    private void showPermission() {
        EasyPermissions.requestPermissions(this,"provide location permission",10,locationPermission);
    }


    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }
}