package com.mapp.huawei.view.image_related;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.common.LensEngine;
import com.huawei.hms.mlsdk.face.MLFaceAnalyzer;
import com.huawei.hms.mlsdk.face.MLFaceAnalyzerSetting;
import com.huawei.hms.mlsdk.text.MLTextAnalyzer;
import com.mapp.huawei.face_detection.FaceAnalyzer;
import com.mapp.huawei.R;
import com.mapp.huawei.databinding.ActivityFaceDetectionBinding;

import java.io.IOException;

public class FaceDetection extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "LiveImageDetection";

    private static final int CAMERA_PERMISSION_CODE = 2;
    MLFaceAnalyzer analyzer;
    MLTextAnalyzer txtAnalyzer = MLAnalyzerFactory.getInstance().getLocalTextAnalyzer();


    private LensEngine mLensEngine;
    private int lensType = LensEngine.BACK_LENS;

    private ActivityFaceDetectionBinding faceDetectionBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        faceDetectionBinding = DataBindingUtil.setContentView(this , R.layout.activity_face_detection);

        this.createFaceAnalyzer();
        ToggleButton facingSwitch = this.findViewById(R.id.facingSwitch);
        facingSwitch.setOnCheckedChangeListener(this);
        // Checking Camera Permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            this.createLensEngine();
        } else {
            this.requestCameraPermission();
        }

    }

    private void requestCameraPermission() {
        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, FaceDetection.CAMERA_PERMISSION_CODE);
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.startLensEngine();
    }

    @Override
    protected void onPause() {
        super.onPause();
        faceDetectionBinding.preview.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.mLensEngine != null) {
            this.mLensEngine.release();
        }
        if (this.analyzer != null) {
            this.analyzer.destroy();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != FaceDetection.CAMERA_PERMISSION_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }
        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            this.createLensEngine();
            return;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (this.mLensEngine != null) {
            if (isChecked) {
                this.lensType = LensEngine.FRONT_LENS;
            } else {
                this.lensType = LensEngine.BACK_LENS;
            }
        }
        this.mLensEngine.close();
        this.createLensEngine();
        this.startLensEngine();
    }

    private MLFaceAnalyzer createFaceAnalyzer() {
        // todo step 2: add on-device face analyzer
        MLFaceAnalyzerSetting setting = new MLFaceAnalyzerSetting.Factory()
                .setFeatureType(MLFaceAnalyzerSetting.TYPE_FEATURES)
                .setPerformanceType(fMLFaceAnalyzerSetting.TYPE_SPEED)
                .allowTracing()
                .create();
        this.analyzer = MLAnalyzerFactory.getInstance().getFaceAnalyzer(setting);
        // finish
        this.analyzer.setTransactor(new FaceAnalyzer(faceDetectionBinding.overlay));
        return this.analyzer;
    }

    private void createLensEngine() {
        Context context = this.getApplicationContext();
        // todo step 3: add on-device lens engine
        this.mLensEngine = new LensEngine.Creator(context, this.analyzer)
                .setLensType(this.lensType)
                .applyDisplayDimension(1600, 1024)
                .applyFps(25.0f)
                .enableAutomaticFocus(true)
                .create();
        // finish
    }

    private void startLensEngine() {
        if (this.mLensEngine != null) {
            try {
                faceDetectionBinding.preview.start(this.mLensEngine, faceDetectionBinding.overlay);
            } catch (IOException e) {
               // Log.e(FaceDetection.this, "Failed to start lens engine.", e);
                this.mLensEngine.release();
                this.mLensEngine = null;
            }
        }
    }

}
