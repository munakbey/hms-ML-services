package com.mapp.huawei.view;

import android.app.Activity;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.view.View;

import com.huawei.hms.mlsdk.common.MLApplication;
import com.mapp.huawei.R;
import com.mapp.huawei.common.App;
import com.mapp.huawei.databinding.ActivityStartPageBinding;
import com.mapp.huawei.view.image_related.FaceDetection;
import com.mapp.huawei.view.image_related.ImageSegmentation;
import com.mapp.huawei.view.image_related.Landmark;
import com.mapp.huawei.view.image_related.ObjectDetection;
import com.mapp.huawei.view.image_related.scene_detection.SceneDetection;
import com.mapp.huawei.view.language_related.SoundDetection;
import com.mapp.huawei.view.nlp.TextEmbedding;
import com.mapp.huawei.view.text_related.TextRelated;
import com.mapp.huawei.view.language_related.Translate;

public class StartPage extends AppCompatActivity implements View.OnClickListener {

    ActivityStartPageBinding startPageBinding;
    public static final String TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startPageBinding = DataBindingUtil.setContentView(this, R.layout.activity_start_page);

        MLApplication.getInstance().setApiKey(App.API_KEY);

        startPageBinding.btnRecognition.setOnClickListener(this);
        startPageBinding.btnTranslate.setOnClickListener(this);
        startPageBinding.btnObjectDetection.setOnClickListener(this);
        startPageBinding.btnLandmark.setOnClickListener(this);
        startPageBinding.btnFaceDetection.setOnClickListener(this);
        startPageBinding.btnImgSegmnt.setOnClickListener(this);
        startPageBinding.btnSoundDetection.setOnClickListener(this);
        startPageBinding.btnTextEmbedding.setOnClickListener(this);
        startPageBinding.btnSceneDetection.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_recognition:
                changeActivity(new TextRelated());
                break;
            case R.id.btn_translate:
                changeActivity(new Translate());
                break;
            case R.id.btn_object_detection:
                changeActivity(new ObjectDetection());
                break;
            case R.id.btn_landmark:
                changeActivity(new Landmark());
                break;
            case R.id.btn_face_detection:
                changeActivity(new FaceDetection());
                break;
            case R.id.btn_img_segmnt:
                changeActivity(new ImageSegmentation());
                break;
            case R.id.btn_sound_detection:
                changeActivity(new SoundDetection());
                break;
            case R.id.btn_text_embedding:
                changeActivity(new TextEmbedding());
                break;
            case R.id.btn_scene_detection:
                changeActivity(new SceneDetection());
                break;
        }
    }

    private void changeActivity(Activity nextAct) {
        Intent intent = new Intent(StartPage.this, nextAct.getClass());
        startActivity(intent);
    }
}