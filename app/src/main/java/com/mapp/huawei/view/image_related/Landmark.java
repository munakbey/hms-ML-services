package com.mapp.huawei.view.image_related;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.landmark.MLRemoteLandmark;
import com.huawei.hms.mlsdk.landmark.MLRemoteLandmarkAnalyzer;
import com.huawei.hms.mlsdk.landmark.MLRemoteLandmarkAnalyzerSetting;
import com.mapp.huawei.R;
import com.mapp.huawei.databinding.ActivityLandmarkBinding;

import java.util.List;

public class Landmark extends AppCompatActivity {

    private static String TAG = "Landmark";
    private Uri imageUri;

    private ActivityLandmarkBinding landmarkBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        landmarkBinding = DataBindingUtil.setContentView(this, R.layout.activity_landmark);

        landmarkBinding.btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 12);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imageUri = data.getData();
        landmarkBinding.image.setImageURI(imageUri);

        BitmapDrawable drawable = (BitmapDrawable) landmarkBinding.image.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        landmark(bitmap);
    }

    private void landmark(Bitmap bitmap) {
        MLRemoteLandmarkAnalyzerSetting settings = new MLRemoteLandmarkAnalyzerSetting.Factory()
                .setLargestNumOfReturns(1)
                .setPatternType(MLRemoteLandmarkAnalyzerSetting.STEADY_PATTERN)
                .create();
        MLRemoteLandmarkAnalyzer analyzer = MLAnalyzerFactory.getInstance().getRemoteLandmarkAnalyzer(settings);

        MLFrame mlFrame = new MLFrame.Creator().setBitmap(bitmap).create();
        Task<List<MLRemoteLandmark>> task = analyzer.asyncAnalyseFrame(mlFrame);
        task.addOnSuccessListener(new OnSuccessListener<List<MLRemoteLandmark>>() {
            public void onSuccess(List<MLRemoteLandmark> landmarkResults) {
                Log.e(TAG, landmarkResults.get(0).getLandmark());
                landmarkBinding.txtInfo.setText(landmarkResults.get(0).getLandmark());
            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(Exception e) {
                Log.e(TAG, "Error");
            }
        });
    }
}