package com.mapp.huawei.object_detectionactivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Toast;

import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.common.LensEngine;
import com.huawei.hms.mlsdk.common.MLAnalyzer;
import com.huawei.hms.mlsdk.objects.MLObject;
import com.huawei.hms.mlsdk.objects.MLObjectAnalyzer;
import com.huawei.hms.mlsdk.objects.MLObjectAnalyzerSetting;
import com.mapp.huawei.R;
import com.mapp.huawei.common.App;
import com.mapp.huawei.databinding.ActivityObjectDetectionCameraBinding;
import com.mapp.huawei.object_detection.ObjectDetectionRectangle;

import java.io.IOException;

public class ObjectDetectionCamera extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ObjectDetectionCamera";
    private final static int STOP_PREVIEW = 1;
    private final static int START_PREVIEW = 2;

    private MLObjectAnalyzer analyzer;
    private LensEngine mLensEngine;

    private boolean isStarted = true;
    public boolean mlsNeedToDetect = true;

    private int lensType = LensEngine.BACK_LENS;

    private ActivityObjectDetectionCameraBinding cameraBinding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraBinding = DataBindingUtil.setContentView(this, R.layout.activity_object_detection_camera);

        if (savedInstanceState != null) {
            this.lensType = savedInstanceState.getInt("lensType");
        }

        createObjectAnalyzer();
        cameraBinding.detectStart.setOnClickListener(this);
        createLensEngine();

    }


    @Override
    protected void onResume() {
        super.onResume();
        this.startLensEngine();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraBinding.objectPreview.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.mLensEngine != null) {
            this.mLensEngine.release();
        }
        if (this.analyzer != null) {
            try {
                this.analyzer.stop();
            } catch (IOException e) {
                Log.e(ObjectDetectionCamera.TAG, "Stop failed: " + e.getMessage());
            }
        }
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("lensType", this.lensType);
        super.onSaveInstanceState(savedInstanceState);
    }

    // When you need to implement a scene that stops after recognizing specific content
    // and continues to recognize after finishing processing, refer to this code
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ObjectDetectionCamera.START_PREVIEW:
                    ObjectDetectionCamera.this.mlsNeedToDetect = true;
                    Log.d("object", "start to preview");
                    ObjectDetectionCamera.this.startPreview();
                    break;
                case ObjectDetectionCamera.STOP_PREVIEW:
                    ObjectDetectionCamera.this.mlsNeedToDetect = false;
                    Log.d("object", "stop to preview");
                    ObjectDetectionCamera.this.stopPreview();
                    break;
                default:
                    break;
            }
        }
    };

    private void stopPreview() {
        this.mlsNeedToDetect = false;
        if (this.mLensEngine != null) {
            this.mLensEngine.release();
        }
        if (this.analyzer != null) {
            try {
                this.analyzer.stop();
            } catch (IOException e) {
                Log.d("object", "Stop failed: " + e.getMessage());
            }
        }
        this.isStarted = false;
    }

    private void startPreview() {
        if (this.isStarted) {
            return;
        }
        this.createObjectAnalyzer();
        cameraBinding.objectPreview.release();
        this.createLensEngine();
        this.startLensEngine();
        this.isStarted = true;
    }

    @Override
    public void onClick(View v) {
        this.mHandler.sendEmptyMessage(ObjectDetectionCamera.START_PREVIEW);
    }

    private void createObjectAnalyzer() {
        MLObjectAnalyzerSetting setting =
                new MLObjectAnalyzerSetting.Factory().setAnalyzerType(MLObjectAnalyzerSetting.TYPE_VIDEO)
                        .allowMultiResults()
                        .allowClassification()
                        .create();
        analyzer = MLAnalyzerFactory.getInstance().getLocalObjectAnalyzer(setting);
        analyzer.setTransactor(new MLAnalyzer.MLTransactor<MLObject>() {
            @Override
            public void destroy() {

            }

            @Override
            public void transactResult(MLAnalyzer.Result<MLObject> result) {
                if (!ObjectDetectionCamera.this.mlsNeedToDetect) {
                    return;
                }
                cameraBinding.objectOverlay.clear();
                SparseArray<MLObject> objectSparseArray = result.getAnalyseList();

                for (int i = 0; i < objectSparseArray.size(); i++) {
                   /* objectSparseArray.valueAt(i).setTracingIdentity(0);
                    objectSparseArray.valueAt(i).setTypePossibility(0.5f);
                    objectSparseArray.valueAt(i).setTypeIdentity(1);*/
                    ObjectDetectionRectangle graphic = new ObjectDetectionRectangle(cameraBinding.objectOverlay, objectSparseArray.valueAt(i));
                    cameraBinding.objectOverlay.add(graphic);
                }
            }
        });

        if (setting.isClassificationAllowed() == true) {
            Toast.makeText(this, "Classification Allowed", Toast.LENGTH_SHORT).show();
        }
        if (setting.isMultipleResultsAllowed() == true) {
            Toast.makeText(this, "Multiple Results Allowed", Toast.LENGTH_SHORT).show();
        }
        if (setting.getAnalyzerType() == 1) {
            Toast.makeText(this, "TYPE_VIDEO; indicating video stream-based detection", Toast.LENGTH_SHORT).show();
        } else if (setting.getAnalyzerType() == 0){
            Toast.makeText(this, "TYPE_PICTURE, indicating image-based detection", Toast.LENGTH_SHORT).show();
        }

    }

    private void createLensEngine() {
        Context context = this.getApplicationContext();
        // Create LensEngine
        this.mLensEngine = new LensEngine.Creator(context, this.analyzer).setLensType(this.lensType)
                .applyDisplayDimension(640, 480)
                .applyFps(25.0f)
                .enableAutomaticFocus(true)
                .create();
    }

    private void startLensEngine() {
        if (this.mLensEngine != null) {
            try {
                cameraBinding.objectPreview.start(this.mLensEngine, cameraBinding.objectOverlay);
            } catch (IOException e) {
                Log.e(ObjectDetectionCamera.TAG, "Failed to start lens engine.", e);
                this.mLensEngine.release();
                this.mLensEngine = null;
            }
        }
    }

}