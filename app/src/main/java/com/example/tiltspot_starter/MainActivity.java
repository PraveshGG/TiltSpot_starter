package com.example.tiltspot_starter;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import android.os.Bundle;



    public class MainActivity extends AppCompatActivity
            implements SensorEventListener {

        // System sensor manager instance.
        private SensorManager mSensorManager;

        // Accelerometer and magnetometer sensors, as retrieved from the
        // sensor manager.
        private Sensor mSensorAccelerometer;
        private Sensor mSensorMagnetometer;

        // TextViews to display current sensor values.
        private TextView mTextSensorAzimuth;
        private TextView mTextSensorPitch;
        private TextView mTextSensorRoll;

        // Very small values for the accelerometer (on all three axes) should
        // be interpreted as 0. This value is the amount of acceptable
        // non-zero drift.
        private static final float VALUE_DRIFT = 0.05f;

        //array for accelerometer data
        private float  []  mAccelerometerData = new float[3];

        //array for magnetometer data
        private float  []  mMagnetometerData  = new float[3];

        float[] rotationMatrix = new float[9];
        float orientationValues[] = new float[3];


        private ImageView mSpotTop;
        private ImageView mSpotBottom;
        private ImageView mSpotLeft;
        private ImageView mSpotRight;


        //Task 3
        private Display mDisplay;



        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            // Lock the orientation to portrait (for now)
           // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            mTextSensorAzimuth = (TextView) findViewById(R.id.value_azimuth);
            mTextSensorPitch = (TextView) findViewById(R.id.value_pitch);
            mTextSensorRoll = (TextView) findViewById(R.id.value_roll);


            mSpotTop = (ImageView) findViewById(R.id.spot_top);
            mSpotBottom = (ImageView) findViewById(R.id.spot_bottom);
            mSpotLeft = (ImageView) findViewById(R.id.spot_left);
            mSpotRight = (ImageView) findViewById(R.id.spot_right);


            // Get accelerometer and magnetometer sensors from the sensor manager.
            // The getDefaultSensor() method returns null if the sensor
            // is not available on the device.
            mSensorManager = (SensorManager) getSystemService(
                    Context.SENSOR_SERVICE);
            mSensorAccelerometer = mSensorManager.getDefaultSensor(
                    Sensor.TYPE_ACCELEROMETER);
            mSensorMagnetometer = mSensorManager.getDefaultSensor(
                    Sensor.TYPE_MAGNETIC_FIELD);

            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            mDisplay = wm.getDefaultDisplay();



        }

        /**
         * Listeners for the sensors are registered in this callback so that
         * they can be unregistered in onStop().
         */
        @Override
        protected void onStart() {
            super.onStart();

            // Listeners for the sensors are registered in this callback and
            // can be unregistered in onStop().
            //
            // Check to ensure sensors are available before registering listeners.
            // Both listeners are registered with a "normal" amount of delay
            // (SENSOR_DELAY_NORMAL).
            if (mSensorAccelerometer != null) {
                mSensorManager.registerListener(this, mSensorAccelerometer,
                        SensorManager.SENSOR_DELAY_NORMAL);
            }
            if (mSensorMagnetometer != null) {
                mSensorManager.registerListener(this, mSensorMagnetometer,
                        SensorManager.SENSOR_DELAY_NORMAL);
            }
        }

        @Override
        protected void onStop() {
            super.onStop();

            // Unregister all sensor listeners in this callback so they don't
            // continue to use resources when the app is stopped.
            mSensorManager.unregisterListener(this);
        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

            //get sensor type i.e accelerometer or gyroscope
            int sensorType = sensorEvent.sensor.getType();

            switch (sensorType) {
                case Sensor.TYPE_ACCELEROMETER:
                    mAccelerometerData = sensorEvent.values.clone();
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    mMagnetometerData = sensorEvent.values.clone();
                    break;
                default:
                    return;
            }

            boolean rotationOK = SensorManager.getRotationMatrix(rotationMatrix,
                    null, mAccelerometerData, mMagnetometerData);

            float[] rotationMatrixAdjusted = new float[9];

            switch (mDisplay.getRotation()) {
                case Surface.ROTATION_0:
                    rotationMatrixAdjusted = rotationMatrix.clone();
                    break;

                case Surface.ROTATION_90:
                    SensorManager.remapCoordinateSystem(rotationMatrix,
                            SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X,
                            rotationMatrixAdjusted);
                    break;

                case Surface.ROTATION_180:
                    SensorManager.remapCoordinateSystem(rotationMatrix,
                            SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y,
                            rotationMatrixAdjusted);
                    break;

                case Surface.ROTATION_270:
                    SensorManager.remapCoordinateSystem(rotationMatrix,
                            SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X,
                            rotationMatrixAdjusted);
                    break;

            }

            //getting orientation angles from rotationMatrix
            if (rotationOK) {
                SensorManager.getOrientation(rotationMatrixAdjusted, orientationValues);
            }

            //we get  orientation values from getOrientation()
            float azimuth = orientationValues[0];
            float pitch = orientationValues[1];
            float roll = orientationValues[2];

            //setting up the first value to be 0 for pitch and roll
            if (Math.abs(pitch) < VALUE_DRIFT) {
                pitch = 0;
            }
            if (Math.abs(roll) < VALUE_DRIFT) {
                roll = 0;
            }


            mTextSensorAzimuth.setText(getResources().getString(
                    R.string.value_format, azimuth));
            mTextSensorPitch.setText(getResources().getString(
                    R.string.value_format, pitch));
            mTextSensorRoll.setText(getResources().getString(
                    R.string.value_format, roll));

            //setting initial alpha values to be 0
            mSpotTop.setAlpha(0f);
            mSpotBottom.setAlpha(0f);
            mSpotLeft.setAlpha(0f);
            mSpotRight.setAlpha(0f);

            //check pitch  to be greater than 0 set the bottom spot shape alpha value to be the pitch value,
            if (pitch > 0) {
                mSpotBottom.setAlpha(pitch);
            } else {

                mSpotTop.setAlpha(Math.abs(pitch));
            }
            if (roll > 0) {
                mSpotLeft.setAlpha(roll);
            } else {
                mSpotRight.setAlpha(Math.abs(roll));
            }

        }

        /**
         * Must be implemented to satisfy the SensorEventListener interface;
         * unused in this app.
         */
        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }

}
