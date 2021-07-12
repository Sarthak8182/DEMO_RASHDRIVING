package com.example.demo_rashdriving;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class ShakeListener implements SensorEventListener {
    private final int FORCE_THRESHOLD = 15500;
    private final int TIME_THRESHOLD = 75;
    private final int SHAKE_TIMEOUT = 500;
    private final int SHAKE_DURATION = 1000;

    private final int SHAKE_COUNT = 1;

    private SensorManager mSensorMgr;
    private float mLastX = -1.0f, mLastY = -1.0f, mLastZ = -1.0f;
    private long mLastTime;
    private OnShakeListener mShakeListener;
    private Context mContext;
    private int mShakeCount = 0;
    private long mLastShake;
    private long mLastForce;


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public interface OnShakeListener {
        public void onShake(int n);


    }

    public ShakeListener(Context context) {
        mContext = context;
        resume();
    }

    public void setOnShakeListener(OnShakeListener listener) {
        mShakeListener = listener;
    }

    public void resume() {
        mSensorMgr = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorMgr == null) {
            throw new UnsupportedOperationException("Sensors not supported");
        }
        boolean supported = mSensorMgr.registerListener(this, mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        if (!supported) {
            mSensorMgr.unregisterListener(this, mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
            throw new UnsupportedOperationException("Accelerometer not supported");
        }
    }

    public void pause() {

        if (mSensorMgr != null) {
            mSensorMgr.unregisterListener(this, mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
            mSensorMgr = null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent eventt) {


        long now = System.currentTimeMillis();

        if ((now - mLastForce) > SHAKE_TIMEOUT) {
            mShakeCount = 0;
        }

        if ((now - mLastTime) > TIME_THRESHOLD) {
            long diff = now - mLastTime;
            float speed = Math.abs(eventt.values[0] + eventt.values[1] + eventt.values[2] - mLastX - mLastY - mLastZ) / diff * 10000;
            if (speed > FORCE_THRESHOLD) {


                if ((++mShakeCount >= SHAKE_COUNT) && (now - mLastShake > SHAKE_DURATION)) {
                    mLastShake = now;
                    mShakeCount = 0;
                    if (mShakeListener != null) {
                        int n = 1;
                        mShakeListener.onShake(n);
                    }
                }


                mLastForce = now;

            }


            mLastTime = now;
            mLastX = eventt.values[SensorManager.DATA_X];
            mLastY = eventt.values[SensorManager.DATA_Y];
            mLastZ = eventt.values[SensorManager.DATA_Z];
        }


    }


}