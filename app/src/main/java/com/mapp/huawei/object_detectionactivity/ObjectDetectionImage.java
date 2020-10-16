package com.mapp.huawei.object_detectionactivity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.objects.MLObject;
import com.huawei.hms.mlsdk.objects.MLObjectAnalyzer;
import com.huawei.hms.mlsdk.objects.MLObjectAnalyzerSetting;
import com.mapp.huawei.view.image_related.ObjectDetection;
import com.mapp.huawei.R;
import com.mapp.huawei.databinding.ActivityObjectDetectionImageBinding;


import java.io.IOException;

import java.util.List;

public class ObjectDetectionImage extends AppCompatActivity {

    final private static int PICK_IMAGE = 1;
    final private static String TAG = "ObjectDetectionImage";

    private ActivityObjectDetectionImageBinding imageBinding;

    Bitmap bitmap;
    MLObjectAnalyzer analyzer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        imageBinding = DataBindingUtil.setContentView(this, R.layout.activity_object_detection_image);

        createObjectAnalyzer();
        pickImage();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {

            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (bitmap != null) {
                analyze(bitmap);
            }
        } else {
            Toast.makeText(getApplicationContext(), "No image is selected.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(ObjectDetectionImage.this, ObjectDetection.class));
        }

    }

    public void pickImage() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);


    }

    private void analyze(final Bitmap bitmap) {

        MLFrame frame = MLFrame.fromBitmap(bitmap);
        // Create a task to process the result returned by the object detector.
        Task<List<MLObject>> task = analyzer.asyncAnalyseFrame(frame);
        // Asynchronously process the result returned by the object detector.
        task.addOnSuccessListener(new OnSuccessListener<List<MLObject>>() {
            @Override
            public void onSuccess(List<MLObject> objects) {
                imageBinding.imageView.setImageBitmap(drawItems(objects, bitmap));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, e.toString());
            }
        });

    }

    private void createObjectAnalyzer() {
        // Use MLObjectAnalyzerSetting.TYPE_PICTURE for static image detection.
        MLObjectAnalyzerSetting setting = new MLObjectAnalyzerSetting.Factory()
                .setAnalyzerType(MLObjectAnalyzerSetting.TYPE_PICTURE)
                .allowMultiResults()
                .allowClassification()
                .create();
        analyzer = MLAnalyzerFactory.getInstance().getLocalObjectAnalyzer(setting);

    }

    private Bitmap drawItems(List<MLObject> mlObjects, Bitmap bitmap) {

        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        String mlObjectType;

        for (int i = 0; i < mlObjects.size(); i++) {
            int mlObjectTypeValue = mlObjects.get(i).getTypeIdentity();
            switch (mlObjectTypeValue) {
                case MLObject.TYPE_OTHER:
                    mlObjectType = "Other";
                    break;
                case MLObject.TYPE_FACE:
                    mlObjectType = "Face";
                    break;
                case MLObject.TYPE_FOOD:
                    mlObjectType = "Food";
                    break;
                case MLObject.TYPE_FURNITURE:
                    mlObjectType = "Furniture";
                    break;
                case MLObject.TYPE_PLACE:
                    mlObjectType = "Place";
                    break;
                case MLObject.TYPE_PLANT:
                    mlObjectType = "Plant";
                    break;
                default:
                    mlObjectType = "no match";
                    break;
            }

            Paint paint = new Paint();

            paint.setColor(Color.YELLOW);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4F);

            canvas.drawRect(mlObjects.get(i).getBorder(), paint);

            Paint paint1 = new Paint();

            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setTextSize(16F);

            canvas.drawText(mlObjectType, (float) (mlObjects.get(i).getBorder().left + 20), (float) (mlObjects.get(i).getBorder().top + 20), paint1);
        }
        return mutableBitmap;
    }


    @Override
    protected void onPause() {
        super.onPause();
        try {
            analyzer.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}