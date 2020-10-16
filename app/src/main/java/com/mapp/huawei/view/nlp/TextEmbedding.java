package com.mapp.huawei.view.nlp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModel;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlsdk.text.MLTextAnalyzer;
import com.huawei.hms.mlsdk.textembedding.MLTextEmbeddingAnalyzer;
import com.huawei.hms.mlsdk.textembedding.MLTextEmbeddingAnalyzerFactory;
import com.huawei.hms.mlsdk.textembedding.MLTextEmbeddingException;
import com.huawei.hms.mlsdk.textembedding.MLTextEmbeddingSetting;
import com.huawei.hms.mlsdk.textembedding.MLVocabularyVersion;
import com.mapp.huawei.R;
import com.mapp.huawei.common.App;
import com.mapp.huawei.databinding.ActivityTextEmbeddingBinding;

import java.util.List;

public class TextEmbedding extends AppCompatActivity implements View.OnClickListener {

    private MLTextEmbeddingSetting setting;
    private MLTextEmbeddingAnalyzer analyzer;
    private final String TAG = "TextEmbedding";
    private ActivityTextEmbeddingBinding textEmbeddingBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textEmbeddingBinding = DataBindingUtil.setContentView(this, R.layout.activity_text_embedding);

        textEmbeddingBinding.btnSentencesSimilarity.setOnClickListener(this);
        textEmbeddingBinding.btnAnalyseSimilarWords.setOnClickListener(this);
        textEmbeddingBinding.btnWordSimilarity.setOnClickListener(this);

        // Create a text embedding analyzer.
        setting = new MLTextEmbeddingSetting.Factory()
                .setLanguage(MLTextEmbeddingSetting.LANGUAGE_EN)
                .create();
        analyzer = MLTextEmbeddingAnalyzerFactory.getInstance().getMLTextEmbeddingAnalyzer(setting);

        Task<MLVocabularyVersion> vocabularyVersionTask = analyzer.getVocabularyVersion();// MLVocabularyVersion is a dictionary information entity class.
        vocabularyVersionTask.addOnSuccessListener(new OnSuccessListener<MLVocabularyVersion>() {
            @Override
            public void onSuccess(MLVocabularyVersion dictionaryVersionVo) {
                textEmbeddingBinding.txtDictionaryDimension.setText(dictionaryVersionVo.getDictionaryDimension());
                textEmbeddingBinding.txtDictionarySize.setText(dictionaryVersionVo.getDictionarySize());
                textEmbeddingBinding.txtDictionaryVersion.setText(dictionaryVersionVo.getVersionNo());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                failure(e);
            }
        });


    }

    public void failure(Exception e) {
        // Check whether text embedding is abnormal.
        if (e instanceof MLTextEmbeddingException) {
            MLTextEmbeddingException embeddingException = (MLTextEmbeddingException) e;
            embeddingException.getErrCode();
            embeddingException.getMessage();
        } else {
            // Handle other exceptions.
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_sentences_similarity:
                final String inputSentence1 = textEmbeddingBinding.txtSentence1.getText().toString();
                String inputSentence2 = textEmbeddingBinding.txtSentence2.getText().toString();
                Task<Float> sentencesSimilarityTask = analyzer.analyseSentencesSimilarity(inputSentence1, inputSentence2);  // input1 and input2 refer to the input text information of the String type.
                sentencesSimilarityTask.addOnSuccessListener(new OnSuccessListener<Float>() {
                    @Override
                    public void onSuccess(Float sentencesSimilarity) {
                        textEmbeddingBinding.txtResult.setText("Sentences Similarity : " + sentencesSimilarity);

                        Task<Float[]> analyseSentenceVector = analyzer.analyseSentenceVector(inputSentence1);
                        analyseSentenceVector.addOnSuccessListener(new OnSuccessListener<Float[]>() {
                            @Override
                            public void onSuccess(Float[] floats) {
                       /*         String result = "";
                                for (int i = 0; i < floats.length; i++) {
                                    result += floats[i];
                                }*/
                                textEmbeddingBinding.txtAnalyseSentenceVector.setText("Analyse sentence vector get successfully");
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        failure(e);
                    }
                });
                break;

            case R.id.btn_analyse_similar_words:
                Task<List<String>> multipleSimilarityWordsTask = analyzer.analyseSimilarWords(textEmbeddingBinding.txtInputWord.getText().toString(),
                        Integer.valueOf(textEmbeddingBinding.txtInputWordCount.getText().toString()));
                multipleSimilarityWordsTask.addOnSuccessListener(new OnSuccessListener<List<String>>() {
                    @Override
                    public void onSuccess(List<String> strings) {
                        String result = "";
                        for (int i = 0; i < strings.size(); i++) {
                            result += strings.get(i) + "\n";
                        }

                        textEmbeddingBinding.txtSimilarWordResult.setText(result);
                        Log.e(TAG, "input : " + textEmbeddingBinding.txtInputWord.getText().toString() + "\nResults: \n" + result);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        failure(e);
                    }
                });
                break;

            case R.id.btn_word_similarity:
                final String input1 = textEmbeddingBinding.txtWord1.getText().toString();
                String input2 = textEmbeddingBinding.txtWord2.getText().toString();

                Task<Float> wordsSimilarityTask = analyzer.analyseWordsSimilarity(input1, input2);  // input1 and input2 refer to the input text information of the String type.
                wordsSimilarityTask.addOnSuccessListener(new OnSuccessListener<Float>() {
                    @Override
                    public void onSuccess(Float wordsSimilarity) {
                        textEmbeddingBinding.txtResultWord.setText("Words similarity: " + wordsSimilarity.toString());

                        Task<Float[]> analyseWordVector = analyzer.analyseWordVector(input1);
                        analyseWordVector.addOnSuccessListener(new OnSuccessListener<Float[]>() {
                            @Override
                            public void onSuccess(Float[] floats) {
                                textEmbeddingBinding.txtAnalyseWordVector.setText("Analyse word vector get successfully");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                textEmbeddingBinding.txtAnalyseWordVector.setText("Analyse word vector failed " + e);
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        failure(e);
                    }
                });
                break;
        }
    }
}