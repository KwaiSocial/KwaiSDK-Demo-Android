package com.kwai.opensdk.demo;

public enum AppInfoEnum {
  SOCIAL_SHARE_FT("ks703687443040312600", "cAQmb4gjTeCW3Sf4enQDbQ");
  private String appId;
  private String appKey;

  AppInfoEnum(String appId, String appKey) {
    this.appId = appId;
    this.appKey = appKey;
  }

  public String getAppId() {
    return appId;
  }

  public String getAppKey() {
    return appKey;
  }
}
