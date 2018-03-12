package bml.bixi.androidlogger;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    SensorManager mSensorManager;
    boolean isRunning;
    final String TAG = "SensorLog";
    FileWriter writer;

    int samplingSpeed = 0;


    LocationListener locationListenerGPS = new LocationListener() {
        public void onLocationChanged(Location location) {
            final TextView view_gps_lat = (TextView) findViewById(R.id.display_gps_latitude);
            final TextView view_gps_lng = (TextView) findViewById(R.id.display_gps_longitude);
            final TextView view_gps_accuracy = (TextView) findViewById(R.id.display_gps_accuracy);
            final TextView view_gps_extra = (TextView) findViewById(R.id.display_gps_extra);
            String lat = String.valueOf(location.getLatitude());
            String lng = String.valueOf(location.getLongitude());
            String acc = String.valueOf(location.getAccuracy());

            view_gps_lat.setText("latitude: " + lat);
            view_gps_lng.setText("longitude: " + lng);
            view_gps_accuracy.setText("accuracy: " + acc);
            if (location.getExtras() != null) {
                Bundle extra = location.getExtras();
                String estr = "";
                for (String s : extra.keySet()) {
                    estr += "  " + s + ": " + extra.get(s).toString() + "\n";
                }
                view_gps_extra.setText(estr);
                

            }

            updatedLastUpdated((TextView) findViewById(R.id.display_gps_updated));
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            final TextView view_gps = (TextView) findViewById(R.id.display_gps_status);
            String str = "Status (" + provider + "): ";
            if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
                str += "temporarily unavailable";
            } else if (status == LocationProvider.OUT_OF_SERVICE) {
                str += "out of service";
            } else if (status == LocationProvider.AVAILABLE) {
                str += "available";
            } else {
                str += "unknown";
            }
            view_gps.setText(str);

        }

        public void onProviderEnabled(String provider) {
            final TextView view_gps = (TextView) findViewById(R.id.display_gps_status_enabled);
            view_gps.setText(provider + " enabled");
        }

        public void onProviderDisabled(String provider) {
            final TextView view_gps = (TextView) findViewById(R.id.display_gps_status_enabled);
            view_gps.setText(provider + " disabled");
        }
    };


    protected void updatedLastUpdated(TextView t) {
        String date = DateFormat.getDateTimeInstance().format(System.currentTimeMillis());
        t.setText("updated: " + date);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isRunning = false;
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        final Button buttonStart = findViewById(R.id.button_start);
        final Button buttonStop = findViewById(R.id.button_stop);
        final Button buttonbreak = findViewById(R.id.Break);
        final Button buttonAcc = findViewById(R.id.Acceleration);
        final Button buttonSteer = findViewById(R.id.Aggressive_steering);
        final RadioGroup radioGroup = findViewById(R.id.SamplingOption);
        final RadioButton slow = findViewById(R.id.slow);
        final RadioButton fast = findViewById(R.id.fast);
        final RadioButton normal = findViewById(R.id.normal);

        LocationManager locationManagerGPS = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManagerGPS.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationListenerGPS.onProviderEnabled("GPS");
        } else {
            locationListenerGPS.onProviderDisabled("GPS");
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManagerGPS.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGPS);


        buttonStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                buttonStart.setEnabled(false);
                buttonStop.setEnabled(true);

                buttonbreak.setEnabled(true);
                buttonAcc.setEnabled(true);
                buttonSteer.setEnabled(true);

                radioGroup.setEnabled(false);
                slow.setEnabled(false);
                fast.setEnabled(false);
                normal.setEnabled(false);

                Sensor mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                Sensor mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                Sensor mMagnometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                Sensor mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
                Sensor mLinerAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
                Sensor mRotVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
                Sensor mMotion = mSensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);

                mSensorManager.flush(MainActivity.this);
                mSensorManager.registerListener(MainActivity.this, mGyroscope, samplingSpeed);
                mSensorManager.registerListener(MainActivity.this, mMagnometer, samplingSpeed);
                mSensorManager.registerListener(MainActivity.this, mGravity, samplingSpeed);
                mSensorManager.registerListener(MainActivity.this, mLinerAccel, samplingSpeed);
                mSensorManager.registerListener(MainActivity.this, mRotVector, samplingSpeed);
                mSensorManager.registerListener(MainActivity.this, mMotion, samplingSpeed);
                mSensorManager.registerListener(MainActivity.this, mMotion, samplingSpeed);
                mSensorManager.registerListener(MainActivity.this, mAccelerometer, samplingSpeed);

                Log.d(TAG, "Writing to " + getStorageDir());
                try {
                    writer = new FileWriter(new File(getStorageDir(), "sensors_" + System.currentTimeMillis() + ".csv"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isRunning = true;

            }
        });


        buttonStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                buttonStart.setEnabled(true);
                buttonStop.setEnabled(false);
                radioGroup.setEnabled(true);

                buttonbreak.setEnabled(false);
                buttonAcc.setEnabled(false);
                buttonSteer.setEnabled(false);

                slow.setEnabled(true);
                fast.setEnabled(true);
                normal.setEnabled(true);

                isRunning = false;
                mSensorManager.flush(MainActivity.this);
                mSensorManager.unregisterListener(MainActivity.this);
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void onResume() {
        super.onResume();
        /*mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mMagnometer, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mLinerAccel, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mRotVector, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mMotion, SensorManager.SENSOR_DELAY_FASTEST);*/
    }

    protected void onPause() {
        super.onPause();
        //mSensorManager.unregisterListener(this);
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.fast:
                if (checked)
                    samplingSpeed = SensorManager.SENSOR_DELAY_FASTEST;
                    break;
            case R.id.slow:
                if (checked)
                    samplingSpeed = SensorManager.SENSOR_DELAY_UI;
                    break;
            case R.id.normal:
                if (checked)
                    samplingSpeed = SensorManager.SENSOR_DELAY_NORMAL;
                    break;
        }
    }
    @Override
    public void onSensorChanged(SensorEvent event) {

        if(isRunning) {
            Log.d(TAG, "Running " );
            try {
                long timeInMillis = (new Date()).getTime() + (event.timestamp - System.nanoTime()) / 1000000L;
                Log.d(TAG, "Running "+timeInMillis );
                switch(event.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        writer.write(String.format("%d; ACCELEROMETER; %f;\t%f;\t%f\n", timeInMillis, event.values[0], event.values[1], event.values[2]));
                        break;
                    case Sensor.TYPE_GYROSCOPE:
                        writer.write(String.format("%d; GYROSCOPE; %f;\t%f;\t%f\n", timeInMillis, event.values[0], event.values[1], event.values[2]));
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        writer.write(String.format("%d; MAGNETIC; %f;\t%f;\t%f\n", timeInMillis, event.values[0], event.values[1], event.values[2]));
                        break;
                    case Sensor.TYPE_GRAVITY:
                        writer.write(String.format("%d; GRAVITY; %f;\t%f;\t%f\n", timeInMillis, event.values[0], event.values[1], event.values[2]));
                        break;
                    case Sensor.TYPE_LINEAR_ACCELERATION:
                        writer.write(String.format("%d; LINEAR_ACCELERATION; %f;\t%f;\t%f\n", timeInMillis, event.values[0], event.values[1], event.values[2]));
                        Log.d(TAG, "Linear acc " );
                        break;
                    case Sensor.TYPE_ROTATION_VECTOR:
                        writer.write(String.format("%d; ROTATION; %f;\t%f;\t%f;\t%f\n", timeInMillis, event.values[0], event.values[1], event.values[2], event.values[3]));
                        Log.d(TAG, "ROT vect " );
                        break;
                    case Sensor.TYPE_SIGNIFICANT_MOTION:
                        writer.write(String.format("%d; MOTION; %f;\t%f;\t%f;\t%f\n", timeInMillis, event.values[0], event.values[1], event.values[2], event.values[3]));
                        Log.d(TAG, "Motion " );
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /** Called when the user touches the button */
    public void sendMessageBrake(View view) throws IOException {
        writer.write(String.format("\r\n#################Break##################\r\n"));
    }

    /** Called when the user touches the button */
    public void sendMessageAcc(View view) throws IOException {
        writer.write(String.format("\n\r#################Acceleration##################\n"));
    }

    /** Called when the user touches the button */
    public void sendMessageSteer(View view) throws IOException {
        writer.write(String.format("\n\r#################Aggressive Steering##################\n"));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private String getStorageDir() {
        return this.getExternalFilesDir(null).getAbsolutePath();
        //  return "/storage/emulated/0/Android/data/com.iam360.sensorlog/";
    }
}
