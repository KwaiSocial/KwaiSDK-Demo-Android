package com.kwai.opensdk.demo;

import android.content.Context;
import android.os.Bundle;

import com.kwai.opensdk.sdk.constants.KwaiOpenSdkConstants;
import com.kwai.opensdk.sdk.openapi.KwaiOpenAPI;

import androidx.annotation.NonNull;

public class MockHelper {

  public static boolean isTest = false;
  public static String mAppIdTest = "";
  public static String mAppNameTest = "";
  public static String mAppPackageNameTest = "";
  public static String mAppSecretTest = "";

  static class KwaiOpenAPITest extends KwaiOpenAPI {

    public KwaiOpenAPITest(@NonNull Context context) {
      super(context);
    }

    @Override
    public Bundle generateBaseBundle() {
      if (isTest) {
        Bundle baseBundle = new Bundle();
        baseBundle.putString(KwaiOpenSdkConstants.BUNDLE_APP_ID, mAppIdTest);
        baseBundle.putString(KwaiOpenSdkConstants.BUNDLE_APP_NAME, mAppNameTest);
        baseBundle.putString(KwaiOpenSdkConstants.BUNDLE_CALLING_PACKAGE_NAME, mAppPackageNameTest);
        return baseBundle;
      } else {
        return super.generateBaseBundle();
      }
    }
  }

}
