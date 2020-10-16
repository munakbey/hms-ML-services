package com.mapp.huawei.view.text_related;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.mapp.huawei.R;
import com.mapp.huawei.databinding.ActivityBankCardRecogBinding;

public class BankCardRecog extends AppCompatActivity {

    private ActivityBankCardRecogBinding bankCardRecogBinding;
    private Bundle bundle;
    private String getExpire, getIssuer, getOrganization, getNumber, getType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bankCardRecogBinding = DataBindingUtil.setContentView(this, R.layout.activity_bank_card_recog);

        bundle = getIntent().getExtras();

        getExpire = bundle.getString("getExpire");
        getIssuer = bundle.getString("getIssuer");
        getOrganization = bundle.getString("getOrganization");
        getNumber = bundle.getString("getNumber");
        getType = bundle.getString("getType");

        bankCardRecogBinding.imgResult.setImageBitmap(CapturePhotoActivity.bankCard);
        bankCardRecogBinding.imgOriginal.setImageBitmap(CapturePhotoActivity.originalBankCard);

        bankCardRecogBinding.txtResult.setText("Expire: " + getExpire + "\nIssuer: " + getIssuer + "\nOrganization: " + getOrganization + "\nNumber: " + getNumber + "\nType: " + getType);

    }


}