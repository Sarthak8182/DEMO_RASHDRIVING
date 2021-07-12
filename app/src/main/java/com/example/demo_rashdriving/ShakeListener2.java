package com.example.demo_rashdriving;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import android.hardware.SensorManager;
import android.content.Context;



import java.lang.UnsupportedOperationException;

public class ShakeListener2 extends Activity implements SensorEventListener
{
    private  final int FORCE_THRESHOLD2 = 5000;

    private  final int TIME_THRESHOLD2 = 75;
    private final int SHAKE_TIMEOUT2 = 500;
    private  final int SHAKE_DURATION2 = 250;

    private  final int SHAKE_COUNT2 = 1;

    private SensorManager mSensorMgr2;
    private float mLastX=-1.0f, mLastY=-1.0f, mLastZ=-1.0f;
    private long mLastTime;
    private OnShakeListener mShakeListener2;
    private Context mContext2;
    private int mShakeCount = 0;
    private long mLastShake;
    private long mLastForce;


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }



    public interface OnShakeListener
    {
        public void onShake(int n);


    }

    public ShakeListener2(Context context)
    {
        mContext2 = context;
        resume();
    }

    public void setOnShakeListener2(OnShakeListener listener)
    {
        mShakeListener2 = listener;
    }

    public void resume() {
        mSensorMgr2 = (SensorManager)mContext2.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorMgr2 == null) {
            throw new UnsupportedOperationException("Sensors not supported");
        }
        boolean supported = mSensorMgr2.registerListener(this,mSensorMgr2.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_GAME);
        if (!supported) {
            mSensorMgr2.unregisterListener(this, mSensorMgr2.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
            throw new UnsupportedOperationException("Accelerometer not supported");
        }
    }

    public void pause() {

        if (mSensorMgr2 != null) {
            mSensorMgr2.unregisterListener(this, mSensorMgr2.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
            mSensorMgr2 = null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {


        long now = System.currentTimeMillis();

        if ((now - mLastForce) > SHAKE_TIMEOUT2) {
            mShakeCount = 0;
        }

        if ((now - mLastTime) > TIME_THRESHOLD2) {
            long diff = now - mLastTime;
            float speed = Math.abs(event.values[0] + event.values[1] + event.values[2] - mLastX - mLastY - mLastZ) / diff * 10000;
            if (speed > FORCE_THRESHOLD2 && speed < 6000) {


                if ((++mShakeCount >= SHAKE_COUNT2) && (now - mLastShake > SHAKE_DURATION2)) {
                    mLastShake = now;
                    mShakeCount = 0;
                    if (mShakeListener2 != null) {
                        int n=2;
                        mShakeListener2.onShake(n);
                        n=0;
                    }
                }


                mLastForce = now;

            }



            mLastTime = now;
            mLastX = event.values[SensorManager.DATA_X];
            mLastY = event.values[SensorManager.DATA_Y];
            mLastZ = event.values[SensorManager.DATA_Z];
        }


    }





}