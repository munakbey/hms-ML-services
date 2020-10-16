package com.mapp.huawei.view.text_related;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hms.mlplugin.card.bcr.MLBcrCapture;
import com.huawei.hms.mlplugin.card.bcr.MLBcrCaptureConfig;
import com.huawei.hms.mlplugin.card.bcr.MLBcrCaptureFactory;
import com.huawei.hms.mlplugin.card.bcr.MLBcrCaptureResult;
import com.huawei.hms.mlplugin.card.gcr.MLGcrCapture;
import com.huawei.hms.mlplugin.card.gcr.MLGcrCaptureConfig;
import com.huawei.hms.mlplugin.card.gcr.MLGcrCaptureFactory;
import com.huawei.hms.mlplugin.card.gcr.MLGcrCaptureResult;
import com.huawei.hms.mlplugin.card.gcr.MLGcrCaptureUIConfig;
import com.mapp.huawei.R;
import com.mapp.huawei.common.App;
import com.mapp.huawei.text_recogcamera.CameraConfiguration;
import com.mapp.huawei.text_recogcamera.LensEngine;
import com.mapp.huawei.text_recogcamera.LensEnginePreview;
import com.mapp.huawei.text_recogutil.Constant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CapturePhotoActivity extends AppCompatActivity {
    private static final String TAG = "CapturePhotoActivity";
    private LensEngine lensEngine = null;
    private LensEnginePreview preview;
    private CameraConfiguration cameraConfiguration = null;
    private int facing = CameraConfiguration.CAMERA_FACING_BACK;

    private MLBcrCapture.Callback callback;
    private MLGcrCapture.Callback callbackGcr;

    public static Bitmap bankCard;
    public static Bitmap originalBankCard;
    public static Bitmap generalCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_photo);

        ImageButton takePhotoButton = findViewById(R.id.img_takePhoto);
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CapturePhotoActivity.this.toTakePhoto();
            }
        });
        ImageButton backButton = findViewById(R.id.capture_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CapturePhotoActivity.this.finish();
            }
        });
        this.preview = this.findViewById(R.id.capture_preview);
        this.cameraConfiguration = new CameraConfiguration();
        this.cameraConfiguration.setCameraFacingBack(this.facing);
        this.createLensEngine();
        this.startLensEngine();

        Bundle bundle = getIntent().getExtras();
        Boolean flagBankCard = bundle.getBoolean("flagBankCard");
        if (flagBankCard == true) {
            bankCardRecog(callback);
        }

        Boolean flagGeneralCard = bundle.getBoolean("flagGeneralCard");
        if (flagGeneralCard == true) {
            generalCardRecog(null, callbackGcr);
        }
    }

    public void bankCardRecog(MLBcrCapture.Callback callback) {
        callback = new MLBcrCapture.Callback() {

            @Override
            public void onSuccess(MLBcrCaptureResult mlBcrCaptureResult) {
                Intent intent = new Intent(getApplicationContext(), BankCardRecog.class);
                bankCard = mlBcrCaptureResult.getNumberBitmap();
                originalBankCard = mlBcrCaptureResult.getOriginalBitmap();
                intent.putExtra("getExpire", mlBcrCaptureResult.getExpire());
                intent.putExtra("getIssuer", mlBcrCaptureResult.getIssuer());
                intent.putExtra("getNumber", mlBcrCaptureResult.getNumber());
                intent.putExtra("getOrganization", mlBcrCaptureResult.getOrganization());
                intent.putExtra("getType", mlBcrCaptureResult.getType());
                App.LOGGER("Bank ", "Error Code: " + mlBcrCaptureResult.getErrorCode());
                startActivity(intent);
            }

            @Override
            public void onCanceled() {
                App.LOGGER("Bank", "onCanceled");
            }

            @Override
            public void onFailure(int i, Bitmap bitmap) {
                App.LOGGER("Bank", "onFailure " + i);
            }

            @Override
            public void onDenied() {
                App.LOGGER("Bank", "onDenied");
            }
        };

        MLBcrCaptureConfig config = new MLBcrCaptureConfig.Factory()
                // Set the expected result type of bank card recognition.
                // MLBcrCaptureConfig.RESULT_NUM_ONLY: Recognize only the band card number.
                // MLBcrCaptureConfig.RESULT_SIMPLE: Recognize only the bank card number and validity period.
                // MLBcrCaptureConfig.ALL_RESULT: Recognize information such as the band card number, validity period, issuing bank, card organization, and card type.
                .setResultType(MLBcrCaptureConfig.RESULT_ALL)
                // Set the recognition screen display orientation.
                // MLBcrCaptureConfig.ORIENTATION_AUTO: adaptive mode. The display orientation is determined by the physical sensor.
                // MLBcrCaptureConfig.ORIENTATION_LANDSCAPE: landscape mode.
                // MLBcrCaptureConfig.ORIENTATION_PORTRAIT: portrait mode.
                .setOrientation(MLBcrCaptureConfig.ORIENTATION_AUTO)
                /*    0: weak mode
                    1: strict mode*/
                .setRecMode(1)
                .create();
        MLBcrCapture bankCapture = MLBcrCaptureFactory.getInstance().getBcrCapture(config);
        bankCapture.captureFrame(CapturePhotoActivity.this, callback);

        App.LOGGER("Bank", "Orientation: " + config.getOrientation() + " Result Type: " + config.getResultType() + " Rec Mode: " + config.getRecMode());

    }

    private void generalCardRecog(Object object, MLGcrCapture.Callback callback) {

        callbackGcr = new MLGcrCapture.Callback() {

            @Override
            public int onResult(MLGcrCaptureResult mlGcrCaptureResult, Object o) {
                App.LOGGER("General", "sucess " + mlGcrCaptureResult.text.getStringValue());
                Intent intent = new Intent(getApplicationContext(), GeneralCardRecog.class);
                intent.putExtra("result", mlGcrCaptureResult.text.getStringValue());
                generalCard = mlGcrCaptureResult.cardBitmap;
                startActivity(intent);
                return 0;
            }

            @Override
            public void onCanceled() {
                App.LOGGER("General", "onCanceled");
            }

            @Override
            public void onFailure(int i, Bitmap bitmap) {
                App.LOGGER("General", "onFailure");
            }

            @Override
            public void onDenied() {
                App.LOGGER("General", "onDenied");
            }
        };


        MLGcrCaptureConfig cardConfig = new MLGcrCaptureConfig.Factory().setLanguage("tr").create();
        // Create a general card recognition UI configurator.
        MLGcrCaptureUIConfig uiConfig = new MLGcrCaptureUIConfig.Factory()
                // Set the color of the scanning box.
                .setScanBoxCornerColor(Color.GREEN)
                // Set the prompt text in the scanning box. It is recommended that the text contain less than 30 characters.
                .setTipText("Recognizing, align edges")
                .setTipTextColor(Color.YELLOW)
                // Set the recognition screen display orientation.
                // MLGcrCaptureUIConfig.ORIENTATION_AUTO: adaptive mode. The display orientation is determined by the physical sensor.
                // MLGcrCaptureUIConfig.ORIENTATION_LANDSCAPE: landscape mode.
                // MLGcrCaptureUIConfig.ORIENTATION_PORTRAIT: portrait mode.

                .setOrientation(MLGcrCaptureUIConfig.ORIENTATION_AUTO)
                .create();
        App.LOGGER("General" , uiConfig.getTorchOffResId()+" "+uiConfig.getTorchOnResId());
        MLGcrCapture ocrManager = MLGcrCaptureFactory.getInstance().getGcrCapture(cardConfig, uiConfig);/*
        MLGcrCapture ocrManager = MLGcrCaptureFactory.getInstance().getGcrCapture(cardConfig);
*/
        ocrManager.capturePreview(this, object, callbackGcr);
    }

    private void createLensEngine() {
        if (this.lensEngine == null) {
            this.lensEngine = new LensEngine(this, this.cameraConfiguration);
        }
    }

    private void startLensEngine() {
        if (this.lensEngine != null) {
            try {
                this.preview.start(this.lensEngine, false);
            } catch (IOException e) {
                Log.e(CapturePhotoActivity.TAG, "Unable to start lensEngine.", e);
                this.lensEngine.release();
                this.lensEngine = null;
            }
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
        this.preview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.lensEngine != null) {
            this.lensEngine.release();
        }
        this.facing = CameraConfiguration.CAMERA_FACING_BACK;
        this.cameraConfiguration.setCameraFacingBack(this.facing);
    }

    private void toTakePhoto() {
        lensEngine.takePicture(new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                String filePath = null;
                try {
                    filePath = saveBitmapToDisk(bitmap);
                } catch (IOException e) {
                    Log.e(TAG, "Save bitmap failed: " + e.getMessage());
                }
                Intent intent = new Intent();
                intent.putExtra(Constant.IMAGE_PATH_VALUE, filePath);
                setResult(Activity.RESULT_OK, intent);
                CapturePhotoActivity.this.finish();
            }
        });
    }


    private String saveBitmapToDisk(Bitmap bitmap) throws IOException {
        String storePath = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + "PhotoTranslate";
        File appDir = new File(storePath);
        if (!appDir.exists()) {
            boolean res = appDir.mkdir();
            if (!res) {
                Log.e(TAG, "saveBitmapToDisk failed");
                return "";
            }
        }

        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            fos = null;

            Uri uri = Uri.fromFile(file);
            this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Save bitmap failed: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "Save bitmap failed: " + e.getMessage());
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Close stream failed: " + e.getMessage());
            }
            fos = null;
        }

        return file.getCanonicalPath();
    }


}
