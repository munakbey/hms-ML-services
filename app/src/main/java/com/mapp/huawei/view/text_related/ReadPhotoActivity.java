package com.mapp.huawei.view.text_related;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.AlertDialog;
import android.os.Bundle;

import com.huawei.hms.mlsdk.common.MLAnalyzer;
import com.huawei.hms.mlsdk.common.MLApplication;
import com.huawei.hms.mlsdk.text.MLRemoteTextSetting;
import com.huawei.hms.mlsdk.text.TextLanguage;
import com.huawei.hms.mlsdk.tts.MLTtsAudioFragment;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.view.View;
import android.widget.Toast;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.text.MLLocalTextSetting;
import com.huawei.hms.mlsdk.text.MLText;
import com.huawei.hms.mlsdk.text.MLTextAnalyzer;
import com.huawei.hms.mlsdk.tts.MLTtsCallback;
import com.huawei.hms.mlsdk.tts.MLTtsConfig;
import com.huawei.hms.mlsdk.tts.MLTtsConstants;
import com.huawei.hms.mlsdk.tts.MLTtsEngine;
import com.huawei.hms.mlsdk.tts.MLTtsError;
import com.huawei.hms.mlsdk.tts.MLTtsWarn;

import com.mapp.huawei.R;
import com.mapp.huawei.common.App;
import com.mapp.huawei.databinding.ActivityReadPhotoBinding;
import com.mapp.huawei.text_recogutil.Constant;
import com.mapp.huawei.text_recogutil.BitmapUtils;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ReadPhotoActivity extends AppCompatActivity {

    private Uri imageUri;
    private String path;
    public static Bitmap originBitmap;
    private Integer maxWidthOfImage;
    private Integer maxHeightOfImage;
    private boolean isLandScape;

    private static String TAG = "ReadPhotoActivity";
    private int REQUEST_CHOOSE_ORIGINPIC = 2001;
    private int REQUEST_TAKE_PHOTO = 2000;
    private static final String KEY_IMAGE_URI = "asd";
    private static final String KEY_IMAGE_MAX_WIDTH = "5000";
    private static final String KEY_IMAGE_MAX_HEIGHT = "5000";
    private String sourceText = "";

    private ActivityReadPhotoBinding readPhotoBinding;
    private MLTextAnalyzer textAnalyzer;
    private MLTtsEngine mlTtsEngine;
    private TextLanguage textLanguage;


    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        readPhotoBinding = DataBindingUtil.setContentView(this, R.layout.activity_read_photo);
        MLApplication.getInstance().setApiKey(App.API_KEY);

        this.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReadPhotoActivity.this.finish();
            }
        });

        isLandScape = (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);

        createLocalTextAnalyzer();
        //createRemoteTextAnalyzer();
        createTtsEngine();
        initAction();

        readPhotoBinding.imgDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext() , DocumentRecognition.class);
                startActivity(i);
            }
        });

    }

    private void initAction() {
        readPhotoBinding.relativateChooseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReadPhotoActivity.this.selectLocalImage(ReadPhotoActivity.this.REQUEST_CHOOSE_ORIGINPIC);
            }
        });

        readPhotoBinding.relativateCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReadPhotoActivity.this.takePhoto(ReadPhotoActivity.this.REQUEST_TAKE_PHOTO);
            }
        });

        readPhotoBinding.relativateRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ReadPhotoActivity.this.sourceText == null) {
                    Toast.makeText(ReadPhotoActivity.this.getApplicationContext(), R.string.please_select_picture_trec, Toast.LENGTH_SHORT).show();
                } else {
                    ReadPhotoActivity.this.mlTtsEngine.speak(sourceText, MLTtsEngine.QUEUE_APPEND);
                    Toast.makeText(ReadPhotoActivity.this.getApplicationContext(), R.string.read_start_trec, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void takePhoto(int requestCode) {
        Intent intent = new Intent(ReadPhotoActivity.this, CapturePhotoActivity.class);
        intent.putExtra("flagBankCard" , false);
        intent.putExtra("flagGeneralCard" , false);
        this.startActivityForResult(intent, requestCode);
    }

    private void selectLocalImage(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        this.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == this.REQUEST_CHOOSE_ORIGINPIC)
                && (resultCode == Activity.RESULT_OK)) {
            // In this case, imageUri is returned by the chooser, save it.
            imageUri = data.getData();
            loadOriginImage();
            // syncTextAnalyser();
            asyncTextAnalyser();

        } else if ((requestCode == this.REQUEST_TAKE_PHOTO)
                && (resultCode == Activity.RESULT_OK)
                && data != null) {
            path = data.getStringExtra(Constant.IMAGE_PATH_VALUE);
            loadCameraImage();
            //syncTextAnalyser();
            asyncTextAnalyser();
        }
    }

    private void loadCameraImage() {
        FileInputStream fis = null;

        try {
            if (path == null) {
                return;
            }
            fis = new FileInputStream(path);
            this.originBitmap = BitmapFactory.decodeStream(fis);
            this.originBitmap = this.originBitmap.copy(Bitmap.Config.ARGB_4444, true);
            readPhotoBinding.previewPane.setImageBitmap(this.originBitmap);
        } catch (IOException e) {
            Log.e(TAG, "file not found");
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException error) {
                    Log.e(TAG, "Load camera image failed: " + error.getMessage());
                }
            }
        }
    }

    private void createTtsEngine() {
        MLTtsConfig mlConfigs = new MLTtsConfig()
                .setLanguage(MLTtsConstants.TTS_EN_US)
                .setPerson(MLTtsConstants.TTS_SPEAKER_FEMALE_EN)
                .setSpeed(1.0f)
                .setVolume(1.0f);
        this.mlTtsEngine = new MLTtsEngine(mlConfigs);
        mlTtsEngine.updateConfig(mlConfigs);
        MLTtsCallback callback = new MLTtsCallback() {
            @Override
            public void onError(String taskId, MLTtsError err) {
            }

            @Override
            public void onWarn(String taskId, MLTtsWarn warn) {
            }

            @Override
            public void onRangeStart(String taskId, int start, int end) {
            }

            @Override
            public void onAudioAvailable(String s, MLTtsAudioFragment mlTtsAudioFragment, int i, Pair<Integer, Integer> pair, Bundle bundle) {

            }

            @Override
            public void onEvent(String taskId, int eventName, Bundle bundle) {
                if (eventName == MLTtsConstants.EVENT_PLAY_STOP) {
                    if (!bundle.getBoolean(MLTtsConstants.EVENT_PLAY_STOP_INTERRUPTED)) {
                        Toast.makeText(ReadPhotoActivity.this.getApplicationContext(), R.string.read_finish_trec, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        mlTtsEngine.setTtsCallback(callback);
    }

    private void createLocalTextAnalyzer() {
        MLLocalTextSetting setting = new MLLocalTextSetting.Factory()
                .setOCRMode(MLLocalTextSetting.OCR_DETECT_MODE)
                .setLanguage("en")
                .create();
        textAnalyzer = MLAnalyzerFactory.getInstance().getLocalTextAnalyzer(setting);
    }

    private void createRemoteTextAnalyzer() {
        List<String> languageList = new ArrayList();
        languageList.add("en");
        languageList.add("zh");
        MLRemoteTextSetting setting = new MLRemoteTextSetting.Factory()
                // Set the on-cloud text detection mode.
                // MLRemoteTextSetting.OCR_COMPACT_SCENE: dense text recognition
                // MLRemoteTextSetting.OCR_LOOSE_SCENE: sparse text recognition
                .setTextDensityScene(MLRemoteTextSetting.OCR_LOOSE_SCENE)                // Specify the languages that can be recognized, which should comply with ISO 639-1.
                .setLanguageList(languageList)
                // Set the format of the returned text border box.
                // MLRemoteTextSetting.NGON: Return the coordinates of the four corner points of the quadrilateral.
                // MLRemoteTextSetting.ARC: Return the corner points of a polygon border in an arc. The coordinates of up to 72 corner points can be returned.
                .setBorderType(MLRemoteTextSetting.ARC)
                .create();
        textAnalyzer = MLAnalyzerFactory.getInstance().getRemoteTextAnalyzer(setting);
    }

    private void asyncTextAnalyser() {
        if (isChosen(this.originBitmap)) {
            MLFrame mlFrame = new MLFrame.Creator().setBitmap(this.originBitmap).create();
            Task<MLText> task = textAnalyzer.asyncAnalyseFrame(mlFrame);
            task.addOnSuccessListener(new OnSuccessListener<MLText>() {
                @Override
                public void onSuccess(MLText mlText) {
                    App.LOGGER(TAG, textAnalyzer.isAvailable() + " <- ");
                    // Transacting logic for segment success.
                    if (mlText != null) {
                        ReadPhotoActivity.this.remoteDetectSuccess(mlText);
                    } else {
                        ReadPhotoActivity.this.displayFailure();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    // Transacting logic for segment failure.
                    ReadPhotoActivity.this.displayFailure();
                    return;
                }
            });
        } else {
            Toast.makeText(this.getApplicationContext(), R.string.please_select_picture_trec, Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void syncTextAnalyser() {
        if (isChosen(this.originBitmap)) {
            MLFrame mlFrame = new MLFrame.Creator().setBitmap(this.originBitmap).create();
            SparseArray<MLText.Block> result = this.textAnalyzer.analyseFrame(mlFrame);
            App.LOGGER(TAG, result.size() + " ### ");
/*
            for (int i = 0; i < result.size(); i++) {
                App.LOGGER(TAG,result.get(i).getStringValue() +" ### ");
            }
*/

        } else {
            Toast.makeText(this.getApplicationContext(), R.string.please_select_picture_trec, Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void remoteDetectSuccess(MLText mlTexts) {
        this.sourceText = "";
        List<MLText.Block> blocks = mlTexts.getBlocks();
        List<MLText.TextLine> lines = new ArrayList<>();
        List<MLText.Base> words = new ArrayList<>();
        String tmp = "";

        for (MLText.Block block : blocks) {
            for (MLText.TextLine line : block.getContents()) {
                if (line.getStringValue() != null) {
                    lines.add(line);
                }
            }
            for (MLText.Base word : block.getContents()) {
                words.add(word);
                tmp += "Language: " + word.getLanguage() + "\n";
                App.LOGGER(TAG,  word.getLanguageList().size()+"Languages List Size: " + word.getLanguageList().get(0).getLanguage() + " Border: "+word.getBorder()+"\n");
            }
        }

        Collections.sort(lines, new SortComparator());
        for (int i = 0; i < lines.size(); i++) {
            this.sourceText = this.sourceText + lines.get(i).getStringValue().trim() + " -> Rotating Degree: " + lines.get(i).getRotatingDegree() +
                    " Is Vertical: " + lines.get(i).isVertical() + " Content: " + lines.get(i).getContents().get(0).getStringValue() + "\n";
        }

        for (int i = 0; i < words.size(); i++) {
            tmp += words.get(i).getStringValue() + " (" + words.get(i).getLanguage() + ") Get Possibility: " + words.get(i).getPossibility() + "\n";
        }


        final String msg = tmp;
        readPhotoBinding.txtDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    getDetails(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        readPhotoBinding.translateResult.setText(this.sourceText);
    }

    private static class SortComparator implements Comparator<MLText.Base> {
        @Override
        public int compare(MLText.Base base, MLText.Base t1) {
            Point[] point1 = base.getVertexes();
            Point[] point2 = t1.getVertexes();
            return point1[0].y - point2[0].y;
        }
    }

    private void displayFailure() {
        Toast.makeText(this.getApplicationContext(), "Fail", Toast.LENGTH_SHORT).show();
    }

    private boolean isChosen(Bitmap bitmap) {
        if (bitmap == null) {
            return false;
        } else {
            return true;
        }
    }

    private void loadOriginImage() {
        if (this.imageUri == null) {
            return;
        }
        Pair<Integer, Integer> targetedSize = this.getTargetSize();
        int targetWidth = targetedSize.first;
        int maxHeight = targetedSize.second;

        originBitmap = BitmapUtils.loadFromPath(ReadPhotoActivity.this, this.imageUri, targetWidth, maxHeight);
        // Determine how much to scale down the image.
        readPhotoBinding.previewPane.setImageBitmap(this.originBitmap);
    }

    // Returns max width of image.
    private Integer getMaxWidthOfImage() {
        if (this.maxWidthOfImage == null) {
            if (this.isLandScape) {
                this.maxWidthOfImage = ((View) readPhotoBinding.previewPane.getParent()).getHeight();
            } else {
                this.maxWidthOfImage = ((View) readPhotoBinding.previewPane.getParent()).getWidth();
            }
        }
        return this.maxWidthOfImage;
    }

    // Returns max height of image.
    private Integer getMaxHeightOfImage() {
        if (this.maxHeightOfImage == null) {
            if (this.isLandScape) {
                this.maxHeightOfImage = ((View) readPhotoBinding.previewPane.getParent()).getWidth();
            } else {
                this.maxHeightOfImage = ((View) readPhotoBinding.previewPane.getParent()).getHeight();
            }
        }
        return this.maxHeightOfImage;
    }

    // Gets the targeted size(width / height).
    private Pair<Integer, Integer> getTargetSize() {
        Integer targetWidth;
        Integer targetHeight;
        Integer maxWidth = this.getMaxWidthOfImage();
        Integer maxHeight = this.getMaxHeightOfImage();
        targetWidth = this.isLandScape ? maxHeight : maxWidth;
        targetHeight = this.isLandScape ? maxWidth : maxHeight;
        Log.i(ReadPhotoActivity.TAG, "height:" + targetHeight + ",width:" + targetWidth);
        return new Pair<>(targetWidth, targetHeight);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.textAnalyzer != null) {
            try {
                textAnalyzer.stop();
            } catch (IOException e) {
                Log.e(ReadPhotoActivity.TAG, "Stop analyzer failed: " + e.getMessage());
            }
        }
        if (this.mlTtsEngine != null) {
            this.mlTtsEngine.stop();
        }
        this.imageUri = null;
        this.path = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ReadPhotoActivity.KEY_IMAGE_URI, this.imageUri);
        if (this.maxWidthOfImage != null) {
            outState.putInt(ReadPhotoActivity.KEY_IMAGE_MAX_WIDTH, this.maxWidthOfImage);
        }
        if (this.maxHeightOfImage != null) {
            outState.putInt(ReadPhotoActivity.KEY_IMAGE_MAX_HEIGHT, this.maxHeightOfImage);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void getDetails(String msg) throws IOException {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Details");

        if (textAnalyzer.getAnalyseType() == 0) {
            msg += "\nAnalyse Type: OCR_LOCAL_TYPE, indicating the on-device detection mode.";
        } else if (textAnalyzer.getAnalyseType() == 1) {
            msg += "\nAnalyse Type: OCR_REMOTE_TYPE, indicating the on-cloud detection mode.";
        }

        builder.setMessage(msg);
        builder.setNegativeButton("OK", null);
        builder.show();
    }


    public class OcrDetectorProcessor implements MLAnalyzer.MLTransactor<MLText.Block> {
        @Override
        public void transactResult(MLAnalyzer.Result<MLText.Block> results) {
            SparseArray<MLText.Block> items = results.getAnalyseList();
            // Determine detection result processing as required. Note that only the detection results are processed.
            // Other detection-related APIs provided by ML Kit cannot be called.
        }

        @Override
        public void destroy() {
            // Callback method used to release resources when the detection ends.
        }
    }
}