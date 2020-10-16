package com.mapp.huawei.view.image_related;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.View;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentation;
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationAnalyzer;
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationScene;
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationSetting;
import com.mapp.huawei.R;
import com.mapp.huawei.common.App;
import com.mapp.huawei.databinding.ActivityImageSegmentationBinding;

import java.io.IOException;

public class ImageSegmentation extends AppCompatActivity implements View.OnClickListener {

    private ActivityImageSegmentationBinding segmentationBinding;

    private final static String TAG = "Segmentation";
    private final static int REQUEST_CODE = 12;

    private Bitmap bitmap;
    private Uri imageUri;
    public MLImageSegmentation segmentationResult;
    private MLImageSegmentationAnalyzer analyzer;
    private MLFrame frame;
    private MLImageSegmentationSetting setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        segmentationBinding = DataBindingUtil.setContentView(this, R.layout.activity_image_segmentation);

        segmentationBinding.btnGetImg.setOnClickListener(this);

        setting = new MLImageSegmentationSetting
                .Factory()
                .setExact(true)                // Set whether to support fine segmentation. The value true indicates fine segmentation, and the value false indicates fast segmentation.
                .setAnalyzerType(MLImageSegmentationSetting.BODY_SEG)
                .setScene(MLImageSegmentationScene.ALL)
                //.setAnalyzerType(MLImageSegmentationSetting.IMAGE_SEG)
                .create();
        analyzer = MLAnalyzerFactory.getInstance().getImageSegmentationAnalyzer(setting);

    }

    private void asyncAnalyseFrame(Bitmap bitmap) {
        frame = MLFrame.fromBitmap(bitmap);
        Task<MLImageSegmentation> task = analyzer.asyncAnalyseFrame(frame);
        task.addOnSuccessListener(new
                                          OnSuccessListener<MLImageSegmentation>() {
                                              @Override
                                              public void onSuccess(MLImageSegmentation segmentation) {
                                                  segmentationBinding.imgForeground.setImageBitmap(segmentation.getForeground());
                                                  segmentationBinding.txtForeground.setText("FOREGROUND");
                                                  segmentationBinding.imgGrayscale.setImageBitmap(segmentation.getGrayscale());
                                                  segmentationBinding.txtGrayscale.setText("GRAYSCALE");
                                          /*     Bitmap bmp = BitmapFactory.decodeByteArray(segmentation.getMasks(), 0, segmentation.getMasks().length);
                                                segmentationBinding.imgMask.setImageBitmap(bmp);*/
                                                  segmentationBinding.imgOriginal.setImageBitmap(segmentation.getOriginal());
                                                  segmentationBinding.txtOriginal.setText("ORIGINAL");
                                                  details();
                                              }
                                          }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                App.LOGGER(TAG, "Exception: " + e);
            }
        });
        if (analyzer != null) {
            try {
                analyzer.stop();
            } catch (IOException e) {
            }
        }
    }

    private void analyseFrame(Bitmap bitmap) {
        frame = MLFrame.fromBitmap(bitmap);
        SparseArray<MLImageSegmentation> segmentations = analyzer.analyseFrame(frame);
        segmentationBinding.imgForeground.setImageBitmap(segmentations.get(0).getForeground());
        segmentationBinding.txtForeground.setText("FOREGROUND");
        segmentationBinding.imgGrayscale.setImageBitmap(segmentations.get(0).getGrayscale());
        segmentationBinding.txtGrayscale.setText("GRAYSCALE");
        segmentationBinding.imgOriginal.setImageBitmap(segmentations.get(0).getOriginal());
        segmentationBinding.txtOriginal.setText("ORIGINAL");
        details();
    }

    private void getImage() {
        segmentationBinding.btnGetImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        imageUri = data.getData();
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            //asyncAnalyseFrame(bitmap);
            analyseFrame(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_get_img:
                getImage();
                break;
        }
    }

    private void details() {
        String message = "";
        if (setting.getAnalyzerType() == 0) {
            message += "SEG. TYPE : BODE_SEG \n";
        } else if (setting.getAnalyzerType() == 1) {
            message += "SEG. TYPE : IMAGE_SEG \n";
        }else{
            message = setting.getAnalyzerType()+"**";
        }

        if (setting.getScene() == 0) {
            message += "all results\n";
        } else if (setting.getScene() == 1) {
            message += "pixel-level label information\n";
        } else if (setting.getScene() == 2) {
            message += "human body image with a transparent background\n";
        }

        if(setting.isExact()==true){
            message += "Whether fine detection is supported.";
        }else{
            message += "Whether fine detection is not supported.";
        }
        segmentationBinding.txtDetails.setText(message);
    }

}