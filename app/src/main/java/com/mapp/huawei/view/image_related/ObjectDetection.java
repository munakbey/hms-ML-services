package com.mapp.huawei.view.image_related;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.mapp.huawei.R;
import com.mapp.huawei.object_detectionactivity.ObjectDetectionCamera;
import com.mapp.huawei.object_detectionactivity.ObjectDetectionImage;

public class ObjectDetection extends AppCompatActivity implements View.OnClickListener  {

    Button objectDetectionCamera;
    Button objectDetectionPicture;

    private static final int PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_detection);

        objectDetectionCamera = findViewById(R.id.obj_detection_cam);
        objectDetectionPicture = findViewById(R.id.obj_detection_pic);

        objectDetectionCamera.setOnClickListener(this);
        objectDetectionPicture.setOnClickListener(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermission();
        }

    }

    private void requestPermission() {
        final String[] permissions = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
                !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, permissions, ObjectDetection.PERMISSION_CODE);
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != ObjectDetection.PERMISSION_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }
    }
    @Override
    public void onClick(View view) {

        switch(view.getId()){
            case R.id.obj_detection_cam:
                startActivity(new Intent(ObjectDetection.this, ObjectDetectionCamera.class));
                break;
            case R.id.obj_detection_pic:
                 startActivity(new Intent(ObjectDetection.this, ObjectDetectionImage.class));
                break;
        }
    }


}