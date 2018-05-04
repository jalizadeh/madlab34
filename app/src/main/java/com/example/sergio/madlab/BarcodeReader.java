package com.example.sergio.madlab;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

import static java.lang.System.exit;

public class BarcodeReader extends AppCompatActivity {

    private static final int PHOTO_REQUEST_CODE = 6;

    private BarcodeDetector detector;
    private SurfaceView surfaceView;
    private CameraSource cameraSource;
    private TextView ISBNCodeEditText;
    private String ISBNCode;
    //I don't add here shared preferences because i don't want that the value scanned stays here permanently, i just put it on onSaveInstanceState to pass it to main activity
    private Button ButtonImport;

    private SharedPreferences preferences;
    private  SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_reader);
        surfaceView = findViewById(R.id.surface_view);
        ISBNCodeEditText =  findViewById(R.id.barcode_text);
        ButtonImport = findViewById(R.id.buttonImport);

        preferences = getSharedPreferences("ISBN", Context.MODE_PRIVATE);
        editor = preferences.edit();

        // chiediamo di individuare QR code e EAN 13
        detector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.QR_CODE | Barcode.EAN_13)
                .build();

        // verifichiamo che BarcodeDetector sia operativo
        if (!detector.isOperational()) {
            exit(0);
            return;
        }

        // istanziamo un oggetto CameraSource collegata al detector
        cameraSource = new CameraSource
                .Builder(this, detector)
                .setAutoFocusEnabled(true)
                .build();

        // gestione delle fasi di vita della SurfaceView
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                activateCamera();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        detector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> items = detections.getDetectedItems();

                if (items.size() != 0)
                    runOnUiThread(new Runnable() {
                        public void run() {
                            ISBNCode = items.valueAt(0).displayValue;
                            ISBNCodeEditText.setText(ISBNCode);
                        }
                    });

            }
        });

        ButtonImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("isbnNumber", ISBNCode);
                editor.commit();
                finish();
            }
        });

    }

    //Salva per la main activity l'ISBNCode acquisito
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("ISBNCode", ISBNCode);
    }

    private void activateCamera() {

        // verifichiamo che sia stata concessa la permission CAMERA
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                Log.d("debug","No Permission Camera");
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},PHOTO_REQUEST_CODE);

            }
        } else {
            try {
                cameraSource.start(surfaceView.getHolder());
            } catch (IOException e) {
                exit(0);
            }
        }

    }



}
