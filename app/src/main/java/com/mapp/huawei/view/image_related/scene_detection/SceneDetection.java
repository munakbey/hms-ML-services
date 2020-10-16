package com.mapp.huawei.view.image_related.scene_detection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.SparseArray;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlsdk.common.MLException;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.scd.MLSceneDetection;
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzer;
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzerFactory;
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzerSetting;
import com.mapp.huawei.R;
import com.mapp.huawei.common.App;
import com.mapp.huawei.databinding.ActivitySceneDetectionBinding;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class SceneDetection extends AppCompatActivity {

    private final static String TAG = "Scene Detection";
    private ActivitySceneDetectionBinding sceneDetectionBinding;
    private MLSceneDetectionAnalyzer analyzer;

    private final List<Item> mItems = new ArrayList<>();
    private final ArrayList<String> files = new ArrayList<>();
    private ArrayList<String> mResult = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sceneDetectionBinding = DataBindingUtil.setContentView(this, R.layout.activity_scene_detection);


        File folder = new File(Environment.getExternalStorageDirectory().toString(), Environment.DIRECTORY_DCIM + "/Camera");
        if (folder.exists()) {
            final File[] allFiles = folder.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {

                    return (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png"));
                }

            });
            for (int i = 0; i < allFiles.length; i++) {
                files.add(allFiles[i].getAbsolutePath());
            }
            App.LOGGER(TAG, files.get(0) + " !!!!");
        }


        MLSceneDetectionAnalyzerSetting setting = new MLSceneDetectionAnalyzerSetting.Factory()
                // Set confidence for scene detection.
                .setConfidence(0.0001f)
                .create();
        App.LOGGER(TAG, "> Confidence: " + setting.getConfidence());

        //analyzer = MLSceneDetectionAnalyzerFactory.getInstance().getSceneDetectionAnalyzer();
        analyzer = MLSceneDetectionAnalyzerFactory.getInstance().getSceneDetectionAnalyzer(setting);

        for (int i = 0; i < files.size(); i++) {
            MLFrame frame = new MLFrame.Creator().setBitmap(BitmapFactory.decodeFile(files.get(i))).create();

            /* detectSyncMode(frame);
             */

            final int tmp = i;
            Task<List<MLSceneDetection>> task = analyzer.asyncAnalyseFrame(frame);
            task.addOnSuccessListener(new OnSuccessListener<List<MLSceneDetection>>() {
                public void onSuccess(List<MLSceneDetection> result) {
                    App.LOGGER(TAG, "Confidence: " + result.get(0).getConfidence());
                    mResult.add(result.get(0).getResult());
                    if (tmp == files.size() - 1) {
                        sceneDetectionBinding.gridview.setAdapter(new GridViewAdapter(SceneDetection.this, files, mResult));
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                public void onFailure(Exception e) {

                    if (e instanceof MLException) {
                        MLException mlException = (MLException) e;
                        int errorCode = mlException.getErrCode();
                        String errorMessage = mlException.getMessage();
                        e.printStackTrace();
                    } else {
                        // Other errors.
                    }
                }
            });
        }
    }

    private void detectSyncMode(MLFrame frame) {
        SparseArray<MLSceneDetection> results = analyzer.analyseFrame(frame);
        Task<List<MLSceneDetection>> task = analyzer.asyncAnalyseFrame(frame);
        task.addOnSuccessListener(new OnSuccessListener<List<MLSceneDetection>>() {
            public void onSuccess(List<MLSceneDetection> result) {
                App.LOGGER(TAG, ">> " + result.get(0).getResult());
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    public void onFailure(Exception e) {
                        if (e instanceof MLException) {
                            MLException mlException = (MLException) e;
                            int errorCode = mlException.getErrCode();
                            String errorMessage = mlException.getMessage();
                            e.printStackTrace();
                        } else {
                            // Other errors.
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        analyzer.stop();
    }
}