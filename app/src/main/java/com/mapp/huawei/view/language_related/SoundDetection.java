package com.mapp.huawei.view.language_related;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.huawei.hms.mlsdk.sounddect.MLSoundDectListener;
import com.huawei.hms.mlsdk.sounddect.MLSoundDector;
import com.mapp.huawei.R;
import com.mapp.huawei.common.App;
import com.mapp.huawei.databinding.ActivitySoundDetectionBinding;

public class SoundDetection extends AppCompatActivity implements View.OnClickListener {

    private ActivitySoundDetectionBinding soundDetectionBinding;
    private MLSoundDector soundDector;
    private final String TAG = "SoundDetection";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        soundDetectionBinding = DataBindingUtil.setContentView(this, R.layout.activity_sound_detection);

        soundDetectionBinding.imgDetection.setOnClickListener(this);
        soundDetectionBinding.txtStop.setOnClickListener(this);

        soundDector = MLSoundDector.createSoundDector();
        soundDector.setSoundDectListener(listener);

    }

    private MLSoundDectListener listener = new MLSoundDectListener() {
        @Override
        public void onSoundSuccessResult(Bundle result) {
           // result.gets
            int soundType = result.getInt(MLSoundDector.RESULTS_RECOGNIZED);
            soundDetectionBinding.txtResult.setText("Detection Result: "+soundType );
        }

        @Override
        public void onSoundFailResult(int errCode) {
            App.LOGGER(TAG, "Error: " + errCode);
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_detection:
                soundDector.start(this);
                break;
            case R.id.txt_stop:
                soundDector.stop(); // Context.
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundDector.destroy();
    }
}