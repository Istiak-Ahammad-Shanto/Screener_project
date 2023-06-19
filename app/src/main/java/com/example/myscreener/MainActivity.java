package com.example.myscreener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;

import eu.livotov.labs.android.camview.ScannerLiveView;
import eu.livotov.labs.android.camview.scanner.decoder.zxing.ZXDecoder;

public class MainActivity extends AppCompatActivity {
    ScannerLiveView scannerID;
    BottomNavigationView bottom_nav;
    public static String DATA = "";
    public boolean permission = false;
    public LinearLayout bottomSheet;
    BottomSheetBehavior<LinearLayout> behavior;


    private android.hardware.camera2.CameraManager cameraManager;
    private String cameraId;
    private boolean isFlashlightOn = false;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;


    @SuppressLint({"MissingInflatedId", "ServiceCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scannerID = findViewById(R.id.scannerID);
        bottom_nav = findViewById(R.id.bottom_nav);
        bottomSheet = findViewById(R.id.bottomSheet);



        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (checkPermission()) {
                Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show();

            } else {
                requstPermission();
            }
        }*/


        cameraManager = (android.hardware.camera2.CameraManager) getSystemService(Context.CAMERA_SERVICE);

        // Check for camera permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            initializeFlashlight();
        }


        if (permission) {
            dexter();
        } else {
            dexter();
        }

        bottom_nav.setOnItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("ServiceCast")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if (item.getItemId() == R.id.flash) {
                    //toggleFlashlight();
                    try {
                        flash(true);
                    } catch (CameraAccessException e) {
                        throw new RuntimeException(e);
                    }


                } else {
                    Toast.makeText(MainActivity.this, "Flash not found", Toast.LENGTH_SHORT).show();
                }

                return false;
            }
        });


    }//<<<<<<<<<<<<<<<<<<<<<<<<<<<< onCreate end here >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>//


    public void flash(boolean input) throws CameraAccessException {
        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                android.hardware.camera2.CameraManager cameraManager1 = (android.hardware.camera2.CameraManager) getSystemService(CAMERA_SERVICE);
                String cameraID = null;
                try {
                    cameraID = cameraManager1.getCameraIdList()[0];
                    cameraManager1.setTorchMode(cameraID, input);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            } else {
                Camera camera = Camera.open();
                Camera.Parameters p = camera.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(p);

                SurfaceTexture texture = new SurfaceTexture(0);
                try {
                    camera.setPreviewTexture(texture);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (input) {
                    camera.startPreview();
                } else {
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.stopPreview();
                    camera.release();
                }


            }
        }
    }

    private void initializeFlashlight() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraId = cameraManager.getCameraIdList()[0];
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void toggleFlashlight() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, !isFlashlightOn);
                isFlashlightOn = !isFlashlightOn;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    public void dexter() {

        Dexter.withContext(MainActivity.this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        try {
                            screener();
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        //System.exit(0);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();

    }


    public void screener() {
        scannerID.setScannerViewEventListener(new ScannerLiveView.ScannerViewEventListener() {
            @Override
            public void onScannerStarted(ScannerLiveView scanner) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    scanner.setForceDarkAllowed(false);
                }
            }

            @Override
            public void onScannerStopped(ScannerLiveView scanner) {

            }

            @Override
            public void onScannerError(Throwable err) {
                Toast.makeText(MainActivity.this, "" + err.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeScanned(String data) {
                //Toast.makeText(MainActivity.this, "" + data, Toast.LENGTH_SHORT).show();
                DATA = data;
                Intent intent = new Intent(MainActivity.this, ViewResultActivity.class);
                intent.putExtra("data", data);
                startActivity(intent);
                /*behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setPeekHeight(200);
                behavior.setHideable(true);
                behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {

                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                    }
                });*/
            }
        });
    }

/*
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private boolean checkPermission() {
        int camara_permission = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA_SERVICE);
        int vibrete_permission = ContextCompat.checkSelfPermission(getApplicationContext(), VIBRATOR_SERVICE);
        return camara_permission == PackageManager.PERMISSION_GRANTED &&
                vibrete_permission == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void requstPermission() {
        int PERMISSION_CODE = 200;
        ActivityCompat.requestPermissions(this, new String[]{CAMERA_SERVICE, VIBRATOR_SERVICE}, PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > grantResults[0]) {
            boolean camaraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean vibreteAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
            if (camaraAccepted && vibreteAccepted) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Deny", Toast.LENGTH_SHORT).show();
            }
        }
    }*/


    @Override
    protected void onPause() {
        super.onPause();
        scannerID.stopScanner();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ZXDecoder decoder = new ZXDecoder();
        decoder.setScanAreaPercent(0.8);
        scannerID.setDecoder(decoder);
        scannerID.startScanner();
    }


}