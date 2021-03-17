package com.kwai.opensdk.demo;

import android.app.Application;

import com.kwai.auth.KwaiAuthAPI;

public class MyApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    KwaiAuthAPI.init(this);
  }

}
