package com.example.myscreener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ViewResultActivity extends AppCompatActivity implements View.OnClickListener {
    TextView display;
    ImageView image;
    Bitmap bitmap,qrCodeBitmap;
    MaterialToolbar toolBar;
    Button copy,share,browse;
    String data = MainActivity.DATA;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_result);

        display = findViewById(R.id.display);
        toolBar = findViewById(R.id.toolBar);
        copy = findViewById(R.id.copy);
        share = findViewById(R.id.share);
        browse = findViewById(R.id.browse);
        image = findViewById(R.id.image);

        copy.setOnClickListener(this);
        share.setOnClickListener(this);
        browse.setOnClickListener(this);

        display.setText(data);

        bitmap = stringToBitmap(data, 200, 100);

        // Generate the QR code bitmap
        qrCodeBitmap = generateQRCode(data);

        image.setImageBitmap(qrCodeBitmap);


    }

    public Bitmap stringToBitmap(String text, int width, int height) {
        // Create a bitmap with the specified width and height
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // Create a canvas object and associate it with the bitmap
        Canvas canvas = new Canvas(bitmap);

        // Set a background color or any other customization if needed
        canvas.drawColor(Color.WHITE);

        // Create a paint object for drawing the text
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(24); // Set the desired text size

        // Calculate the text bounds to center it within the bitmap
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        // Calculate the coordinates to center the text within the bitmap
        int x = (bitmap.getWidth() - bounds.width()) / 2;
        int y = (bitmap.getHeight() + bounds.height()) / 2;

        // Draw the text on the canvas
        canvas.drawText(text, x, y, paint);

        return bitmap;
    }

    private Bitmap generateQRCode(String text) {
        try {
            // Set QR code encoding parameters
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            // Create QR code writer
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            // Encode the text into a QR code
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 512, 512, hints);

            // Create a bitmap from the bit matrix
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.copy) {
            String text = display.getText().toString();
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", "Text to copy");
            clipboardManager.setPrimaryClip(clip);
            //clipboardManager.setText(text);
            Toast.makeText(this, "Copy to ClipBoard\n"+text, Toast.LENGTH_SHORT).show();
        } else if (v.getId() == R.id.share) {
            /*String text = display.getText().toString();
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(intent,"Share via.."));*/
            try {
                flash(false);
            } catch (CameraAccessException e) {
                throw new RuntimeException(e);
            }
        } else if (v.getId() == R.id.browse) {
            try {
                flash(true);
            } catch (CameraAccessException e) {
                throw new RuntimeException(e);
            }
            /*String text = display.getText().toString();
            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY,text);
            startActivity(intent);*/
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}