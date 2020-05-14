package com.kwai.opensdk.demo;

import android.app.Application;

import com.kwai.opensdk.auth.KwaiOpenSdkAuth;

public class MyApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    KwaiOpenSdkAuth.init(this);
  }

}
