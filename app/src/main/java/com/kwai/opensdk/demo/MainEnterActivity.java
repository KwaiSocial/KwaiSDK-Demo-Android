package com.kwai.opensdk.demo;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.kwai.opensdk.sdk.constants.KwaiOpenSdkConstants;
import com.kwai.opensdk.sdk.utils.VerifyAppUtil;

import androidx.fragment.app.FragmentActivity;

public class MainEnterActivity extends FragmentActivity {
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main_enter);
    initView();
  }

  private void initView() {
    TextView sdkInfo = findViewById(R.id.sdk_info);
    //基本信息
    sdkInfo.setText("open sdk version:" + KwaiOpenSdkConstants.SDK_VERSION + "\n" +
        "快手是否安装:" + VerifyAppUtil.isKwaiAppInstalled(getApplicationContext()));
    EditText appIdEditText = findViewById(R.id.app_id);
    appIdEditText.addTextChangedListener(new TextWatcher() {

      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        MockHelper.mAppIdTest = charSequence.toString();
      }

      @Override
      public void afterTextChanged(Editable editable) {

      }
    });
    EditText appNameEditText = findViewById(R.id.app_name);
    appNameEditText.addTextChangedListener(new TextWatcher() {

      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        MockHelper.mAppNameTest = charSequence.toString();
      }

      @Override
      public void afterTextChanged(Editable editable) {

      }
    });
    EditText appPackageNameEditText = findViewById(R.id.app_package_name);
    appPackageNameEditText.addTextChangedListener(new TextWatcher() {

      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        MockHelper.mAppPackageNameTest = charSequence.toString();
      }

      @Override
      public void afterTextChanged(Editable editable) {

      }
    });
    EditText appSecretEditText = findViewById(R.id.app_secret);
    appSecretEditText.addTextChangedListener(new TextWatcher() {

      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        MockHelper.mAppSecretTest = charSequence.toString();
      }

      @Override
      public void afterTextChanged(Editable editable) {

      }
    });
    Switch switchButton = findViewById(R.id.switch_btn);
    switchButton.setChecked(false);
    switchButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
      if (isChecked) {
        MockHelper.isTest = true;
      } else {
        MockHelper.isTest = false;
      }
    });
    TextView notifyText = findViewById(R.id.notify_text);
    Button enterButton = findViewById(R.id.enter_button);
    enterButton.setOnClickListener(view -> {
      if (MockHelper.isTest) {
        if (TextUtils.isEmpty(MockHelper.mAppIdTest)) {
          notifyText.setVisibility(View.VISIBLE);
          notifyText.setText("请设置appId");
          return;
        } else if (TextUtils.isEmpty(MockHelper.mAppNameTest)) {
          notifyText.setVisibility(View.VISIBLE);
          notifyText.setText("请设置appName");
          return;
        } else if (TextUtils.isEmpty(MockHelper.mAppPackageNameTest)) {
          notifyText.setVisibility(View.VISIBLE);
          notifyText.setText("请设置appPackageName");
          return;
        } else if (TextUtils.isEmpty(MockHelper.mAppSecretTest)) {
          notifyText.setVisibility(View.VISIBLE);
          notifyText.setText("请设置appSecret");
          return;
        }
        notifyText.setVisibility(View.GONE);
      }
      startActivity(new Intent(this, TabMainActivity.class));
    });
  }
}
