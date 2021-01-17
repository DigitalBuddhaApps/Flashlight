package com.flashlightpowered;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks,
        SensorEventListener, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMapLongClickListener,
        View.OnClickListener {


    protected static final int REQUEST_CHECK_SETTINGS = 1;
    private static final int LOCATION_REQUEST_CODE = 101;
    private final static long TIME_UNIT = 250L;
    private final static long DOT_DELAY = TIME_UNIT * 2;
    private final static long DASH_DELAY = TIME_UNIT * 3;
    private final static long INTRA_LETTER_DELAY = TIME_UNIT;
    private final static long INTER_LETTER_DELAY = TIME_UNIT * 3;
    private final static long INTER_WORD_DELAY = TIME_UNIT * 7;
    int CHECK_CAMERA = 100;
    int value = 0;
    MediaPlayer mp;
    MediaPlayer mediaPlayer;
    ImageView color_pick, morse_btn, minus_brightness, plus_brightness, cancel_map,
            current_location;
    LinearLayout cancel;
    TextView light_on, done;
    LinearLayout ratting;
    ScrollView main_content;
    RelativeLayout color_bg, morse_design, main_lay;
    String location = "";
    Location myLocation;
    LocationManager manager;
    SupportMapFragment mapFragment;
    FrameLayout map_lay;
    InterstitialAd mInterstitialAd;
    int Brightness;
    StroboRunner sr;
    Thread t;
    boolean playOn = false;
    androidx.appcompat.app.AlertDialog alertDialog1;
    int color;
    private Camera camera = null;
    private boolean isLighOn = false;
    private float currentDegree = 0f;
    private ImageView compass_image, light_on_off, indication_img;
    private SensorManager mSensorManager;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double longitude;
    private double latitude;
    private Context mContext;
    private Activity mActivity;
    private CameraManager camManager;
    private Parameters parameters;
    private Parameters parameters_on;
    private Parameters parameters_off;
    private String txtToConvert;
    /**
     * Wheel Utility functions and listeners
     */

    // Wheel scrolled flag
    private boolean wheelScrolled = false;
    // Wheel scrolled listener
    OnWheelScrollListener scrolledListener = new OnWheelScrollListener() {
        public void onScrollingStarted(WheelView wheel) {
            if (!isLighOn) {
                turnOnFlash();
            }
            wheelScrolled = true;
            // Do something here
            mp.start();
        }

        public void onScrollingFinished(WheelView wheel) {
            wheelScrolled = false;
            // Do something here
            mp.pause();
            value = wheel.getCurrentItem();
            if (value != 0) {
                sr = new StroboRunner();
                sr.freq = value;
                t = new Thread(sr);
                t.start();
                return;
            }

            turnOnFlash();
            // Do something here
            mp.pause();
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWheel(R.id.slotwheel);
        mContext = MainActivity.this;
        mActivity = MainActivity.this;

        manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Initializing googleapi client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        AdView adView = this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        color_pick = findViewById(R.id.color_pick);
        current_location = findViewById(R.id.current_location);

        main_content = findViewById(R.id.main_content);
        ratting = findViewById(R.id.ratting);
        main_lay = findViewById(R.id.main_lay);
        morse_design = findViewById(R.id.morse_design);
        map_lay = findViewById(R.id.map_lay);
        cancel_map = findViewById(R.id.cancel_map);
        light_on = findViewById(R.id.light_on);
        compass_image = findViewById(R.id.compass_image);
        indication_img = findViewById(R.id.indication_img);
        light_on_off = findViewById(R.id.light_on_off);
        morse_btn = findViewById(R.id.morse_btn);
        minus_brightness = findViewById(R.id.minus_brightness);
        plus_brightness = findViewById(R.id.plus_brightness);
        brightness_low = findViewById(R.id.brightness_low);
//        brightness_high = (ArcSeekBar) findViewById(R.id.brightness_high);


        mp = new MediaPlayer();
        try {
            mp = MediaPlayer.create(getApplicationContext(), R.raw.adjustment_move);
            mp.prepare();

        } catch (Exception e) {
            e.printStackTrace();
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.sos_morse_code);
            mediaPlayer.prepare();

        } catch (Exception e) {
            e.printStackTrace();
        }
        light_on_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLighOn) {
                    mp.start();
                    turnOffFlash();
                    indication_img.setImageResource(R.drawable.indicator_off);
                } else {
                    mp.start();
                    turnOnFlash();
                    indication_img.setImageResource(R.drawable.indicator_on);
                }
            }
        });


        color_pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showColor();

                loadListeners();
                mColorPickerView.setDrawDebug(false);


            }
        });

        morse_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showMorse();
            }
        });

        compass_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMap();

            }
        });

        cancel_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map_lay.setVisibility(View.GONE);
                main_content.setVisibility(View.VISIBLE);
            }
        });

        light_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLighOn) {
                    turnOffFlash();
                    light_on.setText("On");
                } else {
                    turnOnFlash();
                    light_on.setText("Off");
                }
            }
        });

        minus_brightness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Settings.System.canWrite(mContext)) {
                    brightness_low.setVisibility(View.VISIBLE);
//                    brightness_high.setVisibility(View.GONE);
                    brightness_low.setProgress(Brightness);
                } else {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:" + mContext.getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }

            }
        });

        plus_brightness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Settings.System.canWrite(mContext)) {
                    brightness_low.setVisibility(View.VISIBLE);
                    brightness_low.setProgress(Brightness);

                } else {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:" + mContext.getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });

        brightness_low.setOnStopTrackingTouch(new ProgressListener() {
            @Override
            public void invoke(int i) {

                brightness_low.setVisibility(View.GONE);
            }
        });

        brightness_low.setOnProgressChangedListener(new ProgressListener() {
            @Override
            public void invoke(int i) {
                ContentResolver cResolver = mContext.getContentResolver();
                Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, i);
                Brightness = i;
            }
        });

        ratting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        current_location.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    protected void onPause() {
        super.onPause();
        turnOffFlash();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
        }
        mSensorManager.unregisterListener(this);

    }

    //Getting current location
    private void getCurrentLocation() {
        mMap.clear();
        //Creating a location object
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
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null) {
            //Getting longitude and latitude
            longitude = location.getLongitude();
            latitude = location.getLatitude();

            //moving the map to location
            moveMap();
        }
    }

    //Function to move the map
    private void moveMap() {
        //String to display current latitude and longitude
        String msg = latitude + ", " + longitude;

        //Creating a LatLng Object to store Coordinates
        LatLng latLng = new LatLng(latitude, longitude);

        //Adding marker to map
        mMap.addMarker(new MarkerOptions()
                .position(latLng) //setting position
                .draggable(true) //Making the marker draggable
                .title("Current Location")); //Adding a title

        //Moving the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        //Animating the camera
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng latLng = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.setOnMarkerDragListener((GoogleMap.OnMarkerDragListener) mActivity);
        mMap.setOnMapLongClickListener((GoogleMap.OnMapLongClickListener) mActivity);
    }

    @Override
    public void onConnected(Bundle bundle) {
        getCurrentLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        //Clearing all the markers
        mMap.clear();

        //Adding a new marker to the current pressed position
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true));
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        //Getting the coordinates
        latitude = marker.getPosition().latitude;
        longitude = marker.getPosition().longitude;

        //Moving the map
        moveMap();
    }

    @Override

    protected void onResume() {
        super.onResume();
        methodRequiresTwoPermission();
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),

                SensorManager.SENSOR_DELAY_GAME);


    }

    @Override
    public void onBackPressed() {


        if (main_content.getVisibility() == View.GONE) {
            morse_design.setVisibility(View.GONE);
            map_lay.setVisibility(View.GONE);
            main_content.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.ratting_popup, null);

        // Set the custom layout as alert dialog view
        builder.setView(dialogView);

        // Get the custom alert dialog view widgets reference
        final EditText et_email = dialogView.findViewById(R.id.et_email);
        final EditText et_des = dialogView.findViewById(R.id.et_des);
        TextView txt_submit = dialogView.findViewById(R.id.txt_submit);
        final RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);

        // Create the alert dialog
        final AlertDialog dialog = builder.create();

        // Set positive/yes button click listener

        // Display the custom alert dialog on interface

        txt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String rating = String.valueOf(ratingBar.getRating());

                if (rating.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Rating can't be blank", Toast.LENGTH_LONG).show();
                } else if (et_email.getText().toString().isEmpty()) {

                    et_email.setError("Can't be blank");
                } else if (et_des.getText().toString().isEmpty()) {
                    et_des.setError("Can't be blank");
                } else {
                    Intent mEmail = new Intent(Intent.ACTION_SEND);
                    mEmail.putExtra(Intent.EXTRA_EMAIL, new String[]{"abc@gmail.com"});
                    mEmail.putExtra(Intent.EXTRA_SUBJECT, "subject");
                    mEmail.putExtra(Intent.EXTRA_TEXT, "email" + et_email.getText().toString());
                    mEmail.putExtra(Intent.EXTRA_TEXT, "description" + et_des.getText().toString());
// prompts to choose email client
                    mEmail.setType("message/rfc822");
                    startActivity(Intent.createChooser(mEmail, "Choose an email client to send " +
                            "your"));
                }
            }
        });
        dialog.show();


    }

    private void showMorse() {

        AdView adView = this.findViewById(R.id.adview);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        morse_design.setVisibility(View.VISIBLE);
        main_content.setVisibility(View.GONE);

        final EditText type_edit = findViewById(R.id.type_edit);
        type_edit.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        final TextView morse_space = findViewById(R.id.morse_space);
        final TextView transmate = findViewById(R.id.transmate);
        final TextView flash_off = findViewById(R.id.flash_off);
        TextView off = findViewById(R.id.off);

        if (isLighOn) {
            flash_off.setText("Off");
        } else {
            flash_off.setText("On");
        }
//        final android.support.v7.app.AlertDialog alertDialog1 = alertDialogBuilder.create();

        transmate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InputMethodManager in =
                        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(type_edit.getApplicationWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                txtToConvert = type_edit.getText().toString();
                String convertedTxt = MorseCode.alphaToMorse(txtToConvert);
                morse_space.setText(convertedTxt);
                if (!txtToConvert.isEmpty()) {
                    if (transmate.getText().toString().equalsIgnoreCase("Transmit")) {
                        turnOnFlash();
                        flash_off.setText("Off");
                        mediaPlayer.start();
                        transmate.setText("Stop");
                        sr = new StroboRunner();
                        sr.freq = txtToConvert.length();
                        t = new Thread(sr);
                        t.start();
                        return;
                    } else {
                        turnOffFlash();
                        flash_off.setText("On");
                        mediaPlayer.pause();
                        transmate.setText("Transmit");
                    }

                }

            }
        });


        flash_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flash_off.getText().toString().equalsIgnoreCase("On")) {
                    turnOnFlash();
                    flash_off.setText("Off");
                    mediaPlayer.start();
                    transmate.setText("Stop");
                    sr = new StroboRunner();
                    sr.freq = txtToConvert.length();
                    t = new Thread(sr);
                    t.start();
                    return;
                } else {
                    turnOffFlash();
                    flash_off.setText("On");
                    mediaPlayer.pause();
                    transmate.setText("Transmit");
                }

            }
        });


        off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                morse_design.setVisibility(View.GONE);
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                    mediaPlayer.stop();
                }
                type_edit.setText("");
                morse_space.setText("");
                flash_off.setText("Off");
                transmate.setText("Transmit");
                wheel.setCurrentItem(0);
                value = 0;
                turnOffFlash();
                main_content.setVisibility(View.VISIBLE);

//                alertDialog1.dismiss();

            }
        });

//        alertDialog1.show();

    }

    public void showMap() {

        mInterstitialAd = new InterstitialAd(this);

        // set the ad unit ID
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");

        AdRequest adRequest = new AdRequest.Builder()
                .build();

        // Load ads into Interstitial Ads
        mInterstitialAd.loadAd(adRequest);

        mInterstitialAd.setAdListener(new AdListener() {
            public void onAdLoaded() {
                showInterstitial();
            }
        });

        main_content.setVisibility(View.GONE);
        map_lay.setVisibility(View.VISIBLE);


    }

    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    public void showColor() {
        LayoutInflater li = LayoutInflater.from(this);
        final View dialogView = li.inflate(R.layout.color_popup, null);
        /*android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7
        .app.AlertDialog.Builder(
                this);*/
        androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder =
                new androidx.appcompat.app.AlertDialog.Builder(
                this);

        alertDialogBuilder.setView(dialogView);
        alertDialogBuilder.setCancelable(true);

        color_bg = dialogView.findViewById(R.id.color_bg);
        mColorPickerView = dialogView.findViewById(R.id.colorpicker);
        cancel = dialogView.findViewById(R.id.cancel);
        done = dialogView.findViewById(R.id.done);
        alertDialog1 = alertDialogBuilder.create();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog1.dismiss();

            }
        });

        alertDialog1.show();

    }

    private void loadListeners() {
        mColorPickerView.setColorListener(new ColorPickerView.ColorListener() {
            @Override
            public void onColorSelected(int color) {
                done.setTextColor(color);
                color_bg.setBackgroundColor(color);
                main_lay.setBackgroundColor(color);


            }
        });
    }

    @Override

    public void onSensorChanged(SensorEvent event) {

        float degree = Math.round(event.values[0]);


        RotateAnimation ra = new RotateAnimation(

                currentDegree,

                -degree,

                Animation.RELATIVE_TO_SELF, 0.5f,

                Animation.RELATIVE_TO_SELF,

                0.5f);

        ra.setDuration(210);

        ra.setFillAfter(true);

        compass_image.startAnimation(ra);

        currentDegree = -degree;

    }

    @Override

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void methodRequiresTwoPermission() {
        String[] perms = {Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, perms)) {
            turnOnFlash();
        } else {
            EasyPermissions.requestPermissions(this, "Permission required", CHECK_CAMERA, perms);
        }


    }

    private void turnOnFlash() {
        isLighOn = true;
        indication_img.setImageResource(R.drawable.indicator_on);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                camManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
                String cameraId = null; // Usually front camera is at 0 position.
                if (camManager != null) {
                    cameraId = camManager.getCameraIdList()[0];
                    camManager.setTorchMode(cameraId, true);
                }
            } catch (CameraAccessException e) {
                Log.e("resposne", e.toString());
            }
        } else {
            camera = Camera.open();
            parameters = camera.getParameters();
            parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
            camera.setParameters(parameters);
            camera.startPreview();
            light_on_off.setImageResource(R.drawable.center_on_button);
            if (value != 0) {
                sr = new StroboRunner();
                sr.freq = value;
                t = new Thread(sr);
                t.start();
            }
        }
    }

    private void turnOffFlash() {
        isLighOn = false;
        indication_img.setImageResource(R.drawable.indicator_off);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                String cameraId;
                camManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
                if (camManager != null) {
                    cameraId = camManager.getCameraIdList()[0]; // Usually front camera is at 0
                    // position.
                    camManager.setTorchMode(cameraId, false);
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            camera = Camera.open();
            parameters = camera.getParameters();
            parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
            camera.setParameters(parameters);
            camera.stopPreview();
            light_on_off.setImageResource(R.drawable.center_off_button);
        }

//        try {
//            camera = Camera.open();
//            Parameters p = camera.getParameters();
//            p.setFlashMode(Parameters.FLASH_MODE_OFF);
//            camera.setParameters(p);
//            camera.stopPreview();
//            camera.release();
//            camera = null;
//            light_on_off.setImageResource(R.drawable.center_off_button);
//        } catch (Exception e) {
//            Log.e("Error", "" + e);
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        checkLocationandAddToMap();
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        turnOnFlash();

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        methodRequiresTwoPermission();
    }

    /**
     * Initializes wheel
     *
     * @param id the wheel widget Id
     */
    private void initWheel(int id) {
        wheel = getWheel(id);
        wheel.setViewAdapter(new SlotMachineAdapter(this));

        wheel.addScrollingListener(scrolledListener);
        wheel.setCyclic(true);
        wheel.setEnabled(true);
        wheel.setVertical(false);
        wheel.setCurrentItem(0);
        wheel.setInterpolator(new AnticipateOvershootInterpolator());
    }

    /**
     * Returns wheel by Id
     *
     * @param id the wheel Id
     * @return the wheel with passed Id
     */
    private WheelView getWheel(int id) {
        return (WheelView) findViewById(id);
    }

    /**
     * Mixes wheel
     *
     * @param id the wheel id
     */
    private void mixWheel(int id) {
        WheelView wheel = getWheel(id);
        wheel.scroll(-350 + (int) (Math.random() * 50), 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        turnOffFlash();
    }

    @Override
    public void onClick(View v) {
        if (v == current_location) {
            getCurrentLocation();
            moveMap();
        }
    }

    private class StroboRunner implements Runnable {

        int freq;
        boolean stopRunning = false;

        @Override
        public void run() {
//            Camera.Parameters paramsOn = camera.getParameters();
//            Camera.Parameters paramsOff = camera.getParameters();
//            paramsOn.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
//            paramsOff.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);

            try {
                while (isLighOn) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        try {
                            camManager =
                                    (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
                            String cameraId = null; // Usually front camera is at 0 position.
                            if (camManager != null) {
                                cameraId = camManager.getCameraIdList()[0];
                                camManager.setTorchMode(cameraId, true);
                            }
                        } catch (CameraAccessException e) {
                            Log.e("resposne", e.toString());
                        }
                    } else {
                        camera = Camera.open();
                        parameters = camera.getParameters();
                        parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
                        camera.setParameters(parameters);
                        camera.startPreview();
                        light_on_off.setImageResource(R.drawable.center_on_button);
                    }
                    // We make the thread sleeping
                    Thread.sleep(100 - freq);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        try {
                            String cameraId;
                            camManager =
                                    (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
                            if (camManager != null) {
                                cameraId = camManager.getCameraIdList()[0]; // Usually front
                                // camera is at 0 position.
                                camManager.setTorchMode(cameraId, false);
                            }
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    } else {
                        camera = Camera.open();
                        parameters = camera.getParameters();
                        parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
                        camera.setParameters(parameters);
                        camera.stopPreview();
                        light_on_off.setImageResource(R.drawable.center_off_button);
                    }
                    Thread.sleep(freq);
                }
            } catch (Throwable t) {
            }
        }
    }
}
