package com.example.demo_rashdriving;


import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;


public class ShakeService extends Service implements SensorEventListener {
    private static final int ID_SERVICE = 32;
    private ShakeListener mShaker;
    private SensorManager mSensorManager;
    private ShakeListener2 mShaker2;
    private final int FORCE_THRESHOLD2 = 5000;

    private final int TIME_THRESHOLD2 = 75;
    private final int SHAKE_TIMEOUT2 = 500;
    private final int SHAKE_DURATION2 = 250;

    private final int SHAKE_COUNT2 = 1;




    float Rot[]=null; //for gravity rotational data
    //don't use R because android uses that for other stuff
    float I[]=null; //for magnetic rotational data
    float accels[]=new float[3];
    float mags[]=new float[3];
    float[] values = new float[3];

    float azimuth;
    float pitch;
    float roll;


    private SensorManager mSensorMgr2;
    private float mLastX = -1.0f, mLastY = -1.0f, mLastZ = -1.0f;
    private long mLastTime;
    private ShakeListener2.OnShakeListener mShakeListener2;
    private Context mContext2;
    private int mShakeCount = 0;
    private long mLastShake;
    private long mLastForce;
    private final int FORCE_THRESHOLD = 15500;
    private final int TIME_THRESHOLD = 75;
    private final int SHAKE_TIMEOUT = 500;
    private final int SHAKE_DURATION = 1000;

    private final int SHAKE_COUNT = 1;


    private Sensor mAccelerometer;
    public int check;
    private HomeWidget widget;
    private float speed;



    private Sensor mRotationSensor;

    private static final int SENSOR_DELAY = 500 * 100; // 500ms
    private static final int FROM_RADS_TO_DEGS = -57;
    private int t;
    private float pitch1;
    private float roll1;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();

        try {
            mSensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
            mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        } catch (Exception e) {
            Toast.makeText(this, "Hardware compatibility issue", Toast.LENGTH_LONG).show();
        }



        this.mSensorManager = ((SensorManager) getSystemService(Context.SENSOR_SERVICE));
//        mShaker = new ShakeListener(this);
//        mShaker.setOnShakeListener(this);
//        mShaker2 = new ShakeListener2(this);
//        mShaker2.setOnShakeListener2(this);
        Toast.makeText(ShakeService.this, "Service is created!", Toast.LENGTH_LONG).show();
        Log.d(getPackageName(), "Created the Service!");
        check = 1;

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(PRIORITY_MIN)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setSubText("Application Running")
                .build();
        widget = new HomeWidget();
        IntentFilter filter = new IntentFilter("android.appwidget.action.APPWIDGET_UPDATE");
        registerReceiver(widget,filter);

        startForeground(ID_SERVICE, notification);

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(NotificationManager notificationManager) {
        String channelId = "my_service_channelid";
        String channelName = "My Foreground Service";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        // omitted the LED color
        channel.setImportance(NotificationManager.IMPORTANCE_NONE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(channel);
        return channelId;
    }


    public void onShake(int n) {
        if (check == 1 && n == 1) {
            Toast.makeText(ShakeService.this, "SHAKEN!", Toast.LENGTH_LONG).show();
            final Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vib.vibrate(3000);
            Intent i = new Intent();
            i.setClass(this, CheckCertainty.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } else if (check == 1 && n == 2) {
            Toast.makeText(ShakeService.this, "SHAKEN!", Toast.LENGTH_LONG).show();
            final Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vib.vibrate(3000);
            Intent ii = new Intent();
            ii.setClass(this, CheckCertainity2.class);
            ii.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(ii);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mAccelerometer = mSensorManager.getDefaultSensor(1);
        mSensorManager.registerListener(this, mAccelerometer, SENSOR_DELAY);
        mSensorManager.registerListener(this, mRotationSensor, SENSOR_DELAY);



        return super.onStartCommand(intent, flags, startId);
    }


    public void onDestroy() {
        super.onDestroy();
        check = 0;
        Log.d(getPackageName(), "Service Destroyed.");
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
            mSensorManager.unregisterListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR));
            mSensorManager = null;
        }
        unregisterReceiver(widget);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor == mRotationSensor) {
            if (event.values.length > 4) {
                float[] truncatedRotationVector = new float[4];
                System.arraycopy(event.values, 0, truncatedRotationVector, 0, 4);
                update(truncatedRotationVector);
            } else {
                update(event.values);
            }
        }





        long now = System.currentTimeMillis();

        if ((now - mLastForce) > SHAKE_TIMEOUT2) {
            mShakeCount = 0;
        }

        if ((now - mLastTime) > TIME_THRESHOLD2) {
            long diff = now - mLastTime;
            speed = Math.abs(event.values[0] + event.values[1] + event.values[2] - mLastX - mLastY - mLastZ) / diff * 10000;
            if (speed > FORCE_THRESHOLD  || t>202) {


                if ((++mShakeCount >= SHAKE_COUNT) && (now - mLastShake > SHAKE_DURATION)) {
                    mLastShake = now;
                    mShakeCount = 0;

                    int n = 1;
                    onShake(n);

                }


                mLastForce = now;

            } else if (speed > FORCE_THRESHOLD2 && speed < 6000) {


                if ((++mShakeCount >= SHAKE_COUNT2) && (now - mLastShake > SHAKE_DURATION2)) {
                    mLastShake = now;
                    mShakeCount = 0;

                    int n = 2;
                    onShake(n);


                }


                mLastForce = now;

            }


            mLastTime = now;
            mLastX = event.values[SensorManager.DATA_X];
            mLastY = event.values[SensorManager.DATA_Y];
            mLastZ = event.values[SensorManager.DATA_Z];
        }
            String x = Float.toString(speed);
            updateWidget(x);


    }

    public void updateWidget(String data){

        Intent intent = new Intent(this, HomeWidget.class);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), HomeWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
        intent.putExtra("data",data);
        intent.putExtra("pitch",Float.toString(pitch1));
        intent.putExtra("roll",Float.toString(roll1));
        intent.putExtra("key",1);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendBroadcast(intent);

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    private void update(float[] vectors) {
        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, vectors);
        int worldAxisX = SensorManager.AXIS_X;
        int worldAxisZ = SensorManager.AXIS_Z;
        float[] adjustedRotationMatrix = new float[9];
        SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisX, worldAxisZ, adjustedRotationMatrix);
        float[] orientation = new float[3];
        SensorManager.getOrientation(adjustedRotationMatrix, orientation);
        pitch1 = orientation[1] * FROM_RADS_TO_DEGS;
        roll1 = orientation[2] * FROM_RADS_TO_DEGS;

        int p=(int) pitch1;
        int r=(int) roll1;
        t = p+r;


        if(t >202)
            Toast.makeText(this, "Rotated", Toast.LENGTH_SHORT).show();





    }
}