package com.mapp.huawei.view.text_related;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.common.MLException;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.document.MLDocument;
import com.huawei.hms.mlsdk.document.MLDocumentAnalyzer;
import com.huawei.hms.mlsdk.document.MLDocumentSetting;
import com.huawei.hms.mlsdk.text.MLRemoteTextSetting;
import com.mapp.huawei.R;
import com.mapp.huawei.common.App;
import com.mapp.huawei.databinding.ActivityDocumentRecognitionBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DocumentRecognition extends AppCompatActivity {

    private Bitmap bitmap;
    private MLDocumentAnalyzer documentAnalyzer;
    private final String TAG = "DocumentRecog";
    private ActivityDocumentRecognitionBinding documentRecognitionBinding;
    private MLDocumentSetting setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        documentRecognitionBinding = DataBindingUtil.setContentView(this, R.layout.activity_document_recognition);

        bitmap = ReadPhotoActivity.originBitmap;
        recognition();
        try {
            asyncAnalyser();
        } catch (IOException e) {
            e.printStackTrace();
        }

        documentRecognitionBinding.btnDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = "";
                for (int i = 0; i < setting.getLanguageList().size(); i++) {
                    msg += setting.getLanguageList().get(i) + "\n";
                }
                msg += "Border Type: " + setting.getBorderType() + "\nIs Enable Fingerprint Verification: " + setting.isEnableFingerprintVerification();
          /*      NGON: coordinates of the four vertices of a text line bounding box that is a quadrilateral.
                  ARC: coordinates of up to 72 vertices of a text bounding box that is a polygon for text in a curved layout.*/
                detailsDialog(msg);
            }
        });

    }

    private void recognition() {
        List<String> languageList = new ArrayList();
        languageList.add("zh");
        languageList.add("en");
        setting = new MLDocumentSetting.Factory()
                .setLanguageList(languageList)
                .setBorderType(MLRemoteTextSetting.ARC)
                .enableFingerprintVerification()
                .create();
        MLDocumentAnalyzer analyzer = MLAnalyzerFactory.getInstance().getRemoteDocumentAnalyzer(setting);
        //   MLDocumentAnalyzer analyzer = MLAnalyzerFactory.getInstance().getRemoteDocumentAnalyzer();// Method 2: Use the default parameter settings to automatically detect languages for text recognition. The format of the returned text box is MLRemoteTextSetting.NGON.

        // Create an MLFrame object using the bitmap, which is the image data in bitmap format.
        MLFrame frame = MLFrame.fromBitmap(bitmap);
        documentAnalyzer = MLAnalyzerFactory.getInstance().getRemoteDocumentAnalyzer(setting);

        Task<MLDocument> task = analyzer.asyncAnalyseFrame(frame);
        task.addOnSuccessListener(new OnSuccessListener<MLDocument>() {
            @Override
            public void onSuccess(MLDocument document) {
                App.LOGGER("TAG", document.getStringValue() + " ____----____");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                try {
                    MLException mlException = (MLException) e;
                    String errorMessage = mlException.getMessage();
                } catch (Exception error) {
                }
            }
        });
    }

    private void asyncAnalyser() throws IOException {
        MLFrame mlFrame = new MLFrame.Creator().setBitmap(bitmap).create();
        Task<MLDocument> task = documentAnalyzer.asyncAnalyseFrame(mlFrame);

        task.addOnSuccessListener(new OnSuccessListener<MLDocument>() {
            @Override
            public void onSuccess(MLDocument mlDocument) {
                remoteDetectSuccess(mlDocument);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(DocumentRecognition.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
        documentAnalyzer.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            documentAnalyzer.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void remoteDetectSuccess(MLDocument mlDocument) {
        List<MLDocument.Block> blocks = mlDocument.getBlocks();
        List<MLDocument.Line> lines = new ArrayList<>();
        List<MLDocument.Base> base = new ArrayList<>();
        List<MLDocument.Word> word = new ArrayList<>();

        String msg = "";

        for (MLDocument.Block block : blocks) {
            for (MLDocument.Section line : block.getSections()) {
                lines = line.getLineList();
                word = line.getWordList();
            }
            for (MLDocument.Base base1 : block.getSections()) {
                base.add(base1);
                msg += base1.getStringValue();
                msg += " Interval Type: " + base1.getInterval().getIntervalType() + " Languages List Size: " + base1.getLanguageList().size() /*+ " Border: " + word.getBorder()*/ + "\n";
                if (base1.getInterval().isTextFollowed() == true) {
                    msg += "Is text followed : true \n";
                } else {
                    msg += "Is text followed : false \n";
                }
                msg += "\n";
            }

        }
        String msg2 = "";
        for (int i = 0; i < lines.size(); i++) {
            msg2 += "Lines : " + lines.get(i).getStringValue() + "\n";
        }
        for (int i = 0; i < word.size(); i++) {
            msg2 += "Words : " + word.get(i).getStringValue() + "\n";
/*            for (int j = 0; j < word.get(i).getCharacterList().size(); j++) {
                App.LOGGER(TAG , word.get(i).getCharacterList().get(j).getStringValue() +"\n");
            }*/
        }
        documentRecognitionBinding.txtDocRecogResult.setText(msg2 + "\n\n " + msg);

    }

    private void detailsDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Details");
        builder.setMessage(msg);
        builder.setNegativeButton("OK", null);
        builder.show();
    }
}