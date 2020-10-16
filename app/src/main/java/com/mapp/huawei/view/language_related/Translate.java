package com.mapp.huawei.view.language_related;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlplugin.asr.MLAsrCaptureActivity;
import com.huawei.hms.mlplugin.asr.MLAsrCaptureConstants;
import com.huawei.hms.mlsdk.asr.MLAsrConstants;
import com.huawei.hms.mlsdk.common.MLException;
import com.huawei.hms.mlsdk.langdetect.MLDetectedLang;
import com.huawei.hms.mlsdk.langdetect.MLLangDetectorFactory;
import com.huawei.hms.mlsdk.langdetect.cloud.MLRemoteLangDetector;
import com.huawei.hms.mlsdk.langdetect.cloud.MLRemoteLangDetectorSetting;
import com.huawei.hms.mlsdk.langdetect.local.MLLocalLangDetector;
import com.huawei.hms.mlsdk.langdetect.local.MLLocalLangDetectorSetting;
import com.huawei.hms.mlsdk.model.download.MLLocalModelManager;
import com.huawei.hms.mlsdk.model.download.MLModelDownloadListener;
import com.huawei.hms.mlsdk.model.download.MLModelDownloadStrategy;
import com.huawei.hms.mlsdk.translate.MLTranslatorFactory;
import com.huawei.hms.mlsdk.translate.cloud.MLRemoteTranslateSetting;
import com.huawei.hms.mlsdk.translate.cloud.MLRemoteTranslator;
import com.huawei.hms.mlsdk.translate.local.MLLocalTranslateSetting;
import com.huawei.hms.mlsdk.translate.local.MLLocalTranslator;
import com.huawei.hms.mlsdk.translate.local.MLLocalTranslatorModel;
import com.huawei.hms.mlsdk.tts.MLTtsAudioFragment;
import com.huawei.hms.mlsdk.tts.MLTtsCallback;
import com.huawei.hms.mlsdk.tts.MLTtsConfig;
import com.huawei.hms.mlsdk.tts.MLTtsConstants;
import com.huawei.hms.mlsdk.tts.MLTtsEngine;
import com.huawei.hms.mlsdk.tts.MLTtsError;
import com.huawei.hms.mlsdk.tts.MLTtsWarn;
import com.mapp.huawei.R;
import com.mapp.huawei.databinding.ActivityTranslateBinding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Translate extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    private static final int AUDIO_RECORD_REQUEST_CODE = 0;
    private static final String TAG = "Translate";
    private int progress = 0;

    Spinner languageSpinner;
    TextView txtResult, txtTranslate, txtLanguage;
    ImageView imgMicrTarget, imgAudioSrc;

    private HashMap<String, String> languages = new HashMap<String, String>();
    private ActivityTranslateBinding translateBinding;

    public MLLocalTranslator localTranslator;
    private MLLocalTranslateSetting localTranslateSetting;
    private MLRemoteTranslateSetting remoteTranslateSetting;
    private MLTtsConfig ttsConfig;
    private MLTtsEngine ttsEngine;
    private MLTtsCallback callback;
    private MLRemoteLangDetector mlRemoteLangDetector;
    private MLLocalLangDetector localLangDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        translateBinding = DataBindingUtil.setContentView(this, R.layout.activity_translate);

        definition();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.languages, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_checked);
        languageSpinner.setAdapter(adapter);
        int initialposition = languageSpinner.getSelectedItemPosition();
        languageSpinner.setSelection(initialposition, false);
        languageSpinner.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) this);

        setLangCode();

        translateBinding.translateButton.setOnClickListener(this);
        translateBinding.txtDownloadModel.setOnClickListener(this);
        imgAudioSrc.setOnClickListener(this);
        translateBinding.txtDelModels.setOnClickListener(this);
        translateBinding.txtTtsDetails.setOnClickListener(this);
        translateBinding.txtPause.setOnClickListener(this);
        translateBinding.txtResume.setOnClickListener(this);
        translateBinding.txtStop.setOnClickListener(this);
        translateBinding.txtDetect.setOnClickListener(this);

        translateBinding.progressBar.setVisibility(View.GONE);

        callback = new MLTtsCallback() {
            @Override
            public void onError(String s, MLTtsError mlTtsError) {
                Toast.makeText(Translate.this, "onError tts callback", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onWarn(String s, MLTtsWarn mlTtsWarn) {
                Toast.makeText(Translate.this, "onWarning tts callback", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRangeStart(String s, int i, int i1) {
                Toast.makeText(Translate.this, "onRangeStart tts callback", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAudioAvailable(String s, MLTtsAudioFragment mlTtsAudioFragment, int i, Pair<Integer, Integer> pair, Bundle bundle) {
                Toast.makeText(Translate.this, "onAudioAvailable tts callback", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEvent(String s, int i, Bundle bundle) {

            }
        };

    }

    private void definition() {
        languageSpinner = translateBinding.languageSpinner;
        txtResult = translateBinding.translationText;
        txtTranslate = translateBinding.translateEdittext;
        txtLanguage = translateBinding.translationLanguageText;
        imgMicrTarget = translateBinding.imgMicrophoneTarget;
        imgAudioSrc = translateBinding.imgAudioSource;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void translate() {
        MLRemoteLangDetectorSetting mlRemoteLangDetectorSetting = new MLRemoteLangDetectorSetting.Factory()
                .setTrustedThreshold(0.01f)
                .create();
        mlRemoteLangDetector = MLLangDetectorFactory.getInstance()
                .getRemoteLangDetector(mlRemoteLangDetectorSetting);

        Task<List<MLDetectedLang>> probabilityDetectTask = mlRemoteLangDetector.probabilityDetect(txtTranslate.getText().toString());
        probabilityDetectTask.addOnSuccessListener(new OnSuccessListener<List<MLDetectedLang>>() {
            public void onSuccess(List<MLDetectedLang> result) {

                String targetLang = languages.get(languageSpinner.getSelectedItem().toString());
                String sourceLang = result.get(0).getLangCode();

                remoteTranslateSetting = new MLRemoteTranslateSetting.Factory()
                        .setSourceLangCode(sourceLang)
                        .setTargetLangCode(targetLang)
                        .create();
                MLRemoteTranslator mlRemoteTranslator = MLTranslatorFactory.getInstance().getRemoteTranslator(remoteTranslateSetting);

                txtLanguage.setText(getDetectedLang(remoteTranslateSetting.getSourceLangCode()));
                Toast.makeText(Translate.this, remoteTranslateSetting.getSourceLangCode() + " -> " + remoteTranslateSetting.getTargetLangCode() + "\nProbability : " + result.get(0).getProbability(), Toast.LENGTH_SHORT).show();

                final Task<String> task = mlRemoteTranslator.asyncTranslate(txtTranslate.getText().toString());
                task.addOnSuccessListener(new OnSuccessListener<String>() {
                    public void onSuccess(final String text) {
                        txtResult.setText(text);
                        imgMicrTarget.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                textToSpeech(text);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    public void onFailure(Exception e) {
                        Toast.makeText(Translate.this, "Error: " + e, Toast.LENGTH_LONG).show();
                    }
                });

                /*******************************
                 *    Run Sync Remote Translate
                 * ******************************
                 */
                //new SyncTranslate().execute(settings);

            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(Exception e) {
                Toast.makeText(Translate.this, "Error " + e, Toast.LENGTH_LONG).show();
            }
        });


        mlRemoteLangDetector.stop();
    }

    private void localTranslate() throws MLException {
        /**********************
         Async Local Translate
         **********************/
       /* final Task<String> task = mlLocalTranslator.asyncTranslate(txtTranslate.getText().toString());
        task.addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                translateBinding.translationText.setText(s);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });*/

        // Sync Local Translate

        translateBinding.translationText.setText(localTranslator.syncTranslate(txtTranslate.getText().toString()));
        Toast.makeText(this, localTranslateSetting.getSourceLangCode() + " -> " + localTranslateSetting.getTargetLangCode(), Toast.LENGTH_SHORT).show();
        localTranslator.stop();


        MLLocalLangDetectorSetting localLangDetectorSetting = new MLLocalLangDetectorSetting.Factory()
                .setTrustedThreshold(0.01f)
                .create();
        localLangDetector = MLLangDetectorFactory.getInstance()
                .getLocalLangDetector(localLangDetectorSetting);

        String input = translateBinding.translateEdittext.getText().toString();
        Toast.makeText(this, "Sync Firsth Best Detect: " + localLangDetector.syncFirstBestDetect(input) +
                "\n!!!!!!!!!!!!!!!!!!SyncProbability Detect : " + localLangDetector.syncProbabilityDetect(input).get(0).getLangCode() +
                "\nFirsth Best Detect: " +  localLangDetector.firstBestDetect(input).getResult(), Toast.LENGTH_SHORT).show(); //!# return null

        localLangDetector.probabilityDetect(input).addOnSuccessListener(new OnSuccessListener<List<MLDetectedLang>>() {
            @Override
            public void onSuccess(List<MLDetectedLang> mlDetectedLangs) {
                Toast.makeText(getApplicationContext(), "Detect Languages Size: " + mlDetectedLangs.size(), Toast.LENGTH_SHORT).show();
            }
        });
        localLangDetector.stop();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.translateButton:
                if (isNetworkAvailable() == true) {
                    translate();
                } else {
                    try {
                        localTranslate();
                    } catch (MLException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.txt_download_model:
                downloadModel();// download model for local translation.
                translateBinding.progressBar.setVisibility(View.VISIBLE);
                break;
            case R.id.imgAudioSource:
                speechToText();
                break;
            case R.id.txt_del_models:
                delUnnecessaryModel();
                break;
            case R.id.txt_tts_details:
                ttsDetails();
                break;
            case R.id.txt_pause:
                if (ttsEngine != null) {
                    ttsEngine.pause();
                }
                break;
            case R.id.txt_resume:
                if (ttsEngine != null) {
                    ttsEngine.resume();
                }
                break;
            case R.id.txt_stop:
                if (ttsEngine != null) {
                    ttsEngine.stop();
                }
                break;
            case R.id.txt_detect:
                Task<String> firstBestDetectTask = mlRemoteLangDetector.firstBestDetect(txtTranslate.getText().toString()); // sourceText: input text string.

            /*    try {
                    List<MLDetectedLang> result= mlRemoteLangDetector.syncProbabilityDetect(txtTranslate.getText().toString());
                    Toast.makeText(getApplicationContext(), "\nSyncProbabilityDetect Size: " + result.size(), Toast.LENGTH_SHORT).show();
                } catch (MLException e) {
                    e.printStackTrace();
                }*/
                firstBestDetectTask.addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        Toast.makeText(getApplicationContext(), "\nFirsth Best Detect: "+s, Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // Processing logic for detection failure.
                    }
                });
                /* List<MLDetectedLang> detectedLangs = mlRemoteLangDetector.syncProbabilityDetect(txtTranslate.getText().toString());
                 String detectedLang = mlRemoteLangDetector.syncFirstBestDetect(txtTranslate.getText().toString());*/
                break;

        }
    }

    class SyncTranslate extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... objects) {
            MLRemoteTranslator mlRemoteTranslator = MLTranslatorFactory.getInstance().getRemoteTranslator((MLRemoteTranslateSetting) objects[0]);
            try {
                String text = mlRemoteTranslator.syncTranslate(txtTranslate.getText().toString());
                txtResult.setText(text);
            } catch (MLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }



    private MLLocalTranslator downloadModel() {
        localTranslateSetting = new MLLocalTranslateSetting.Factory()
                .setSourceLangCode("en")
                .setTargetLangCode(languages.get(languageSpinner.getSelectedItem().toString()))
                .create();
        localTranslator = MLTranslatorFactory.getInstance().getLocalTranslator(localTranslateSetting);

        MLModelDownloadStrategy downloadStrategy = new MLModelDownloadStrategy.Factory()
                .needWifi()
                /*  .needCharging()
                  .needDeviceIdle()*/
                .create();

        if (downloadStrategy.isChargingNeed() == true) {
            Toast.makeText(this, "Please charge your phone to download model.", Toast.LENGTH_SHORT).show();
        }
        if (downloadStrategy.isDeviceIdleNeed() == true) {
            Toast.makeText(this, "Your device is not in idle state.", Toast.LENGTH_SHORT).show();
        }
        if (downloadStrategy.isWifiNeed() == false) {
            Toast.makeText(this, "Please connect wifi to download mode.", Toast.LENGTH_SHORT).show();
        }

        /*************************************************
         * preparedModel(MLModelDownloadStrategy strategy)
         ***************************************************/
      /*  mlLocalTranslator.preparedModel(downloadStrategy).
                addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    // Called when the model package is successfully downloaded.
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Translate.this, "Translate model downloaded successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(Translate.this, "Translate model download error", Toast.LENGTH_SHORT).show();
            }
        });*/

        /*************************************************
         * preparedModel()
         ***************************************************/
      /*mlLocalTranslator.preparedModel().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.e(TAG,"suc");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG,"fail");
            }
        });*/


        localTranslator.preparedModel(downloadStrategy, new MLModelDownloadListener() {
            @Override
            public void onProcess(long l, long l1) {
                Log.e(TAG, "**** " + l + " - " + l1);
                updateProgressBar();
                progress += 1;
                if (l == l1) {
                    translateBinding.progressBar.setVisibility(View.INVISIBLE);
                }if(progress==100){
                    progress=0;
                }
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
            }
        });

        return localTranslator;
    }

    private void updateProgressBar() {
        translateBinding.progressBar.setProgress(progress);
    }

    private void delUnnecessaryModel() {
        final MLLocalModelManager manager = MLLocalModelManager.getInstance();
        final MLLocalTranslatorModel model = new MLLocalTranslatorModel
                .Factory("tr")
                .create();
        manager.deleteModel(model).addOnSuccessListener(new OnSuccessListener<Void>() {
            public void onSuccess(Void aVoid) {
                Toast.makeText(Translate.this, "deleteModel: " + model.getModelName() + " success (" + model.getLanguageCode() + ")", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "deleteModel: " + model.getModelName() + " success (" + model.getLanguageCode() + ")");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(Translate.this, "deleteModel: " + model.getModelName() + " fail(" + model.getLanguageCode() + ")", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "deleteModel: " + model.getModelName() + "  fail (" + model.getLanguageCode() + ")");
            }
        });

    }

    private void textToSpeech(String text) {
        ttsConfig = new MLTtsConfig()
                .setLanguage(MLTtsConstants.TTS_EN_US)
                .setPerson(MLTtsConstants.TTS_SPEAKER_FEMALE_EN)
                .setSpeed(1.0f)
                .setVolume(1.0f);
        ttsEngine = new MLTtsEngine(ttsConfig);
        ttsEngine.updateConfig(ttsConfig);
        ttsEngine.setTtsCallback(callback);

        ttsEngine.speak(text, MLTtsEngine.QUEUE_APPEND);
        Toast.makeText(this, "TTS speed: " + ttsConfig.getSpeed() + "\nVolume: " + ttsConfig.getVolume() +
                "\nLanguage: " + ttsConfig.getLanguage() + "\nPerson: " + ttsConfig.getPerson(), Toast.LENGTH_SHORT).show();

        if (ttsEngine != null) {
            int isAvailable = ttsEngine.isLanguageAvailable(languageSpinner.getSelectedItem().toString());

            if (isAvailable == MLTtsConstants.LANGUAGE_AVAILABLE) {
                Toast.makeText(this, "LANGUAGE_AVAILABLE", Toast.LENGTH_SHORT).show();
            } else if (isAvailable == MLTtsConstants.LANGUAGE_NOT_SUPPORT) {
                Toast.makeText(this, "LANGUAGE_NOT_SUPPORT", Toast.LENGTH_SHORT).show();
            } else if (isAvailable == MLTtsConstants.LANGUAGE_UPDATING) {
                Toast.makeText(this, "LANGUAGE_UPDATING", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void ttsDetails() {
        // ttsEngine = new MLTtsEngine(ttsConfig);
        String speakers = "\n";
        String languages = "\n";
        String getSpeaker = "\n";

        for (int i = 0; i < ttsEngine.getSpeakers().size(); i++) {
            speakers += ttsEngine.getSpeakers().get(i) + "\n";
        }
        for (int i = 0; i < ttsEngine.getLanguages().size(); i++) {
            languages += ttsEngine.getLanguages().get(i) + "\n";
        }
        for (int i = 0; i < ttsEngine.getSpeaker(languageSpinner.getSelectedItem().toString()).size(); i++) {
            getSpeaker += ttsEngine.getSpeaker(languageSpinner.getSelectedItem().toString()).get(i).getLanguage() + "\n";
        } //!#! return empty

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ML Kit");
        builder.setMessage("Speakers: " + speakers + "\nLanguages: " + languages + "\ngetSpeaker: " + getSpeaker);
        builder.setNegativeButton("OK", null);
        builder.show();


    }

    private void speechToText() {
        isRecordAudioPermissionGranted();

        Intent intent = new Intent(this, MLAsrCaptureActivity.class)
                // Set the language that can be recognized to English.
                // If this parameter is not set, English is recognized by default.
                // Example: "MLAsrConstants.LAN_ZH_CN": Chinese;
                // "MLAsrConstants.LAN_EN_US":English;
                // "MLAsrConstants.LAN_FR_FR":French;
                // "MLAsrConstants.LAN_ES_ES":Spanish;
                // "MLAsrConstants.LAN_DE_DE": German
                .putExtra(MLAsrCaptureConstants.LANGUAGE, MLAsrConstants.LAN_EN_US)
                .putExtra(MLAsrCaptureConstants.FEATURE, MLAsrCaptureConstants.FEATURE_WORDFLUX);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String text = "";
        if (requestCode == 100) {
            switch (resultCode) {
                case MLAsrCaptureConstants.ASR_SUCCESS:
                    if (data != null) {
                        Bundle bundle = data.getExtras();
                        if (bundle != null && bundle.containsKey(MLAsrCaptureConstants.ASR_RESULT)) {
                            text = bundle.getString(MLAsrCaptureConstants.ASR_RESULT);
                            txtTranslate.setText(text);
                            // translate();
                            Log.e("LOG", text);
                        }
                    }
                    break;
                case MLAsrCaptureConstants.ASR_FAILURE:
                    if (data != null) {
                        Bundle bundle = data.getExtras();
                        if (bundle != null && bundle.containsKey(MLAsrCaptureConstants.ASR_ERROR_CODE)) {
                            int errorCode = bundle.getInt(MLAsrCaptureConstants.ASR_ERROR_CODE);
                        }
                        if (bundle != null && bundle.containsKey(MLAsrCaptureConstants.ASR_ERROR_MESSAGE)) {
                            String errorMsg = bundle.getString(MLAsrCaptureConstants.ASR_ERROR_MESSAGE);
                            Log.e("LOG", errorMsg);
                        }
                        if (bundle != null && bundle.containsKey(MLAsrCaptureConstants.ASR_SUB_ERROR_CODE)) {
                            int subErrorCode = bundle.getInt(MLAsrCaptureConstants.ASR_SUB_ERROR_CODE);
                        }
                    }
                default:
                    break;
            }
        }
    }

    private boolean isRecordAudioPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED) {
                // put your code for Version>=Marshmallow
                return true;
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                    Toast.makeText(this,
                            "App required access to audio", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO
                }, AUDIO_RECORD_REQUEST_CODE);
                return false;
            }

        } else {
            // put your code for Version < Marshmallow
            return true;
        }
    }

    private void setLangCode() {
        languages.put("Turkish", "tr");
        languages.put("Chinese", "zh");
        languages.put("English", "en");
        languages.put("Russian", "ru");
        languages.put("Portuguese", "pt");
        languages.put("Japanese", "ja");
        languages.put("German", "de");
        languages.put("Italian", "it");
        languages.put("French", "fr");
        languages.put("Spanish", "es");
        languages.put("Arabic", "ar");
    }

    private String getDetectedLang(String langCode) {
        String lang = null;
        for (Map.Entry<String, String> entry : languages.entrySet()) {
            if (entry.getValue().equals(langCode)) {
                lang = entry.getKey();
            }
        }
        return lang;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       // ttsEngine.shutdown();
    }
}