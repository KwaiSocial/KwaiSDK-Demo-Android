package com.kwai.opensdk.demo;

/**
 * Created by：zhaokai on 2018/5/29.
 * desc：
 * OpenSDK 登录请求相关配置参数
 * 此参数有后端服务动态设置
 */
public class Config {

  //默认申请的权限 user_info,payment
  //所有权限 user_info/用户信息、payment/支付功能、relation/读取好友关系、message/可发私信
  public static String SCOPE = "user_info,relation";

}
