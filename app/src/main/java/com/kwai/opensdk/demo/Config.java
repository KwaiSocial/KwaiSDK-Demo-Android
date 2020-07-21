package com.kwai.opensdk.demo;

/**
 * OpenSDK 登录请求相关配置参数
 * 此参数后端服务动态设置
 */
public class Config {
  //支持的 Scope 列表:user_base(授权之后的默认权限, 不用显式配置); user_info(用户基本信息)
  public static String SCOPE = "user_info";
  public static String STATE = "1234";

}
