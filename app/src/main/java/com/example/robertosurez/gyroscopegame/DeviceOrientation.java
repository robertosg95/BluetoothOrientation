package com.example.robertosurez.gyroscopegame;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.ExifInterface;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;


public class DeviceOrientation extends Activity{

    /**
     * Sensors
     */
    private SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;

    /**
     * UI
     */
    private ImageView myMove;
    private static ImageView rivalMove;
    static int up;
    static int down;
    static int right;
    static int left;

    private final int ORIENTATION_PORTRAIT = ExifInterface.ORIENTATION_ROTATE_90; // 6
    private final int ORIENTATION_LANDSCAPE = ExifInterface.ORIENTATION_ROTATE_180; // 3
    private final int ORIENTATION_LANDSCAPE_REVERSE = ExifInterface.ORIENTATION_NORMAL; // 1
    private final int ORIENTATION_PORTRAIT_REVERSE = ExifInterface.ORIENTATION_ROTATE_270; // 8

    int smoothness = 1;
    private float averagePitch = 0;
    private float averageRoll = 0;
    private int orientation;

    private float[] pitches;
    private float[] rolls;

    int currentPosition = 6;

    public DeviceOrientation() {
        pitches = new float[smoothness];
        rolls = new float[smoothness];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        myMove = (ImageView) findViewById(R.id.your_move);
        rivalMove = (ImageView) findViewById(R.id.rival_move);
        up = R.drawable.arriba;
        down = R.drawable.abajo;
        right = R.drawable.derecha;
        left = R.drawable.izquierda;

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(getEventListener(), accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(getEventListener(), magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(getEventListener());

    }

    public static void setDirectionReceive(String dir) {
        if(dir.equals("ORIENTATION_PORTRAIT"))
            rivalMove.setImageResource(up);
        if(dir.equals("ORIENTATION_LANDSCAPE_REVERSE"))
            rivalMove.setImageResource(left);
        if(dir.equals("ORIENTATION_PORTRAIT_REVERSE"))
            rivalMove.setImageResource(down);
        if(dir.equals("ORIENTATION_LANDSCAPE"))
            rivalMove.setImageResource(right);
    }


    public SensorEventListener getEventListener() {
        return sensorEventListener;
    }

    public int getOrientation() {
        return orientation;
    }

    SensorEventListener sensorEventListener = new SensorEventListener() {
        float[] mGravity;
        float[] mGeomagnetic;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                mGravity = event.values;
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                mGeomagnetic = event.values;
            if (mGravity != null && mGeomagnetic != null) {
                float R[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
                if (success) {
                    float orientationData[] = new float[3];
                    SensorManager.getOrientation(R, orientationData);
                    averagePitch = addValue(orientationData[1], pitches);
                    averageRoll = addValue(orientationData[2], rolls);
                    orientation = calculateOrientation();
                    if (getOrientation()== 6 && currentPosition!=6){
                        currentPosition = 6;
                        sendMessage("ORIENTATION_PORTRAIT");
                        myMove.setImageResource(up);
                    }if(getOrientation()== 3 && currentPosition!=3){
                        sendMessage("ORIENTATION_LANDSCAPE_REVERSE");
                        myMove.setImageResource(left);
                        currentPosition = 3;
                    }if (getOrientation()== 8 && currentPosition!=8) {
                        sendMessage("ORIENTATION_PORTRAIT_REVERSE");
                        myMove.setImageResource(down);
                        currentPosition = 8;
                    }if (getOrientation()== 1 && currentPosition!=1) {
                        sendMessage("ORIENTATION_LANDSCAPE");
                        myMove.setImageResource(right);
                        currentPosition = 1;
                    }


                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {


        }
    };

    public void sendMessage(String message) {
        if (MainActivity.BTcontroller.getState() != BluetoothController.STATE_CONNECTED) {
            Toast.makeText(this, "Connection was lost!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (message.length() > 0) {

            byte[] send = String.valueOf(message).getBytes();
            MainActivity.BTcontroller.write(send);


        }
    }




    private float addValue(float value, float[] values) {
        value = (float) Math.round((Math.toDegrees(value)));
        float average = 0;
        for (int i = 1; i < smoothness; i++) {
            values[i - 1] = values[i];
            average += values[i];
        }
        values[smoothness - 1] = value;
        average = (average + value) / smoothness;
        return average;
    }

    private int calculateOrientation() {
        // finding local orientation dip
        if (((orientation == ORIENTATION_PORTRAIT
                || orientation == ORIENTATION_PORTRAIT_REVERSE)
                && (averageRoll > -30 && averageRoll < 30))) {
            if (averagePitch > 0)
                return ORIENTATION_PORTRAIT_REVERSE;
            else
                return ORIENTATION_PORTRAIT;
        } else {
            // divides between all orientations
            if (Math.abs(averagePitch) >= 30) {
                if (averagePitch > 0)
                    return ORIENTATION_PORTRAIT_REVERSE;
                else
                    return ORIENTATION_PORTRAIT;
            } else {
                if (averageRoll > 0) {
                    return ORIENTATION_LANDSCAPE_REVERSE;
                } else {
                    return ORIENTATION_LANDSCAPE;
                }
            }
        }
    }
}
