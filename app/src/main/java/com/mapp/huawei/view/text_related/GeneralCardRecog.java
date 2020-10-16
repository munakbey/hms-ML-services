package com.mapp.huawei.view.text_related;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.mapp.huawei.R;
import com.mapp.huawei.databinding.ActivityGeneralCardRecogBinding;

public class GeneralCardRecog extends AppCompatActivity {

    private ActivityGeneralCardRecogBinding generalCardRecogBinding;
    private Bundle bundle;
    private String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        generalCardRecogBinding = DataBindingUtil.setContentView(this,R.layout.activity_general_card_recog);

        bundle = getIntent().getExtras();
        result = bundle.getString("result");

        generalCardRecogBinding.txtResult.setText(result);
        generalCardRecogBinding.imgOriginal.setImageBitmap(CapturePhotoActivity.generalCard);
    }
}